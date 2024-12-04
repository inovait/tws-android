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
        val serviceAccount = ServiceAccount(
            privateKey = "-----BEGIN PRIVATE KEY-----\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDO6d/Isc/PLwQP\nOYStfIMQyXmD13lJH31cqvSHkNkuJ5/x6LgT/JZ5ibs6tAknJD8EAxO+yDBZo9sj\nmekoX+f7tprEbCwr177iRoujTVGy6Cco3h13xBFOfttDb4tu43Dode4PUR8Zr+DA\nUmlAcgTui48prKZaRk5iI+ksY6nwROoc6URhfVNWXcE93EHgDm0sBcyEPNlFOi4o\n5U4EWAWrqqFU3BmPSj6r6CHQJKM3/LXLg7EIRz22YFn7OAqY1unGelb0GDbA08IZ\nv0+kc1Nf3/UHSPeI4ereQH/alaNhgaSnV3YlUmmolK8J06Sgka+ajmPgZNm6kEh1\n9bMdurZ5AgMBAAECggEAGyQRXx130UjtAuQ8E5ecB5Ut883NUnqncoLYLXoiCsxW\nziAFGVYOFOYsoYAhHTnDbE7NBb7znKaCJbLYIxwlbV6gAXjg+GVF3B99zI9lUo7i\nKBmuitm0nXPFZhyZERQTaOKjNe4horhvxjppJPNv0yyKPGCxt3C2cmgce16dWW8s\nVcNfaHIlBpny4/FnLzMpe626iy5AqtTw4ZLWTqXzE832A3moqDdtuhI18Ndk/sfk\nK6u2yREXyxRauj4shO8ax4+tGN/CyEtWLEqwNXZePxL90Clze2RiDo3qsSuRgPxB\nN2lIfmPmZ48bHX48X3BVM33n3ciLFVR6CeCW8CNdiwKBgQDpce/lsptJHX1zyz/H\nrhs3MqkXU2A5UDL2wTkKQEB+nbYQwlQMNCei3DfrNI20tMEQkBhxLgOf8FD+NLFQ\nMy90l0Tj30n1c6PK/Ov8L7blgylq3G+oVb8LIoZGS1zZj9KOQ37IvMilZ/60UYcy\n+c431luBs37Fh9xMmbqV1m2+fwKBgQDi57QNhjt4AxhVQM+ig3nl+dyxlxniy3uD\noBVzHa0S5ErmrpBTsvPbPeOzql16FY7pGQ6INWSpMGIuY3eufzdYosE65o/vyw/g\n97lq8pUKGjNk1YmVz89+IfYiTI1yS5vQZzJU4ik6Ndmag5utLPGySdikCOIQCF6f\n58RFBwb/BwKBgQDaHwaLNfZYO8DDWA6xEaIGUkSiMdo/0wq7etFEHcLlS5EZWanU\neUrrph5tm34ocZfPVQQxhFAIui/DRhLDIABgTRMmY7UkLbfGa8pHqYGYM5SYb324\n6N2HluuldLc95V9UZAZro9FKk7/uVhsgaz7e92Aq8T8rbAVQYXdwl5mVGwKBgEN+\nNFqJgQrzsLs7KgfnH80+g1z32yVQ7Y57gaXaP+8MpRL0/c1Hv62JPT/l/yQfYYEI\niQAPEcAZujqeL61h/e6JivygNFRZjIGYbjjXq2lz3bGyqtg7FjcolvQg3ToQL2rQ\nJ3KES0sbVXzWhOoAImyr9prkYYnvU2YxJHp72qLlAoGAfr8dreHLnVd8aJ1afbhU\nmLpDmyS8tyTjM0oM4HFv6RBQl+homxM0GE420AjzyfyRuQS5kCfm25EsQ2JPfBBs\nBjail51APLNkknt86JsH67Ck3ZS+3hH/Q5Ympw2853sYQbZRX9Qc4p9FV/2xdLiX\nkq3OZFnQSV+oB8WFevf9QC8=\n-----END PRIVATE KEY-----",
            privateKeyId = "test-key-id",
            clientId = "test-client-id",
            organizationId = "test ODG"
        )

        // Act
        val jwt = generateJWT(serviceAccount)

        // Parse the JWT to validate its contents
        val parsedJwt = JsonWebSignature.parse(GsonFactory.getDefaultInstance(), jwt)

        // Assert
        assertEquals("RS256", parsedJwt.header.algorithm)
        assertEquals("test-key-id", parsedJwt.header.keyId)
        assertEquals("test-client-id", parsedJwt.payload.issuer)
        assertEquals("test-client-id", parsedJwt.payload["client_id"])
    }
}
