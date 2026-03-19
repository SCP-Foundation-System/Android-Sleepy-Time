package de.mikaharberts.sleepytime.data

enum class ThemeMode {
    SYSTEM,
    DARK,
    LIGHT,
}

data class AppPreferences(
    val onboardingCompleted: Boolean = false,
    val displayName: String? = null,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val lastImportedFileName: String? = null,
)
