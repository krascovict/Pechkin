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

import com.google.protobuf.ByteString;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bouncycastle.util.Arrays;
import pro.fenenko.Pechkin.ProtobufPrivateData.Address;
import pro.fenenko.Pechkin.ProtobufPrivateData.Message;
import pro.fenenko.Pechkin.ProtobufPrivateData.PrivateData;
import pro.fenenko.Bitmessage.BMAddress;
import pro.fenenko.Bitmessage.BMAddressUtils;
import pro.fenenko.Bitmessage.BMConstant;
import pro.fenenko.Bitmessage.BMLog;
import pro.fenenko.Bitmessage.BMMessage;
import pro.fenenko.Bitmessage.BMUtils;
import pro.fenenko.Pechkin.ProtobufPrivateData.Ack;
import pro.fenenko.Pechkin.ProtobufPrivateData.NameAddress;
import pro.fenenko.Pechkin.ProtobufPrivateData.PubKey;
import pro.fenenko.Pechkin.ProtobufPrivateData.Subscrib;

/**
 *
 * @author Fenenko Aleksandr
 */
public class PechkinPrivateData implements BMConstant {

    private static final String FileName = PechkinConfigDir.getConfigDir() + "/PrivateData.raw";
    private static PrivateData data;

    private static BMUtils utils = new BMUtils();
    private static Random rand = new Random();
    private static int lock = 0;

    public static int getCountBMAddress() {
        int ret;
        synchronized (data) {
            ret = data.getAddressesCount();
        }
        return ret;
    }

    public static synchronized void init() {
        boolean flag = false;
        try {
            data = PrivateData.parseFrom(new FileInputStream(FileName));
        } catch (FileNotFoundException ex) {
            flag = true;

        } catch (IOException ex) {
            flag = true;

        }
        //BMLog.LogD("PechkinPrivateData", "PubKey "+data.getKeysCount());
        if (flag) {
            PrivateData.Builder build = PrivateData.newBuilder();
            NameAddress.Builder a = NameAddress.newBuilder();
            a.setAddress("BM-2cT9H4ow7R35qLhcsiTNxFjTpnx44XTbiY");
            a.setName("Pechkin new release");
            build.addAddressbook(a);
            a.clear();
            a.setAddress(BMConstant.ADDRESS_BROADCAST);
            a.setName(java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("SUBSCRIBTION"));
            build.addAddressbook(a);
            Subscrib.Builder s = Subscrib.newBuilder();
            s.setAddress("BM-2cT9H4ow7R35qLhcsiTNxFjTpnx44XTbiY");
            build.addAddressSubscrib(s);
           
            data = build.build();
            write();
            
        }

    }

    public static int getCountPubKey() {
        int ret = 0;
        synchronized (data) {
            ret = data.getKeysCount();
        }
        return ret;
    }

    public static String getPubkeyAddress(int position) {
        String ret = "";
        ret = data.getKeys(position).getAddress();

        return ret;
    }

    public static byte[] getPubkeyKey(int position) {
        byte[] ret;
        ret = data.getKeys(position).getKey().toByteArray();

        return ret;
    }

    public static int getPubkeyPow1(int position) {
        return data.getKeys(position).getPow1();
    }

    public static int getPubkeyPow2(int position) {
        return data.getKeys(position).getPow2();
    }

    public static synchronized void addPubKey(String address, byte[] key, int pow1, int pow2) {
        PubKey.Builder build = PubKey.newBuilder();
        build.setID(rand.nextInt());
        build.setAddress(address);
        build.setKey(ByteString.copyFrom(key));
        build.setPow1(pow1);
        build.setPow2(pow2);
        synchronized (data) {
            boolean flag = true;
            for (int i = 0; (flag) && (i < data.getKeysCount()); i++) {
                flag = !data.getKeys(i).getAddress().contains(address);
            }
            BMLog.LogD("PechkinPrivateData", "addPubKey " + flag);
            if (flag) {
                data = data.toBuilder().addKeys(build).build();
                write();
            }
        }

    }

    public static synchronized String getAddress(String name) {
        String ret = "";
        synchronized (data) {
            for (int i = 0; (i < data.getAddressbookCount()) && (ret.length() == 0); i++) {
                //System.out.println(data.getAddressbook(i).getName()+" |"+name+"| "+data.getAddressbook(i).getAddress());
                if (data.getAddressbook(i).getName().contains(name)) {
                    //System.out.println("TRUE");
                    ret = data.getAddressbook(i).getAddress();
                }
            }
        }
        if (ret.length() == 0) {
            ret = name;
        }
        return ret;
    }

