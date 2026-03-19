package de.mikaharberts.sleepytime.feature.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.mikaharberts.sleepytime.data.AppPreferences

private enum class MainTab(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    Capture("Erfassen", Icons.Default.EditNote),
    Analysis("Analyse", Icons.Default.Analytics),
    Dreams("Träume", Icons.Default.AutoStories),
}

@Composable
fun MainScaffold(appPreferences: AppPreferences) {
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.Capture) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                MainTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(imageVector = tab.icon, contentDescription = tab.label)
                        },
                        label = { Text(tab.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        when (selectedTab) {
            MainTab.Capture -> CaptureScreen(innerPadding = innerPadding, displayName = appPreferences.displayName)
            MainTab.Analysis -> AnalysisScreen(innerPadding = innerPadding)
            MainTab.Dreams -> DreamsScreen(innerPadding = innerPadding)
        }
    }
}

@Composable
private fun CaptureScreen(
    innerPadding: PaddingValues,
    displayName: String?,
) {
    MainScreenFrame(innerPadding = innerPadding) {
        Text(
            text = displayName?.let { "Willkommen zurück, $it" } ?: "Erfassen",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Hier entsteht später der wichtigste Einstiegspunkt für neue Traumaufzeichnungen – mit schnellem oder ausführlichem Eintrag.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(28.dp))
        Button(onClick = { }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.AddCircleOutline, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Neuer Traum")
        }
    }
}

@Composable
private fun AnalysisScreen(innerPadding: PaddingValues) {
    MainScreenFrame(innerPadding = innerPadding) {
        Text(
            text = "Analyse",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Dieser Bereich ist als eigene Hauptansicht vorbereitet und kann später Muster, Triggerpunkte und visuelle Auswertungen aufnehmen.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DreamsScreen(innerPadding: PaddingValues) {
    MainScreenFrame(innerPadding = innerPadding) {
        Text(
            text = "Träume",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Hier wird später die Übersicht aller Traumdaten, Bearbeitung und die Erweiterung von Schnelleinträgen vorbereitet.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun MainScreenFrame(
    innerPadding: PaddingValues,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.Top,
        content = content,
    )
}
