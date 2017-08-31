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

public class BMMessage {

    public String address_to;
    public String address_from;
    public String message;
    public byte[] ack;
    public byte[] pub_key;
    public int pow1;
    public int pow2;
    public long time;
    public BMMessage(String address_from,String address_to,String message,long time,byte[] ack,byte[] pub_key,int pow1,int pow2){
        this.address_from = address_from;
        this.address_to = address_to;
        this.message = message;
        this.ack = ack;
        this.pub_key = pub_key;
        this.pow1 = pow1;
        this.pow2 = pow2;
        this.time = time;

    }

    public BMMessage(String address_from,String address_to,String message,long time,byte[] pub_key){
        this.address_from = address_from;
        this.address_to = address_to;
        this.message = message;

        this.pub_key = pub_key;

        this.time = time;

    }

    public BMMessage(String address_from,String address_to,String message,int pow1,int pow2){
        this.address_from = address_from;
        this.address_to = address_to;
        this.message = message;
        this.pow1 = pow1;
        this.pow2 = pow2;


    }
    
    public BMMessage(String address_from,String address_to,String message,int pow1,int pow2,long time){
        this.address_from = address_from;
        this.address_to = address_to;
        this.message = message;
        this.pow1 = pow1;
        this.pow2 = pow2;
        this.time = time;


    }

    public BMMessage(String address_from,String address_to,String message){
        this.address_from = address_from;
        this.address_to = address_to;
        this.message = message;
        this.pow1 = 1000;
        this.pow2 = 1000;


    }
}
