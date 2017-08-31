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

//import android.util.Log;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Fenenko Aleksandr
 */

public class BMLog {
    private static FileHandler file = null;//
    private   static Logger log = Logger.getLogger("Pechkin");
    private static int logLevel = 0;
    
    public static void setLogLevel(int level){
        logLevel = level;
        try {
            file =  new FileHandler("Pechkin.log");
            log.addHandler(file);
        } catch (IOException ex) {
            Logger.getLogger(BMLog.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(BMLog.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public synchronized static void LogD(String name,String value){
        if(0 < logLevel){
        //Log.d(name,value);
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        
        System.out.println(format.format(date)+" "+name+" "+value);
        }
    }
    public synchronized static void LogE(String name,String value){
        //Log.e(name,value);
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        System.err.println(format.format(date)+" "+name+" "+value);
    }
}
