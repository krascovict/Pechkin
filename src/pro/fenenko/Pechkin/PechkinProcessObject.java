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

import java.util.Arrays;
import org.bouncycastle.util.encoders.Hex;
import pro.fenenko.Bitmessage.BMAddress;
import pro.fenenko.Bitmessage.BMAddressUtils;
import pro.fenenko.Bitmessage.BMConstant;
import pro.fenenko.Bitmessage.BMLog;
import pro.fenenko.Bitmessage.BMMessage;
import pro.fenenko.Bitmessage.BMMethodClientProcess;
import pro.fenenko.Bitmessage.BMNodeAddress;
import pro.fenenko.Bitmessage.BMPacket;
import pro.fenenko.Bitmessage.BMParse;
import pro.fenenko.Bitmessage.BMUtils;

/**
 *
 * @author Fenenko Aleksandr
 */
public class PechkinProcessObject implements BMConstant {
    private static BMParse parse = new BMParse();
    private static BMUtils utils = new BMUtils();
    private static BMAddressUtils addressUtils = new BMAddressUtils();
    private static byte[] objectProcess =new byte[1600];
    
    
    public static synchronized void process(byte[] object){
        process(object,parse.getObjectType(object));
    }
    
    private static boolean testIsInv(byte[] inv32){
        boolean flag = false;
        for(int i = 0; (i < objectProcess.length)&&(!flag);i+=32){
            int n = 0;
            for(int j = 0; (j < 32)&&(inv32[j] == objectProcess[i+j]);j++,n++);
            if(n == 32){
                flag = true;
            }
                
        }
        return flag;
    }
    
    public static synchronized void process(byte[] object,int type){
        byte[] inv32 = utils.createInv(object);
        if(!testIsInv(inv32)){
            //System.out.println("TEST OBJECT "+Hex.toHexString(inv32));
            System.arraycopy(objectProcess, 0, objectProcess, 32, objectProcess.length-32);
            System.arraycopy(inv32, 0, objectProcess, 0, 32);
        switch(type){
            
            case PACKET_OBJECT_MSG:
                for(int i = 0; i < PechkinPrivateData.getCountBMAddress();i++){
                    BMMessage  msg = parse.decodeMessage(object, PechkinPrivateData.getBMAddress(i).getPrivateCipherKey()
                            , PechkinPrivateData.getBMAddress(i).getAddress(), 1000, 1000);
                    if(msg != null){
                        System.out.println("receiv message "+Hex.toHexString(object));
                        msg.time = System.currentTimeMillis()/1000L;

                        PechkinPrivateData.addMessage(msg, MESSAGE_RECEIV_UNREAD);
                        if(msg.ack.length != 0)
                            PechkinData.addNew(msg.ack);
                        if(!msg.address_from.contains("BM-"))
                            msg.address_from = "BM-"+msg.address_from;
                        PechkinPrivateData.addPubKey(msg.address_from, msg.pub_key, msg.pow1, msg.pow2);
                        PechkinMainWindow.updateListMessage();
                        PechkinMainWindow.updateAddress();
                        BMLog.LogD("ProcessObject", "Receiv Message");
                    }
                }
            case PACKET_OBJECT_MSG_ACK:
                byte[] testInv = new byte[32];
                byte[] ack = parse.decodeAckMessage(object, 1000, 1000);
                if(ack != null){
                //BMLog.LogD("ProcessObject", "receiv ACK "+Hex.toHexString(ack));
                for(int i = 0; i < PechkinPrivateData.getWaitAckCount();i++){
                    testInv = PechkinPrivateData.getWaitACK(i);
                    if(Arrays.equals(ack, testInv)){
                        BMLog.LogD("ProcessObject","message delivery");
                        PechkinMessage msg = PechkinPrivateData.getMessage(PechkinPrivateData.getWaitAckMessageID(i));
                        msg.setStatus(BMConstant.MESSAGE_DELIVERY);
                        msg.setTime(System.currentTimeMillis()/1000L);
                        PechkinPrivateData.updateMessage(msg);
                        PechkinPrivateData.getWaitAckDelete(i);
                        PechkinMainWindow.updateListMessage();
                        PechkinMainWindow.updateAddress();
                    }
                }
                //
                }
            case PACKET_OBJECT_BROADCAST:
                String[] sub = PechkinPrivateData.getSubscribed();
                BMMessage msg = null;
                for(int i = 0; (i < sub.length)&&(msg == null);i++){
                   msg = parse.decodeBroadcastMessage(object, sub[i]);
                   
                }
                if(msg != null){
                    System.out.println("receiv broadcast message "+msg.address_from);
                    msg.address_to = BMConstant.ADDRESS_BROADCAST;
                    PechkinPrivateData.addMessage(msg, MESSAGE_RECEIV_UNREAD);
                    PechkinMainWindow.updateListMessage();
                    PechkinMainWindow.updateAddress();
                    
                }
                
            case PACKET_OBJECT_PUBKEY:
                break;
            case PACKET_OBJECT_GETPUBKEY:
                
            boolean flag = false;
            String address = "";
            int i = 0;
            //BMLog.LogD("ProcessObject", "RECEIV GETPUBKEY  "+Hex.toHexString(utils.createInv(object)));
            for(; (i < PechkinPrivateData.getCountBMAddress())&&(address.length() == 0);i++){
                if(Arrays.equals(parse.decodeGetPubKey(object)
                        ,addressUtils.getTagAddressBM(PechkinPrivateData.getBMAddress(i).getAddress()))){
                    address = PechkinPrivateData.getBMAddress(i).getAddress();
                    i--;
                }
            }
            if(address.length() != 0){
                BMAddress adr = PechkinPrivateData.getBMAddress(i);
                BMLog.LogD("ProcessObject","receiv request pubkey address "+adr.getAddress());
                PechkinWork.addAddress(adr);
                
                
                //return utils.createInv(ob);
            }
            break;
            default:
                BMLog.LogE("ProcessObject","not supported packet type "+type);
                break;
                    
        }
       
    }
   


}
}
