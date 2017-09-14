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

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.DLSequence;
import org.bouncycastle.crypto.digests.SHA512Digest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.bouncycastle.util.encoders.Hex;

/**
 *
 * @author Fenenko Aleksandr
 */
public class BMParse extends BMUtils implements BMConstant {

    private SHA512Digest digest;// = new SHA512Digest();
    private byte[] sha512;// = new byte[digest.getDigestSize()];

    //BMObject[] ret = new BMObject[2];
    //BMObject ret0 = new BMObject();
    // BMObject ret1 = new BMObject();
    BMUtils utils;
    BMPow pow;
    BMCrypto crypto;
    BMAddressUtils addressUtils;

    public BMParse() {
        digest = new SHA512Digest();
        sha512 = new byte[digest.getDigestSize()];
        utils = new BMUtils();
        pow = new BMPow();
        crypto = new BMCrypto();
        addressUtils = new BMAddressUtils();
    }

    @Deprecated private String getSubjectMessage(String message) {
        //byte[] buff = Base64.decode(message,Base64.DEFAULT);

        //Log.d("DecodeMessage","message "+message+" "+buff.toString());
        //    message = buff.toString();
        int start = message.indexOf("Subject:");//+"Subject:".length();
        int end_string = message.indexOf("\n");
        if (start < 0) {
            return null;
        }
        start += "Subject:".length();
        return message.substring(start, end_string);
    }

    @Deprecated private String getBodyMessage(String message) {
        int start = message.indexOf("Body:");//+"Subject:".length();

        if (start < 0) {
            return null;
        }
        start += "Body:".length();
        return message.substring(start);
    }

    public long getVersionServices(byte[] buff, int position) {
        return utils.bytesToLong(buff, position);
    }

    public byte[] decodeGetPubKey(byte[] data) {
        long[] dat = decodeVarInt(data, 20);
        if (dat[1] != 4) {
            return null;
        }
        dat = decodeVarInt(data, (int) dat[0]);
        byte[] ret = new byte[32];
        System.arraycopy(data, (int) dat[0], ret, 0, 32);
        return ret;
    }

    public long getExpireTime(byte[] buff) {

        return utils.bytesToLong(buff, 8);
    }

    public BMPubKey decodePubKeys(byte[] data, String address) {
        if ("BM-".compareTo(address.substring(0, 3).toUpperCase()) == 0) {
            address = address.substring(3);
        }
        try {
            if (bytesToInt(data, 16) != 1) //Проверка на то что обект pubkey
            {
                return null;
            }
        } catch (java.lang.NullPointerException ex) {
            return null;
        }
        byte[] tag_adr = addressUtils.getTagAddressBM(address);
        long[] dat = decodeVarInt(data, 20); // read version
        if (dat[1] == 3) {
            BMLog.LogD("BMParse", "find pubkey version 3");
        }
        if (dat[1] != 4) //проверка версии pubkey
        {
            return null;
        }
        dat = decodeVarInt(data, (int) dat[0]); //read stream
        if ((int) dat[1] != addressUtils.getStreamAddressBM(address)) //проверка соответствия stream
        {
            return null;
        }
        if (bytesToLong(data, 8) <= ((long) System.currentTimeMillis() / 1000L)) // проверка expireTime
        {
            return null;
        }
        if (!equalBuff(data, (int) dat[0], tag_adr, 0, 32)) // проверка tag аддреса
        {
            return null;
        }
        if (!pow.checkPow(1000, 1000, data)) // Проверка Proof Of Work
        {
            return null;
        }
        byte[] key_address = addressUtils.getKeyAddressBM(address);

        byte[] encrypt_data = getNewBuffer(data, (int) dat[0] + 32, data.length - ((int) dat[0] + 32));

        byte[] decrypt_data = crypto.decrypt(key_address, encrypt_data);
        if (decrypt_data.length == 0) {
            return null;
        }
        data = addBuff(getNewBuffer(data, 0, (int) dat[0] + 32), decrypt_data);

        byte[] pub_key_sign = getNewBuffer(data, ((int) dat[0]) + 36, 64);
        byte[] pub_key_ciph = getNewBuffer(data, ((int) dat[0]) + 100, 64);
        pub_key_ciph = addBuff(new byte[]{0x04}, pub_key_ciph);
        pub_key_sign = addBuff(new byte[]{0x04}, pub_key_sign);
        dat = decodeVarInt(data, (int) dat[0] + 164);
        int pow1 = (int) dat[1];
        dat = decodeVarInt(data, (int) dat[0]);
        int pow2 = (int) dat[1];
        byte[] sign_data = getNewBuffer(data, 8, (int) dat[0] - 8);
        dat = decodeVarInt(data, (int) dat[0]);
        byte[] signing = getNewBuffer(data, (int) dat[0], (int) dat[1]);
        //Log.d("decodePubkey","pow1 "+pow1+" pow2 "+pow2);

        try {
            boolean flag;
            ASN1InputStream reader = new ASN1InputStream(signing);
            ASN1Encodable[] ar = ((DLSequence) reader.readObject()).toArray();
            flag = crypto.checksigning(pub_key_sign, new BigInteger(ar[0].toString()), new BigInteger(ar[1].toString()), sign_data);
            reader.close();
            if (!flag) {
                return null;
            }

        } catch (IOException e) {

            return null;
        }

        String t = addressUtils.createAddress(1, pub_key_sign, pub_key_ciph);
        //Log.d("decodePubkey","find pubkey "+t+" "+address);
        if (t.compareTo(address) == 0) {
            return null;
        }
        //Log.d("decodePubkey","find pubkey "+t+" "+address);
        BMPubKey pubKey = new BMPubKey(address, pub_key_ciph, pow1, pow2);

        return pubKey;
    }

