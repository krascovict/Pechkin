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

public class BMPubKey {
    public String address;
    public byte[] key;
    public int pow1;
    public int pow2;
    public BMPubKey(String address,byte[] key,int pow1,int pow2){
        this.address = address;
        this.key = key;
        this.pow1 = pow1;
        this.pow2 = pow2;
    }
}
