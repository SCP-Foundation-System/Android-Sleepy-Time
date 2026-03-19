package de.mikaharberts.sleepytime.feature.onboarding

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.mikaharberts.sleepytime.data.ThemeMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private enum class OnboardingStep {
    Name,
    Theme,
    Import,
}

private data class OnboardingUiState(
    val displayName: String = "",
    val selectedThemeMode: ThemeMode = ThemeMode.SYSTEM,
    val importedFileName: String? = null,
)

private class OnboardingViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun initializeTheme(themeMode: ThemeMode) {
        _uiState.update { current ->
            if (current.selectedThemeMode == ThemeMode.SYSTEM) {
                current.copy(selectedThemeMode = themeMode)
            } else {
                current
            }
        }
    }

    fun updateDisplayName(value: String) {
        _uiState.update { current ->
            current.copy(displayName = value.take(OnboardingValidator.maxNameLength))
        }
    }

    fun updateTheme(themeMode: ThemeMode) {
        _uiState.update { current -> current.copy(selectedThemeMode = themeMode) }
    }

    fun updateImport(uri: Uri?) {
        _uiState.update { current ->
            current.copy(
                importedFileName = uri?.lastPathSegment
                    ?.substringAfterLast('/')
                    ?.takeIf { it.isNotBlank() },
            )
        }
    }
}

@Composable
fun OnboardingFlow(
    initialThemeMode: ThemeMode,
    onSetupCompleted: (String?, ThemeMode, String?) -> Unit,
) {
    val viewModel: OnboardingViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var currentStep by rememberSaveable { mutableStateOf(OnboardingStep.Name) }
    var showSkipDialog by rememberSaveable { mutableStateOf(false) }
    var showNameInfoDialog by rememberSaveable { mutableStateOf(false) }
    var showImportPlaceholder by rememberSaveable { mutableStateOf(false) }
    var showCompletionMessage by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun navigateBack() {
        currentStep = when (currentStep) {
            OnboardingStep.Name -> OnboardingStep.Name
            OnboardingStep.Theme -> OnboardingStep.Name
            OnboardingStep.Import -> OnboardingStep.Theme
        }
    }

    BackHandler(enabled = currentStep != OnboardingStep.Name) {
        navigateBack()
    }

    LaunchedEffect(initialThemeMode) {
        viewModel.initializeTheme(initialThemeMode)
    }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        viewModel.updateImport(uri)
        if (uri != null) {
            showImportPlaceholder = true
        }
    }

    when {
        showCompletionMessage -> SetupCompletionScreen(
            onFinished = {
                onSetupCompleted(
                    uiState.displayName.takeIf { OnboardingValidator.isNameValid(it) }
                        ?.let(OnboardingValidator::sanitizeName),
                    uiState.selectedThemeMode,
                    null,
                )
            },
        )

        showImportPlaceholder -> ImportPreparationScreen(
            importedFileName = uiState.importedFileName,
            onFinish = {
                onSetupCompleted(
                    uiState.displayName.takeIf { OnboardingValidator.isNameValid(it) }
                        ?.let(OnboardingValidator::sanitizeName),
                    uiState.selectedThemeMode,
                    uiState.importedFileName,
                )
            },
        )

        else -> Scaffold(
            topBar = {
                OnboardingTopBar(
                    canNavigateBack = currentStep != OnboardingStep.Name,
                    currentStep = currentStep,
                    onNavigateBack = ::navigateBack,
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        ) { innerPadding ->
            when (currentStep) {
                OnboardingStep.Name -> NameStepScreen(
                    modifier = Modifier.padding(innerPadding),
                    displayName = uiState.displayName,
                    onDisplayNameChange = viewModel::updateDisplayName,
                    onShowNameInfo = { showNameInfoDialog = true },
                    onContinue = { currentStep = OnboardingStep.Theme },
                    onSkip = { showSkipDialog = true },
                )

                OnboardingStep.Theme -> ThemeStepScreen(
                    modifier = Modifier.padding(innerPadding),
                    selectedThemeMode = uiState.selectedThemeMode,
                    onThemeSelected = viewModel::updateTheme,
                    onContinue = { currentStep = OnboardingStep.Import },
                )

                OnboardingStep.Import -> ImportStepScreen(
                    modifier = Modifier.padding(innerPadding),
                    importedFileName = uiState.importedFileName,
                    onImportClick = { importLauncher.launch(arrayOf("*/*")) },
                    onContinueWithoutImport = { showCompletionMessage = true },
                    onShowImportHint = {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Der Importfluss ist vorbereitet und kann später an eine vollständige Backup-Wiederherstellung angebunden werden.",
                            )
                        }
                    },
                )
            }
        }
    }

    if (showSkipDialog) {
        AlertDialog(
            onDismissRequest = { showSkipDialog = false },
            title = { Text("Namenseingabe überspringen?") },
            text = {
                Text(
                    "Möchtest du die Namenseingabe wirklich überspringen? Dein Name wird nur genutzt, damit die App dich persönlicher ansprechen kann, und wird bei Backups mitgespeichert. Wenn du keinen Namen angibst, bleibt die App in der Ansprache neutral.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSkipDialog = false
                        currentStep = OnboardingStep.Theme
                    },
                ) {
                    Text("Überspringen")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSkipDialog = false }) {
                    Text("Zurück")
                }
            },
        )
    }

    if (showNameInfoDialog) {
        AlertDialog(
            onDismissRequest = { showNameInfoDialog = false },
            title = { Text("Wofür ist der Name gedacht?") },
            text = {
                Text(
                    "Der Name dient nur der persönlichen Ansprache in der App und wird zusätzlich in Backups gespeichert. Ohne Namen bleibt Android-Sleepy-Time bewusst neutral.",
                )
            },
            confirmButton = {
                TextButton(onClick = { showNameInfoDialog = false }) {
                    Text("Verstanden")
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OnboardingTopBar(
    canNavigateBack: Boolean,
    currentStep: OnboardingStep,
    onNavigateBack: () -> Unit,
) {
    CenterAlignedTopAppBar(
        title = { ProgressDots(currentStep = currentStep) },
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Zurück",
                    )
                }
            }
        },
    )
}

