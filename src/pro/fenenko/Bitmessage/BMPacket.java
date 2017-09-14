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

import java.io.UnsupportedEncodingException;
import org.bouncycastle.crypto.digests.SHA512Digest;

import java.math.BigInteger;
import java.net.Inet6Address;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Fenenko Aleksandr
 */
public class BMPacket implements BMConstant {

    private static BMUtils ut = new BMUtils();
    private BMAddressUtils addressUtils;

    public BMPacket() {
        //ut = new BMUtils();
        addressUtils = new BMAddressUtils();
    }

    public byte[] createVersion(String local, int local_port, String to, int to_port, long time, long nonce, int[] stream) {

        byte[] payload = ut.intToBytes(PROTOCOL_VERSION);
        byte[] ret = new byte[0];
        if (stream.length == 0) {
            stream = new int[1];
            stream[0] = 1;
        }
        payload = ut.addBuff(payload, ut.longToBytes(SERVICES));
        payload = ut.addBuff(payload, ut.longToBytes(time));
        payload = ut.addBuff(payload, ut.longToBytes(SERVICES));
        payload = ut.addBuff(payload, ut.convertIP(local));
        payload = ut.addBuff(payload, ut.shortToBytes(local_port));
        payload = ut.addBuff(payload, ut.longToBytes(SERVICES));
        payload = ut.addBuff(payload, ut.convertIP(to));
        payload = ut.addBuff(payload, ut.shortToBytes(to_port));

        payload = ut.addBuff(payload, ut.longToBytes(nonce));
        payload = ut.addBuff(payload, ut.createVarStr(USER_AGENT));
        //println(USER_AGENT.length());
        payload = ut.addBuff(payload, ut.createVarListInt(stream));
        ret = ut.addBuff(ret, MAGICK);
        String command = "version\0";
        ret = ut.addBuff(ret, command.getBytes());
        ret = ut.addBuff(ret, new byte[12 - command.length()]);
        ret = ut.addBuff(ret, ut.intToBytes(payload.length));
        SHA512Digest digest = new SHA512Digest();
        byte[] sha512 = new byte[digest.getDigestSize()];
        digest.update(payload, 0, payload.length);
        digest.doFinal(sha512, 0);
        ret = ut.addBuff(ret, ut.getNewBuffer(sha512, 0, 4));
        ret = ut.addBuff(ret, payload);

        return ret;
    }

    public byte[] createVerack() {
        byte[] ret = new byte[0];
        ret = ut.addBuff(ret, MAGICK);
        String command = "verack\0";
        ret = ut.addBuff(ret, command.getBytes());
        ret = ut.addBuff(ret, new byte[12 - command.length()]);
        ret = ut.addBuff(ret, ut.intToBytes(0));
        SHA512Digest digest = new SHA512Digest();
        byte[] sha512 = new byte[digest.getDigestSize()];
        digest.update(new byte[0], 0, 0);
        digest.doFinal(sha512, 0);
        ret = ut.addBuff(ret, ut.getNewBuffer(sha512, 0, 4));
        return ret;
    }

    public byte[] createPacketPong() {
        byte[] ret = new byte[0];
        ret = ut.addBuff(ret, MAGICK);
        String command = "pong\0";
        ret = ut.addBuff(ret, command.getBytes());
        ret = ut.addBuff(ret, new byte[12 - command.length()]);
        ret = ut.addBuff(ret, ut.intToBytes(0));
        SHA512Digest digest = new SHA512Digest();
        byte[] sha512 = new byte[digest.getDigestSize()];
        digest.update(new byte[0], 0, 0);
        digest.doFinal(sha512, 0);
        ret = ut.addBuff(ret, ut.getNewBuffer(sha512, 0, 4));
        return ret;
    }

