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

package com.thewebsnippet.service.task

import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.json.webtoken.JsonWebSignature
import com.thewebsnippet.service.data.ServiceAccount
import groovy.test.GroovyTestCase.assertEquals
import org.junit.Test

class GenerateJWTTest {

    @Test
    fun `generateJWT should create a valid JWT with correct header and payload`() {
        val serviceAccount = testService

        // Act
        val jwt = generateJWT(serviceAccount)

        // Parse the JWT to validate its contents
        val parsedJwt = JsonWebSignature.parse(GsonFactory.getDefaultInstance(), jwt)

        // Assert
        assertEquals("RS256", parsedJwt.header.algorithm)
        assertEquals("test-key-id", parsedJwt.header.keyId)
        assertEquals("test-client-id", parsedJwt.payload.issuer)
        assertEquals("test-client", parsedJwt.payload["client_id"])
    }
}

private val testService = ServiceAccount(
    privateKey = """-----BEGIN PRIVATE KEY-----
MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDO6d/Isc/PLwQP
OYStfIMQyXmD13lJH31cqvSHkNkuJ5/x6LgT/JZ5ibs6tAknJD8EAxO+yDBZo9sj
mekoX+f7tprEbCwr177iRoujTVGy6Cco3h13xBFOfttDb4tu43Dode4PUR8Zr+DA
UmlAcgTui48prKZaRk5iI+ksY6nwROoc6URhfVNWXcE93EHgDm0sBcyEPNlFOi4o
5U4EWAWrqqFU3BmPSj6r6CHQJKM3/LXLg7EIRz22YFn7OAqY1unGelb0GDbA08IZ
v0+kc1Nf3/UHSPeI4ereQH/alaNhgaSnV3YlUmmolK8J06Sgka+ajmPgZNm6kEh1
9bMdurZ5AgMBAAECggEAGyQRXx130UjtAuQ8E5ecB5Ut883NUnqncoLYLXoiCsxW
ziAFGVYOFOYsoYAhHTnDbE7NBb7znKaCJbLYIxwlbV6gAXjg+GVF3B99zI9lUo7i
KBmuitm0nXPFZhyZERQTaOKjNe4horhvxjppJPNv0yyKPGCxt3C2cmgce16dWW8s
VcNfaHIlBpny4/FnLzMpe626iy5AqtTw4ZLWTqXzE832A3moqDdtuhI18Ndk/sfk
K6u2yREXyxRauj4shO8ax4+tGN/CyEtWLEqwNXZePxL90Clze2RiDo3qsSuRgPxB
N2lIfmPmZ48bHX48X3BVM33n3ciLFVR6CeCW8CNdiwKBgQDpce/lsptJHX1zyz/H
rhs3MqkXU2A5UDL2wTkKQEB+nbYQwlQMNCei3DfrNI20tMEQkBhxLgOf8FD+NLFQ
My90l0Tj30n1c6PK/Ov8L7blgylq3G+oVb8LIoZGS1zZj9KOQ37IvMilZ/60UYcy
+c431luBs37Fh9xMmbqV1m2+fwKBgQDi57QNhjt4AxhVQM+ig3nl+dyxlxniy3uD
oBVzHa0S5ErmrpBTsvPbPeOzql16FY7pGQ6INWSpMGIuY3eufzdYosE65o/vyw/g
97lq8pUKGjNk1YmVz89+IfYiTI1yS5vQZzJU4ik6Ndmag5utLPGySdikCOIQCF6f
58RFBwb/BwKBgQDaHwaLNfZYO8DDWA6xEaIGUkSiMdo/0wq7etFEHcLlS5EZWanU
eUrrph5tm34ocZfPVQQxhFAIui/DRhLDIABgTRMmY7UkLbfGa8pHqYGYM5SYb324
6N2HluuldLc95V9UZAZro9FKk7/uVhsgaz7e92Aq8T8rbAVQYXdwl5mVGwKBgEN+
NFqJgQrzsLs7KgfnH80+g1z32yVQ7Y57gaXaP+8MpRL0/c1Hv62JPT/l/yQfYYEI
iQAPEcAZujqeL61h/e6JivygNFRZjIGYbjjXq2lz3bGyqtg7FjcolvQg3ToQL2rQ
J3KES0sbVXzWhOoAImyr9prkYYnvU2YxJHp72qLlAoGAfr8dreHLnVd8aJ1afbhU
mLpDmyS8tyTjM0oM4HFv6RBQl+homxM0GE420AjzyfyRuQS5kCfm25EsQ2JPfBBs
Bjail51APLNkknt86JsH67Ck3ZS+3hH/Q5Ympw2853sYQbZRX9Qc4p9FV/2xdLiX
kq3OZFnQSV+oB8WFevf9QC8=
-----END PRIVATE KEY-----""",
    privateKeyId = "test-key-id",
    clientId = "test-client-id",
    organizationId = "test ODG"
)