@Composable
private fun ProgressDots(currentStep: OnboardingStep) {
    val steps = listOf(OnboardingStep.Name, OnboardingStep.Theme, OnboardingStep.Import)
    val currentIndex = steps.indexOf(currentStep)

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        steps.forEachIndexed { index, _ ->
            val isReached = index <= currentIndex
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(
                        if (isReached) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant,
                    ),
            )
        }
    }
}

@Composable
private fun NameStepScreen(
    modifier: Modifier = Modifier,
    displayName: String,
    onDisplayNameChange: (String) -> Unit,
    onShowNameInfo: () -> Unit,
    onContinue: () -> Unit,
    onSkip: () -> Unit,
) {
    val isNameValid = OnboardingValidator.isNameValid(displayName)

    SetupScreenFrame(modifier = modifier) {
        Text(
            text = "Willkommen bei Android-Sleepy-Time",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Du kannst der App optional sagen, wie sie dich ansprechen soll. Es geht nur um Personalisierung – nicht um ein Konto oder eine Registrierung.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(28.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Name / Ansprache",
                style = MaterialTheme.typography.titleMedium,
            )
            AssistChip(
                onClick = onShowNameInfo,
                label = { Text("Info") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null)
                },
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = displayName,
            onValueChange = onDisplayNameChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Dein Name") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            supportingText = {
                Text("Optional, maximal ${OnboardingValidator.maxNameLength} Zeichen")
            },
        )
        Spacer(modifier = Modifier.height(28.dp))
        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth(),
            enabled = isNameValid,
        ) {
            Text("Weiter")
        }
        if (!isNameValid) {
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = onSkip,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            ) {
                Text("Ohne Namen fortfahren")
            }
        }
    }
}

