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

import org.bouncycastle.crypto.digests.SHA512Digest;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 *
 * @author Fenenko Aleksandr
 */
public class BMPow {

    private BMUtils utils;

    public BMPow() {
        utils = new BMUtils();
    }

    public void testCalculate() {
        byte[] t = "hello".getBytes();
        byte[] sha;
        long count = 0;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            do {
                digest.reset();
                sha = digest.digest(t);
                digest.reset();
                sha = digest.digest(sha);

                count++;
            } while (count < 1000000);
            BMLog.LogD("Pechkin", String.valueOf(sha.length));

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public byte[] calculatePow(long ttl, int nonce_trials_per_byte, int extra_bytes,  byte[] data) {
        long count = 0;
        long time;
        long i = 0;
        boolean flagRepeatedCalculateTTL = false;

        byte[] buff_nonce;
        ttl = ttl + (new Random()).nextInt(300);
        BigInteger target;// = calculatePowTarget(ttl,nonce_trials_per_byte,extra_bytes,data.length);

        BigInteger nonce;// = new BigInteger("0");
        BigInteger trial_val;// = new BigInteger("0");
        SHA512Digest digest = new SHA512Digest();
        byte[] sha = new byte[digest.getDigestSize()];
        byte[] initialHash = new byte[digest.getDigestSize()];
        

        time = System.currentTimeMillis() / 1000L;
        do{
            if(flagRepeatedCalculateTTL){
                BMLog.LogE("BMPow","error calculating TTL repeated start calculating");
            }
            target = calculatePowTarget(ttl,nonce_trials_per_byte,extra_bytes,data.length);
            nonce = new BigInteger("0");
            trial_val = new BigInteger("0");
            digest.update(data, 0, data.length);
            digest.doFinal(initialHash, 0);
        do {
            nonce = nonce.add(BigInteger.ONE);

            buff_nonce = nonce.toByteArray();

            buff_nonce = utils.addBuff(new byte[8 - buff_nonce.length], buff_nonce);

            buff_nonce = utils.addBuff(buff_nonce, initialHash);
            digest.reset();
            digest.update(buff_nonce, 0, buff_nonce.length);
            digest.doFinal(sha, 0);
            digest.reset();
            digest.update(sha, 0, sha.length);
            digest.doFinal(sha, 0);
            trial_val = new BigInteger(1, utils.getNewBuffer(sha, 0, 8));
            count++;
            i++;
            if (i == 1000000) {
                System.out.print("calculate ");

                System.out.print(count / 1000000);
                System.out.print("*10^6 ");
                System.out.println(((System.currentTimeMillis() / 1000L) - time));
                i = 0;
            }

        } while ((trial_val.compareTo(target) == 1));
        buff_nonce = nonce.toByteArray();

        buff_nonce = utils.addBuff(new byte[8 - buff_nonce.length], buff_nonce);
        byte[] buff_test = utils.addBuff(buff_nonce, data);
        
        flagRepeatedCalculateTTL = !checkPow(100,nonce_trials_per_byte,extra_bytes,buff_test);
        
        }while(flagRepeatedCalculateTTL);

        

        return buff_nonce;
    }
    
    public boolean checkPow(long ttl,int nonce_trials_per_byte, int extra_bytes, byte[] payload) {
        SHA512Digest sha512 = new SHA512Digest();
        byte[] msha = new byte[sha512.getDigestSize()];
        
        byte[] nonce = utils.getNewBuffer(payload, 0, 8);
        BigInteger target = calculatePowTarget(ttl, nonce_trials_per_byte, extra_bytes, payload.length - 8);
        sha512.update(utils.getNewBuffer(payload, 0, payload.length), 8, payload.length - 8);
        sha512.doFinal(msha, 0);
        //sha512.reset();
        //byte [] n = {-1,-1,-1,-1,-1,-1,-1,-1};
        byte[] t = utils.addBuff(nonce, msha);
        sha512.update(t, 0, t.length);
        sha512.doFinal(msha, 0);
        //sha512.reset();
        sha512.update(msha, 0, msha.length);
        sha512.doFinal(msha, 0);
        //printBuff("res value ",getNewBuffer(msha,0,8));
        BigInteger res = new BigInteger(utils.getNewBuffer(msha, 0, 8));
        //printBuff("nonce ",nonce);

        return res.compareTo(target) == -1;
    }

    public boolean checkPow(int nonce_trials_per_byte, int extra_bytes, byte[] payload) {
        SHA512Digest sha512 = new SHA512Digest();
        byte[] msha = new byte[sha512.getDigestSize()];
        long ttl = new BigInteger(utils.getNewBuffer(payload, 8, 8)).longValue() - (long) (System.currentTimeMillis() / 1000L);
        byte[] nonce = utils.getNewBuffer(payload, 0, 8);
        BigInteger target = calculatePowTarget(ttl, nonce_trials_per_byte, extra_bytes, payload.length - 8);
        sha512.update(utils.getNewBuffer(payload, 0, payload.length), 8, payload.length - 8);
        sha512.doFinal(msha, 0);
        //sha512.reset();
        //byte [] n = {-1,-1,-1,-1,-1,-1,-1,-1};
        byte[] t = utils.addBuff(nonce, msha);
        sha512.update(t, 0, t.length);
        sha512.doFinal(msha, 0);
        //sha512.reset();
        sha512.update(msha, 0, msha.length);
        sha512.doFinal(msha, 0);
        //printBuff("res value ",getNewBuffer(msha,0,8));
        BigInteger res = new BigInteger(utils.getNewBuffer(msha, 0, 8));
        //printBuff("nonce ",nonce);

        return res.compareTo(target) == -1;
    }

    public static BigInteger calculatePowTarget(long ttl, int nonce_trials_per_byte, int extra_bytes, int len_payload) {
        BigInteger target = new BigInteger("2").pow(64);
        BigInteger div = new BigInteger("8");
        div = div.add(new BigInteger(String.valueOf(extra_bytes)));
        div = div.add(new BigInteger(String.valueOf(len_payload)));
        div = div.multiply(new BigInteger(String.valueOf(ttl)));
        div = div.divide(new BigInteger("2").pow(16));
        div = div.add(new BigInteger(String.valueOf(extra_bytes)));
        div = div.add(new BigInteger("8"));
        div = div.add(new BigInteger(String.valueOf(len_payload)));
        div = div.multiply(new BigInteger(String.valueOf(nonce_trials_per_byte)));
        target = target.divide(div);
        return target;
    }
}
