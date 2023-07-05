package com.testing_auth

import com.nimbusds.jose.*
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.util.Base64URL
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File
import java.sql.Timestamp
import java.time.Instant
import java.util.Date


@MicronautTest
class HelloControllerTest(private val embeddedServer: EmbeddedServer) {

    @Test
    fun testServerIsRunning() {
        assert(embeddedServer.isRunning())
    }

    @Test
    fun testIndex() {
        val applicationContext = embeddedServer.applicationContext
        val client: HttpClient = applicationContext.createBean(HttpClient::class.java, embeddedServer.url)
        val blockingHttpClient = client.toBlocking()
        val uri = "/hello"
        val file = File("certs.json")
        val localKeys = JWKSet.load(file)
        println(localKeys.keys[0].keyID)
        val rsaJWK = localKeys.keys[0].toRSAKey()
        val rsaPublicJWK: RSAKey = rsaJWK.toPublicJWK()
        val signer = RSASSASigner(rsaJWK)
        var jwsObject = JWSObject(
            JWSHeader.Builder(JWSAlgorithm.RS256).type(JOSEObjectType("JWT")).keyID(rsaJWK.keyID).build(),
            Payload(Base64URL("eyJleHAiOjE2ODc3MjA0MjcsImlhdCI6MTY4NzcyMDEyNywiYXV0aF90aW1lIjoxNjg3NzE4NDUzLCJqdGkiOiJlMGI1ZWQ5OC0xNzI0LTQxOGUtOTdhNy0xOWMyYWE4NDBlMjgiLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvcmVhbG1zL3NhZGhhayIsImF1ZCI6ImFjY291bnQiLCJzdWIiOiIyZjM5YWQzMC00NmQ5LTRiMWQtOWVmNS00MzU2OGViZGZhZjkiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJwb3N0bWFuIiwic2Vzc2lvbl9zdGF0ZSI6Ijk0NGIyZDJmLWYwYTktNGExOC1iMmZkLTkwMjI2NDYwNjcyYSIsImFjciI6IjEiLCJhbGxvd2VkLW9yaWdpbnMiOlsiaHR0cDovL2xvY2FsaG9zdDo4MDgwIl0sInJlc291cmNlX2FjY2VzcyI6eyJhY2NvdW50Ijp7InJvbGVzIjpbIm1hbmFnZS1hY2NvdW50IiwibWFuYWdlLWFjY291bnQtbGlua3MiLCJ2aWV3LXByb2ZpbGUiXX19LCJzY29wZSI6Im9wZW5pZCBwcm9maWxlIGVtYWlsIiwic2lkIjoiOTQ0YjJkMmYtZjBhOS00YTE4LWIyZmQtOTAyMjY0NjA2NzJhIiwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJyb2xlcyI6WyJvZmZsaW5lX2FjY2VzcyIsImFkbWluIiwidW1hX2F1dGhvcml6YXRpb24iLCJkZWZhdWx0LXJvbGVzLXNhZGhhayJdLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJoYXJzaCIsImdpdmVuX25hbWUiOiIiLCJmYW1pbHlfbmFtZSI6IiJ9"))
        )
        jwsObject.sign(signer);
        val accessToken = jwsObject.serialize()
        println(accessToken)
        jwsObject = JWSObject.parse(accessToken)

        val verifier: JWSVerifier = RSASSAVerifier(rsaPublicJWK)

        assertTrue(jwsObject.verify(verifier))
        val httpRequest = HttpRequest.GET<String>(uri).bearerAuth(accessToken)
        val httpResponse = blockingHttpClient.retrieve(httpRequest)
        val status = httpResponse
        assertEquals("Example Response", status)
        println(status)
        client.close()
    }
}
