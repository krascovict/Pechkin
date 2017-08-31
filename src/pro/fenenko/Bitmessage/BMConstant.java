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

public interface  BMConstant {
    public static final byte[] MAGICK = {-23, -66, -76, -39};
    public static final String USER_AGENT = "/Pechkin:v0.3.0/";
    public static final int PROTOCOL_VERSION = 3;
    public static final int SERVICES = 3;
    public static final int PACKET_VERSION  = 1;
    public static final int PACKET_VERACK  = 2;
    public static final int PACKET_INV = 3;
    public static final int PACKET_GETDATA = 4;
    public static final int PACKET_OBJECT = 5;
    public static final int PACKET_ADDRESS = 10;
    public static final int PACKET_PING = 11;
    public static final int PACKET_PONG = 12;
    public static final int PACKET_NOTMAGIK = -1;
    public static final int PACKET_NOTCRC = -2;
    public static final int PACKET_NOTDATA = -3;
    public static final int PACKET_NULL = -4;
    public static final int PACKET_OBJECT_GETPUBKEY = 0;
    public static final int PACKET_OBJECT_PUBKEY = 1;
    public static final int PACKET_OBJECT_MSG = 2;
    public static final int PACKET_OBJECT_BROADCAST = 3;
    public static final int PACKET_OBJECT_MSG_ACK = 10;
    public static final int PACKET_OBJECT_UNKNOW = -1;
    //public static final int PACKET_OBJECT_NOOBJECT  = 100;

    public static final int VALUE_LEN_ARRAY = 100;
    public static final String ADDRESS_BROADCAST = "SUBSCRIPTION";
    public static final int CLIENT_RECEIV_ADDRESS = PACKET_ADDRESS;
    public static final int CLIENT_RECEIV_INV = PACKET_INV;
    public static final int CLIENT_RECEIV_GETDATA = PACKET_GETDATA;
    public static final int CLIENT_RECEIV_OBJECT = PACKET_OBJECT;
    public static final int CLIENT_NOT_DATA     = PACKET_NOTDATA;
    public static final int CLIENT_DISCONNECT = 20;

    public static final int CLIENT_STATUS_CONNECT = 21;
    public static final int CLIENT_STATUS_NOTCONNECT = 22;
    public static final int CLIENT_REQUEST_INV = 23;
    public static final int CLIENT_REQUEST_ADDRESS = 24;
    public static final int CLIENT_NOT_CONNECT      = 25;
    
    
    public static final int MESSAGE_RECEIV_UNREAD =   1000;
    public static final int MESSAGE_RECEIV_READ = 1100;
    public static final int MESSAGE_CREATE  = 1001;
    public static final int MESSAGE_FIND_PUBKEY = 1002;
    public static final int MESSAGE_SEND        = 1003;
    public static final int MESSAGE_DELIVERY    = 1004;



}