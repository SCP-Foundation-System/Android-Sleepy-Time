package de.mikaharberts.sleepytime.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private const val DATASTORE_NAME = "sleepy_time_preferences"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = DATASTORE_NAME)

class AppPreferencesRepository(private val context: Context) {

    val preferences: Flow<AppPreferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs ->
            AppPreferences(
                onboardingCompleted = prefs[Keys.onboardingCompleted] ?: false,
                displayName = prefs[Keys.displayName],
                themeMode = prefs[Keys.themeMode]
                    ?.let(ThemeMode::valueOf)
                    ?: ThemeMode.SYSTEM,
                lastImportedFileName = prefs[Keys.lastImportedFileName],
            )
        }

    suspend fun completeOnboarding(
        displayName: String?,
        themeMode: ThemeMode,
        importedFileName: String?,
    ) {
        context.dataStore.edit { prefs ->
            prefs[Keys.onboardingCompleted] = true
            if (displayName.isNullOrBlank()) {
                prefs.remove(Keys.displayName)
            } else {
                prefs[Keys.displayName] = displayName
            }
            prefs[Keys.themeMode] = themeMode.name
            if (importedFileName.isNullOrBlank()) {
                prefs.remove(Keys.lastImportedFileName)
            } else {
                prefs[Keys.lastImportedFileName] = importedFileName
            }
        }
    }
}

private object Keys {
    val onboardingCompleted = booleanPreferencesKey("onboarding_completed")
    val displayName = stringPreferencesKey("display_name")
    val themeMode = stringPreferencesKey("theme_mode")
    val lastImportedFileName = stringPreferencesKey("last_imported_file_name")
}
