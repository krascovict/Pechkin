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

import java.io.IOException;
import org.bouncycastle.crypto.tls.CipherSuite;
import org.bouncycastle.crypto.tls.DefaultTlsServer;
import org.bouncycastle.crypto.tls.ProtocolVersion;
import org.bouncycastle.util.Arrays;

/**
 *
 * @author Fenenko Aleksandr
 */
public class BMTlsServer extends DefaultTlsServer{
    protected int[] nameCurves = {714};
    private boolean handshakeComplate = false;
    
    public boolean getHandshakeComplate(){
        return handshakeComplate;
    }
    
    public void notifyHandshakeComplete(){
        handshakeComplate = true;
        System.out.println("Server handshake finish");
    }
    protected int[] getCipherSuites()
    {
        return Arrays.concatenate(super.getCipherSuites(),
            new int[]
            {
                CipherSuite.TLS_ECDH_anon_WITH_AES_256_CBC_SHA,
            });
    }

    protected ProtocolVersion getMaximumVersion()
    {
        return ProtocolVersion.TLSv10;
    }

    public ProtocolVersion getServerVersion() throws IOException
    {
        ProtocolVersion serverVersion = super.getServerVersion();
        return serverVersion;
    }
    
}
