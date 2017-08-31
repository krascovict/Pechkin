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

import java.io.File;
import pro.fenenko.Bitmessage.BMLog;

/**
 *
 * @author Fenenko Aleksandr
 */
public class PechkinConfigDir {
    public static synchronized String getConfigDir(){
        String os = System.getProperty("os.name");
        String dir = System.getProperty("user.home");
        if(os.contains("Linux")){
            dir += "/.config/Pechkin";
            
        } else{
            if(os.contains("Window")){
                dir += "/Pechkin";
            }
        }
        File f = new File(dir);
        if(!f.exists()){
            BMLog.LogD("PechkinConfigDir", "create dir"+dir);
            boolean flag = f.mkdirs();
            if(!flag){
                BMLog.LogE("PechkinConfigDir","error create dir");
            }
        }
        //dir = "./config";
        return dir;
    }
    
    public static synchronized String getPyBitmessagDir(){
        String os = System.getProperty("os.name");
        String dir = System.getProperty("user.home");
        if(os.contains("Linux")){
            dir += "/.config/PyBitmessage";
            
        }else{
            if(os.contains("Window")){
                dir += "/Pechkin";
            }
        }
        return dir;
    }
}
