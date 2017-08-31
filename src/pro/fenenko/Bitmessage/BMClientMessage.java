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

public class BMClientMessage implements  BMConstant {
    int id;
    int type;
    byte[] buff;
    BMNodeAddress[] address;

    public BMClientMessage(){

    }
    public BMClientMessage(int id,int type ,byte[] buff){
        this.id = id;
        this.type = type;
        this.buff = buff;
        address = null;
        if(buff == null)
            this.type = PACKET_NULL;
    }
    
    

    public BMClientMessage(int id,int type ){
        this.id = id;
        this.type = type;

    }
    public BMClientMessage(int id,BMNodeAddress[] addresses){
        this.address = addresses;
        this.id = id;
        this.type = PACKET_ADDRESS;
        if(address == null)
            this.type = PACKET_NULL;
    }
    /*
    public void setMessage(int id,int type ,Object object){
        this.id = id;
        this.type = type;
        this.object = object;
        if(object == null)
            this.type = PACKET_NULL;
    }

    public void setMessage(int id,int type ){
        this.id = id;
        this.type = type;

    }
    public void setMessage(int id,BMNodeAddress[] addresses){
        this.object = addresses;
        this.id = id;
        this.type = PACKET_ADDRESS;
    }
*/

    public int getID(){
        return id;
    }
    public int getType(){
        return type;
    }
/*
    public BMNodeAddress[] getAddress(){
        if(type != PACKET_ADDRESS) {
            BMLog.LogE("Pechkin.BMClientMessage","error not PACKET_ADDRESS "+type);
            return null;
        }
        return (BMNodeAddress[])object;
    }
    */
/*
    public byte[] getObject(){
        if(type != PACKET_OBJECT)
            return null;
        return (byte[])object;
    }
    public byte[] getInv(){
        if(type != PACKET_INV)
            return null;
        return (byte[])object;
    }
    public byte[] getData(){
        if(type != PACKET_GETDATA)
            return null;
        return (byte[])object;
    }
*/

}
