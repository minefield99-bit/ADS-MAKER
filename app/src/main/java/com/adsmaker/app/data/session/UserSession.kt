package com.adsmaker.app.data.session

/**
 * Placeholder for the future account system. Week 1 has no login, but every part
 * of the app that will eventually be per-user (usage logs, billing, quotas)
 * already routes through a [UserSession] so adding real accounts later is
 * additive, not a rewrite.
 */
data class UserSession(
    val userId: String,
    val isAnonymous: Boolean,
) {
    companion object {
        /** The single local, logged-out user used throughout Week 1. */
        val ANONYMOUS = UserSession(userId = "local-anonymous", isAnonymous = true)
    }
}

/**
 * Supplies the current session. Backed by a constant today; swap for a real
 * auth-backed implementation later without touching callers.
 */
interface SessionProvider {
    fun current(): UserSession
}

class LocalSessionProvider : SessionProvider {
    override fun current(): UserSession = UserSession.ANONYMOUS
}
