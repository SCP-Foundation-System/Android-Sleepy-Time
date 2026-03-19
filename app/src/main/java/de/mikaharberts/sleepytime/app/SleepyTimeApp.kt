package de.mikaharberts.sleepytime.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
    val appPreferences by repository.preferences.collectAsState(initial = AppPreferences())
    var startupIssue by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val currentDestination = if (appPreferences.onboardingCompleted) {
        RootDestination.MAIN
    } else {
        RootDestination.ONBOARDING
    }

    SleepyTimeTheme(themeMode = appPreferences.themeMode) {
        Surface(modifier = Modifier.fillMaxSize()) {
            when {
                startupIssue != null -> StartupFallbackScreen(message = startupIssue!!)
                currentDestination == RootDestination.ONBOARDING -> OnboardingFlow(
                    initialThemeMode = appPreferences.themeMode,
                    onSetupCompleted = { name, themeMode, importedFileName ->
                        scope.launch {
                            runCatching {
                                repository.completeOnboarding(name, themeMode, importedFileName)
                            }.onFailure {
                                startupIssue = "Die lokalen Einstellungen konnten nicht gespeichert werden. Die App startet vorerst mit einer sicheren Standardansicht."
                            }
                        }
                    },
                )

                else -> MainScaffold(appPreferences = appPreferences)
            }
        }
    }
}

@Composable
private fun StartupFallbackScreen(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Android-Sleepy-Time",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = message,
            modifier = Modifier.padding(top = 12.dp),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
