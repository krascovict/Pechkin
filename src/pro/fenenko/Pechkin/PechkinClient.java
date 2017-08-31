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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bouncycastle.util.encoders.Hex;
import pro.fenenko.Bitmessage.BMClient;
import pro.fenenko.Bitmessage.BMClientMessage;
import pro.fenenko.Bitmessage.BMConstant;
import static pro.fenenko.Bitmessage.BMConstant.CLIENT_RECEIV_OBJECT;
import pro.fenenko.Bitmessage.BMCreateClient;
import pro.fenenko.Bitmessage.BMLog;
import pro.fenenko.Bitmessage.BMMethodClientConnect;
import pro.fenenko.Bitmessage.BMMethodClientProcess;
import pro.fenenko.Bitmessage.BMMethodNotConnect;
import pro.fenenko.Bitmessage.BMNodeAddress;
import pro.fenenko.Bitmessage.BMPacket;
import pro.fenenko.Bitmessage.BMVersion;

/**
 *
 * @author Fenenko Aleksandr
 */
public class PechkinClient extends Thread implements BMConstant {

    private int idAddress;
    private BMClient[] clients;
    private boolean clientConnect;
    //private int countReceivObject;

    private String ip;
    private int port;
    private BMCreateClient createClient;
    private BMPacket packet;
    private int countReceivObject = 0;
    private boolean flagRun = false;
    private long timeoutRunGC = System.currentTimeMillis();

    private void RunGC() {

        if (5000 < (System.currentTimeMillis() - timeoutRunGC)) {
            System.gc();

            timeoutRunGC = System.currentTimeMillis();
        }

    }

