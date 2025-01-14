/*
 * Copyright 2024 INOVA IT d.o.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.thewebsnippet.service.task

import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.json.webtoken.JsonWebSignature
import com.google.api.client.json.webtoken.JsonWebToken
import com.google.api.client.util.PemReader
import com.thewebsnippet.service.data.ServiceAccount
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
        .setAlgorithm(ALG_RS)
        .setKeyId(serviceAccount.privateKeyId)

    val payload = JsonWebToken.Payload()
        .setIssuer(serviceAccount.clientId)
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

private const val EXP_TIME_SECONDS = 9_999_999_999_999
private const val ALG_RS = "RS256"
private const val KEY_RSA = "RSA"
