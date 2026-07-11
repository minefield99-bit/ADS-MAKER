package com.adsmaker.app.domain

/**
 * The signature visual style the product owner asked for. Encapsulated so it can
 * be tweaked or offered as presets later without touching prompt-building logic.
 */
data class AdStyle(
    val transitionDescription: String,
    val moodDescription: String,
) {
    companion object {
        val DEFAULT = AdStyle(
            transitionDescription =
                "After about 2-3 seconds, the shot smoothly pushes back and the product " +
                    "gently defocuses into a soft, blurred background — a rack-focus / depth-of-field pull.",
            moodDescription =
                "Cinematic, premium and confident with warm, vibrant lighting.",
        )
    }
}
