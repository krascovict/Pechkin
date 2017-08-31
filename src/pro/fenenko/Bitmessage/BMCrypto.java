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
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.DERSequenceGenerator;
import org.bouncycastle.asn1.DLSequence;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.agreement.ECDHBasicAgreement;
import java.security.MessageDigest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PKCS7Padding;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.math.ec.ECPoint;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 *
 * @author Fenenko Aleksandr
 */

public class BMCrypto {
    private ECNamedCurveParameterSpec dom = ECNamedCurveTable.getParameterSpec("secp256k1");
    private ECDomainParameters param = new ECDomainParameters(dom.getCurve(),dom.getG(),dom.getN(),dom.getH(),dom.getSeed());
    private BMUtils utils;
    private BMPow pow;

    public BMCrypto(){
        utils = new BMUtils();
        pow = new BMPow();
    }


    public byte[] getSHA512first32(byte[] buff){
        byte[] ret = new byte[32];
        byte[] b = new byte[64];
        SHA512Digest sha512 = new SHA512Digest();
        sha512.update(buff,0,buff.length);
        sha512.doFinal(b,0);
        for(int i = 0; i < 32;i++)
        {
            ret[i] = b[i];
        }
        pow = new BMPow();
        return ret;
    }

    public byte[] getSHA512last32(byte[] buff){
        byte[] ret = new byte[32];
        byte[] b = new byte[64];
        SHA512Digest sha512 = new SHA512Digest();
        sha512.update(buff,0,buff.length);
        sha512.doFinal(b,0);
        for(int i = 0; i < 32;i++)
        {
            ret[i] = b[i+32];
        }
        return ret;
    }


    public byte[] add04(byte[] buff){
        byte[] ret = new byte[buff.length+1];
        for(int i = 1;i < ret.length;i++)
        {
            ret[i] = buff[i-1];
        }
        ret[0] = 0x04;
        return ret;
    }






    public byte[] sign(byte[] sign_key,byte[] buff){


        ECDSASigner signer = new ECDSASigner();
        ECPrivateKeyParameters keysign = new ECPrivateKeyParameters( new BigInteger(1,sign_key), param);
        signer.init(true,keysign);
        MessageDigest sha1 = null;
        try {
            sha1 = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        //byte[] msha1 = new byte[sha1.getDigestSize()];
        sha1.update(buff,0,buff.length);
        //sha1.doFinal(msha1,0);
        byte[] msha1 = sha1.digest();
        ByteArrayOutputStream s = new ByteArrayOutputStream ();
        try{
            BigInteger[] signature = signer.generateSignature(msha1);
            ASN1Encodable[] arr = new ASN1Encodable[2];
            arr[0] = new ASN1Integer(signature[0]);
            arr[1] = new ASN1Integer(signature[1]);
            DERSequenceGenerator dseq = new DERSequenceGenerator (s);
            DLSequence seq = new DLSequence(arr);
            dseq.addObject (new ASN1Integer(signature[0]));
            dseq.addObject (new ASN1Integer(signature[1]));
            dseq.close ();
            ASN1InputStream reader = new ASN1InputStream(s.toByteArray());
        }catch(IOException e ){
            BMLog.LogE("Pechkin","error BMCrypto "+e.getMessage());
        }
        return s.toByteArray();

    }


    public  boolean checksigning(byte[] pub_key,BigInteger r,BigInteger s,byte[] buff){
        boolean flag = false;
        ECNamedCurveParameterSpec dom = ECNamedCurveTable.getParameterSpec("secp256k1");
        ECDomainParameters param = new ECDomainParameters(dom.getCurve(),dom.getG(),dom.getN(),dom.getH(),dom.getSeed());
        ECDSASigner signer = new ECDSASigner();
        MessageDigest sha1 = null;
        try {
            sha1 = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        SHA256Digest sha256 = new SHA256Digest();
        byte[] msha256 = new byte[sha256.getDigestSize()];
        byte[] msha1 = sha1.digest(buff);
        sha256.update(buff,0,buff.length);
        sha256.doFinal(msha256,0);
        ECPublicKeyParameters key_pub;
        try {
            key_pub = new ECPublicKeyParameters(dom.getCurve().decodePoint(pub_key), param);
        }catch(java.lang.IllegalArgumentException e){
            return  false;
        }
        signer.init(false,key_pub);
        try{
            flag  = signer.verifySignature(msha1,r,s);
            if(!flag){
                flag  = signer.verifySignature(msha256,r,s);
            }
        }catch(NullPointerException e){
            BMLog.LogE("Pechkin","error BMCrypto "+e.getMessage());
        }
        return flag;

    }


    public  byte[] encrypt(byte[] pub_key,byte[] buff){
        ECNamedCurveParameterSpec dom = ECNamedCurveTable.getParameterSpec("secp256k1");
        ECDomainParameters param = new ECDomainParameters(dom.getCurve(),dom.getG(),dom.getN(),dom.getH(),dom.getSeed());
        ECDHBasicAgreement ecdh = new ECDHBasicAgreement();
        PKCS7Padding padding = new PKCS7Padding();
        padding.init(new SecureRandom());
        PaddedBufferedBlockCipher pcipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESEngine()), padding );
        byte[] iv = utils.genKey(16);
        byte[] r_priv = (new BMAddressUtils()).genPrivateKey();
        byte[] r_pub = (new BMAddressUtils()).getPublicKey(r_priv);
        ECPoint point = dom.getCurve().decodePoint(r_pub);
        ecdh.init(new ECPrivateKeyParameters(new BigInteger(1,r_priv),param));
        byte[] key_pub= null;
        try {
             key_pub = utils.convertBigIntegerToByte(ecdh.calculateAgreement(new ECPublicKeyParameters(dom.getCurve().decodePoint(pub_key), param)));
        }catch(java.lang.IllegalArgumentException ex){
            return null;
        }
        while(key_pub[0] == 0){
            key_pub = utils.getNewBuffer(key_pub,1,key_pub.length-1);
        }
        byte[] key_e = getSHA512first32(key_pub);
        byte[] key_mac = getSHA512last32(key_pub);
        HMac hmac = new HMac(new SHA256Digest());
        byte[] resBuf = new byte[hmac.getMacSize()];
        pcipher.init(true,new ParametersWithIV(new KeyParameter(key_e),iv));
        byte[] encr = new byte[pcipher.getOutputSize(buff.length)];
        int i = pcipher.processBytes(buff,0,buff.length,encr,0);
        try{
            pcipher.doFinal(encr,i);
        }catch(InvalidCipherTextException e){
            BMLog.LogE("Pechkin","error BMCrypto "+e.getMessage());
            return new byte[0];
        }
        byte[] data = iv;
        data = utils.addBuff(data, utils.shortToBytes(714));
        byte[] pub_x = point.getXCoord().getEncoded();
        byte[] pub_y = point.getYCoord().getEncoded();
        data = utils.addBuff(data, utils.shortToBytes(pub_x.length));
        data = utils.addBuff(data,pub_x);
        data = utils.addBuff(data, utils.shortToBytes(pub_y.length));
        data = utils.addBuff(data,pub_y);
        data = utils.addBuff(data,encr);
        hmac.init(new KeyParameter(key_mac));
        hmac.update(data,0,data.length);
        hmac.doFinal(resBuf,0);
        data = utils.addBuff(data,resBuf);
        return data;
    }