    public byte[] createNetAddress(long time, String ip, int port) {
        byte[] ret = ut.longToBytes(time);
        ret = ut.addBuff(ret, ut.intToBytes(1));
        ret = ut.addBuff(ret, ut.longToBytes(3));
        byte[] ipb = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1, 0, 0, 0, 0};
        byte[] tmp = null;
        try {
            //tmp = InetAddress.getByName(ip).getAddress();
            tmp = Inet6Address.getByName(ip).getAddress();
            if (tmp.length == 4) {
                System.arraycopy(tmp, 0, ipb, 12, 4);
            } else {
                ipb = tmp;
            }

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        ret = ut.addBuff(ret, ipb);
        ret = ut.addBuff(ret, ut.shortToBytes(port));

        return ret;
    }

    public String getMessage(String title, String body) {
        return "Subject:" + title + "\nBody:" + body;
    }

    public byte[] createPacketPing() {
        byte[] ret = new byte[0];
        ret = ut.addBuff(ret, MAGICK);
        String command = "ping\0";
        ret = ut.addBuff(ret, command.getBytes());
        ret = ut.addBuff(ret, new byte[12 - command.length()]);
        ret = ut.addBuff(ret, ut.intToBytes(0));
        SHA512Digest digest = new SHA512Digest();
        byte[] sha512 = new byte[digest.getDigestSize()];
        digest.update(new byte[0], 0, 0);
        digest.doFinal(sha512, 0);
        ret = ut.addBuff(ret, ut.getNewBuffer(sha512, 0, 4));
        return ret;
    }
    
    public byte[] createObject(byte[] buff,int len) {
        byte[] ret = MAGICK;
        String command = "object\0";
        ret = ut.addBuff(ret, command.getBytes());
        ret = ut.addBuff(ret, new byte[12 - command.length()]);
        ret = ut.addBuff(ret, ut.intToBytes(len));
        SHA512Digest digest = new SHA512Digest();
        byte[] sha512 = new byte[digest.getDigestSize()];
        digest.update(buff, 0, len);
        digest.doFinal(sha512, 0);
        ret = ut.addBuff(ret, ut.getNewBuffer(sha512, 0, 4));
        ret = ut.addBuff(ret, ut.getNewBuffer(buff, 0, len));
        return ret;
    }

    public byte[] createObject(byte[] buff) {
        byte[] ret = MAGICK;
        String command = "object\0";
        ret = ut.addBuff(ret, command.getBytes());
        ret = ut.addBuff(ret, new byte[12 - command.length()]);
        ret = ut.addBuff(ret, ut.intToBytes(buff.length));
        SHA512Digest digest = new SHA512Digest();
        byte[] sha512 = new byte[digest.getDigestSize()];
        digest.update(buff, 0, buff.length);
        digest.doFinal(sha512, 0);
        ret = ut.addBuff(ret, ut.getNewBuffer(sha512, 0, 4));
        ret = ut.addBuff(ret, buff);
        return ret;
    }

    public byte[] createPacketInv(byte[] buff) {
        byte[] ret = new byte[0];
        /*
        while (5000 < buff.length/32) {
            ret = ut.addBuff(ret, createInv(ut.getNewBuffer(buff, 0, 5000*32)));
            buff = ut.getNewBuffer(buff, 5000*32, buff.length-5000*32);
        }
         */
        ret = ut.addBuff(ret, MAGICK);
        byte[] payload = ut.addBuff(ut.createVarInt(buff.length / 32), buff);
        String command = "inv\0";
        ret = ut.addBuff(ret, command.getBytes());
        ret = ut.addBuff(ret, new byte[12 - command.length()]);

        SHA512Digest digest = new SHA512Digest();
        byte[] sha512 = new byte[digest.getDigestSize()];

        digest.update(payload, 0, payload.length);
        digest.doFinal(sha512, 0);
        ret = ut.addBuff(ret, ut.intToBytes(payload.length));
        ret = ut.addBuff(ret, ut.getNewBuffer(sha512, 0, 4));
        ret = ut.addBuff(ret, payload);
        return ret;
    }

