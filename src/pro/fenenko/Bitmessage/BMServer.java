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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bouncycastle.crypto.tls.TlsServerProtocol;
import org.bouncycastle.util.encoders.Hex;
import pro.fenenko.Pechkin.PechkinData;

/**
 *
 * @author Fenenko Aleksandr
 */
public class BMServer implements BMConstant {
    private static final String LOG_TAG="BMServer";
    private Socket socket;
    private BufferedInputStream bin;
    private BufferedOutputStream bout;
    private byte[] dataIn;
    private int lenDataIn;
    private BMParse parse;
    private BMPacket packet;
    private int count = 0;
    private BMObject retVal;
    private long timeout;
    private boolean flagConnect = false;
    private byte[] sendData = new byte[409600];
    private BMTlsServer serverTls;
    private TlsServerProtocol serverProtocol;
    private BMMethodServer methodServer;
    private int lenSendData = 0;
    private long timeoutSendData = 0;
    private boolean flagSendPing;
    private byte[] ob = new byte[409600];

    private byte[] inv32 = new byte[32];

    public BMServer() {
        flagConnect = false;
    }

    public BMServer(Socket sock, BufferedInputStream in, BufferedOutputStream out, BMMethodServer method) {
        methodServer = method;
        socket = sock;
        bin = in;
        bout = out;
        flagSendPing = false;
        dataIn = new byte[64 * 1024];
        lenDataIn = 0;
        parse = new BMParse();
        packet = new BMPacket();

        retVal = new BMObject();
        timeout = System.currentTimeMillis();
        flagConnect = true;
        BMLog.LogD(LOG_TAG, "connectClient Address,"+socket.getInetAddress());

    }

    public boolean isConnect() {
        return flagConnect;
    }

    public void close() {
        flagConnect = false;
        try {
            bout.close();
        } catch (IOException ex) {
            //Logger.getLogger(BMServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            bin.close();
        } catch (IOException ex) {
           //Logger.getLogger(BMServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            socket.close();
        } catch (IOException ex) {
            //Logger.getLogger(BMServer.class.getName()).log(Level.SEVERE, null, ex);
        }
       
    }

    public void process() {
        if(!flagConnect)
            return;
        try {

            if (600000 < (System.currentTimeMillis() - timeout)) {
                close();
            }
            if ((300000 < (System.currentTimeMillis() - timeout))&&(!flagSendPing)) {
                //timeout = System.currentTimeMillis();
                byte[] t = packet.createPacketPing();
                BMLog.LogD("BMServer", "SEND PACKET PING");
                flagSendPing = true;
                transmit(t);
            }
            synchronized (sendData) {
                if ((lenSendData != 0) && (20 < (System.currentTimeMillis() - timeoutSendData))) {
                    int count = 2048;
                    if (lenSendData < count) {
                        count = lenSendData;
                    }
                    
                    bout.write(sendData, 0, count);
                    bout.flush();

                    System.arraycopy(sendData, count, sendData, 0, lenSendData - count);
                    lenSendData -= count;
                    timeoutSendData = System.currentTimeMillis();
                    timeout = System.currentTimeMillis();
                    

                }
                
            }

            count = bin.available();
            if (count != 0) {
                if (dataIn.length < (lenDataIn + count)) {
                    dataIn = Arrays.copyOf(dataIn, lenDataIn + count);
                }
                bin.read(dataIn, lenDataIn, count);
                lenDataIn += count;
            }
            byte[] tmp = dataIn;
            if(sendData.length/2 < lenSendData){
                   return;
                        
                }
            lenDataIn = parse.getCommand(dataIn, lenDataIn, retVal);
            /*
            if (retVal.command != PACKET_NOTDATA) {
                BMLog.LogD("BMServer", "receiv type message " + retVal.command + " len " + lenDataIn+Hex.toHexString(tmp));
            }
            */
            switch (retVal.command) {
                case PACKET_NOTDATA:
                    break;
                case PACKET_INV:
                    byte[] t = (byte[]) retVal.object;
                    timeout = System.currentTimeMillis();
                    BMLog.LogD("BMServer", " receiv INV " + t.length + " ");
                    t = methodServer.receivINV(t);
                    if (0 < t.length) {
                        BMLog.LogD("BMServer", "send packet getdata "+t.length/32);
                        transmit(packet.createPacketGetdata(t));
                    }
                    t = null;
                    break;
                case PACKET_PONG:
                    BMLog.LogD("BMServer", " receiv PONG");
                    timeout = System.currentTimeMillis();
                    flagSendPing = false;
                    break;
                case PACKET_PING:
                    t = packet.createPacketPong();
                    BMLog.LogD("BMServer", " receiv PING send PONG");
                    
                    // serverProtocol.offerOutput(t,0,t.length);
                    transmit(t);
                    timeout = System.currentTimeMillis();
                    break;
                case PACKET_OBJECT:
                    t = (byte[]) retVal.object;
                    BMLog.LogD("BMServer", " receiv OBJECT");
                    methodServer.receivObject(t);
                    timeout = System.currentTimeMillis();
                    break;
                case PACKET_ADDRESS:
                    t = (byte[]) retVal.object;
                    BMLog.LogD("BMServer", "Receiv Address");
                    //methodServer.receivAddress(address);
                    t = packet.createPacketAddress(methodServer.sendAddress());
                    transmit(t);
                    t = methodServer.sendINV();
                    t = packet.createPacketInv(t);
                    transmit(t);
                    BMLog.LogD("BMServer", "send INV ");
                    break;
                case PACKET_GETDATA:
                    t = (byte[]) retVal.object;
                    //System.out.println("REQUEST GETDATA ");
                    
                    
                    int countSendObjects = 0;
                    for (int i = 0; i < t.length; i += 32) {
                        System.arraycopy(t, i, inv32, 0, 32);
                        //ob = methodServer.getObject(inv32);
                        int count = methodServer.getObject(inv32,ob);
                        if (0 < count) {
                            transmit(packet.createPacketObject(ob,count));
                            countSendObjects++;
                        }
                        //ob = null;
                    }
                    
                    BMLog.LogD("BMServer", "REQUEST GETDATA CountGetDataObjects SendObjects  " + t.length / 32+","+countSendObjects);
                    t = null;
                    timeout = System.currentTimeMillis();
                    break;
            }
            retVal.command = PACKET_NOTDATA;

        } catch (IOException ex) {
            BMLog.LogE("BMServer", "error " + ex.toString());
            close();
        }

    }

    public synchronized void transmit(byte[] data) {

        synchronized (sendData) {
            //BMLog
            if (sendData.length < (lenSendData + data.length)) {
                BMLog.LogE("BMServer", "realloc sendData"+(sendData.length)+" "+(lenSendData+data.length));
                sendData = Arrays.copyOf(sendData, sendData.length + data.length);
            }
            System.arraycopy(data, 0, sendData, lenSendData, data.length);
            lenSendData += data.length;
            //} else{
            //    System.err.println("ERROR SEND DATA");
            //}
        }
    }
}
