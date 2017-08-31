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

import java.net.Socket;
import java.net.InetSocketAddress;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;

import org.bouncycastle.crypto.tls.*;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Arrays;
import static java.util.Arrays.stream;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bouncycastle.util.encoders.Hex;

/**
 *
 * @author Fenenko Aleksandr
 */
public class BMClient implements BMConstant {

    byte[] data;
    private Socket sock;
    private BufferedInputStream sin;
    private BufferedOutputStream sout;
    private TlsClientProtocol protocol;
    private BMNodeAddress address;
    boolean wait_pong;
    boolean flagRunning;
    long timeout;
    int id;
    boolean flagReceivInv;
    boolean flagReceivAddress;
    boolean flagConnect;
    boolean flagDisconnect;
    long timeoutDisconnect;
    long timeoutRecvInv;
    boolean flagSendInv;
    boolean flagSendAddress;
    boolean flagNotConnect;
    boolean flagNotDisconnect;
    boolean thisConnect = false;
    boolean flagReceivVerack = false;
    boolean flagFirstSendGetdata = true;
    long timeoutProcessa;
    int counterIn;
    int counterOut;
    int pCin;
    int pCout;
    int dataCount;

    long services;
    BMClientMessage msg;
    BMParse parse;
    BMPacket packet;
    int t1;
    BMObject retVal = new BMObject();
    byte[] sendData;// = new byte[0];
    int lenSendData;
    long timeoutSendGetdata = -1;
    long timeoutSendData = 0;
    private long timeoutReceivObject;
    private BMMethodClientProcess receiver = null;
    private BMUtils utils = new BMUtils();
    //private byte[] requestGetData = new byte[32 * 9000];
    private int lenRequestGetData = 0;

    public BMClient() {
        flagRunning = false;
        flagDisconnect = true;
        flagConnect = false;
        flagNotConnect = false;
        data = new byte[0];
        flagNotDisconnect = false;
        counterIn = 0;
        counterOut = 0;
        pCin = 0;
        pCout = 0;
        dataCount = 0;
        data = new byte[0];
        msg = new BMClientMessage();
        parse = new BMParse();
        packet = new BMPacket();
        sendData = new byte[0];
        lenSendData = 0;
        t1 = 0;
        timeoutReceivObject = System.currentTimeMillis();
    }

    public BMClient(BMNodeAddress address, Socket socket, BufferedInputStream in, BufferedOutputStream out, TlsClientProtocol protocol, int id, long service, BMMethodClientProcess receiver) {
        BMLog.LogD("BMClient", "SERVICES "+service);
        this.receiver = receiver;
        sock = socket;
        sin = in;
        sout = out;
        this.protocol = protocol;
        this.id = id;
        flagConnect = true;
        flagDisconnect = false;
        flagSendInv = false;
        flagSendAddress = false;
        timeout = System.currentTimeMillis();
        timeoutDisconnect = timeout;
        timeoutRecvInv = timeout;
        flagReceivAddress = false;
        flagReceivInv = false;
        flagRunning = true;
        data = new byte[0];
        services = service;
        this.address = address;

        timeoutProcessa = System.currentTimeMillis();
        counterIn = 0;
        counterOut = 0;
        pCin = 0;
        pCout = 0;
        dataCount = 0;

        msg = new BMClientMessage();
        parse = new BMParse();
        packet = new BMPacket();
        sendData = new byte[0];
        lenSendData = 0;
        timeoutReceivObject = System.currentTimeMillis();
    }

    public String getRemoteAddress() {
        return address.toString();
    }

    public boolean isSendInv() {
        return flagSendInv;
    }

    public void setSendInv() {
        flagSendInv = true;
    }

    public boolean isSendAddress() {
        return flagSendAddress;
    }

    public void setSendAddress() {
        flagSendAddress = true;
    }

    public boolean isTimeout() {
        return 300000 < (System.currentTimeMillis() - timeout);
    }

    public void setNotDisconnect() {
        flagNotDisconnect = true;
    }

