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
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.encoders.Hex;
import pro.fenenko.Bitmessage.BMConstant;
import pro.fenenko.Pechkin.ProtobufBMObject.BMObjectData;
import pro.fenenko.Pechkin.ProtobufBMObject.BMObjectInv;
import pro.fenenko.Pechkin.ProtobufBMObject.DataObjects;
import pro.fenenko.Pechkin.ProtobufBMObject.InvObjects;
import pro.fenenko.Bitmessage.BMLog;
import pro.fenenko.Bitmessage.BMNodeAddress;
import pro.fenenko.Bitmessage.BMParse;
import pro.fenenko.Bitmessage.BMUtils;

/**
 *
 * @author Fenenko Aleksandr
 */
public class PechkinData implements BMConstant {

    private static final String FileName = PechkinConfigDir.getConfigDir() + "/ListObject.raw";
    private static byte[] newInv = new byte[0];
    private static InvObjects data;
    private static DataObjects buffWriteData;

    //private static PechkinObject[] objects = new PechkinObject[100000];
    private static BMUtils utils = new BMUtils();
    private static BMParse parse = new BMParse();

    //private static ObjectBM[] objects = new ObjectBM[50];
    //private static int countObject = 0;
    // private static ObjectBM[] buffObject = new ObjectBM[50];
    private static int countPassObject = 0;
    private static int countAddObject = 0;

    private static String getFileName(int id) {
        return PechkinConfigDir.getConfigDir() + "/Objects/object" + String.valueOf(id) + ".raw";
    }

    private static void addNewInv(byte[] inv) {
        synchronized (newInv) {
            newInv = Arrays.copyOf(newInv, newInv.length + 32);
            System.out.println("PechkinData.addNewInv " + newInv.length);
            System.arraycopy(inv, 0, newInv, newInv.length - 32, 32);
        }
    }

    public static byte[] getNewInv() {
        byte[] ret = new byte[0];
        synchronized (newInv) {
            if (0 < newInv.length) {
                ret = new byte[newInv.length];
                System.arraycopy(newInv, 0, ret, 0, ret.length);
                //newInv = null;
                newInv = new byte[0];
            }
        }
        return ret;
    }
    
    private static String[] deleteFiles(String[] listFile,String name){
        for(int i = 0; i < listFile.length;i++){
            if(name.contains(listFile[i])){
                listFile[i] = "";
            }
        }
        return listFile;
    }

