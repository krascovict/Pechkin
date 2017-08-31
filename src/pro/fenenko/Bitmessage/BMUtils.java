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
import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.Arrays;


/**
 *
 * @author Fenenko Aleksandr
 */

public class BMUtils {
    private static final String ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";

    public BMUtils(){


    }

    public  void printBuff(byte[] buff){
        String s = encodeHexString(buff);
        
    }
    public  void printBuff(String tag,byte[] buff,int position,int len){
        String s = encodeHexString(buff,position,len);
     
    }

    public  BMNodeAddress[] helperBootStrap(){
        BMNodeAddress[] ret = new BMNodeAddress[0];
        InetAddress[] adr = new InetAddress[0];

        try {
            BMLog.LogD("BMUtils.helpreBootStrap","start bootstrap");
            //try {
                adr = InetAddress.getAllByName("bootstrap8444.bitmessage.org");
            //}catch(android.os.NetworkOnMainThreadException e){
            //    BMLog.LogE("Pechkin.BMUtils","helperBototstrap error "+e.toString());
            //}
            
            BMNodeAddress[] tmp1 = new BMNodeAddress[adr.length];
            for (int i = 0; i < adr.length; i++) {
                tmp1[i] = new BMNodeAddress(adr[i].getHostAddress(), 8444,System.currentTimeMillis()/1000L);
                BMLog.LogD("BMUtils.helpreBootStrap","add NodeAddress "+tmp1[i].getIp()+" "+tmp1[i].getPort());
            }
            adr = InetAddress.getAllByName("bootstrap8080.bitmessage.org");
            
            BMNodeAddress[] tmp2 = new BMNodeAddress[adr.length];
            for (int i = 0; i < adr.length; i++) {
                tmp2[i] = new BMNodeAddress(adr[i].getHostAddress(), 8080);
                BMLog.LogD("BMUtils.helpreBootStrap","add NodeAddress "+tmp1[i].getIp()+" "+tmp1[i].getPort());
            }
            
            ret = new BMNodeAddress[tmp1.length+tmp2.length];
            //ret = new BMNodeAddress[]{new BMNodeAddress("192.168.1.240",18444)};
            System.arraycopy(tmp1,0,ret,0,tmp1.length);
            System.arraycopy(tmp2,0,ret,tmp1.length,tmp2.length);
            BMLog.LogD("BMUtils.helpreBootStrap","finish bootstrap receiv node address "+ret.length);
        } catch (UnknownHostException e) {
            BMLog.LogE("BMUtils.helpreBootStrap","error bootstrap "+e.toString());
        }
        //ret = new BMNodeAddress[]{new BMNodeAddress("192.168.1.240",18444)};

        return ret;


    }

    public  byte[] convertBigIntegerToByte(BigInteger value){
        byte[] buff;
        buff = value.toByteArray();
        if(buff[0] == 0){
            byte[] nbuff = new byte[buff.length-1];
            System.arraycopy(buff,1,nbuff,0,buff.length-1);
            buff = nbuff;
        }
        return buff;

    }



    public String encodeBase58(byte[] data){
        BigInteger value = new BigInteger(1,data);

        String ret = "";
        BigInteger base = new BigInteger("58",10);
        while(0 < value.compareTo(BigInteger.ZERO)){
            int n = value.mod(base).intValue();
            ret = ALPHABET.charAt(n) + ret;
            value = value.divide(base);

        }

        return ret;
    }

    public  String encodeBase58(BigInteger value){

        String ret = "";
        BigInteger base = new BigInteger("58",10);
        while(0 < value.compareTo(BigInteger.ZERO)){
            int n = value.mod(base).intValue();
            ret = ALPHABET.charAt(n) + ret;
            value = value.divide(base);

        }

        return ret;
    }

    public  String createKeyBitmessageFormat(byte[] keys){
        byte[] ret = {-128};
        ret = addBuff(ret,keys);
        SHA256Digest digest = new SHA256Digest();
        byte[] sha256 = new byte[digest.getDigestSize()];
        digest.update(ret,0,ret.length);
        digest.doFinal(sha256,0);
        digest.update(sha256,0,sha256.length);
        digest.doFinal(sha256,0);
        return (new BMUtils()).encodeBase58(new BigInteger(1,addBuff(ret,getNewBuffer(sha256,0,4))));
    }
    
    public byte[] importKeyBitmessageFormat(String key){
        byte[] ret = (new BMUtils()).decodeBase58(key);
        SHA256Digest digest = new SHA256Digest();
        byte[] sha256 = new byte[digest.getDigestSize()];
        digest.update(ret,0,ret.length-4);
        digest.doFinal(sha256,0);
        digest.update(sha256,0,sha256.length);
        digest.doFinal(sha256,0);
        if((sha256[3] != ret[ret.length-1])
                ||(sha256[2] != ret[ret.length-2])
                ||(sha256[1] != ret[ret.length-3])
                ||(sha256[0] != ret[ret.length-4])){
            return null;
        }
        ret = getNewBuffer(ret,1,ret.length-5);
        return ret;
    }
    
    

