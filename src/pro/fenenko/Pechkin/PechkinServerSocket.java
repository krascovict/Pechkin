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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.encoders.Hex;
import pro.fenenko.Bitmessage.BMLog;
import pro.fenenko.Bitmessage.BMMethodServer;
import pro.fenenko.Bitmessage.BMNodeAddress;
import pro.fenenko.Bitmessage.BMPacket;
import pro.fenenko.Bitmessage.BMServer;
import pro.fenenko.Bitmessage.BMUtils;

/**
 *
 * @author Fenenko Aleksandr
 */
public class PechkinServerSocket extends Thread implements BMMethodServer {

    public BMServer[] sockets;
    private static byte[] newInv = new byte[0];
    private BMPacket packet = new BMPacket();
    private boolean flagRunning;

    public static synchronized void addInv(byte[] buff) {
        synchronized (newInv) {
            int l = newInv.length;
            newInv = java.util.Arrays.copyOf(newInv, l + buff.length);
            System.arraycopy(buff, 0, newInv, l, buff.length);
        }
    }

    public PechkinServerSocket(int countInputConnect) {
        flagRunning = true;
        sockets = new BMServer[countInputConnect];
        for (int i = 0; i < sockets.length; i++) {
            sockets[i] = new BMServer();
        }

    }

    public synchronized void addSocket(Socket socket,BufferedInputStream in,BufferedOutputStream out) {
        boolean flag = true;
        synchronized (sockets) {
            for (int i = 0; (i < sockets.length) && flag; i++) {
                if (!sockets[i].isConnect()) {
                    sockets[i] = null;
                    System.gc();

                    sockets[i] = new BMServer(socket,in,out, this);
                    
                    byte[] pack = packet.createPacketAddress(PechkinNodeAddress.getConnectAddress());
                    sockets[i].transmit(pack);
                    flag = false;

                }
            }
        }
    }
    
    public void Stop(){
        flagRunning = false;
        Thread.currentThread().interrupt();
        for(int i = 0; i < sockets.length;i++){
            if(sockets[i].isConnect())
                sockets[i].close();
        }
    }

    @Override
    public void run() {
        long time = System.currentTimeMillis();
        while (flagRunning) {
            time = System.currentTimeMillis();
            synchronized (sockets) {
              
                    for (int i = 0; i < sockets.length; i++) {
                        //System.out.println("PechkinServerSocket "+i+" "+sockets[i].isConnect());

                        if (sockets[i].isConnect()) {
                            sockets[i].process();
                            if(0 < newInv.length)
                            {
                                BMLog.LogD("PechkinServerSocket", "SEND new INV");
                                sockets[i].transmit(packet.createPacketInv(newInv));
                            }
                        }

                    }
                    if(0 < newInv.length)
                        newInv = new byte[0];
                }
            
            if (System.currentTimeMillis() - time < 100) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                    Logger.getLogger(PechkinServerSocket.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    @Override
    public byte[] sendINV() {
        //PechkinData.readINV();
        return PechkinData.getInv();
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public BMNodeAddress[] sendAddress() {
        return PechkinNodeAddress.getConnectAddress();
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public byte[] receivINV(byte[] inv) {
        //byte[] invKnow = PechkinData.getAllInv();
        //int c = PechkinNative.getInv(inv, invKnow);
        //inv = Arrays.copyOf(inv, c);
        BMLog.LogD("PechkinServerSocket", "RECEIV INV");
        inv = PechkinData.getRequestNewInv(inv);
        //invKnow = null;
        return inv;

    }

    @Override
    public void receivAddress(BMNodeAddress[] address) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public byte[] getObject(byte[] inv) {
        BMLog.LogD("PechkinServerSocket", "request object " + Hex.toHexString(inv));
        return PechkinData.get(inv);
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public byte[] requestNewINV(byte[] inv) {
        return PechkinData.getInv();
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void receivObject(byte[] object) {
        PechkinProcessObject.process(object);
        PechkinData.add(object);
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