    public static void init() throws FileNotFoundException {

        long timeout = System.currentTimeMillis();
        File f = new File(PechkinConfigDir.getConfigDir() + "/Objects");
        if (!f.exists()) {
            f.mkdir();
        }
        String[] listFile = f.list();
        
        try {
            data = InvObjects.parseFrom(new FileInputStream(FileName));
            synchronized (data) {

                BMLog.LogD("PechkinData", "READ " + (data.getINVCount()) + " OBJECTS " + (System.currentTimeMillis() - timeout) + " ms");
                timeout = System.currentTimeMillis();
                int oldCount = data.getINVCount();
                long time = System.currentTimeMillis() / 1000L;
                for (int i = 0; i < data.getINVCount();) {
                    if (data.getINV(i).getExpireTime() < time) {
                        data = data.toBuilder().removeINV(i).build();
                    } else {
                        listFile = deleteFiles(listFile,data.getINV(i).getFileName());
                        i++;
                    }
                }
                FileOutputStream output = new FileOutputStream(FileName);

                try {
                    data.writeTo(output);
                    output.close();
                } catch (IOException ex1) {
                    Logger.getLogger(PechkinData.class.getName()).log(Level.SEVERE, null, ex1);
                }
                BMLog.LogD("PechkinData", "DELETE EXPIRE OBJECT " + (oldCount - data.getINVCount()) + " OBJECTS " + (System.currentTimeMillis() - timeout) + " ms");
                for(int i = 0; i < listFile.length;i++)
                    if(0 < listFile[i].length())
                    {
                        BMLog.LogD("PechkinData", "DELETE OBJECT FILE "+listFile[i]);
                        File d = new File(f.getAbsolutePath()+"/"+listFile[i]);
                        d.delete();
                    }
                

            }
        } catch (FileNotFoundException ex) {
            BMLog.LogE("PechkinPrivateData", "create new file " + FileName);
            data = InvObjects.newBuilder().setId(0).build();

            FileOutputStream output = new FileOutputStream(FileName);

            try {
                data.writeTo(output);
                output.close();
            } catch (IOException ex1) {
                Logger.getLogger(PechkinData.class.getName()).log(Level.SEVERE, null, ex1);
            }

        } catch (IOException ex) {
            BMLog.LogE("PechkinPrivateData", "IOEXCEPTION REWRITE " + FileName);
            data = InvObjects.newBuilder().setId(0).build();
            FileOutputStream output = new FileOutputStream(FileName);

            try {
                data.writeTo(output);
                output.close();
            } catch (IOException ex1) {
                Logger.getLogger(PechkinData.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
        buffWriteData = DataObjects.newBuilder().setFileName(getFileName(data.getId())).build();

    }

    public static synchronized byte[] getAllInv() {
        byte[] ret;
        synchronized (data) {

            ret = new byte[data.getINVCount() * 32];
            int countInv = 0;

            for (int i = 0; i < data.getINVCount(); i++) {

                data.getINV(i).getInv().copyTo(ret, 0, countInv, 32);
                countInv += 32;

                //System.arraycopy(inv, 0,ret, i*32, 32);
            }

            //System.arraycopy(invKnow, 0, ret, 0, countInv);
        }

        return ret;
    }

    public static synchronized byte[] getPubKeys() {
        byte[] ret = null;
        synchronized (data) {
            int count = 0;
            for (int i = 0; i < data.getINVCount(); i++) {
                if (data.getINV(i).getType() == PACKET_OBJECT_PUBKEY) {
                    count++;
                }
            }
            ret = new byte[count * 32];
            count = 0;
            for (int i = 0; i < data.getINVCount(); i++) {
                if (data.getINV(i).getType() == PACKET_OBJECT_PUBKEY) {
                    data.getINV(i).getInv().copyTo(ret, count);
                    count += 32;
                }
            }
        }
        return ret;
    }

    public static synchronized byte[] getRequestNewInv(byte[] inv) {
        byte[] ret = new byte[inv.length];
        byte[] inv32 = new byte[32];
        byte[] b = new byte[32];
        int len3 = 0;
        boolean flag;
        long[] buff = new long[inv.length / 32];
        for (int i = 0; i < buff.length; i++) {
            buff[i] = 0;
            for (int j = i * 32; j < i * 32 + 32; buff[i] = buff[i] + inv[j], j++);
        }

        synchronized (data) {
            boolean[] flagNotTest = new boolean[data.getINVCount()];
            for (int i = 0; i < flagNotTest.length; flagNotTest[i] = true, i++);
            for (int i = 0; (i < inv.length) && (len3 < ret.length); i += 32) {
                flag = true;
                System.arraycopy(inv, i, b, 0, 32);
                for (int j = 0; (j < data.getINVCount()) && flag; j++) {
                    if (flagNotTest[j]) {
                        if ((buff[i / 32] == data.getINV(j).getSumInv())) {
                            data.getINV(j).getInv().copyTo(inv32, 0);

                            //flag = !Arrays.areEqual(b, inv32);
                            flag = !utils.equalBuff(b, 0, inv32, 0, 32);
                            if (!flag) {
                                flagNotTest[j] = false;
                            }
                            // flag = !PechkinNative.equalInv(b, 0, inv32, 0);

                        }
                    }
                }
                if (flag) {

                    System.arraycopy(inv, i, ret, len3, 32);

                    len3 += 32;
                }
            }
            ret = Arrays.copyOf(ret, len3);
        }
        return ret;

    }

    public static synchronized byte[] getInv() {
        byte[] ret;
        synchronized (data) {
            byte[] inv;
            ret = new byte[data.getINVCount() * 32];
            int countInv = 0;
            long time = System.currentTimeMillis() / 1000L;
            long timeout = System.currentTimeMillis();

            for (int i = 0; i < data.getINVCount(); i++) {
                if (time < data.getINV(i).getExpireTime()) {
                    data.getINV(i).getInv().copyTo(ret, 0, countInv, 32);
                    countInv += 32;

                } else {
                    //data = data.toBuilder().removeINV(i).build();
                }
                //System.arraycopy(inv, 0,ret, i*32, 32);
            }
            ret = Arrays.copyOf(ret, countInv);
            System.out.println("GENERATE INV " + (System.currentTimeMillis() - timeout) + " ms");
            //System.arraycopy(invKnow, 0, ret, 0, countInv);
        }

        return ret;
    }

    public static synchronized int getCountObject() {
        return data.getINVCount();
    }

    public static synchronized void write() {

        long maxExpireTime = 0;
        long expireTime = 0;

        synchronized (data) {
            try {

                //RandomAccessFile f = new RandomAccessFile(new File(FileName),"rw");
                //f.seek(0);
                //FileOutputStream output = new FileOutputStream(f.getFD());
                FileOutputStream f = new FileOutputStream(new File(FileName));
                BufferedOutputStream output = new BufferedOutputStream(f, 5 * 1024 * 1024);

                data.writeTo(output);
                output.close();
                f.close();

                output = null;
                f = null;
                f = new FileOutputStream(new File(buffWriteData.getFileName()));

                output = new BufferedOutputStream(f, 512 * 1024);
                buffWriteData.writeTo(output);
                output.close();
                f.close();
                output = null;
                f = null;
                int id = data.getId() + 1;
                data = data.toBuilder().setId(id).build();
                buffWriteData = null;
                //System.gc();
                buffWriteData = DataObjects.newBuilder().setFileName(getFileName(id)).build();

            } catch (IOException ex) {
                Logger.getLogger(PechkinData.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        System.gc();

        //PechkinGC.RunGC();
    }
    private static byte[] inv32 = new byte[32];
    private static FileInputStream fis;
    public static synchronized int get(byte[] inv,byte[] objectRet) {
       
        long time = System.currentTimeMillis();
        boolean flag = false;
        boolean flagFindObject = false;
        int ret = -1;
        synchronized (data) {
            

            for (int i = 0; (i < data.getINVCount()) && (!flag); i++) {
                data.getINV(i).getInv().copyTo(inv32, 0, 0, 32);
                if (equalInv(inv32, 0, inv, 0)) {
                    String s = data.getINV(i).getFileName();
                    try {
                        fis = new FileInputStream(s);
                        DataObjects object = DataObjects.parseFrom(fis);
                        for (int j = 0; (j < object.getObjectsCount()) && (!flag); j++) {
                            object.getObjects(j).getInv().copyTo(inv32, 0, 0, 32);
                            if (equalInv(inv32, 0, inv, 0)) {
                                flag = true;
                                //ret = new byte[object.getObjects(j).getData().size()];
                                if(objectRet.length < object.getObjects(j).getData().size()){
                                    byte[] t = new byte[object.getObjects(j).getData().size()+2048];
                                    objectRet = t;
                                }   
                                object.getObjects(j).getData().copyTo(objectRet, 0);
                                flagFindObject = true;
                                ret = object.getObjects(j).getData().size();

                            }

                        }
                        fis.close();
                        
                        object = null;

                    } catch (FileNotFoundException ex) {
                        //Logger.getLogger(PechkinData.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        //Logger.getLogger(PechkinData.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

            for (int i = 0; (i < buffWriteData.getObjectsCount()) && (!flagFindObject); i++) {
                buffWriteData.getObjects(i).getInv().copyTo(inv32, 0);
                if (equalInv(inv32, 0, inv, 0)) {
                    if(objectRet.length < buffWriteData.getObjects(i).getData().size()){
                        byte[] t = new byte[buffWriteData.getObjects(i).getData().size()+2048];
                        objectRet = t;
                    }
                    //ret = new byte[buffWriteData.getObjects(i).getData().size()];
                    buffWriteData.getObjects(i).getData().copyTo(objectRet, 0);
                    ret = buffWriteData.getObjects(i).getData().size();
                    flagFindObject = true;
                }
            }

        }
        //System.out.println("FIND OBJECT " + flag + " TIME " + (System.currentTimeMillis() - time) + " ms");

        return ret;
    }
    
    public static synchronized byte[] get(byte[] inv) {
        byte[] ret = null;
        long time = System.currentTimeMillis();
        boolean flag = false;
        synchronized (data) {
            

            for (int i = 0; (i < data.getINVCount()) && (!flag); i++) {
                data.getINV(i).getInv().copyTo(inv32, 0, 0, 32);
                if (equalInv(inv32, 0, inv, 0)) {
                    String s = data.getINV(i).getFileName();
                    try {
                        fis = new FileInputStream(s);
                        DataObjects object = DataObjects.parseFrom(fis);
                        for (int j = 0; (j < object.getObjectsCount()) && (!flag); j++) {
                            object.getObjects(j).getInv().copyTo(inv32, 0, 0, 32);
                            if (equalInv(inv32, 0, inv, 0)) {
                                flag = true;
                                ret = new byte[object.getObjects(j).getData().size()];
                                object.getObjects(j).getData().copyTo(ret, 0);

                            }

                        }
                        fis.close();
                        
                        object = null;

                    } catch (FileNotFoundException ex) {
                        //Logger.getLogger(PechkinData.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        //Logger.getLogger(PechkinData.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

            for (int i = 0; (i < buffWriteData.getObjectsCount()) && (ret == null); i++) {
                buffWriteData.getObjects(i).getInv().copyTo(inv32, 0);
                if (equalInv(inv32, 0, inv, 0)) {
                    ret = new byte[buffWriteData.getObjects(i).getData().size()];
                    buffWriteData.getObjects(i).getData().copyTo(ret, 0);
                }
            }

        }
        //System.out.println("FIND OBJECT " + flag + " TIME " + (System.currentTimeMillis() - time) + " ms");

        return ret;
    }

    public static synchronized boolean testINV(byte[] inv) {
        boolean flag = false;
        byte[] inv32 = new byte[32];
        for (int i = 0; (i < data.getINVCount()) && (!flag); i++) {
            data.getINV(i).getInv().copyTo(inv32, 0, 0, 32);
            flag = Arrays.areEqual(inv32, inv);
            if (flag) {
                System.out.println("OBJECTS " + i + " " + Hex.toHexString(inv32) + " == " + Hex.toHexString(inv));
            }

        }
        return flag;
    }

    public static synchronized void addNew(byte[] object) {
        byte[] inv = utils.createInv(object);
        add(object);
        addNewInv(inv);
        inv = null;
    }

    public static synchronized void add(byte[] object) {
        byte[] inv32 = new byte[32];
        // byte[] inv = utils.createInv(object);
        if ((object == null)) {
            System.err.println("OBJECT NULL");
            return;
        }

        byte[] inv = utils.createInv(object);
        synchronized (data) {

            boolean flag = false;
            for (int i = 0; (i < data.getINVCount()) && (!flag); i++) {
                data.getINV(i).getInv().copyTo(inv32, 0, 0, 32);
                flag = Arrays.areEqual(inv, inv32);

            }
            if (!flag) {
                long sum = 0;
                for (int i = 0; i < 32; sum += inv[i], i++);
                BMObjectInv.Builder builder = BMObjectInv.newBuilder();

                builder.setExpireTime(parse.getExpireTime(object));
                builder.setType(parse.getObjectType(object));
                builder.setInv(ByteString.copyFrom(inv));
                builder.setSumInv(sum);
                builder.setFileName(buffWriteData.getFileName());
                data = data.toBuilder().addINV(builder).build();
                BMObjectData.Builder build = BMObjectData.newBuilder();
                build.setExpireTime(parse.getExpireTime(object));
                build.setType(parse.getObjectType(object));
                build.setInv(ByteString.copyFrom(inv));
                build.setData(ByteString.copyFrom(object));

                buffWriteData = buffWriteData.toBuilder().addObjects(build).build();
                countAddObject++;
                if (data.getINVCount() % 100 == 0) {
                    BMLog.LogD("PechkinData", "RECEIV OBJECT " + data.getINVCount());
                }
                if (buffWriteData.getObjectsCount() == 10) {

                    write();
                }
            } else {
                countPassObject++;
                if (countPassObject % 1000 == 0) {
                    BMLog.LogE("PechkinData", "REPEAT RECEIV OBJECT " + countPassObject + " RECEIV OBJECT " + countAddObject);
                }
            }
        }

        inv = null;
        //object = null;

    }

    public static synchronized boolean equalInv(byte[] buff1, int start1, byte[] buff2, int start2) {
        boolean ret = true;
        for (int i = 0; (i < 32) && ret; i++) {
            ret = buff1[start1 + i] == buff2[start2 + i];
        }
        return ret;
    }

}
