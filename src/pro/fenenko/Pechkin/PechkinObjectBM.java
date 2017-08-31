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

import pro.fenenko.Bitmessage.BMUtils;

/**
 *
 * @author Fenenko Aleksandr
 */
 public   class PechkinObjectBM{
        private static BMUtils utils = new BMUtils();
        byte[] object;
        byte[] inv;
        public PechkinObjectBM(byte[] object){
            inv = utils.createInv(object);
            this.object = object;
        }
        
        public byte[] getInv(){
            return inv;
        }
        public byte[] getObject(){
            return object;
        }
        
        public boolean isObject(byte[] testInv){
            boolean ret = true;
            for(int i = 0;(i < 32)&&ret;i++){
                if(inv[i]!=testInv[i])
                    ret = false;
            }
            return ret;
        }
    }