    public  boolean checkKeyImportBitmessage(String keyInBase58)
    {
        byte[] data = decodeBase58(keyInBase58);

        if(data == null)
            return  false;

        if(  data.length  < 5 ){
            return false;
        }



        SHA256Digest digest = new SHA256Digest();
        byte[] sha256 = new byte[digest.getDigestSize()];
        digest.update(data,0,data.length-4);
        digest.doFinal(sha256,0);
        //digest.reset();
        digest.update(sha256,0,sha256.length);
        digest.doFinal(sha256,0);

        for(int i = data.length-4,j=0; i < data.length;i++,j++){
            if(sha256[j]!=data[i])

                return false;
        }
        BMLog.LogD("Pechkin","valid keys in");
        return  true;
    }
    
    public String[] add(String[] buff,String value){
        String[] t = new String[buff.length+1];
        System.arraycopy(buff, 0, t, 0,buff.length);
        t[buff.length] = value;
        return t;
    }
    
    public long[] add(long[] buff,long value){
        long[] t = new long[buff.length+1];
        System.arraycopy(buff, 0, t, 0,buff.length);
        t[buff.length] = value;
        return t;
    }
    
    public int find(String[] buff,String value){
        int ret = -1;
        for(int i = 0;(ret==-1)&&( i < buff.length);i++){
            if(buff[i].contains(value)){
                ret = i;
            }
        }
        return ret;
    }

    public  byte[] decodeBase58(String value){
        int t;
        BigInteger base = new BigInteger("58");
        BigInteger ret = BigInteger.ZERO;
        if(value.indexOf("BM-") == 0){
            value = value.substring(3,value.length());
        }
        for(int i =0; i < value.length();i++){
            ret = ret.multiply(base);
            t = ALPHABET.indexOf(value.charAt(i));
            if(t == -1){
                BMLog.LogE("Pechkin","Not decoding char in Base58");
                return null;

            }

            ret = ret.add(new BigInteger(String.valueOf(t)));


        }
        return convertBigIntegerToByte(ret);
    }


    public byte[] convertIP(String ip)
    {
        byte[] ret = new byte[0];
        byte[] ip_map = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1};
        try {
            ret = Inet6Address.getByName(ip).getAddress();
        }
        catch(UnknownHostException e) {
            BMLog.LogE("Pechkin","Error convert ip address"+ip);
        }
        if (ret.length == 4) {
            ret = addBuff(ip_map, ret);
        }