@Composable
private fun ThemeStepScreen(
    modifier: Modifier = Modifier,
    selectedThemeMode: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit,
    onContinue: () -> Unit,
) {
    SetupScreenFrame(modifier = modifier) {
        Text(
            text = "Wähle dein Startdesign",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Du kannst das Design später jederzeit in den Einstellungen ändern. Geräte-Standard bleibt die empfohlene Voreinstellung.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(24.dp))
        ThemeMode.entries.forEach { themeMode ->
            ThemeOptionCard(
                themeMode = themeMode,
                selected = selectedThemeMode == themeMode,
                onSelect = { onThemeSelected(themeMode) },
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
            Text("Weiter")
        }
    }
}

@Composable
private fun ThemeOptionCard(
    themeMode: ThemeMode,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    val (title, description, icon) = when (themeMode) {
        ThemeMode.SYSTEM -> Triple("Geräte-Standard", "Passt sich automatisch an dein System an.", Icons.Default.PhoneAndroid)
        ThemeMode.DARK -> Triple("Dark", "Sanftes dunkles Design für Abend und Nacht.", Icons.Default.DarkMode)
        ThemeMode.LIGHT -> Triple("Light", "Helles, klares Design für tagsüber.", Icons.Default.LightMode)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "$title auswählen"
                this.selected = selected
                role = Role.RadioButton
            }
            .clickable(role = Role.RadioButton, onClick = onSelect),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        when (themeMode) {
                            ThemeMode.DARK -> Color(0xFF1E1B2E)
                            ThemeMode.LIGHT -> Color(0xFFF5F3FF)
                            ThemeMode.SYSTEM -> Color(0xFFE3E8FF)
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (themeMode == ThemeMode.DARK) Color(0xFFE5DEFF) else MaterialTheme.colorScheme.primary,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (selected) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Ausgewählt",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun ImportStepScreen(
    modifier: Modifier = Modifier,
    importedFileName: String?,
    onImportClick: () -> Unit,
    onContinueWithoutImport: () -> Unit,
    onShowImportHint: () -> Unit,
) {
    SetupScreenFrame(modifier = modifier) {
        Text(
            text = "Backup importieren oder leer starten",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Der Import ist schon als wichtiger Hauptpfad vorbereitet. Die erste Version bleibt bewusst technisch schlank, damit die spätere vollständige Wiederherstellung sauber ergänzt werden kann.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            ),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(Icons.Default.Description, contentDescription = null)
                    Text("Vorbereitet für vollständige Backups", style = MaterialTheme.typography.titleMedium)
                }
                Text(
                    text = "Langfristig sollen darüber Name, Design, Träume, spätere Analysen und weitere Einstellungen wiederhergestellt werden.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(onClick = onShowImportHint) {
                    Text("Was ist in dieser Version schon vorbereitet?")
                }
            }
        }
        if (importedFileName != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Ausgewählte Datei: $importedFileName",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onImportClick, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.UploadFile, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Daten importieren")
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(onClick = onContinueWithoutImport, modifier = Modifier.fillMaxWidth()) {
            Text("Ohne Daten starten")
        }
    }
}

@Composable
private fun SetupCompletionScreen(onFinished: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(3_000)
        onFinished()
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier.padding(24.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Glückwunsch, deine App ist jetzt eingerichtet.", style = MaterialTheme.typography.headlineSmall)
                Text(
                    text = "Viel Freude beim Festhalten deiner Träume.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }
    }
}

@Composable
private fun ImportPreparationScreen(
    importedFileName: String?,
    onFinish: () -> Unit,
) {
    var activeStep by remember { mutableIntStateOf(0) }
    val steps = remember {
        listOf(
            "Träume werden geladen",
            "Einstellungen werden geladen",
            "Design wird angewendet",
            "Begrüßungsoptionen werden übernommen",
        )
    }

    LaunchedEffect(Unit) {
        steps.indices.forEach { index ->
            activeStep = index
            delay(850)
        }
        delay(500)
        onFinish()
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier.padding(24.dp),
            shape = RoundedCornerShape(28.dp),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text("Import wird vorbereitet", style = MaterialTheme.typography.headlineSmall)
                Text(
                    text = importedFileName?.let { "Datei: $it" } ?: "Backup-Datei wird verarbeitet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                steps.forEachIndexed { index, label ->
                    val reached = index <= activeStep
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(
                                    if (reached) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant,
                                ),
                        )
                        Text(
                            text = label,
                            color = if (reached) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text("Geplante Kontrollübersicht", style = MaterialTheme.typography.titleMedium)
                        Text("• Profil\n• Design & Einstellungen\n• Traumdaten\n• Importstatus")
                    }
                }
            }
        }
    }
}

@Composable
private fun SetupScreenFrame(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp),
    ) {
        item {
            Column(content = content)
        }
    }
}
