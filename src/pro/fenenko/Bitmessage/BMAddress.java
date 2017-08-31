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

/**
 *
 * @author Fenenko Aleksandr
 */
import java.math.BigInteger;
import org.bouncycastle.crypto.digests.SHA512Digest;

/**
 * Created by fenenko on 12.01.17.
 */
public class BMAddress {

    public byte[] signing;
    public byte[] cipher;
    public int version;
    public int stream;
    public boolean flagChan;
    private String label;
    private BMUtils utils;
    private BMAddressUtils addressUtils;

    public void BMAddress() {

        utils = new BMUtils();
        addressUtils = new BMAddressUtils();
        flagChan = false;
    }

    public BMAddress(String password) {
        this.version = 4;
        this.stream = 1;
        flagChan = true;
        SHA512Digest sha = new SHA512Digest();
        byte[] digest = new byte[sha.getDigestSize()];
        byte[] passBuff = password.getBytes();
        BMAddress addr = null;
        int nonce = 0;
        do {
            byte[] t = utils.addBuff(passBuff, utils.createVarInt(nonce));
            nonce++;
            sha.update(t, 0, t.length);
            sha.doFinal(digest, 0);
            System.arraycopy(digest, 0, signing, 0, 32);
            t = utils.addBuff(passBuff, utils.createVarInt(nonce));
            nonce++;
            sha.update(t, 0, t.length);
            sha.doFinal(digest, 0);
            System.arraycopy(digest, 0, cipher, 0, 32);

        } while (getRipe()[0] != 0);
    }
    
    public BMAddress(String password,int version,int stream) {
        utils = new BMUtils();
        addressUtils = new BMAddressUtils();
        this.version = version;
        this.stream = stream;
        flagChan = true;
        signing = new byte[32];
        cipher = new byte[32];
        SHA512Digest sha = new SHA512Digest();
        byte[] digest = new byte[sha.getDigestSize()];
        byte[] passBuff = password.getBytes();
        BMAddress addr = null;
        int nonce = 0;
        do {
                
            byte[] t = utils.addBuff(passBuff, utils.createVarInt(nonce));
            nonce++;
            sha.update(t, 0, t.length);
            sha.doFinal(digest, 0);
            System.arraycopy(digest, 0, signing, 0, 32);
            t = utils.addBuff(passBuff, utils.createVarInt(nonce));
            nonce++;
            sha.update(t, 0, t.length);
            sha.doFinal(digest, 0);
            System.arraycopy(digest, 0, cipher, 0, 32);

        } while (getRipe()[0] != 0);
    }

    public BMAddress(BigInteger key_s, BigInteger key_c) {
        BMAddress();
        flagChan = false;
        signing = key_s.toByteArray();
        cipher = key_c.toByteArray();
        stream = 1;
        version = 4;

    }

    public BMAddress(byte[] key_s, byte[] key_c) {
        BMAddress();
        flagChan = false;
        signing = key_s;
        cipher = key_c;
        stream = 1;
        version = 4;

    }
    
    public BMAddress(byte[] key_s, byte[] key_c,int version,int stream,boolean isChan) {
        BMAddress();
        flagChan = isChan;
        signing = key_s;
        cipher = key_c;
        this.stream = stream;
        this.version = version;

    }

    public BMAddress(byte[] key_s, byte[] key_c, int st) {
        BMAddress();
        flagChan = false;
        signing = key_s;
        cipher = key_c;
        stream = st;
        version = 4;

    }

    public BMAddress(byte[] key_s, byte[] key_c, int version, int st) {
        BMAddress();
        flagChan = false;
        signing = key_s;
        cipher = key_c;
        stream = st;
        this.version = version;

    }

    public BMAddress(int version, int stream, byte[] key_s, byte[] key_c) {
        BMAddress();
        flagChan = false;
        signing = key_s;
        cipher = key_c;
        this.stream = stream;
        this.version = this.version;

    }

    public BMAddress() {
        BMAddress();
        flagChan = false;
        signing = addressUtils.genPrivateKey();
        cipher = addressUtils.genPrivateKey();
        stream = 1;
        version = 4;
    }

    public byte[] getPrivateSigningKey() {
        return signing;
    }

    public byte[] getPrivateCipherKey() {
        return cipher;
    }

    public byte[] getPublicSigningKey() {
        return addressUtils.getPublicKey(signing);
    }

    public byte[] getPublicCipherKey() {
        return addressUtils.getPublicKey(cipher);
    }

    public byte[] getRipe() {
        return addressUtils.getRipe(getPublicSigningKey(), getPublicCipherKey());
    }

    public String getAddress() {

        return addressUtils.getBMAddress(version, stream, addressUtils.getRipe(getPublicSigningKey(), getPublicCipherKey()));
    }

}
