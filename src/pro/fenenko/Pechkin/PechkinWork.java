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

import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bouncycastle.util.encoders.Hex;
import pro.fenenko.Bitmessage.BMAddress;
import pro.fenenko.Bitmessage.BMLog;
import pro.fenenko.Bitmessage.BMPacket;

/**
 *
 * @author Fenenko Aleksandr
 */
public class PechkinWork extends Thread{
    private static BMAddress[] genPubKeyAddress = new BMAddress[10];
    private static int countAddress = 0;
    private static BMPacket packet = new BMPacket();
    
    
    public static void addAddress(BMAddress address){
        synchronized(genPubKeyAddress){
            if(countAddress < genPubKeyAddress.length){
                genPubKeyAddress[countAddress] = address;
                countAddress++;
            }
        }
    }
    
    
    @Override
    public void run(){
        for(;true;){
            int i = 0;
            BMAddress addr = null;
            synchronized(genPubKeyAddress){
                if(0 < countAddress){
                   addr = genPubKeyAddress[countAddress-1];
                    countAddress--;
                }
            }
            
            if(addr != null){
                byte[] ob = (new BMPacket()).createPubKey(addr.getPrivateSigningKey(),addr.getPrivateCipherKey() ,addr.getAddress());
                 BMLog.LogD("PechkinWord", "create pubkey "+Hex.toHexString(ob));
                PechkinData.addNew(ob);
                //byte[] ob = packet.crea
            }
            
            
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                Logger.getLogger(PechkinWork.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
}
