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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bouncycastle.util.Arrays;
import pro.fenenko.Pechkin.ProtobufNodeAddress.NodeAddresses;
import pro.fenenko.Pechkin.ProtobufNodeAddress.NodeAddress;
import pro.fenenko.Bitmessage.BMLog;
import pro.fenenko.Bitmessage.BMNodeAddress;
import pro.fenenko.Bitmessage.BMUtils;

/**
 *
 * @author Fenenko Aleksandr
 */
public class PechkinNodeAddress {

    private static final String FileName = PechkinConfigDir.getConfigDir()+"/NodeAddress.raw";


    private static NodeAddresses addresses;

    private static int[] idNot = new int[16];

  
    private static int updateCount = 0;
    private static BMUtils utils = new BMUtils();

   

    public static void init() {
        long timeout = System.currentTimeMillis();
        for (int i = 0; i < idNot.length; i++) {
            idNot[i] = -1;
        }
        boolean flagBootstrap = false;
        try {
            addresses = NodeAddresses.parseFrom(new FileInputStream(FileName));
            synchronized(addresses){
            long time = (System.currentTimeMillis()/1000L)-(3*3600);
            for(int i = 0; i < addresses.getAddressCount();){
                if(addresses.getAddress(i).getTime() < time){
                    addresses = addresses.toBuilder().removeAddress(i).build();
                } else{
                    i++;
                }
                
            }
            }
        } catch (FileNotFoundException ex) {
            BMLog.LogE("PechkinPrivateData", "create new file " + FileName);
            addresses = NodeAddresses.newBuilder().setOldId(1).build();
            write();
            flagBootstrap = true;

        } catch (IOException ex) {
            addresses = NodeAddresses.newBuilder().setOldId(1).build();
            write();
            flagBootstrap = true;
        }
        if(addresses.getAddressCount() == 0){
            flagBootstrap = true;
        }
        if (flagBootstrap) {
            synchronized(addresses){
            BMNodeAddress[] a = (new BMUtils()).helperBootStrap();
            add(a);
            }
            BMLog.LogD("PechkinNodeAddress", "BOOTSTRAP 10 ADDRESS ");
        }
        BMLog.LogD("PechkinNodeAddress", "READ "+FileName+" ADDRESS "+addresses.getAddressCount());

    }