    public void process() {
        if (!flagConnect) {
            return;
        }
        if (flagDisconnect) {
            return;
        }
        try {
            if ((services & 2) == 2) {
                int count = protocol.getAvailableInputBytes();
                if (0 < count) {
                    if (data.length < (dataCount + count)) {
                        try {
                            data = Arrays.copyOfRange(data, 0, dataCount + count);

                        } catch (java.lang.OutOfMemoryError ex) {
                            BMLog.LogE("Pechkin.BMClient", "error alloc memory " + (dataCount + count) + " " + ex.toString());
                            close();
                        }
                    }

                    protocol.readInput(data, dataCount, count);
                    dataCount += count;
                    t1 = dataCount;
                }
                if (protocol.getAvailableOutputBytes() != 0) {
                    count = protocol.getAvailableOutputBytes();
                    byte[] tmp = new byte[count];

                    protocol.readOutput(tmp, 0, count);
                    counterOut += count;

                    sout.write(tmp, 0, count);
                    sout.flush();

                }
                if (sin.available() != 0) {
                    byte[] tmp = new byte[sin.available()];

                    sin.read(tmp, 0, tmp.length);
                    counterIn += tmp.length;

                    protocol.offerInput(tmp);
                    tmp = null;

                }
            } else {
                int count = sin.available();
                if (0 < count) {
                    //byte[] tmp = new byte[sin.available()];
                    if (data.length < (dataCount + count)) {
                        try {
                            data = Arrays.copyOfRange(data, 0, dataCount + count);
                        } catch (java.lang.OutOfMemoryError ex) {
                            BMLog.LogE("Pechkin.BMClient", "error alloc memory " + (dataCount + count) + " " + ex.toString());
                            close();
                        }
                    }
                    sin.read(data, dataCount, count);
                    counterIn += count;
                    dataCount += count;
                    //BMLog.LogD("BMClient","receiv data "+Hex.toHexString(data));
                    //data = BMUtils.addBuff(data,tmp);

                }
            }
            if (counterIn / 1048576 != pCin) {
                pCin = counterIn / 1048576;

            }
            synchronized (sendData) {
                if ((lenSendData != 0) && (100 < (System.currentTimeMillis() - timeoutSendData))) {
                    int count = 2048;
                    if (lenSendData < count) {
                        count = lenSendData;
                    }

                    if ((services & 2) == 2) {
                        protocol.offerOutput(sendData, 0, count);

                    } else {
                        counterOut += count;
                        sout.write(sendData, 0, count);
                        sout.flush();
                    }
                    System.arraycopy(sendData, count, sendData, 0, lenSendData - count);
                    lenSendData -= count;
                    timeoutSendData = System.currentTimeMillis();
                    timeout = System.currentTimeMillis();

                }
            }
            if (counterOut / 1048576 != pCout) {
                pCout = counterOut / 1048576;
                BMLog.LogD("Pechkin.BMClient", address.toString() + " send " + pCout + "*10k");

            }
            if ((300000 < (System.currentTimeMillis() - timeout)) && (!wait_pong)) {
                transmit(packet.createPacketPing());
                wait_pong = true;
                BMLog.LogD("Pechkin.BMClient", address.toString() + " send packet PING " + id);

            }
            if ((300000 < (System.currentTimeMillis() - timeoutRecvInv)) && (180000 < (System.currentTimeMillis() - timeout))) {
                BMLog.LogE("Pechkin.BMClient", address.toString() + " ID " + id + " disconnect not receiv INV");
                close();
            }
            if (480000 < (System.currentTimeMillis() - timeoutReceivObject)) {
                BMLog.LogE("Pechkin.BMClient", address.toString() + " ID " + id + " disconnect timeout receiv OBJECT");
                close();
            }
            int log = dataCount;
            dataCount = parse.getCommand(data, dataCount, retVal);
            if ((log <= dataCount) && (retVal.command != PACKET_NOTDATA)) {
                BMLog.LogE("Pechkin.BMClient", "error parse data  " + this.getAddress());
            }
            if (data == null) {
                System.err.println("ERROR DATA NULL");
            } else {
                data = Arrays.copyOf(data, dataCount);
            }

            switch (retVal.command) {
                case PACKET_VERACK:
                    BMLog.LogD("Pechkin.BMClient", "Receiv VERACK");
                    flagReceivVerack = true;
                    break;
                case PACKET_VERSION:
                    if (flagReceivVerack) {
                        BMLog.LogD("Pechkin.BMClient", "Receiv VERSION");
                        services = ((BMVersion) retVal.object).services;
                        sout.write(packet.createPacketVerack());
                        sout.flush();
                        if ((services & 0x02) == 0x02) {
                            protocol = new TlsClientProtocol(new SecureRandom());
                            BMtls client = new BMtls();
                            protocol.connect(client);
                        }
                    } else {
                        close();
                    }
                    break;
                case PACKET_ADDRESS:
                    timeoutProcessa = System.currentTimeMillis();
                    timeout = System.currentTimeMillis();
                    flagReceivAddress = true;
                    receiver.receiverAddress(id, (byte[]) retVal.object);
                    break;
                case PACKET_INV:
                    timeoutProcessa = System.currentTimeMillis();
                    timeout = System.currentTimeMillis();
                    flagReceivInv = true;
                    timeoutRecvInv = System.currentTimeMillis();
                    receiver.receiverInv(id, (byte[]) retVal.object);
                    break;
                case PACKET_GETDATA:
                    timeoutProcessa = System.currentTimeMillis();
                    timeout = System.currentTimeMillis();
                    receiver.receiverGetData(id, (byte[]) retVal.object);
                    break;
                case PACKET_OBJECT:
                    timeoutProcessa = System.currentTimeMillis();
                    timeout = System.currentTimeMillis();
                    timeoutReceivObject = System.currentTimeMillis();
                    byte[] object = (byte[]) retVal.object;
                    byte[] inv = utils.createInv(object);
                    receiver.receiverObject(id, inv, object);
                    break;
                case PACKET_PING:
                    timeout = System.currentTimeMillis();
                    transmit(packet.createPacketPong());
                    BMLog.LogD("Pechkin.BMClient", address.toString() + " receiv packet PING and send packet PONG " + id);
                    break;
                case PACKET_PONG:
                    timeout = System.currentTimeMillis();
                    BMLog.LogD("Pechkin.BMClient", address.toString() + " receiv packet PONG " + id);
                    wait_pong = false;
                    break;
                case PACKET_NOTDATA:
                    break;
                default:
                    timeout = System.currentTimeMillis();
                    BMLog.LogE("Pechkin.BMClient", address.toString() + " receiv not supported packet  " + id + " TYPE " + retVal.command);
                    break;
            }
            retVal.command = PACKET_NOTDATA;
            retVal.object = null;

        } catch (IOException e) {

        } catch(java.lang.NullPointerException e){
            BMLog.LogE("BMClient", id+" "+e.toString());
        }
    }