    public static synchronized String getName(String address) {
        String ret = "";
        synchronized (data) {
            for (int i = 0; (i < data.getAddressbookCount()) && (ret.length() == 0); i++) {
                if (data.getAddressbook(i).getAddress().contains(address)) {
                    ret = data.getAddressbook(i).getName();
                }
            }
        }
        if (ret.length() == 0) {
            ret = address;
        }
        return ret;
    }

    public static synchronized boolean isName(String name) {
        boolean ret = false;
        synchronized (data) {
            for (int i = 0; (i < data.getAddressbookCount()) && (!ret); i++) {
                if (data.getAddressbook(i).getName().contains(name)
                        && (name.length() == data.getAddressbook(i).getName().length())) {
                    ret = true;
                }
            }
        }
        return ret;
    }

    public static synchronized int getCountAddressbook() {
        return data.getAddressbookCount();
    }

    public static synchronized String getName(int position) {
        return data.getAddressbook(position).getName();
    }

    public static synchronized String getAddress(int position) {
        return data.getAddressbook(position).getAddress();
    }

    public static synchronized boolean isAddress(String address) {
        boolean ret = false;
        synchronized (data) {
            for (int i = 0; (i < data.getAddressbookCount()) && (!ret); i++) {
                ret = data.getAddressbook(i).getAddress().contains(address);
            }
        }
        return ret;
    }

    public static synchronized void addOrChangeAddressbok(String address, String name) {
        boolean flag = true;
        synchronized (data) {
            NameAddress.Builder builder = NameAddress.newBuilder();
            BMLog.LogD("PechkinPrivateData", "addOrChangeAddressbok( " + address + " , " + name + " )");
            builder.setAddress(address);
            builder.setName(name);

            for (int i = 0; (i < data.getAddressbookCount()) && (flag); i++) {
                if (data.getAddressbook(i).getAddress().contains(address)) {
                    data = data.toBuilder().setAddressbook(i, builder).build();
                    flag = false;
                }
            }
            if (flag) {
                data = data.toBuilder().addAddressbook(builder).build();
            }
            write();
        }
    }

    public static boolean updateMessage(PechkinMessage message) {
        boolean flag = false;
        synchronized (data) {
            for (int i = 0; (!flag) && (i < data.getMessagesCount()); i++) {
                if (message.getID() == data.getMessages(i).getID()) {
                    Message msg = data.getMessages(i);
                    msg = msg.toBuilder().setStatus(message.getStatus())
                            .setTime(message.getTime()).build();
                    data = data.toBuilder().setMessages(i, msg).build();
                }
            }
            write();
        }
        return flag;
    }

    public static synchronized PechkinMessage getMessage(long id) {
        PechkinMessage ret = null;
        synchronized (data) {
            for (int i = 0; (ret == null) && (i < data.getMessagesCount()); i++) {
                if (id == data.getMessages(i).getID()) {
                    ret = new PechkinMessage(id,
                            data.getMessages(i).getAddressFrom(),
                            data.getMessages(i).getAddressTo(),
                            data.getMessages(i).getText(),
                            data.getMessages(i).getTime(),
                            data.getMessages(i).getStatus());
                }
            }
        }
        return ret;
    }

    public static synchronized void deleteMessages(long[] ids) {
        synchronized (data) {
            for (int i = 0; i < ids.length; i++) {
                int j = 0;
                for (; j < data.getMessagesCount();) {
                    if (data.getMessages(j).getID() == ids[i]) {
                        data = data.toBuilder().removeMessages(j).build();
                    } else {
                        j++;
                    }
                }
            }
            write();
        }
    }

    public static synchronized boolean deleteAddressInAddressbook(String address) {
        boolean flag = false;
        synchronized (data) {
            for (int i = 0; (!flag) && (i < data.getAddressbookCount()); i++) {
                if (data.getAddressbook(i).getAddress().contains(address)) {
                    flag = true;
                    data = data.toBuilder().removeAddressbook(i).build();
                    write();
                }
            }
        }
        return flag;
    }

    public static synchronized long[] getMessageIDs() {
        long[] ret = null;
        synchronized (data) {
            ret = new long[data.getMessagesCount()];
            for (int i = 0; i < ret.length; i++) {
                ret[i] = data.getMessages(i).getID();
            }
        }
        return ret;
    }

    public static int getWaitAckCount() {

        return data.getWaitAckCount();
    }

    public static byte[] getWaitACK(int position) {
        return data.getWaitAck(position).getAck().toByteArray();
    }

    public static long getWaitAckMessageID(int position) {

        return data.getWaitAck(position).getMessageID();
    }

    public static long getWaitAckDelete(int position) {
        byte[] testAck = new byte[32];
        long id = -1;
        boolean flag = false;
        synchronized (data) {

            id = data.getWaitAck(position).getMessageID();
            data = data.toBuilder().removeWaitAck(position).build();

            write();
        }
        return id;
    }

