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

public class BMNodeAddress {
    private String ip;
    private int port;
    private int stream;
    private long service;
    private long time;
    private int countConnect;
    private int id;
    public BMNodeAddress(String ip,int port){
        this.ip = ip;
        this.port = port;
        time = System.currentTimeMillis()/1000L;
        stream = 1;
        service = 1;
    }
    public BMNodeAddress(String ip,int port,long time){
        this.ip = ip;
        this.port = port;
        this.time = time;
        countConnect = 0;
        stream = 1;
        service = 1;
    }
    public BMNodeAddress(String ip,int port,long time,long service){
        this.ip = ip;
        this.port = port;
        this.time = time;
        countConnect = 0;
        stream = 1;
        this.service = service;
        id = -1;
    }
    public BMNodeAddress(String ip,int port,long time,int connect){
        this.ip = ip;
        this.port = port;
        this.time = time;
        countConnect = connect;
        stream = 1;
        service = 1;
    }
    public BMNodeAddress(String ip,int port,int id){
        this.ip = ip;
        this.port = port;
        time = System.currentTimeMillis()/1000L;
        stream = 1;
        this.id = id;
        service = 1;
    }
    public BMNodeAddress(String ip,int port,int id,long time){
        this.ip = ip;
        this.port = port;
        this.time = time;
        countConnect = 0;
        stream = 1;
        service = 1;
        this.id = id;
    }
    public BMNodeAddress(String ip,int port,int id,long time,int connect){
        this.ip = ip;
        this.port = port;
        this.time = time;
        countConnect = connect;
        stream = 1;
        service = 1;
        this.id = id;
    }
    public int getId(){
        return id;
    }
    
    public void setId(int id){
        this.id = id;
    }
    
    public String getIp(){
        return ip;
    }
    
    public void setTime(long time){
        this.time = time;
    }
    
    public void setCountConnect(int value){
        countConnect = value;
    }
    
    public int getCountConnect(){
        return countConnect;
    }
    
    public int getPort(){
        return port;
    }
    public long getTime(){
        return time;
    }
    public long getService(){
        return service;
    }
    
    public int getStream(){
        return stream;
    }

    public boolean isAddress(String ip,int port)
    {
        return this.ip.contains(ip)&&(this.port == port);
    }

    public boolean isAddress(BMNodeAddress address)
    {
        return this.ip.contains(address.ip)&&(this.port == address.port);
    }

    public String toString(){
        return ip+" "+String.valueOf(port);
    }

    @Deprecated
    public BMNodeAddress(){
        ip = "";
        port = 0;

    }
}