    public boolean isConnected() {
        return flagConnect;
    }

    public boolean isDisconnect() {
        return flagDisconnect;
    }

    public String getAddress() {
        return address.toString();
    }

    public int getID() {
        return id;
    }

    public int getReceivBytes() {
        return counterIn;
    }

    public int getSendBytes() {
        return counterOut;
    }

    public void close() {
        flagDisconnect = true;
        flagConnect = false;
        flagRunning = false;
        data = null;

        try {
            protocol.close();

        } catch (IOException e) {
        } catch (java.lang.NullPointerException e) {
        }
        try {
            sout.close();
        } catch (IOException e) {
        } catch (java.lang.NullPointerException e) {
        }
        try {
            sin.close();
        } catch (IOException e) {
        } catch (java.lang.NullPointerException e) {
        }
        try {
            sock.close();
        } catch (IOException e) {
        } catch (java.lang.NullPointerException e) {
        }
        receiver.receiverDisconnect(id);
        System.gc();
    }

    public void transmit(byte[] buff) {
        synchronized (sendData) {
            //BMLog
            if (sendData.length < (lenSendData + buff.length)) {
                sendData = Arrays.copyOf(sendData, lenSendData + buff.length);
            }
                System.arraycopy(buff, 0, sendData, lenSendData, buff.length);
                lenSendData += buff.length;
            //} else{
            //    System.err.println("ERROR SEND DATA");
            //}
        }
    }

}
