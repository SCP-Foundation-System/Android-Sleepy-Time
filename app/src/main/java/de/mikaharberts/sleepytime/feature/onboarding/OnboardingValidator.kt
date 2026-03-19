package de.mikaharberts.sleepytime.feature.onboarding

object OnboardingValidator {
    const val maxNameLength: Int = 25

    fun sanitizeName(input: String): String = input.trim().take(maxNameLength)

    fun isNameValid(input: String): Boolean = sanitizeName(input).isNotEmpty()
}
