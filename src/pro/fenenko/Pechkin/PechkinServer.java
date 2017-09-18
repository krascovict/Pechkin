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
import java.net.ServerSocket;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bouncycastle.crypto.tls.TlsServerProtocol;
import pro.fenenko.Bitmessage.BMConstant;
import static pro.fenenko.Bitmessage.BMConstant.PACKET_NOTDATA;
import static pro.fenenko.Bitmessage.BMConstant.PACKET_VERACK;
import static pro.fenenko.Bitmessage.BMConstant.PACKET_VERSION;
import pro.fenenko.Bitmessage.BMLog;
import pro.fenenko.Bitmessage.BMObject;
import pro.fenenko.Bitmessage.BMPacket;
import pro.fenenko.Bitmessage.BMParse;
import pro.fenenko.Bitmessage.BMTlsServer;
import pro.fenenko.Bitmessage.BMVersion;

/**
 *
 * @author Fenenko Aleksandr
 */
public class PechkinServer extends Thread implements BMConstant {

    private ServerSocket socket;
    private PechkinServerSocket server;
    private boolean flagRun;

    public PechkinServer(int port, int countInputConnect) {
        flagRun = true;
        server = new PechkinServerSocket(countInputConnect);
        server.start();
        try {
            socket = new ServerSocket(port, countInputConnect);
            socket.setSoTimeout(100);
        } catch (IOException ex) {
            Logger.getLogger(PechkinServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void Stop(){
        server.Stop();
        
        flagRun = false;
        
        
        
        BMLog.LogD("PechkinServer", "Disconnect");
        
    }

    @Override
    public void run() {
        long time;
        BMLog.LogD("PechkinServer", "START");
        for (; flagRun;) {
            try {
                
                Socket sock = null;
                try{
                sock = socket.accept();
                } catch(java.net.SocketTimeoutException ex){
                //System.err.println("TIMEOUT ");
                if(!flagRun){
                    socket.close();
                }
                continue;
            }
                System.out.println("Connect to " + sock.getInetAddress().toString());
                BufferedInputStream bin = new BufferedInputStream(sock.getInputStream(), 10240);
                BufferedOutputStream bout = new BufferedOutputStream(sock.getOutputStream(), 10240);
                long timeout = System.currentTimeMillis();
                boolean flag = true;
                BMObject retVal = new BMObject();
                int lenDataIn = 0;
                byte[] dataIn = new byte[4096];
                BMParse parse = new BMParse();
                BMPacket packet = new BMPacket();
                while (flag) {
                    int count = bin.available();
                    if (count != 0) {
                        if (dataIn.length < (lenDataIn + count)) {
                            dataIn = Arrays.copyOf(dataIn, lenDataIn + count);
                        }
                        bin.read(dataIn, lenDataIn, count);
                        lenDataIn += count;
                    }
                    lenDataIn = parse.getCommand(dataIn, lenDataIn, retVal);
                    switch (retVal.command) {
                        case PACKET_NOTDATA:
                            break;
                        case PACKET_VERSION:
                            BMLog.LogD("BMServer", "receiv VERSION");

                            timeout = System.currentTimeMillis();
                            byte[] pack = packet.createPacketVerack();
                            bout.write(pack);
                            bout.flush();
                            pack = packet.createPacketVersion(1, "127.0.0.1", 37234,
                                    "127.0.0.1", 8444, (System.currentTimeMillis() / 1000L), (new Random()).nextLong(), new int[]{1});
                            bout.write(pack);
                            bout.flush();
                            break;
                        case PACKET_VERACK:
                            BMLog.LogD("BMServer", "receiv VERACK");
                            timeout = System.currentTimeMillis();

                            pack = packet.createPacketAddress(PechkinNodeAddress.getConnectAddress());
                            //bout.write(pack, 0, pack.length);
                            
                             
                            flag = false;
                            BMLog.LogD("PechkinServer", "set server client ");
                            server.addSocket(sock, bin, bout);
                             

                            break;
                    }
                    if (120000 < (System.currentTimeMillis() - timeout)) {
                        flag = false;
                        bin.close();
                        bout.close();
                        sock.close();
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(PechkinServer.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }
}
