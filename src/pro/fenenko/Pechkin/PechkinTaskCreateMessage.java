/*
 * Copyright 2017 Fenenko Aleksandr.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pro.fenenko.Pechkin;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bouncycastle.util.encoders.Hex;
import pro.fenenko.Bitmessage.BMAddress;
import pro.fenenko.Bitmessage.BMAddressUtils;
import pro.fenenko.Bitmessage.BMConstant;
import pro.fenenko.Bitmessage.BMLog;
import pro.fenenko.Bitmessage.BMObject;
import pro.fenenko.Bitmessage.BMPacket;
import pro.fenenko.Bitmessage.BMParse;
import pro.fenenko.Bitmessage.BMPubKey;
import pro.fenenko.Bitmessage.BMUtils;

/**
 *
 * @author Fenenko Aleksandr
 */
public class PechkinTaskCreateMessage extends Thread implements BMConstant {

    private static BMParse parse = new BMParse();
    private static BMUtils utils = new BMUtils();
    private static BMPacket packet = new BMPacket();
    private String[] findAddress = new String[0];
    private long[] ttlFindAddress = new long[0];

    @Override
    public void run() {
        BMLog.LogD("PechkinTaskCreateMessage", "START TASK");
        boolean flag = false;
        for (; true;) {
            flag = false;
            long[] ids = PechkinPrivateData.getMessageIDs();
            for (int i = 0; i < ids.length; i++) {
                PechkinMessage msg = PechkinPrivateData.getMessage(ids[i]);
                switch (msg.getStatus()) {
                    case MESSAGE_CREATE:
                        
                        msg.setStatus(MESSAGE_FIND_PUBKEY);
                        //msg.setTime(System.currentTimeMillis()/1000L);
                        PechkinPrivateData.updateMessage(msg);
                        PechkinMainWindow.updateListMessage();
                        BMLog.LogD("PechkinTaskCreateMessage", "Start create message");
                        break;
                    case MESSAGE_FIND_PUBKEY:
                        BMLog.LogD("PechkinTaskCreateMessage", "Start find pubkey "+msg.getAddressTo());
                        BMPubKey key = null;
                        long timeout = System.currentTimeMillis();
                        if(msg.getAddressTo().contains(BMConstant.ADDRESS_BROADCAST)){
                            BMLog.LogD("PechkinTaskCreateMessage", "CREATE BROADCAST");
                            BMAddress adr = PechkinPrivateData.getBMAddress(msg.getAddressFrom());
                            byte[] ob = packet.createBroadcastMessage(adr.getAddress(),adr.getPrivateSigningKey(),adr.getPrivateCipherKey(),msg.getMessage());
                            PechkinProcessObject.process(ob);
                            PechkinData.addNew(ob);
                            msg.setStatus(MESSAGE_SEND);
                            PechkinPrivateData.updateMessage(msg);
                            break;
                        }
                        String s = msg.getAddressTo();
                        if(s.contains("BM-")){
                            s = s.substring(3, s.length());
                        }
                        long time = System.currentTimeMillis();
                        for (int j = 0; (j < PechkinPrivateData.getCountPubKey()) && (key == null); j++) {
                            if (PechkinPrivateData.getPubkeyAddress(j).contains(s)) {
                                key = new BMPubKey(msg.getAddressTo(),
                                        PechkinPrivateData.getPubkeyKey(j),
                                        PechkinPrivateData.getPubkeyPow1(j),
                                        PechkinPrivateData.getPubkeyPow2(j));

                            }
                        }
                        

                        if (key == null) {
                            
                            byte[] inv = PechkinData.getPubKeys();
                            byte[] inv32 = new byte[32];

                            for (int j = 0; (j < inv.length) && (key == null); j += 32) {
                                System.arraycopy(inv, j, inv32, 0, 32);
                                byte[] ob = PechkinData.get(inv32);
                                key = parse.decodePubKeys(ob, msg.getAddressTo());
                                ob = null;

                            }
                            BMLog.LogD("PechkinTaskCreateMessage", "find pubkey "+(System.currentTimeMillis()-time)+" ms");
                            if (key == null) {
                                int t =utils.find(findAddress, msg.getAddressTo());
                                boolean f = false;
                                if(t == -1)
                                {
                                    f = true;
                                } else{
                                    if(ttlFindAddress[t] < System.currentTimeMillis()/1000L){
                                        f = true;
                                    }
                                }
                                    if(f){
                                    byte[] obGetPubKey = packet.createGetPubkey(24*60*60,msg.getAddressTo());
                                    //BMObject object = new BMObject();
                                    PechkinProcessObject.process(obGetPubKey);
                                    PechkinData.addNew(obGetPubKey);
                                    BMLog.LogD("PechkinTaskCreateMessage", "create GET PUB KEY FOR address " + msg.getAddressTo()
                                            +" INV "+Hex.toHexString(utils.createInv(obGetPubKey)));
                                    if(t != -1){
                                        ttlFindAddress[t] = System.currentTimeMillis()/1000L + 12*60*60;
                                    } else{
                                        findAddress = utils.add(findAddress, msg.getAddressTo());
                                        ttlFindAddress = utils.add(ttlFindAddress, 12*60*60);
                                    }
                                    } else{
                                        BMLog.LogD("PechkinTaskCreateMessage", "not send getpubkey is already send");
                                    }
                                flag = true;
                                
                            } else {
                                s = key.address;
                                if(!s.contains("BM-"))
                                    s = "BM-"+s;
                                PechkinPrivateData.addPubKey(s, key.key, key.pow1, key.pow2);

                            }
                        }

                        if (key != null) {
                            //byte[] ack = new 
                            BMLog.LogD("PechkinTaskCreateMessage", "find pubkey " + msg.getAddressTo() + " time "
                                    + (System.currentTimeMillis() - timeout) + " ms");
                            byte[] ack = new byte[32];
                            
                            Random rand = new Random();
                            rand.nextBytes(ack);
                            BMLog.LogD("PechkinTaskCreateMessage", "CREATE ACK "+Hex.toHexString(ack));
                            PechkinPrivateData.addAck(ack, ids[i]);
                            
                            BMAddress addr = PechkinPrivateData.getBMAddress(msg.getAddressFrom());
                            System.out.println(addr.getAddress());
                           long ttl = msg.getTime() - (System.currentTimeMillis()/1000L);
                            byte[] ob = packet.createMessage(ack, ttl,
                                    msg.getAddressFrom(), msg.getAddressTo(), addr.getPrivateSigningKey()
                                    , addr.getPrivateCipherKey(), key.key, msg.getMessage(), key.pow1, key.pow2);
                            msg.setStatus(MESSAGE_SEND);
                            msg.setTime(System.currentTimeMillis()/1000L);
                            PechkinPrivateData.updateMessage(msg);
                            //ProcessObject.process(ob);
                            PechkinData.addNew(ob);
                            BMLog.LogD("PechkinTaskCreateMessage", "finish create message INV "+Hex.toHexString(utils.createInv(ob)) );
                            PechkinMainWindow.updateListMessage();
                            //ob = packet.c
                            
                        } 

                        break;
                }
            }
            try {
                Thread.sleep(60000);
             
            } catch (InterruptedException ex) {
                Logger.getLogger(PechkinTaskCreateMessage.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
