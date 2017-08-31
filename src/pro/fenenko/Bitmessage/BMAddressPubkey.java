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

public class BMAddressPubkey  {
    public byte[] signing;
    public byte[] cipher;
    public int version;
    public int stream;






    public BMAddressPubkey(int version ,int stream,byte[] key_s,byte[] key_c){
        signing = key_s;
        cipher = key_c;
        this.stream = stream;
        this.version = version;

    }




    public byte[] getPublicSigningKey(){
        return signing;
    }
    public byte[] getPublicCipherKey(){
        return cipher;
    }

    public String getAddress(){
        BMUtils utils = new BMUtils();
        String ret = (new BMAddressUtils()).getBMAddress(version,stream
                ,(new BMAddressUtils()).getRipe(signing,cipher));
        utils = null;

        return ret;
    }
}