    public byte[] createPacketAddress(BMNodeAddress[] adr) {
        byte[] ret = new byte[0];

        ret = ut.addBuff(ret, MAGICK);
        byte[] payload = ut.createVarInt(adr.length);
        for (int i = 0; i < adr.length; i++) {
            payload = ut.addBuff(payload, convert(adr[i]));
        }
        String command = "addr\0";
        ret = ut.addBuff(ret, command.getBytes());
        ret = ut.addBuff(ret, new byte[12 - command.length()]);
        SHA512Digest digest = new SHA512Digest();
        byte[] sha512 = new byte[digest.getDigestSize()];
        digest.update(payload, 0, payload.length);
        digest.doFinal(sha512, 0);

        ret = ut.addBuff(ret, ut.intToBytes(payload.length));
        ret = ut.addBuff(ret, ut.getNewBuffer(sha512, 0, 4));
        ret = ut.addBuff(ret, payload);
        return ret;

    }

    public byte[] createPacketVersion(long services, String local, int local_port, String to, int to_port, long time, long nonce, int[] stream) {
        byte[] payload = ut.intToBytes(PROTOCOL_VERSION);
        byte[] ret = new byte[0];
        if (stream.length == 0) {
            stream = new int[1];
            stream[0] = 1;
        }
        payload = ut.addBuff(payload, ut.longToBytes(services));
        payload = ut.addBuff(payload, ut.longToBytes(time));
        payload = ut.addBuff(payload, ut.longToBytes(services));
        payload = ut.addBuff(payload, ut.convertIP(local));
        payload = ut.addBuff(payload, ut.shortToBytes(local_port));
        payload = ut.addBuff(payload, ut.longToBytes(services));
        payload = ut.addBuff(payload, ut.convertIP(to));
        payload = ut.addBuff(payload, ut.shortToBytes(to_port));

        payload = ut.addBuff(payload, ut.longToBytes(nonce));
        payload = ut.addBuff(payload, ut.createVarStr(USER_AGENT));
        //println(USER_AGENT.length());
        payload = ut.addBuff(payload, ut.createVarListInt(stream));
        ret = ut.addBuff(ret, MAGICK);
        String command = "version\0";
        ret = ut.addBuff(ret, command.getBytes());
        ret = ut.addBuff(ret, new byte[12 - command.length()]);
        ret = ut.addBuff(ret, ut.intToBytes(payload.length));
        SHA512Digest digest = new SHA512Digest();
        byte[] sha512 = new byte[digest.getDigestSize()];
        digest.update(payload, 0, payload.length);
        digest.doFinal(sha512, 0);
        ret = ut.addBuff(ret, ut.getNewBuffer(sha512, 0, 4));
        ret = ut.addBuff(ret, payload);

        return ret;
    }

    public byte[] createPacketVerack() {
        byte[] ret = new byte[0];
        ret = ut.addBuff(ret, MAGICK);
        String command = "verack\0";
        ret = ut.addBuff(ret, command.getBytes());
        ret = ut.addBuff(ret, new byte[12 - command.length()]);
        ret = ut.addBuff(ret, ut.intToBytes(0));
        SHA512Digest digest = new SHA512Digest();
        byte[] sha512 = new byte[digest.getDigestSize()];
        digest.update(new byte[0], 0, 0);
        digest.doFinal(sha512, 0);
        ret = ut.addBuff(ret, ut.getNewBuffer(sha512, 0, 4));
        return ret;
    }

