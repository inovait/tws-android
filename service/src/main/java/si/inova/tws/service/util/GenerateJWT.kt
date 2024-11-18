/*
 * Copyright 2024 INOVA IT d.o.o.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
 *  is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 *   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package si.inova.tws.service.util

import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.json.webtoken.JsonWebSignature
import com.google.api.client.json.webtoken.JsonWebToken
import com.google.api.client.util.SecurityUtils
import si.inova.tws.service.data.ServiceAccount
import java.security.PrivateKey

internal fun generateJWT(serviceAccount: ServiceAccount): String {
    // Parse the private key from the PEM format
    val privateKey: PrivateKey = SecurityUtils.loadPrivateKeyFromKeyStore(
        SecurityUtils.getPkcs12KeyStore(),
        serviceAccount.privateKey.byteInputStream(),
        "",
        "",
        ""
    )

    // Create the JWT header
    val header = JsonWebSignature.Header()
        .setAlgorithm("RS256")
        .setKeyId(serviceAccount.privateKeyId)

    // Create the JWT payload
    val payload = JsonWebToken.Payload()
        .setIssuer(serviceAccount.clientId)
        .set("client_id", serviceAccount.clientId)

    val jsonFactory: JsonFactory = GsonFactory.getDefaultInstance()

    // Sign the JWT
    return JsonWebSignature.signUsingRsaSha256(privateKey, jsonFactory, header, payload)
}