    public BMMessage decodeBroadcastMessage(byte[] data, String address) {
 long[] t;
        byte[] tag_adr = addressUtils.getTagAddressBM(address);
        if (bytesToInt(data, 16) != 3) {
            return null;
        }
        t = decodeVarInt(data, 20);
        if (t[1] < 5) {
            return null;
        }
        t = decodeVarInt(data, (int) t[0]);

        if ((int) t[1] != addressUtils.getStreamAddressBM(address)) {
            return null;
        }

        if (bytesToLong(data, 8) <= ((long) System.currentTimeMillis() / 1000L)) // проверка expireTime
        {
            return null;
        }
        if (!pow.checkPow(1000, 1000, data)) // Проверка Proof Of Work
        {
            return null;
        }
        if (!equalBuff(data, (int) t[0], tag_adr, 0, 32)) // проверка tag аддреса
        {
            return null;
        }
        byte[] key_address = addressUtils.getKeyAddressBM(address);

        byte[] tmp_data = getNewBuffer(data, (int) t[0] + 32, data.length - ((int) t[0] + 32));
        tmp_data = crypto.decrypt(key_address, tmp_data);

        if (tmp_data.length == 0) {
            return null;
        }

        data = addBuff(getNewBuffer(data, 0, (int) t[0] + 32), tmp_data);
        t[0] = t[0] + 32;

        t = decodeVarInt(data, (int) t[0]); // чтение версии address
        if ((int) t[1] < 4) {
            return null;
        }

        t = decodeVarInt(data, (int) t[0]); // чтение номера stream
        if ((int) t[1] != addressUtils.getStreamAddressBM(address)) {
            return null;
        }

        byte[] pub_key_sign = new byte[65];
        byte[] pub_key_ciph = new byte[65];
        getNewBuffer(data, (int) t[0] + 4, 64);
        System.arraycopy(data, (int) t[0] + 4, pub_key_sign, 1, 64);
        System.arraycopy(data, (int) t[0] + 68, pub_key_ciph, 1, 64);
        pub_key_ciph[0] = 0x04;
        pub_key_sign[0] = 0x04;

        t = decodeVarInt(data, (int) t[0] + 132);
        int pow1 = (int) t[1];
        t = decodeVarInt(data, (int) t[0]);
        int pow2 = (int) t[1];
        t = decodeVarInt(data, (int) t[0]);

        t = decodeVarInt(data, (int) t[0]);
        String textMessage = "";
        try {
            textMessage = new String(utils.getNewBuffer(data, (int) t[0], (int) t[1]), "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        t[0] = t[0] + t[1];
        byte[] sign_data = getNewBuffer(data, 8, (int) t[0] - 8);
        t = decodeVarInt(data, (int) t[0]);
        byte[] signing = getNewBuffer(data, (int) t[0], (int) t[1]);
        try {
            boolean flag;
            ASN1InputStream reader = new ASN1InputStream(signing);
            ASN1Encodable[] ar = ((DLSequence) reader.readObject()).toArray();
            flag = crypto.checksigning(pub_key_sign, new BigInteger(ar[0].toString()), new BigInteger(ar[1].toString()), sign_data);
            reader.close();
            if (!flag) {
                return null;
            }

        } catch (IOException e) {
            //println("ERROR CHECK SIGNATURE PUBKEY");
            return null;
        }
        String adr = addressUtils.getBMAddress(4, 1, addressUtils.getRipe(pub_key_sign, pub_key_ciph));
        BMPubKey key = new BMPubKey(adr, pub_key_ciph, pow1, pow2);
        BMMessage broadcast = new BMMessage(adr, ADDRESS_BROADCAST, textMessage, System.currentTimeMillis() / 1000L, key.key);
        return broadcast;
    }

    public BMMessage decodeMessage(byte[] data, byte[] priv_key, String address, int p1, int p2) {
        long[] t;
        if (bytesToInt(data, 16) != 2) {
            return null;
        }
        t = decodeVarInt(data, 20);
        if (t[1] != 1) {
            return null;
        }
        if (bytesToLong(data, 8) <= ((long) System.currentTimeMillis() / 1000L)) {
            return null;
        }
        t = decodeVarInt(data, (int) t[0]);

        if ((int) t[1] != addressUtils.getStreamAddressBM(address)) {
            return null;
        }
        if ((data.length - (int) t[0]) <= 32) {
            return null;
        }
        if (!pow.checkPow(p1, p2, data)) {
            return null;
        }
        byte[] encryption_data = getNewBuffer(data, (int) t[0], data.length - ((int) t[0]));
        //System.out.println("Encrypt data length "+encryption_data.length);
        byte[] decryption_data  = null;
        try{
            decryption_data = crypto.decrypt(priv_key, encryption_data);
        } catch(java.lang.ArrayIndexOutOfBoundsException ex){
           // BMLog.LogE("BMParse.decodeMessage", "decrypt message error "+ex.getMessage());
            return null;
        }

        if (decryption_data.length == 0) {
            return null;
        }
        data = addBuff(getNewBuffer(data, 0, (int) t[0]), decryption_data);

        t = decodeVarInt(data, (int) t[0]);
        int version = (int) t[1];

        if (t[1] != 4) {
            return null;
        }
        t = decodeVarInt(data, (int) t[0]);
        int stream = (int) t[1];
        //byte[] pub_key_sign = getNewBuffer(data,(int)t[0]+4,64);
        byte[] key_sign = new byte[65];
        byte[] key_ciph = new byte[65];
        System.arraycopy(data, (int) t[0] + 4, key_sign, 1, 64);
        System.arraycopy(data, (int) t[0] + 68, key_ciph, 1, 64);
        key_sign[0] = 4;
        key_ciph[0] = 4;
        t[0] = t[0] + 132;
        t = decodeVarInt(data, (int) t[0]);
        int pow1 = (int) t[1];
        t = decodeVarInt(data, (int) t[0]);
        int pow2 = (int) t[1];

        t[0] = t[0] + 20;
        t = decodeVarInt(data, (int) t[0]);
        t = decodeVarInt(data, (int) t[0]);
        byte[] buff = new byte[(int) t[1]];
        System.arraycopy(data, (int) t[0], buff, 0, (int) t[1]);
        String message = null;
        try {
            message = new String(buff, "utf-8");
        } catch (UnsupportedEncodingException e) {
            BMLog.LogE("decodeMessage", "error decoding message " + e.toString());
        }
        
        t[0] = t[0] + t[1];

        t = decodeVarInt(data, (int) t[0]);
        byte[] ack = new byte[0];
        if(t[1] != 0){
         ack = utils.getNewBuffer(data, (int) t[0], (int) t[1]);
        BMObject ret = new BMObject();
        (new BMParse()).getCommand(ack, ack.length, ret);

        if (ret.command == PACKET_OBJECT) {
            ack = (byte[]) ret.object;


        }
        }

        t[0] = t[0] + t[1];

        byte[] sign_data = getNewBuffer(data, 8, (int) t[0] - 8);
        t = decodeVarInt(data, (int) t[0]);
        //String addr = AddressUtils.g

        byte[] signing = getNewBuffer(data, (int) t[0], (int) t[1]);
        String addr = addressUtils.getBMAddress(version, stream, addressUtils.getRipe(key_sign, key_ciph));
        boolean flag_sign = false;
        try {
            ASN1InputStream reader = new ASN1InputStream(signing);
            ASN1Encodable[] ar = ((DLSequence) reader.readObject()).toArray();
            flag_sign = crypto.checksigning(key_sign, new BigInteger(ar[0].toString()), new BigInteger(ar[1].toString()), sign_data);
            reader.close();
            if (!flag_sign) {
                return null;
            }

        } catch (IOException e) {
            BMLog.LogE("Pechkin_BMParse", "error decode message");
            return null;

        }

        return new BMMessage(addr, address, message, System.currentTimeMillis() / 1000L, ack, key_ciph, pow1, pow2);

    }

    public byte[] decodeAckMessage(byte[] buffer, int pow1, int pow2) {
        long[] t;
        if (bytesToInt(buffer, 16) != 2) {
            return null;
        }
        t = decodeVarInt(buffer, 20);

        if (t[1] != 1) {
            return null;
        }

        t = decodeVarInt(buffer, (int) t[0]);

        if ((buffer.length - (int) t[0]) != 32) {
            return null;
        }

        if (bytesToLong(buffer, 8) <= ((long) System.currentTimeMillis() / 1000L)) {
            return null;
        }
        if (!pow.checkPow(pow1, pow2, buffer)) {
            return null;
        }

        return getNewBuffer(buffer, (int) t[0], 32);
    }

    public boolean isAck(byte[] message) {
        long[] t = decodeVarInt(message, 20);
        t = decodeVarInt(message, (int) t[0]);
        return (message.length - (int) t[0]) == 32;

    }

    public byte[] getAck(byte[] message) {
        long[] t = decodeVarInt(message, 20);
        t = decodeVarInt(message, (int) t[0]);
        if ((message.length - (int) t[0]) != 32) {
            return null;

        }
        return getNewBuffer(message, (int) t[0], 32);

    }

    public int getObjectType(byte[] data) {
        int type = utils.bytesToInt(data, 16);
        //System.err.println(type);
        int ret;
        switch (type) {
            case PACKET_OBJECT_GETPUBKEY:
                ret = PACKET_OBJECT_GETPUBKEY;
                break;
            case PACKET_OBJECT_PUBKEY:
                ret = PACKET_OBJECT_PUBKEY;

                break;
            case PACKET_OBJECT_MSG:
                if (data.length < 128) {
                    //Log.d("BMParse","ack");
                    ret = PACKET_OBJECT_MSG_ACK;
                    return ret;
                }
                ret = PACKET_OBJECT_MSG;
                break;
            case PACKET_OBJECT_BROADCAST:
                ret = PACKET_OBJECT_BROADCAST;
                break;
            default:
                ret = PACKET_OBJECT_UNKNOW;
                break;
        }
        return ret;

    }

    public boolean isCrc(byte[] data, int dataLen) {
        boolean ret = true;
        int len = utils.bytesToInt(data, 16);
        digest.reset();

        digest.update(getNewBuffer(data, 24, len), 0, len);
        digest.doFinal(sha512, 0);
        if (!utils.equalBuff(data, 20, sha512, 0, 4) && (len != 0)) {
            ret = false;
        }
        return ret;

    }

    public boolean isCommand(byte[] data, int dataLen) {
        boolean ret = true;
        if (data == null) {
            ret = false;

        }
        if ((dataLen < 24) || (data.length < 24)) {
            ret = false;
        }

        int len = utils.bytesToInt(data, 16);

        if (dataLen < len + 24) {
            ret = false;
        }
        return ret;

    }

    public int getCommand(byte[] data, int dataLen, BMObject retVal) {

        if (data == null) {
            retVal.command = PACKET_NOTDATA;
            return dataLen;

        }
        if ((dataLen < 24) || (data.length < 24)) {
            retVal.command = PACKET_NOTDATA;
            return dataLen;
        }
        while (((data[0] != MAGICK[0]) || (data[1] != MAGICK[1]) || (data[2] != MAGICK[2]) || (data[3] != MAGICK[3]))) {
            System.arraycopy(data, 1, data, 0, dataLen - 1);
            dataLen--;
            if (dataLen < 24) {
                retVal.command = PACKET_NOTDATA;
                return dataLen;
            }
        }
        int len = utils.bytesToInt(data, 16);

        if (dataLen < len + 24) {
            retVal.command = PACKET_NOTDATA;
            return dataLen;
        }
        digest.reset();
        digest.update(getNewBuffer(data, 24, len), 0, len);
        digest.doFinal(sha512, 0);
        if (!utils.equalBuff(data, 20, sha512, 0, 4) && (len != 0)) {
            System.arraycopy(data, len + 24, data, 0, dataLen - (len + 24));
            BMLog.LogD("BMParse","ERROR CRC PACKET "+Hex.toHexString(data,0,24)
                    +" CRC "+Hex.toHexString(data,20,4)+" "+Hex.toHexString(sha512,0,4));
            dataLen = dataLen - (len + 24);
            retVal.command = PACKET_NOTCRC;
            
            return dataLen;
        }
        String command = utils.getString(data, 4);

        if (command.indexOf("version") == 0) {
            retVal.command = PACKET_VERSION;
            String version = utils.decodeVarStr(data, 104);
            long service = getVersionServices(data, 28);
            retVal.object = new BMVersion(version, service);

        }
        if (command.indexOf("verack") == 0) {
            retVal.command = PACKET_VERACK;
            retVal.object = null;
        }
        if (command.indexOf("inv") == 0) {
            retVal.command = PACKET_INV;
            long[] t = utils.decodeVarInt(data, 24);
            retVal.object = getNewBuffer(data, (int) t[0], (int) t[1] * 32);
        }
        if (command.indexOf("addr") == 0) {
            retVal.command = PACKET_ADDRESS;
            long[] t = utils.decodeVarInt(data, 24);
            int position = (int) t[0];
            byte[] buff = new byte[(int) (t[1] * 38)];
            if (t[1] * 38 + t[0] <= data.length) {
                System.arraycopy(data, (int) t[0], buff, 0, buff.length);
                retVal.object = buff;
            } else {
                retVal.object = null;
            }

        }
        if (command.indexOf("getdata") == 0) {
            retVal.command = PACKET_GETDATA;
            long[] t = decodeVarInt(data, 24);
            retVal.object = getNewBuffer(data, (int) t[0], (int) t[1] * 32);

        }
        if (command.indexOf("object") == 0) {
            retVal.command = PACKET_OBJECT;
            retVal.object = getNewBuffer(data, 24, bytesToInt(data, 16));
        }
        if (command.indexOf("ping") == 0) {
            retVal.command = PACKET_PING;
        }
        if (command.indexOf("pong") == 0) {
            retVal.command = PACKET_PONG;
        }
        int i = bytesToInt(data, 16);
        System.arraycopy(data, i + 24, data, 0, dataLen - (i + 24));
        dataLen = (dataLen - (i + 24));
        return dataLen;

    }

}