    public byte[] convert(BMNodeAddress address) {
        byte[] ret;
        ret = ut.longToBytes(address.getTime());
        ret = ut.addBuff(ret, ut.intToBytes(address.getStream()));
        ret = ut.addBuff(ret, ut.longToBytes(address.getService()));
        byte[] ipb = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1, 0, 0, 0, 0};
        byte[] tmp = null;
        try {
            //tmp = InetAddress.getByName(ip).getAddress();
            tmp = Inet6Address.getByName(address.getIp()).getAddress();
            if (tmp.length == 4) {
                System.arraycopy(tmp, 0, ipb, 12, 4);
            } else {
                ipb = tmp;
            }

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        ret = ut.addBuff(ret, ipb);
        ret = ut.addBuff(ret, ut.shortToBytes(address.getPort()));
        return ret;

    }

    public byte[] createBroadcastMessage(String may_address, byte[] may_priv_sign_key, byte[] may_priv_enc_key, String textMessage) {
        byte[] may_pub_sign = addressUtils.getPublicKey(may_priv_sign_key);
        byte[] may_pub_enc = addressUtils.getPublicKey(may_priv_enc_key);
        byte[] key_enc = addressUtils.getKeyAddressBM(may_address);
        long ttl = 7 * 24 * 60 * 60;
        long expire_time = ttl + System.currentTimeMillis() / 1000L;

        byte[] object_message = ut.longToBytes(expire_time);
        object_message = ut.addBuff(object_message, ut.intToBytes(3));
        object_message = ut.addBuff(object_message, ut.createVarInt(5));
        object_message = ut.addBuff(object_message, ut.createVarInt(addressUtils.getStreamAddressBM(may_address)));
        object_message = ut.addBuff(object_message, addressUtils.getTagAddressBM(may_address));
        byte[] message = ut.createVarInt(4);
        //message = addBuff(message,createVarInt(4));
        message = ut.addBuff(message, ut.createVarInt(addressUtils.getStreamAddressBM(may_address)));
        message = ut.addBuff(message, ut.intToBytes(67174400));
        message = ut.addBuff(message, ut.getNewBuffer(may_pub_sign, 1, may_pub_sign.length - 1));
        message = ut.addBuff(message, ut.getNewBuffer(may_pub_enc, 1, may_pub_enc.length - 1));
        message = ut.addBuff(message, ut.createVarInt(1000));
        message = ut.addBuff(message, ut.createVarInt(1000));

        message = ut.addBuff(message, ut.createVarInt(2));

        try {
            message = ut.addBuff(message, ut.createVarInt(textMessage.getBytes("UTF-8").length));
            message = ut.addBuff(message, textMessage.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(BMPacket.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        BMCrypto crypto = new BMCrypto();
        byte[] signature = crypto.sign(may_priv_sign_key, ut.addBuff(object_message, message));

        message = ut.addBuff(message, ut.createVarInt(signature.length));
        message = ut.addBuff(message, signature);
        byte[] encrypt_data = crypto.encrypt(addressUtils.getPublicKey(key_enc), message);
        message = ut.addBuff(object_message, encrypt_data);

        long start_time = (long) (System.currentTimeMillis() / 1000L);
        /*
        BigInteger target = BMPow.calculatePowTarget(ttl, pow1, pow2, message.length);
        //long target = (two.pow(64).longValue() / (1000*(object_data.length + 8 + 1000 + ((ttl*(object_data.length+8+1000))/two.pow(16).longValue()))));

        message = ut.addBuff(PechkinNativeLibrary.calculatePow(target.longValue(),message), message); // Здесь должна начинатся prof of work
         */
        BigInteger target = BMPow.calculatePowTarget(ttl, 1000, 1000, message.length);
        //long target = (two.pow(64).longValue() / (1000*(object_data.length + 8 + 1000 + ((ttl*(object_data.length+8+1000))/two.pow(16).longValue()))));

        //message = ut.addBuff(PechkinNativeLibrary.calculatePow(target.longValue(),message), message); // Здесь должна начинатся prof of work
        message = ut.addBuff((new BMPow()).calculatePow(target, message), message); // Здесь должна начинатся prof of work

        return message;
    }

    public synchronized byte[] createPacketGetdata(byte[] buff) {
        byte[] ret = new byte[0];
        while (5000 < buff.length / 32) {
            ret = ut.addBuff(ret, (new BMPacket()).createPacketGetdata(ut.getNewBuffer(buff, 0, 5000 * 32)));
            buff = ut.getNewBuffer(buff, 5000 * 32, buff.length - 5000 * 32);
        }
        ret = ut.addBuff(ret, MAGICK);
        byte[] payload = ut.addBuff(ut.createVarInt(buff.length / 32), buff);
        String command = "getdata\0";
        ret = ut.addBuff(ret, command.getBytes());
        ret = ut.addBuff(ret, new byte[12 - command.length()]);
        SHA512Digest digest = new SHA512Digest();
        byte[] sha512 = new byte[digest.getDigestSize()];
        digest.update(payload, 0, payload.length);
        digest.doFinal(sha512, 0);

        ret = ut.addBuff(ret, ut.intToBytes(payload.length));
        ret = ut.addBuff(ret, ut.getNewBuffer(sha512, 0, 4));
        ret = ut.addBuff(ret, payload);
        return ret;
    }

    public byte[] createPacketObject(byte[] buff) {
        byte[] ret = MAGICK;
        String command = "object\0";
        ret = ut.addBuff(ret, command.getBytes());
        ret = ut.addBuff(ret, new byte[12 - command.length()]);
        ret = ut.addBuff(ret, ut.intToBytes(buff.length));
        SHA512Digest digest = new SHA512Digest();
        byte[] sha512 = new byte[digest.getDigestSize()];
        digest.update(buff, 0, buff.length);
        digest.doFinal(sha512, 0);
        ret = ut.addBuff(ret, ut.getNewBuffer(sha512, 0, 4));
        ret = ut.addBuff(ret, buff);
        return ret;
    }
    
    public byte[] createPacketObject(byte[] buff,int len) {
        byte[] ret = MAGICK;
        String command = "object\0";
        ret = ut.addBuff(ret, command.getBytes());
        ret = ut.addBuff(ret, new byte[12 - command.length()]);
        ret = ut.addBuff(ret, ut.intToBytes(len));
        SHA512Digest digest = new SHA512Digest();
        byte[] sha512 = new byte[digest.getDigestSize()];
        digest.update(buff, 0, len);
        digest.doFinal(sha512, 0);
        ret = ut.addBuff(ret, ut.getNewBuffer(sha512, 0, 4));
        ret = ut.addBuff(ret, ut.getNewBuffer(buff, 0, len));
        return ret;
    }

    public byte[] createAckMessage(byte[] ack_key, long ttl, int stream, int pow1, int pow2) {
        byte[] ret;// = new byte[0];
        ret = ut.longToBytes(ttl + System.currentTimeMillis() / 1000L);
        ret = ut.addBuff(ret, ut.intToBytes(2));
        ret = ut.addBuff(ret, ut.createVarInt(1));
        ret = ut.addBuff(ret, ut.createVarInt(stream));
        ret = ut.addBuff(ret, ack_key);
        BigInteger target = BMPow.calculatePowTarget(ttl, pow1, pow2, ret.length);
        //ret = ut.addBuff(PechkinNativeLibrary.calculatePow(target.longValue(),ret),ret);
        ret = ut.addBuff((new BMPow()).calculatePow(target, ret), ret);
        return ret;

    }

    public byte[] createMessage(byte[] ack_key, long ttl, String may_address,
            String dest_address, byte[] may_priv_sign_key,
            byte[] may_priv_encr_key, byte[] pub_encr_key,
            String msg, int pow1, int pow2) {
        if (pub_encr_key.length == 64) {
            byte[] tmp = new byte[65];
            System.arraycopy(pub_encr_key, 0, tmp, 1, 64);;
            tmp[0] = 4;
            pub_encr_key = tmp;
        }
        byte[] may_pub_sign = addressUtils.getPublicKey(may_priv_sign_key);

        byte[] may_pub_enc = addressUtils.getPublicKey(may_priv_encr_key);

        //long ttl = 28 * 24 * 60 * 60 + (long)(random(-1.0, 1.0)*300);
        long expire_time = ttl + System.currentTimeMillis() / 1000L;

        byte[] ack_message = createAckMessage(ack_key, ttl,
                addressUtils.getStreamAddressBM(may_address), pow1, pow2);
        ack_message = createPacketObject(ack_message);

        byte[] object_message = ut.longToBytes(ttl + System.currentTimeMillis() / 1000L);
        object_message = ut.addBuff(object_message, ut.intToBytes(2));
        object_message = ut.addBuff(object_message, ut.createVarInt(1));
        object_message = ut.addBuff(object_message, ut.createVarInt(
                addressUtils.getStreamAddressBM(may_address)));
        byte[] message = ut.createVarInt(4);
        message = ut.addBuff(message, ut.createVarInt(addressUtils.getStreamAddressBM(may_address)));
        message = ut.addBuff(message, ut.intToBytes(1));
        message = ut.addBuff(message, ut.getNewBuffer(may_pub_sign, 1, 64));
        message = ut.addBuff(message, ut.getNewBuffer(may_pub_enc, 1, 64));
        message = ut.addBuff(message, ut.createVarInt(1000));
        message = ut.addBuff(message, ut.createVarInt(1000));

        message = ut.addBuff(message, addressUtils.getRipeAddressBM(dest_address));
        message = ut.addBuff(message, ut.createVarInt(2));
        String s = msg;
        try {
            message = ut.addBuff(message, ut.createVarInt(s.getBytes("UTF-8").length));
            message = ut.addBuff(message, s.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(BMPacket.class.getName()).log(Level.SEVERE, null, ex);
        }
        message = ut.addBuff(message, ut.createVarInt(ack_message.length));
        message = ut.addBuff(message, ack_message);
        BMCrypto crypto = new BMCrypto();
        byte[] signature = crypto.sign(may_priv_sign_key, ut.addBuff(object_message, message));

        message = ut.addBuff(message, ut.createVarInt(signature.length));
        message = ut.addBuff(message, signature);
        byte[] encrypt_data = crypto.encrypt(pub_encr_key, message);
        message = ut.addBuff(object_message, encrypt_data);
        //println("START POW MESSAGE");
        //start_time = (long)(System.currentTimeMillis() / 1000L);
        BigInteger target = BMPow.calculatePowTarget(ttl, pow1, pow2, message.length);
        //long target = (two.pow(64).longValue() / (1000*(object_data.length + 8 + 1000 + ((ttl*(object_data.length+8+1000))/two.pow(16).longValue()))));

        //message = ut.addBuff(PechkinNativeLibrary.calculatePow(target.longValue(),message), message); // Здесь должна начинатся prof of work
        message = ut.addBuff((new BMPow()).calculatePow(target, message), message); // Здесь должна начинатся prof of work

        //long start_time = (long)(System.currentTimeMillis() / 1000L);
        //long target = (two.pow(64).longValue() / (1000*(object_data.length + 8 + 1000 + ((ttl*(object_data.length+8+1000))/two.pow(16).longValue()))));
/*
        ack_message = ut.addBuff(PechkinNativeLibrary.calculatePow(target.longValue(),ack_message), ack_message); // Здесь должна начинатся prof of work
        println("TIME POW ACK MESSAGE ",(long)(System.currentTimeMillis() / 1000L)-start_time);





        //message = addBuff(message,intToBytes(67174400));


         */
        return message;
    }

    public byte[] createGetPubkey(long ttl, String address) {
        long expireTime = (long) ttl + (System.currentTimeMillis() / 1000L);
        BigInteger target;
        byte[] decryptData = ut.longToBytes(expireTime);
        decryptData = ut.addBuff(decryptData, ut.intToBytes(0));
        decryptData = ut.addBuff(decryptData, ut.createVarInt(4));
        decryptData = ut.addBuff(decryptData, ut.createVarInt(1));
        decryptData = ut.addBuff(decryptData, (new BMAddressUtils()).getTagAddressBM(address));
        target = BMPow.calculatePowTarget(1 * 24 * 60 * 60, 1000, 1000, decryptData.length);
        //byte[] pow = PechkinNativeLibrary.calculatePow(target,decryptData);
        byte[] pow = (new BMPow()).calculatePow(target, decryptData);
        //System.out.println("LEN POW = "+pow.length+" decryptDATA LEN = "+decryptData.length);
        decryptData = ut.addBuff(pow, decryptData);

        return decryptData;
    }

    public byte[] createPubKey(byte[] priv_signing_key, byte[] priv_cipher_key, String address) {
        byte[] object_data, pow;
        do {
            byte[] pub_signing_key = addressUtils.getPublicKey(priv_signing_key);
            byte[] pub_encrypt_key = addressUtils.getPublicKey(priv_cipher_key);
            //pub_signing_key = getNewBuffer(pub_signing_key,1,pub_signing_key.length-1);
            //pub_encrypt_key = getNewBuffer(pub_encrypt_key,1,pub_encrypt_key.length-1);

            String addr = addressUtils.createAddress(1, pub_signing_key, pub_encrypt_key);

            byte[] decrypt_data = ut.intToBytes(1); //Здесь нужно проверить
            decrypt_data = ut.addBuff(decrypt_data, ut.getNewBuffer(pub_signing_key, 1, pub_signing_key.length - 1));
            decrypt_data = ut.addBuff(decrypt_data, ut.getNewBuffer(pub_encrypt_key, 1, pub_encrypt_key.length - 1));
            decrypt_data = ut.addBuff(decrypt_data, ut.createVarInt(1000));

            decrypt_data = ut.addBuff(decrypt_data, ut.createVarInt(1000));
            Random rand = new Random();

            long ttl = 2 * 24 * 60 * 60 + (long) (rand.nextInt(300));

            long expire_time = ttl + System.currentTimeMillis() / 1000L;
            object_data = ut.longToBytes(expire_time);
            object_data = ut.addBuff(object_data, ut.intToBytes(1));
            object_data = ut.addBuff(object_data, ut.createVarInt(4));
            object_data = ut.addBuff(object_data, ut.createVarInt(addressUtils.getStreamAddressBM(address)));
            object_data = ut.addBuff(object_data, addressUtils.getTagAddressBM(addr));
            byte[] signature = (new BMCrypto()).sign(priv_signing_key, ut.addBuff(object_data, decrypt_data));

            decrypt_data = ut.addBuff(decrypt_data, ut.createVarInt(signature.length));
            decrypt_data = ut.addBuff(decrypt_data, signature);
            byte[] encrypt_data = (new BMCrypto()).encrypt(addressUtils.getPublicKey(addressUtils.getKeyAddressBM(addr)), decrypt_data);
            object_data = ut.addBuff(object_data, encrypt_data);
            //

            BigInteger target = BMPow.calculatePowTarget(ttl, 1000, 1000, object_data.length);
            //long target = (two.pow(64).longValue() / (1000*(object_data.length + 8 + 1000 + ((ttl*(object_data.length+8+1000))/two.pow(16).longValue()))));

            //pow = PechkinNativeLibrary.calculatePow(target.longValue(), object_data);
            pow = (new BMPow()).calculatePow(target, object_data);
        } while (pow == null);
        object_data = ut.addBuff(pow, object_data); // Здесь должна начинатся prof of work
        //object_data = ut.addBuff(BMPow.calculatePow(target,object_data), object_data); // Здесь должна начинатся prof of work
        //byte[] signature = sign(priv_signing_key,);
        return object_data;
    }

}