    public static synchronized void write() {
        try {
            FileOutputStream file = new FileOutputStream(FileName);
            BufferedOutputStream output = new BufferedOutputStream(file,2048*1024);
            synchronized (addresses) {
                addresses.writeTo(output);
                output.close();
                file.close();
                output = null;
                file = null;
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PechkinPrivateData.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PechkinPrivateData.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    public static synchronized void add(byte[] buffNodeAddress){
        if((buffNodeAddress == null))//||(256 < addresses.getAddressCount()))
            return;
        synchronized (addresses){
            byte[] ip = new byte[16];
            boolean flagWrite = false;
            
            int port;
            boolean flag = true;
            String ip_s = "";
            for(int i = 0; i < buffNodeAddress.length;i+=38){
                System.arraycopy(buffNodeAddress, i+20, ip, 0, 16);
                try {
                    ip_s = InetAddress.getByAddress(ip).getHostAddress();
                } catch (UnknownHostException ex) {
                    Logger.getLogger(PechkinNodeAddress.class.getName()).log(Level.SEVERE, null, ex);
                }
                port = utils.bytesToShort(buffNodeAddress,i+36);
                flag = true;
                for(int j = 0; flag&&(j<addresses.getAddressCount());j++){
                    if(ip_s.contains(addresses.getAddress(j).getIp())
                            &&(port == addresses.getAddress(j).getPort())){
                        flag = false;
                    }
                }
                if(flag){
                    NodeAddress.Builder builder = NodeAddress.newBuilder();
                    builder.setCountConnect(10);
                    builder.setID(addresses.getOldId());
                    builder.setIp(ip_s);
                    builder.setPort(port);
                    builder.setTime(utils.bytesToLong(buffNodeAddress,i));
                    synchronized(addresses){
                        int t= addresses.getOldId()+1;
                        addresses = addresses.toBuilder().setOldId(t).addAddress(builder).build();
                        
                        
                    }
                    flagWrite = true;
                    
                    //id++;
                }
            }
            if(flagWrite){
                write();
            }
        }
    }

    public static synchronized void add(BMNodeAddress[] addr) {
        long time = System.currentTimeMillis() / 1000L - 3 * 3600;
        if(100 < addresses.getAddressCount())
            return;
        synchronized (addresses) {
            int id = addresses.getOldId();
            boolean flag = true;
            for (int i = 0; i < addr.length; i++) {
                flag = true;
                for (int j = 0; (j < addresses.getAddressCount()) && flag; j++) {
                    if(addr[i] != null){
                    if (addresses.getAddress(j).getIp().contains(addr[i].getIp())
                            && addresses.getAddress(j).getPort() == addr[i].getPort()) {
                        flag = false;
                    }
                    } else{
                        flag = false;
                    }
                }
                if (flag) {
                    NodeAddress.Builder builder = NodeAddress.newBuilder();
                    builder.setID(id);
                    builder.setIp(addr[i].getIp());
                    builder.setPort(addr[i].getPort());
                    builder.setCountConnect(10);
                    builder.setTime(time);
                    addresses = addresses.toBuilder().addAddress(builder).build();
                    id++;

                }
            }
            addresses = addresses.toBuilder().setOldId(id).build();
            write();
        }

    }

    public static synchronized int getCountAddress() {
        return addresses.getAddressCount();

    }

    public static synchronized BMNodeAddress[] getConnectAddress() {
        int count = 0;
        BMNodeAddress[] ret = null;
        synchronized (addresses) {

            for (int i = 0; i < addresses.getAddressCount(); i++) {
                if (10 < addresses.getAddress(i).getCountConnect()) {
                    count++;
                }
            }
            ret = new BMNodeAddress[count];
            for (int i = 0, j = 0; i < addresses.getAddressCount(); i++) {
                if (10 < addresses.getAddress(i).getCountConnect()) {
                    BMNodeAddress addr = new BMNodeAddress(
                            addresses.getAddress(i).getIp(),
                            addresses.getAddress(i).getPort(),
                            addresses.getAddress(i).getID());
                    ret[j] = addr;
                    j++;
                }
            }

        }
        return ret;
    }

    public static synchronized void changeCount(int id, int value) {
        synchronized (addresses) {
            boolean flag = true;
            for (int i = 0; (i < addresses.getAddressCount()) && flag; i++) {
                if (addresses.getAddress(i).getID() == id) {

                    NodeAddress addr = addresses.getAddress(i);
                    int r = addr.getCountConnect() + value;
                    addr = addr.toBuilder().setCountConnect(r)
                            .setTime(System.currentTimeMillis() / 1000L).build();
                    addresses = addresses.toBuilder().setAddress(i, addr).build();
                    write();
                    flag = false;
                }

            }
        }

    }
    
    
    public static synchronized void Disconnect(){
        System.arraycopy(idNot, 0, idNot, 1, idNot.length - 1);
        idNot[0] = -1;
    }

    public static synchronized BMNodeAddress get() {
        BMNodeAddress addr = null;
        int maxConnect = -1;
        synchronized (addresses) {
            for (int i = 0; i < addresses.getAddressCount(); i++) {

                boolean flag = true;
                int count = addresses.getAddressCount();
                for (int j = 0; (j < idNot.length) && (j < count ) && flag; j++) {
                    if (addresses.getAddress(i).getID() == idNot[j]) {
                        flag = false;
                    }
                }
                if (flag) {
                    if (maxConnect < addresses.getAddress(i).getCountConnect()) {
                        
                        addr = null;
                        addr = new BMNodeAddress(
                                addresses.getAddress(i).getIp(),
                                addresses.getAddress(i).getPort(),
                                addresses.getAddress(i).getID(),
                                addresses.getAddress(i).getTime(),
                                addresses.getAddress(i).getCountConnect());
                        maxConnect = addresses.getAddress(i).getCountConnect();
                        
                    }
                }
            }
            System.arraycopy(idNot, 0, idNot, 1, idNot.length - 1);
            if(addr != null)
            {
                idNot[0] = addr.getId();
            } else{
                BMNodeAddress[] a = utils.helperBootStrap();
                add(a);
                return get();
            }
           
        }

        return addr;
    }

}
