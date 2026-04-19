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
        AuthTokenManager.setTmcToken("tmc-token")
        AuthTokenManager.setTmcJwt("tmc-jwt")
        AuthTokenManager.setAbhaToken("abha-token")
        AuthTokenManager.setAbhaXToken("abha-x-token")
        AuthTokenManager.setESanjeevaniToken("esanjeevani-token")

        assertEquals("tmc-token", AuthTokenManager.getTmcToken())
        assertEquals("tmc-jwt", AuthTokenManager.getTmcJwt())
        assertEquals("abha-token", AuthTokenManager.getAbhaToken())
        assertEquals("abha-x-token", AuthTokenManager.getAbhaXToken())
        assertEquals("esanjeevani-token", AuthTokenManager.getESanjeevaniToken())
    }

    @Test
    fun concurrentWritesRemainReadable() {
        val executor = Executors.newFixedThreadPool(4)
        val ready = CountDownLatch(1)
        val tasks = (0 until 32).map { index ->
            executor.submit {
                ready.await(5, TimeUnit.SECONDS)
                AuthTokenManager.setTmcToken("tmc-$index")
                AuthTokenManager.setTmcJwt("jwt-$index")
                AuthTokenManager.setAbhaToken("abha-$index")
                AuthTokenManager.setAbhaXToken("x-$index")
                AuthTokenManager.setESanjeevaniToken("es-$index")
            }
        }

        ready.countDown()
        tasks.forEach { it.get(5, TimeUnit.SECONDS) }
        executor.shutdown()
        executor.awaitTermination(5, TimeUnit.SECONDS)

        assertTrue(AuthTokenManager.getTmcToken().startsWith("tmc-"))
        assertTrue(AuthTokenManager.getTmcJwt().startsWith("jwt-"))
        assertTrue(AuthTokenManager.getAbhaToken().startsWith("abha-"))
        assertTrue(AuthTokenManager.getAbhaXToken().startsWith("x-"))
        assertTrue(AuthTokenManager.getESanjeevaniToken().startsWith("es-"))
    }
}
