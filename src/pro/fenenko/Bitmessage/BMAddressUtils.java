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



import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECKeyGenerationParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.math.ec.ECPoint;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 *
 * @author Fenenko Aleksandr
 */

public class BMAddressUtils  {

    private BMUtils utils;

    public BMAddressUtils(){
        utils = new BMUtils();
    }

    public String getAddress(int stream,byte[] privSign,byte[] privEncr){
        byte[] address;
        byte[] ripe = getRipe(getPublicKey(privSign),getPublicKey(privEncr));
        BMLog.LogD("Pechkin","1");
        while(ripe[0] == 0){
            ripe = utils.getNewBuffer(ripe,1,ripe.length-1);
        }
        BMLog.LogD("Pechkin","2");
        address = utils.createVarInt(4);
        address = utils.addBuff(address,utils.createVarInt(stream));
        address = utils.addBuff(address,ripe);
        SHA512Digest sha512 = new SHA512Digest();
        BMLog.LogD("Pechkin","3");

        byte[] msha512 = new byte[sha512.getDigestSize()];
        sha512.update(address,0,address.length);
        sha512.doFinal(msha512,0);
        //sha512.reset();
        sha512.update(msha512,0,msha512.length);
        sha512.doFinal(msha512,0);
        address = utils.addBuff(address,utils.getNewBuffer(msha512,0,4));
        BMLog.LogD("Pechkin","4");
        return "BM-"+utils.encodeBase58(address);
    }


    public byte[] genPrivateKey(){
        ECNamedCurveParameterSpec dom = ECNamedCurveTable.getParameterSpec("secp256k1");
        ECDomainParameters param = new ECDomainParameters(dom.getCurve(),dom.getG(),dom.getN(),dom.getH(),dom.getSeed());
        ECKeyPairGenerator generator = new ECKeyPairGenerator();
        generator.init(new ECKeyGenerationParameters(param,new SecureRandom()));
        AsymmetricCipherKeyPair key_pair = generator.generateKeyPair();

        return ((ECPrivateKeyParameters)key_pair.getPrivate()).getD().toByteArray();
    }
    public byte[] getPublicKey(BigInteger value){
        ECNamedCurveParameterSpec spec = ECNamedCurveTable.getParameterSpec("secp256k1");
        ECPoint public_key = spec.getG().multiply(value);

        return public_key.getEncoded(false);
    }
    
    public BMAddress createChan(String address,String password){
        
        int version = 0;
        int stream = 0;
        if(address.length() != 0){
            version  = getVersionAddressBM(address);
            stream = getStreamAddressBM(address);
        } else
        {
            version = 4;
            stream = 1;
        }
        BMAddress ret = new BMAddress(password,version,stream);
        if(!ret.getAddress().contains(address)){
            ret = null;
        }
        return ret;
    }

    public byte[] getPublicKey(byte[] buff){

        return getPublicKey(new BigInteger(1,buff));
    }

    public byte[] getRipe(byte[] public_signing_key,byte[] public_encryption_key){

        SHA512Digest d_s512 = new SHA512Digest();
        byte[] sha512 = new byte[d_s512.getDigestSize()];
        d_s512.update(public_signing_key,0,public_signing_key.length);
        d_s512.update(public_encryption_key,0,public_encryption_key.length);
        d_s512.doFinal(sha512,0);
        RIPEMD160Digest d_r160 = new RIPEMD160Digest();
        d_r160.update(sha512,0,sha512.length);
        byte[] r160 = new byte[d_r160.getDigestSize()];
        d_r160.doFinal(r160,0);

        return r160;
    }
    public String getBMAddress(int version, int stream,byte[] digest){
        int position = 0;
        byte[] ret = new byte[0];
        while(digest[0] == 0)
        {
            digest = utils.getNewBuffer(digest,1,digest.length-1);
        }
        ret = utils.addBuff(ret, utils.createVarInt(version));
        ret = utils.addBuff(ret, utils.createVarInt(stream));
        ret = utils.addBuff(ret,digest);

        SHA512Digest s512 = new SHA512Digest();

        byte[] sha512 = new byte[s512.getDigestSize()];
        s512.update(ret,0,ret.length);
        s512.doFinal(sha512,0);
        //s512.reset();
        s512.update(sha512,0,sha512.length);
        s512.doFinal(sha512,0);
        ret = utils.addBuff(ret, utils.getNewBuffer(sha512,0,4));

        return "BM-"+ utils.encodeBase58(new BigInteger(1,ret));

    }
    public  boolean checkBMAddress(String address){
        if(address.length() <= 4)
            return false;


        byte[] buff = utils.decodeBase58(address);
        if(buff == null)
            return false;
        if(buff.length <= 4)
            return false;

        SHA512Digest digest = new SHA512Digest();
        byte[] sha512 = new byte[digest.getDigestSize()];
        digest.update(buff,0,buff.length-4);
        digest.doFinal(sha512,0);
        digest.update(sha512,0,sha512.length);
        digest.doFinal(sha512,0);
        return utils.equalBuff(sha512,0,buff,buff.length-4,4);


    }
    public boolean checkAddressBM(String value){
        boolean flag = true;
        SHA512Digest d_sha512 = new SHA512Digest();
        byte[] b = null;
        try{
        b = utils.decodeBase58(value);
        
        d_sha512.update(b,0,b.length-4);
        }catch(java.lang.NullPointerException ex){
            return false;
        }
        byte[] t = new byte[d_sha512.getDigestSize()];
        d_sha512.doFinal(t,0);
        d_sha512.reset();
        d_sha512.update(t,0,t.length);
        d_sha512.doFinal(t,0);
        try{
        for(int i =0,j=b.length-4; i < 4;i++,j++){
            if(b[j] != t[i])
                flag = false;
        }
        }catch(java.lang.ArrayIndexOutOfBoundsException ex){
            return false;
        }

        return flag;
    }