        return ret;
    }
    public byte[] shortToBytes(int value) {
        byte[] ret = new byte[2];
        for (int i = 1; 0 <= i; i--) {
            ret[i] = (byte)(value&0xff);

            value = value >> 8;
        }
        return ret;
    }

    public byte[] createVarStr(String value) {

        byte[] bs = value.getBytes();
        return addBuff(createVarInt(bs.length), bs);
    }

    public byte[] createVarListInt(int[] value) {
        byte[] ret = createVarInt(value.length);
        for (int i = 0; i < value.length; i++)
        {
            ret = addBuff(ret, createVarInt(value[i]));
        }
        return ret;
    }

    public int bytesToInt(byte[] buff, int position) {
        long v = 0;
        for (int i=position; i<4+position; i++) {
            v =v << 8;
            v |= (int)buff[i]&0xff;
        }
        return (int)v;
    }

    public  boolean equalBuff(byte[] buff1,int index1,byte[] buff2 ,int index2,int len){
        if((buff1.length < (index1+len))||(buff2.length < (index2+len)))
            return false;
        for(int i1=index1,i2=index2;i1<index1+len;i1++,i2++)
            if(buff1[i1]!=buff2[i2])
                return false;
        return true;
    }


    public String getString(byte[] buff, int position) {
        String s = "";
        while (buff[position ] != 0) {
            s = s + (char)buff[position];
            position++;
        }
        return s;
    }



    public byte[] intToBytes(int value) {
        byte[] ret = new byte[4];
        for (int i = 3; 0 <= i; i--) {
            ret[i] = (byte)(value&0x000000ff);

            value = value >> 8;
        }
        return ret;
    }

    public byte[] longToBytes(long value) {
        byte[] ret = new byte[8];
        for (int i = 7; 0 < i; i--) {
            ret[i] = (byte)(value&0xff);
            value = value >> 8;
        }
        return ret;
    }


    public byte[] addBuff(byte[] buff1, byte[] buff2)
    {
        int p = buff1.length;
        buff1 = Arrays.copyOf(buff1,buff1.length+buff2.length);


        System.arraycopy(buff2,0,buff1,p,buff2.length);
        //System.gc();
        return buff1;
    }
    private static String hex_s = "0123456789ABCDEF";
    public synchronized String encodeHexString(byte[] buff)
    {


        String ret = "";
        for(int i =0; i < buff.length;i++)
        {
            ret = ret + (char)hex_s.charAt((int)(buff[i]>>4)&0x0f);
            ret = ret + (char)hex_s.charAt((int)(buff[i])&0x0f);
        }
        return ret;

        //return Hex.toHexString(buff);

    }

    public String encodeHexString(byte[] buff,int position,int count)
    {

        String s = "";
        for(int i =position; i < (position+count);i++)
        {

            s = s + (char)hex_s.charAt((int)(buff[i]>>4)&0x0f);
            s = s + (char)hex_s.charAt((int)(buff[i])&0x0f);
        }
        return s;

    }

    public byte[] decodeHexString(String shex) {
        if(shex.length()%2 == 1)
            return null;
        byte[] ret = new byte[shex.length()/2];
        for(int  i = 0; i < shex.length();i++){
            //BMLog.LogD("Pechkin.TEST",shex.charAt(i*2)+" "+shex.charAt(i*2+1));
            //ret[i] = (byte)(hex_s.indexOf(shex.charAt(i*2+1))-1);
            //ret[i] |= (byte)(hex_s.indexOf(shex.charAt(i*2))-1)<<4;
            ret[i/2] = (byte)((ret[i/2]&0x0F)<<4);
            ret[i/2] |= hex_s.indexOf(shex.charAt(i));

        }


        return ret;
    }
    public  byte[] getNewBuffer(byte[] buff, int position, int len) {
        buff = Arrays.copyOfRange(buff,position,position+len);

        return buff;
    }

    public static synchronized long bytesToLong(byte[] buff, int position) {
        long v = 0;
        for (int i=position; i<position+8; i++) {
            v =v << 8;
            v |= (int)buff[i]&0xff;
        }
        return v;
    }

   long bytesToLong(byte[] buff) {
        return bytesToLong(buff, 0);
    }

    public int bytesToShort(byte[] buff, int position) {
        long v = 0;
        for (int i=position; i<position+2; i++) {
            v =v << 8;
            v |= (int)buff[i]&0xff;
        }
        return (int)v;
    }


    public int bytesToShort(byte[] buff) {

        return bytesToShort(buff, 0);
    }
    // return valu in second element  first element new position in massive
    //
    public long[] decodeVarInt(byte[] buff, int position)
    {
        long[] ret =new long[2];
        switch(buff[position])
        {
            case -1:
                ret[1] = bytesToLong(buff, position+1);
                ret[0] = position + 9;

                break;
            case -2:
                ret[1] = bytesToInt(buff, position+1);
                ret[0] = position + 5;
                break;
            case -3:
                ret[1] = bytesToShort(buff, position+1);
                ret[0] = position + 3;
                break;
            default:
                ret[1] = buff[position]&0xff;
                ret[0] = position + 1;
                break;
        }
        return ret;
    }

    public String decodeVarStr(byte[] buff,int position){

        String s = "";
        long[] t = decodeVarInt(buff,position);
        for(int i = (int)t[0];i < (int)(t[0]+t[1]);i++)
        {
            s = s +(char)buff[i];
        }
        return s;

    }

    public byte[] createInv(byte[] dat_object){
        SHA512Digest digest = new SHA512Digest();
        byte[] sha512 = new byte[digest.getDigestSize()];
        digest.update(dat_object,0,dat_object.length);
        digest.doFinal(sha512,0);
        digest.reset();
        digest.update(sha512,0,sha512.length);
        digest.doFinal(sha512,0);
        byte[] ret = new byte[32];
        System.arraycopy(sha512, 0, ret, 0, 32);
        return ret;

    }
    public byte[] genKey(int size){
        byte[] ret = new byte[size];

        new SecureRandom().nextBytes(ret);
        return ret;
    }

    public byte[] createVarInt(long value) {
        byte[] ret;
        if (value <= 252) {
            ret = new byte[1];
            ret[0] = (byte)(value&0xff);
            return ret;
        } else {
            if (value <= 65535) {
                ret = new byte[3];
                ret[0] = -3;
                for (int i =2; 1 <= i; i-- ) {
                    ret[i] = (byte)(value&0xff);
                    value = value >> 8;
                }
                return ret;
            } else {
                if (value <= 0xffffffff) {
                    ret = new byte[5];
                    ret[0] = -2;
                    for (int i =4; 1 <= i; i-- ) {
                        ret[i] = (byte)(value&0xff);
                        value = value >> 8;
                    }
                    return ret;
                } else {
                    ret = new byte[9];
                    ret[0] = -1;
                    for (int i =8; 1 <= i; i-- ) {
                        ret[i] = (byte)(value&0xff);
                        value = value >> 8;
                    }
                    return ret;
                }
            }
        }
    }


}
