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

import pro.fenenko.Bitmessage.BMParse;

/**
 *
 * @author Fenenko Aleksandr
 */
public class PechkinMessage {
    private long ID;
    private long Time;
    private int Status;
    private String address_to;
    private String address_from;
    private String text;
    public static final String SUBJECT = "Subject:";
    public static final String BODY="Body:";
    
    public PechkinMessage(long id,String addressFrom,String addressTo,String Text,long time,int status){
        ID = id;
        Time = time;
        Status = status;
        address_from = addressFrom;
        address_to = addressTo;
        text = Text;
    }
    
    public long getID(){
        return ID;
    }
    
    public long getTime(){
        return Time;
    }
    
    public int getStatus(){
        return Status;
    }
    
    public String getAddressFrom(){
        return address_from;
    }
    
    public String getAddressTo(){
        return address_to;
    }
    
    public String getMessage(){
        return text;
    }
    
    public void setTime( long time){
        Time = time;
    }
    
    public void setStatus(int status){
        this.Status = status;
    }
    
    private static String getSubject(String text){
        int start = text.indexOf(SUBJECT);//+"Subject:".length();
        int end_string = text.indexOf("\n");
        if (start < 0) {
            return null;
        }
        start += SUBJECT.length();
        return text.substring(start, end_string);
    }
    
    private static String getBody(String text){
        int start = text.indexOf(BODY);//+"Subject:".length();

        if (start < 0) {
            return null;
        }
        start += BODY.length();
        return text.substring(start);
    }
    
    public String getSubject(){
        return PechkinMessage.this.getSubject(text);
    }
    
    public String getBody(){
        return getBody(text);
    }
    
    
}
