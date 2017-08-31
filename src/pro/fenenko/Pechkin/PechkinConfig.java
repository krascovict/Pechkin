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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import pro.fenenko.Bitmessage.BMLog;
import pro.fenenko.Pechkin.ProtobufConfig.Config;

/**
 *
 * @author Fenenko Aleksandr
 */
public class PechkinConfig {
    private static final String FileName = PechkinConfigDir.getConfigDir()+"/Config.dat";
    
    private static Config conf;
    
    public static void init(){
        boolean flag = false;
        try {
            conf = Config.parseFrom(new FileInputStream(FileName));
        } catch (FileNotFoundException ex) {
            //Logger.getLogger(PechkinConfig.class.getName()).log(Level.SEVERE, null, ex);
            flag = true;
        } catch (IOException ex) {
            flag = true;
            //Logger.getLogger(PechkinConfig.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(flag){
            BMLog.LogD("PechkinConfig","create config file "+FileName);
            Config.Builder build = Config.newBuilder();
            build.setServerPort(8444);
            build.setServerConnect(8);
            build.setClientConnect(4);
            conf = build.build();
            write();
        }
    }
    
    
    public static int getClientConnect(){
        return conf.getClientConnect();
    }
    
    public static int getServerConnect(){
        return conf.getServerConnect();
    }
    
    public static int getServerPort(){
        return conf.getServerPort();
    }
    
    public static synchronized void setClientConnect(int value){
        synchronized(conf){
            conf = conf.toBuilder().setClientConnect(value).build();
            write();
        }
    }
    
    public static synchronized void setServerConnect(int value){
        synchronized(conf){
            conf = conf.toBuilder().setServerConnect(value).build();
            write();
        }
    }
    
    public static synchronized void setServerPort(int value){
        synchronized(conf){
            conf = conf.toBuilder().setServerPort(value).build();
            write();
        }
    }
    
    
    
    private static void write(){
        try {
            FileOutputStream file = new FileOutputStream(FileName);
            
                conf.writeTo(file);
                file.close();
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PechkinPrivateData.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PechkinPrivateData.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
