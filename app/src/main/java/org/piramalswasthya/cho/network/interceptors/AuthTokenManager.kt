package org.piramalswasthya.cho.network.interceptors

import java.util.concurrent.atomic.AtomicReference

/**
 * Thread-safe token holder for the network interceptors.
 *
 * The app updates these values from multiple workers and screens, so the
 * storage needs to be safe for concurrent reads and writes.
 */
object AuthTokenManager {

    data class TmcCredentials(
        val token: String = "",
        val jwt: String = ""
    )

    private val tmcCredentials = AtomicReference(TmcCredentials())
    private val abhaToken = AtomicReference("")
    private val abhaXToken = AtomicReference("")
    private val eSanjeevaniToken = AtomicReference("")

    fun setTmcCredentials(token: String, jwt: String) {
        tmcCredentials.set(TmcCredentials(token, jwt))
    }

    fun getTmcCredentials(): TmcCredentials = tmcCredentials.get()

    fun setAbhaToken(token: String?) {
        abhaToken.set(token.orEmpty())
    }

    fun getAbhaToken(): String = abhaToken.get()

    fun setAbhaXToken(token: String?) {
        abhaXToken.set(token.orEmpty())
    }

    fun getAbhaXToken(): String = abhaXToken.get()

    fun setESanjeevaniToken(token: String) {
        eSanjeevaniToken.set(token)
    }

    fun getESanjeevaniToken(): String = eSanjeevaniToken.get()

    fun clear() {
        tmcCredentials.set(TmcCredentials())
        abhaToken.set("")
        abhaXToken.set("")
        eSanjeevaniToken.set("")
    }
}
