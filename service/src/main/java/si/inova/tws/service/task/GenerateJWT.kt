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

package si.inova.tws.service.task

import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.json.webtoken.JsonWebSignature
import com.google.api.client.json.webtoken.JsonWebToken
import com.google.api.client.util.PemReader
import si.inova.tws.service.data.ServiceAccount
import java.io.StringReader
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.spec.PKCS8EncodedKeySpec

/**
 * Generates a JSON Web Token (JWT) signed using the provided ServiceAccount's private key.
 * The JWT includes a header, payload, and a digital signature.
 *
 * @param serviceAccount The ServiceAccount object containing the private key and metadata
 * @return A signed JWT as a [String]
 */
internal fun generateJWT(serviceAccount: ServiceAccount): String {
    val privateKey: PrivateKey = loadPrivateKeyFromPEM(serviceAccount.privateKey)

    val header = JsonWebSignature.Header()
        .setAlgorithm(ALG_RSA)
        .setKeyId(serviceAccount.privateKeyId)

    val payload = JsonWebToken.Payload()
        .setIssuer(serviceAccount.clientId)
        .set(PROPERTY_CLIENT, serviceAccount.clientId)
        .setExpirationTimeSeconds(EXP_TIME_SECONDS)

    val jsonFactory: JsonFactory = GsonFactory.getDefaultInstance()

    return JsonWebSignature.signUsingRsaSha256(privateKey, jsonFactory, header, payload)
}

/**
 * Reads a PEM-encoded RSA private key from a string and converts it to a PrivateKey object.
 *
 * @param pemKey The PEM-encoded RSA private key as a String
 * @return The RSA private key as a PrivateKey object
 */
private fun loadPrivateKeyFromPEM(pemKey: String): PrivateKey {
    val reader = PemReader(StringReader(pemKey))
    val section = reader.readNextSection()
    val keyBytes = section.base64DecodedBytes

    val keySpec = PKCS8EncodedKeySpec(keyBytes)
    val keyFactory = KeyFactory.getInstance(KEY_RSA)

    return keyFactory.generatePrivate(keySpec)
}

private const val EXP_TIME_SECONDS = 9999999999999
private const val ALG_RSA = "RSA256"
private const val KEY_RSA = "RSA"
private const val PROPERTY_CLIENT = "client_id"