    public BMAddress genBMKeyAddress(int work){

        byte[] ripe;
        byte[] signing = new byte[32];
        byte[] cipher = new byte[32];
        SecureRandom random = new SecureRandom();
        //BigInteger signing = new BigInteger(1,genPrivateKey());
        //BigInteger cipher = new BigInteger(1,genPrivateKey());
        //signing = signing.subtract(BigInteger.ONE);
        //cipher = cipher.subtract(BigInteger.ONE);
        SHA256Digest digest = new SHA256Digest();
        random.nextBytes(signing);
        digest.update(signing,0,32);
        digest.doFinal(cipher,0);
        do{

            //signing = genPrivateKey();
            //cipher = genPrivateKey();
            digest.update(signing,0,32);
            digest.doFinal(signing,0);
            digest.update(cipher,0,32);
            digest.doFinal(cipher,0);
            //signing = signing.add(BigInteger.ONE);
            //cipher = cipher.add(BigInteger.ONE);
            ripe = getRipe(getPublicKey(signing),getPublicKey(cipher));
            while(ripe[0] == 0)
            {
                ripe = utils.getNewBuffer(ripe,1,ripe.length-1);
            }
            //println("iteration ");
        } while(work <= ripe.length);
        return new BMAddress(signing,cipher);
    }
    public String createAddress(int stream,byte[] public_signing_key,byte[] public_encryption_key){
        byte[] r = getRipe(public_signing_key,public_encryption_key);

        while(r[0] == 0)
        {
            r = utils.getNewBuffer(r,1,r.length-1);
        }
        byte[] address = utils.createVarInt(4);

        address = utils.addBuff(address, utils.createVarInt(stream));
        address = utils.addBuff(address,r);
        SHA512Digest sha512 = new SHA512Digest();

        byte[] msha512 = new byte[sha512.getDigestSize()];
        sha512.update(address,0,address.length);
        sha512.doFinal(msha512,0);
        //sha512.reset();
        sha512.update(msha512,0,msha512.length);
        sha512.doFinal(msha512,0);
        address = utils.addBuff(address, utils.getNewBuffer(msha512,0,4));
        return "BM-"+ utils.encodeBase58(new BigInteger(1,address));
    }

    public byte[] getKeyAddressBM(String value){
        byte[] version = utils.createVarInt(getVersionAddressBM(value));
        byte[] stream = utils.createVarInt(getStreamAddressBM(value));
        byte[] ripe = getRipeAddressBM(value);

        SHA512Digest d_sha512 = new SHA512Digest();
        byte[] b = new byte[d_sha512.getDigestSize()];
        byte[] ret = new byte[32];

        d_sha512.update(version,0,version.length);
        d_sha512.update(stream,0,stream.length);
        d_sha512.update(ripe,0,ripe.length);
        d_sha512.doFinal(b,0);
        d_sha512.reset();
        d_sha512.update(b,0,b.length);
        d_sha512.doFinal(b,0);
        for(int i = 0; i < 32;i++)
        {
            ret[i] = b[i];
        }

        return ret;
    }
    public int getVersionAddressBM(String value){
        return (int) utils.decodeVarInt(utils.decodeBase58(value),0)[1];
    }

    public int getStreamAddressBM(String value){
        byte[] b = utils.decodeBase58(value);
        int position = (int)utils.decodeVarInt(b,0)[0];
        return (int) utils.decodeVarInt(b,position)[1];
    }

    public byte[] getRipeAddressBM(String value){
        byte[] ret = new byte[20];
        byte[] b = utils.decodeBase58(value);
        int position = (int) utils.decodeVarInt(b,0)[0];
        position = (int) utils.decodeVarInt(b,position)[0];
        String s = "";
        for(int i = b.length-5,j=19;position <= i;i--,j--){
            ret[j] =b[i];
        }

        return ret;

    }
    public byte[] getTagAddressBM(String value){
        if(value.contains("BM-"))
            value = value.substring(3,value.length());
        byte[] version = utils.createVarInt(getVersionAddressBM(value));
        byte[] stream = utils.createVarInt(getStreamAddressBM(value));
        byte[] ripe = getRipeAddressBM(value);

        SHA512Digest d_sha512 = new SHA512Digest();
        byte[] b = new byte[d_sha512.getDigestSize()];
        byte[] ret = new byte[32];

        d_sha512.update(version,0,version.length);
        d_sha512.update(stream,0,stream.length);
        d_sha512.update(ripe,0,ripe.length);
        d_sha512.doFinal(b,0);
        d_sha512.reset();
        d_sha512.update(b,0,b.length);
        d_sha512.doFinal(b,0);
        for(int i = 0,j=32;i < 32;i++,j++)
            ret[i] = b[j];
        return ret;
    }
}