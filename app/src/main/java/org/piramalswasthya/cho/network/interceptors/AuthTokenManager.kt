package org.piramalswasthya.cho.network.interceptors

import java.util.concurrent.atomic.AtomicReference

/**
 * Thread-safe token holder for the network interceptors.
 *
 * The app updates these values from multiple workers and screens, so the
 * storage needs to be safe for concurrent reads and writes.
 */
object AuthTokenManager {
    private val tmcToken = AtomicReference("")
    private val tmcJwt = AtomicReference("")
    private val abhaToken = AtomicReference("")
    private val abhaXToken = AtomicReference("")
    private val eSanjeevaniToken = AtomicReference("")

    fun setTmcToken(token: String) {
        tmcToken.set(token)
    }

    fun getTmcToken(): String = tmcToken.get()

    fun setTmcJwt(jwt: String) {
        tmcJwt.set(jwt)
    }

    fun getTmcJwt(): String = tmcJwt.get()

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
        tmcToken.set("")
        tmcJwt.set("")
        abhaToken.set("")
        abhaXToken.set("")
        eSanjeevaniToken.set("")
    }
}