    public static synchronized String[] getSubscribed() {
        String[] ret = new String[0];
        synchronized (data) {
            //ret = new String[data.get]
            ret = new String[data.getAddressSubscribCount()];
            for (int i = 0; i < ret.length; ret[i] = data.getAddressSubscrib(i).getAddress(), i++);
        }
        return ret;
    }

    public static synchronized void addSubscribe(String address) {
        synchronized (data) {
            boolean flagNotPressed = true;
            for (int i = 0; (flagNotPressed) && (i < data.getAddressSubscribCount()); i++) {
                if (data.getAddressSubscrib(i).getAddress().contains(address)) {
                    flagNotPressed = false;
                }
            }
            if (flagNotPressed) {
                if ((new BMAddressUtils()).checkAddressBM(address)) {
                    Subscrib.Builder build = Subscrib.newBuilder();
                    build.setAddress(address);
                    data = data.toBuilder().addAddressSubscrib(build).build();
                    write();
                }
            }
        }
    }

    public static synchronized boolean deleteSubscribe(String address) {
        boolean flag = false;
        synchronized (data) {
            for (int i = 0; (!flag) && (i < data.getAddressSubscribCount()); i++) {
                if (data.getAddressSubscrib(i).getAddress().contains(address)) {
                    flag = true;
                    data = data.toBuilder().removeAddressSubscrib(i).build();
                    write();
                    //data = data.toBuilder()
                }
            }
        }
        return flag;
    }

    private static long genIDMessage() {
        boolean flag = false;
        long ret;
        do {
            ret = rand.nextLong();
            synchronized (data) {
                for (int i = 0; (!flag) && (i < data.getMessagesCount()); i++) {
                    flag = data.getMessages(i).getID() == ret;
                }
            }
        } while (flag);
        return ret;
    }

    public static void addAck(byte[] ack, long idMessage) {
        Ack.Builder builder = Ack.newBuilder();
        builder.setID(rand.nextInt());
        builder.setAck(ByteString.copyFrom(ack));
        builder.setMessageID(idMessage);
        synchronized (data) {
            data = data.toBuilder().addWaitAck(builder).build();
        }
    }

    public static synchronized long addMessage(BMMessage message, int status) {
        Message.Builder builder = Message.newBuilder();
        long id = genIDMessage();
        builder.setID(genIDMessage());
        builder.setAddressFrom(message.address_from);
        builder.setAddressTo(message.address_to);
        builder.setTime(message.time);
        builder.setText(message.message);
        builder.setStatus(status);
        synchronized (data) {
            data = data.toBuilder().addMessages(builder).build();
            write();
        }
        return id;

    }

    private static void write() {
        try {
            FileOutputStream file = new FileOutputStream(FileName);

            data.writeTo(file);
            file.close();

        } catch (FileNotFoundException ex) {
            Logger.getLogger(PechkinPrivateData.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PechkinPrivateData.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static void addBMAddress(BMAddress addr) {

        Address.Builder builder = Address.newBuilder();
        synchronized (data) {
            builder.setID(data.getAddressesCount());
            builder.setAddress(addr.getAddress());
            builder.setSignKey(utils.encodeBase58(addr.getPrivateSigningKey()));
            builder.setCiphKey(utils.encodeBase58(addr.getPrivateCipherKey()));

            data = data.toBuilder().addAddresses(builder.build()).build();
            write();
        }

    }

    public static synchronized BMAddress getBMAddress(String address) {
        BMAddress ret = null;
        synchronized (data) {
            for (int i = 0; (ret == null) && (i < data.getAddressesCount()); i++) {
                Address addr = data.getAddresses(i);
                System.out.println(i + " " + addr.getAddress() + " " + address);
                if (addr.getAddress().contains(address)) {
                    byte[] sign = utils.decodeBase58(addr.getSignKey());
                    byte[] ciph = utils.decodeBase58(addr.getCiphKey());
                    ret = new BMAddress(sign, ciph);
                }
            }
        }
        return ret;
    }

    public static synchronized BMAddress getBMAddress(int position) {
        BMAddress ret;
        synchronized (data) {
            Address addr = data.getAddresses(position);

            byte[] sign = utils.decodeBase58(addr.getSignKey());
            byte[] ciph = utils.decodeBase58(addr.getCiphKey());
            ret = new BMAddress(sign, ciph);
        }
        return ret;
    }

    public static synchronized boolean deleteBMAddress(String address) {
        boolean ret = false;
        synchronized (data) {
            for (int i = 0; (!ret) && (i < data.getAddressesCount()); i++) {
                if (data.getAddresses(i).getAddress().contains(address)) {
                    ret = true;
                    data = data.toBuilder().removeAddresses(i).build();
                }
            }
            write();
        }
        return ret;
    }
}
