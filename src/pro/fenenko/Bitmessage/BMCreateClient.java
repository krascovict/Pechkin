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
package pro.fenenko.Bitmessage;

import org.bouncycastle.crypto.tls.TlsClientProtocol;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.Random;

/**
 *
 * @author Fenenko Aleksandr
 */
public class BMCreateClient extends Thread implements BMConstant {

    String may_ip, server_ip;
    int may_port, server_port;
    long timeout;

    int id;
    Socket sock = null;
    BufferedInputStream sin = null;
    BufferedOutputStream sout = null;
    TlsClientProtocol protocol = null;
    BMMethodClientConnect method;
    BMMethodNotConnect notConnect;

    long services;
    BMVersion version;
    boolean connecting = false;
    BMParse parse;
    private BMMethodClientProcess process;
    private boolean flagRun;

    public BMCreateClient(String ipLocal, int portLocal, BMMethodClientConnect meth, BMMethodNotConnect methNotConnect, BMMethodClientProcess process) {

        BMLog.LogD("Pechkin.BMCreateClient", "start constructor");
        timeout = System.currentTimeMillis();
        flagRun = true;

        this.may_ip = ipLocal;
        this.may_port = portLocal;
        this.process = process;

        method = meth;
        notConnect = methNotConnect;
        //disconnect = false;

        //timeoutReconnect = S
        // ystem.currentTimeMillis();
        //flagConnect = false;
        //flagReceivInv = false;
        //flagRequestInv = false;
        services = 3;
        connecting = false;
        parse = new BMParse();
        //BMLog.LogD("Pechkin.BMCreateClient","create client id "+id+"  to "+ip+" "+port);
    }

    public boolean setClient(String ip, int port, int id) {
        if (connecting) {
            return false;
        }

        server_ip = ip;
        server_port = port;
        sock = new Socket();
        this.id = id;
        connecting = true;
        return true;
    }

    void close() {

        try {
            if (protocol != null) {
                protocol.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (sout != null) {
                sout.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (sin != null) {
                sin.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (sock != null) {
                sock.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        notConnect.notConnect(id);
        BMLog.LogE("Pechkin.BMCreateClient", "error connect " + server_ip + " " + server_port);

    }
    
    public void Stop(){
        flagRun = false;
        Thread.currentThread().interrupt();
    }

    @Override
    public void run() {
        BMLog.LogD("Pechkin.BMCreateClient", "start thread");
        byte[] data = new byte[102400];
        BMObject retVal = new BMObject();
        int dataCount;
        for (; flagRun;) {
            while (!connecting) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            dataCount = 0;
            BMLog.LogD("Pechkin.BMCreateClient", "start connect to " + server_ip + " " + server_port);
            InetSocketAddress address = new InetSocketAddress(server_ip, server_port);
            sock = new Socket();

            long unixTime = System.currentTimeMillis() / 1000L;
            Random rand = new Random();
            int[] stream = {1};
            
            try {
                sock = new Socket();

                sock.connect(address, 120000);

                BufferedInputStream sin = new BufferedInputStream(sock.getInputStream(), 102400);

                BufferedOutputStream sout = new BufferedOutputStream(sock.getOutputStream(), 4096);
                byte[] version = (new BMPacket()).createPacketVersion(SERVICES,server_ip, server_port, may_ip, may_port, unixTime, rand.nextLong(), stream);
                sout.write(version);
                sout.flush();
                timeout = System.currentTimeMillis();

                boolean flagRunning = true;
                boolean receiv_verack = false;
                boolean receiv_version = false;

                while (flagRunning) {
                    if (60000 < (System.currentTimeMillis() - timeout)) {
                        BMLog.LogE("Pechkin.BMCreateClient", "error timeout wait VERSION " + server_ip + " " + server_port);

                        close();
                        flagRunning = false;
                        continue;
                        //return;
                    }
                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (sin.available() != 0) {
                        int count = sin.available();
                        sin.read(data, dataCount, count);
                        dataCount += count;
                        //data = BMUtils.addBuff(data, tmp);
                    }
                    dataCount = parse.getCommand(data, dataCount, retVal);

                    if (retVal.command == PACKET_VERACK) {

                        receiv_verack = true;
                    }

                    if (retVal.command == PACKET_VERSION) {
                        //BMUtils.printBuff("Pechkin.BMCreateClient",(byte[])bm[1].object);
                        //System.out.println("receiv version "+server_ip);
                        //System.out.print("services "+server_ip+" ");
                        //System.out.println((long)bm[1].object);
                        this.version = (BMVersion) retVal.object;
                        services = this.version.getServices();
                        BMLog.LogD("BMCreateClient", "RECEIV VERSION services "+services);
                        sout.write((new BMPacket()).createPacketVerack());
                        sout.flush();
                        receiv_version = true;
                    }
                    if (receiv_verack && receiv_version) {

                        if ((services & 2) == 2) {
                            
                            protocol = new TlsClientProtocol(new SecureRandom());
                            BMtls client = new BMtls();

                            try {
                                protocol.connect(client);
                                //status = 2;
                                //handler.sendEmptyMessage(CLIENT_REQUEST_INV);

                                timeout = System.currentTimeMillis();

                            } catch (IOException e) {

                                close();
                                flagRunning = false;
                                continue;
                                //return;
                            }
                        } else {
                            

                            timeout = System.currentTimeMillis();
                        }
                        //Message msg = handler.obtainMessage(CLIENT_STATUS_CONNECT,new BMCreateClientMessage(id,BMConstant.CLIENT_STATUS_CONNECT));
                        //handler.sendMessage(msg);
                       

                        BMClient cl = new BMClient(new BMNodeAddress(server_ip, server_port), sock, sin, sout, protocol, id, services, process);
                        //BMLog.LogD("Pechkin.BMCreateClient", " setClient ");
                        method.setClient(cl, this.version);
                        flagRunning = false;

                    }
                }
                timeout = System.currentTimeMillis();
            } catch (IOException e) {
                //e.printStackTrace();
                //BMLog.LogD("Pechkin.BMCreateClient","not connect to "+server_ip+" "+server_port);
                close();
            }
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            connecting = false;

        }

    }

    public boolean isFinish() {
        return !connecting;
    }
}