    public byte[] decrypt( byte[] priv_key,byte[] buff){

        PaddedBufferedBlockCipher cipher =new PaddedBufferedBlockCipher( new CBCBlockCipher(new AESEngine()),new PKCS7Padding());
        ECNamedCurveParameterSpec dom = ECNamedCurveTable.getParameterSpec("secp256k1");
        ECDomainParameters param = new ECDomainParameters(dom.getCurve(),dom.getG(),dom.getN(),dom.getH(),dom.getSeed());
        ECPrivateKeyParameters c_param = new ECPrivateKeyParameters(new BigInteger(1,priv_key),param);
        ECDHBasicAgreement ecdh = new ECDHBasicAgreement();
        ecdh.init(c_param);
        byte[] iv = utils.getNewBuffer(buff,0,16);
        int curve = utils.bytesToShort(buff,16);
        int length_x = utils.bytesToShort(buff,18);
        int length_y = utils.bytesToShort(buff,20+length_x);
        byte[] pub_x = utils.getNewBuffer(buff,20,length_x);
        byte[] pub_y = utils.getNewBuffer(buff,22+length_x,length_y);
        ECPoint point = ECNamedCurveTable.getParameterSpec("secp256k1").getCurve().validatePoint(new BigInteger(1,pub_x),new BigInteger(1,pub_y));
        byte[] pub_key = utils.convertBigIntegerToByte(ecdh.calculateAgreement(new ECPublicKeyParameters(point,param)));
        while(pub_key[0] == 0){
            pub_key = utils.getNewBuffer(pub_key,1,pub_key.length-1);
        }
        byte[] key_e = getSHA512first32(pub_key);
        byte[] key_mac = getSHA512last32(pub_key);
        HMac hmac = new HMac(new SHA256Digest());
        byte[] resBuf = new byte[hmac.getMacSize()];
        cipher.init(false,new ParametersWithIV(new KeyParameter(key_e),iv));
        hmac.init(new KeyParameter(key_mac));
        hmac.update(buff,0,buff.length-32);
        hmac.doFinal(resBuf,0);
        if (!utils.equalBuff(buff,buff.length-32,resBuf,0,32))
        {
            return new byte[0];
        }
        byte[] cipherText = utils.getNewBuffer(buff,22+length_x+length_y,buff.length-(54+length_x+length_y));
        byte[] outText = new byte[cipher.getOutputSize(cipherText.length)];
        int len = cipher.processBytes(cipherText,0,cipherText.length,outText,0);
        try{
            len = len + cipher.doFinal(outText,len);
        }catch(InvalidCipherTextException  e){
            BMLog.LogE("Pechkin","error BMCrypto"+e.getMessage());
            return new byte[0];
        }
        outText = utils.getNewBuffer(outText,0,len);
        return outText;
    }
}