    public PechkinClient(int countConnect) {
        flagRun = true;
        clients = new BMClient[countConnect];
        for (int i = 0; i < clients.length; i++) {
            clients[i] = new BMClient();
        }

        clientConnect = true;
        packet = new BMPacket();

        createClient = new BMCreateClient("127.0.0.1", 8444,
                new BMMethodClientConnect() {
            @Override
            public void setClient(BMClient client, BMVersion version) {
                //PechkinData.readINV();
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                PechkinNodeAddress.changeCount(client.getID(), 1);
                boolean flag = true;
                synchronized (clients) {
                    for (int i = 0; (i < clients.length) && flag; i++) {
                        if (clients[i].isDisconnect()) {
                            BMLog.LogD("Pechkin.PechkinClient", "connected client " + i + " to " + client.getAddress());
                            clients[i] = client;
                            flag = false;
                        }
                    }

                }
            }

        },
                new BMMethodNotConnect() {
            @Override
            public void notConnect(int id) {
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                PechkinNodeAddress.changeCount(id, -1);
            }
        }, new BMMethodClientProcess() {

            @Override
            public void receiverDisconnect(int id) {
                PechkinNodeAddress.Disconnect();
                BMLog.LogE("PechkinClient", "disconnect client  " + id);
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public synchronized void receiverObject(int id, byte[] inv, byte[] object) {
                //System.out.println("RECEIV OBJECT "+id+" "+Hex.toHexString(inv)+" "+PechkinData.getCount());
                if (object == null) {
                    return;
                }
                PechkinProcessObject.process(object);
                PechkinData.add(object);

                object = null;
                inv = null;
                RunGC();

                //
                //
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public synchronized void receiverInv(int id, byte[] inv) {
                int number = -1;
                long timeout = System.currentTimeMillis();
                for (int i = 0; (i < clients.length) && (number == -1); i++) {
                    if (clients[i].getID() == id) {
                        number = i;
                    }
                }
                PechkinNodeAddress.changeCount(clients[number].getID(), 1);
                if (!clients[number].isSendInv()) {
                    byte[] t = PechkinData.getInv();
                    if (0 < t.length) {
                        clients[number].transmit(packet.createPacketInv(t));
                        clients[number].setSendInv();

                        BMLog.LogD("PechkinClient", id + " SEND INV ALL ");
                    }
                }
                int count = inv.length / 32;
                inv = PechkinData.getRequestNewInv(inv);
                clients[number].transmit(packet.createPacketGetdata(inv));
                BMLog.LogD("PechkinClient", id + " RECEIV INV " + count + " SEND GETDATA " + inv.length / 32 + " KNOW OBJECTS " + PechkinData.getCountObject() + " time " + (System.currentTimeMillis() - timeout) + " ms");
                RunGC();
            }

            @Override
            public synchronized void receiverGetData(int id, byte[] inv) {
                int number = -1;
                for (int i = 0; (i < clients.length) && (number == -1); i++) {
                    if (clients[i].getID() == id) {
                        number = i;
                    }
                }
                if (0 <= number) {
                    byte[] inv32 = new byte[32];
                    int countSend = 0;
                    BMLog.LogD("PechkinClient", id + " request GETDATA " + inv.length / 32);
                    for (int i = 0; i < inv.length; i += 32) {
                        System.arraycopy(inv, i, inv32, 0, 32);
                        byte[] ob = PechkinData.get(inv32);
                        if (ob == null) {
                            BMLog.LogE("PechkinClient", id + " object " + Hex.toHexString(inv32) + " NOT FIND");
                        } else {
                            clients[number].transmit(packet.createPacketObject(ob));
                            countSend++;
                            //BMLog.LogD("PechkinClient", id + " send object " + Hex.toHexString(inv32));
                        }
                    }
                    BMLog.LogD("PechkinClient", id + " send " + countSend + " OBJECTs ");
                    if (inv.length == 32) {
                        BMLog.LogD("PechkinClient", "INV " + Hex.toHexString(inv));
                    }
                }
                RunGC();
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void receiverAddress(int id, byte[] buffNodeAddress) {
                if (buffNodeAddress == null) {
                    BMLog.LogE("PechkinClient", "receiv NodeAddress NULL");
                    return;
                }
                int number = -1;
                for (int i = 0; (i < clients.length) && (number == -1); i++) {
                    if (clients[i].getID() == id) {
                        number = i;
                    }
                }
                if (!clients[number].isSendAddress()) {
                    clients[number].transmit(packet.createPacketAddress(PechkinNodeAddress.getConnectAddress()));
                    clients[number].setSendAddress();
                    BMLog.LogD("PechkinClient", "SEND ADDRESS TO " + id);
                    BMLog.LogD("PechkinClient", "receiv from " + clients[number].getAddress() + " " + buffNodeAddress.length / 38 + " address KNOW ADDRESS " + PechkinNodeAddress.getCountAddress());

                }
                PechkinNodeAddress.add(buffNodeAddress);

                number = buffNodeAddress.length;

                buffNodeAddress = null;

                if (100 < number) {
                    RunGC();
                }
                //    System.gc();

                //System.gc();
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

        }
        );
        createClient.start();

    }

    public boolean getStatus() {
        return clientConnect;
    }
    
    public void Stop(){
        createClient.Stop();
        flagRun = false;
        Thread.currentThread().interrupt();
        for(int i = 0; i < clients.length;i++){
            if(clients[i].isConnected()){
                clients[i].close();
            }
        }
        PechkinNodeAddress.Disconnect();
        BMLog.LogD("PechkinClient", "Stop ");
    }

    public boolean changeStatusConnect() {
        clientConnect = !clientConnect;
        return clientConnect;
    }

    @Override
    public void run() {

       
        boolean flagReceivObject = false;
        BMClientMessage msg;
        long timeout;
        long timeoutReceiv = System.currentTimeMillis();
        for (; flagRun;) {
            timeout = System.currentTimeMillis();
            byte[] newInv = PechkinData.getNewInv();
            if (0 < newInv.length) {
                //BMLog.LogD("PechkinClient", "PechkinServerSocket add " + newInv.length);
                PechkinServerSocket.addInv(newInv);
            }

            synchronized (clients) {

                for (int i = 0; i < clients.length; i++) {
                    //System.out.println("process client "+i);
                    if (clientConnect) {
                        if (clients[i].isConnected()) {
                            //System.out.println("process client "+i);

                            if (0 < newInv.length) {
                                byte[] ob = packet.createPacketInv(newInv);
                                clients[i].transmit(ob);
                                BMLog.LogD("PechkinClient", "SEND  NEW INV ");
                            }
                            try {
                                clients[i].process();
                            } catch (java.lang.OutOfMemoryError ex) {
                                System.err.println("ERROR PechkinClient 2" + ex.getMessage());
                                clients[i].close();
                            }

                        }
                        if (clients[i].isDisconnect() && (createClient.isFinish())) {
                            //System.out.println("reconnect client "+i);

                            clients[i] = null;
                            System.gc();
                            clients[i] = new BMClient();
                            BMNodeAddress addr = PechkinNodeAddress.get();
                            if (addr != null) {
                                createClient.setClient(addr.getIp(), addr.getPort(), addr.getId());
                            }
                            //System.out.println("start connect to " + addr.getIp() + " " + addr.getPort());
                        }

                    } else {
                        if (clients[i].isConnected()) {
                            clients[i].close();
                        }
                    }
                }
                newInv = new byte[0];
            }
            if ((System.currentTimeMillis() - timeout) < 200) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Logger.getLogger(PechkinClient.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
        //System.out.println("disconnect client "+clients[i].getAddress());

        //clientStatus = false;
    }

}
