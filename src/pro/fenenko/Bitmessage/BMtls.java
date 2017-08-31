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

import org.bouncycastle.crypto.tls.CertificateRequest;
import org.bouncycastle.crypto.tls.CipherSuite;
import org.bouncycastle.crypto.tls.CompressionMethod;
import org.bouncycastle.crypto.tls.DefaultTlsClient;
import org.bouncycastle.crypto.tls.ProtocolVersion;
import org.bouncycastle.crypto.tls.TlsAuthentication;
import org.bouncycastle.crypto.tls.TlsCredentials;

import java.io.IOException;

/**
 *
 * @author Fenenko Aleksandr
 */

public class BMtls
        extends DefaultTlsClient {

    protected int[] nameCurves = {714};
    protected int  selectedCipherSuite = CipherSuite.TLS_ECDH_anon_WITH_AES_256_CBC_SHA ;
    protected int selectedCompressionMethod = CompressionMethod._null;

    public ProtocolVersion getClientVersion(){
        return ProtocolVersion.TLSv10;
    }

    public int[] getCipherSuites(){
        int[] t = {CipherSuite.TLS_ECDH_anon_WITH_AES_256_CBC_SHA };
        return t;
    }

    public TlsAuthentication getAuthentication() throws IOException {
        TlsAuthentication auth = new TlsAuthentication() {
            public void notifyServerCertificate(
                    org.bouncycastle.crypto.tls.Certificate serverCertificate)
                    throws IOException {

            }

            public TlsCredentials getClientCredentials(
                    CertificateRequest certificateRequest) throws IOException {

                return null;
            }
        };
        return auth;
    }

}
