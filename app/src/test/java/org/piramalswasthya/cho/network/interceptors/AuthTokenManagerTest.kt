package org.piramalswasthya.cho.network.interceptors

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class AuthTokenManagerTest {

    @Before
    fun setUp() {
        AuthTokenManager.clear()
    }

    @After
    fun tearDown() {
        AuthTokenManager.clear()
    }

    @Test
    fun storesValuesIndependentlyForEachInterceptor() {
        AuthTokenManager.setTmcCredentials("tmc-token", "tmc-jwt")
        AuthTokenManager.setAbhaToken("abha-token")
        AuthTokenManager.setAbhaXToken("abha-x-token")
        AuthTokenManager.setESanjeevaniToken("esanjeevani-token")

        assertEquals("tmc-token", AuthTokenManager.getTmcCredentials().token)
        assertEquals("tmc-jwt", AuthTokenManager.getTmcCredentials().jwt)
        assertEquals("abha-token", AuthTokenManager.getAbhaToken())
        assertEquals("abha-x-token", AuthTokenManager.getAbhaXToken())
        assertEquals("esanjeevani-token", AuthTokenManager.getESanjeevaniToken())
    }

    @Test
    fun concurrentWritesRemainReadable() {
        val executor = Executors.newFixedThreadPool(4)
        try {
            val ready = CountDownLatch(1)
            val tasks = (0 until 32).map { index ->
                executor.submit {
                    check(ready.await(5, TimeUnit.SECONDS))
                    AuthTokenManager.setTmcCredentials("tmc-$index", "jwt-$index")
                    AuthTokenManager.setAbhaToken("abha-$index")
                    AuthTokenManager.setAbhaXToken("x-$index")
                    AuthTokenManager.setESanjeevaniToken("es-$index")
                }
            }

            ready.countDown()
            tasks.forEach { it.get(5, TimeUnit.SECONDS) }
        } finally {
            executor.shutdownNow()
            executor.awaitTermination(5, TimeUnit.SECONDS)
        }

        val tmcCredentials = AuthTokenManager.getTmcCredentials()
        assertTrue(tmcCredentials.token.startsWith("tmc-"))
        assertTrue(tmcCredentials.jwt.startsWith("jwt-"))
        assertEquals(
            tmcCredentials.token.substringAfter('-'),
            tmcCredentials.jwt.substringAfter('-')
        )
        assertTrue(AuthTokenManager.getAbhaToken().startsWith("abha-"))
        assertTrue(AuthTokenManager.getAbhaXToken().startsWith("x-"))
        assertTrue(AuthTokenManager.getESanjeevaniToken().startsWith("es-"))
    }
}
