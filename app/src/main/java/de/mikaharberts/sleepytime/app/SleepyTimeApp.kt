package de.mikaharberts.sleepytime.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.mikaharberts.sleepytime.data.AppPreferences
import de.mikaharberts.sleepytime.data.AppPreferencesRepository
import de.mikaharberts.sleepytime.feature.main.MainScaffold
import de.mikaharberts.sleepytime.feature.onboarding.OnboardingFlow
import de.mikaharberts.sleepytime.ui.theme.SleepyTimeTheme
import kotlinx.coroutines.launch

private enum class RootDestination {
    ONBOARDING,
    MAIN,
}

@Composable
fun SleepyTimeApp() {
    val context = LocalContext.current.applicationContext
    val repository = remember(context) { AppPreferencesRepository(context) }
    val appPreferences by repository.preferences.collectAsStateWithLifecycle(initialValue = AppPreferences())
    val scope = rememberCoroutineScope()
    var currentDestination by remember(appPreferences.onboardingCompleted) {
        mutableStateOf(
            if (appPreferences.onboardingCompleted) RootDestination.MAIN else RootDestination.ONBOARDING,
        )
    }

    SleepyTimeTheme(themeMode = appPreferences.themeMode) {
        Surface(modifier = Modifier.fillMaxSize()) {
            when (currentDestination) {
                RootDestination.ONBOARDING -> OnboardingFlow(
                    initialThemeMode = appPreferences.themeMode,
                    onSetupCompleted = { name, themeMode, importedFileName ->
                        scope.launch {
                            repository.completeOnboarding(name, themeMode, importedFileName)
                            currentDestination = RootDestination.MAIN
                        }
                    },
                )

                RootDestination.MAIN -> MainScaffold(appPreferences = appPreferences)
            }
        }
    }
}
