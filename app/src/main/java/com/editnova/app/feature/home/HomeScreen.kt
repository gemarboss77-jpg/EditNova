package com.editnova.app.feature.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.editnova.app.R
import com.editnova.app.core.di.AppContainer
import com.editnova.app.core.navigation.HomeTab
import com.editnova.app.core.theme.EditNovaSpacing
import com.editnova.app.core.ui.components.EditNovaBottomNavBar
import com.editnova.app.core.ui.components.EditNovaLogo
import com.editnova.app.core.ui.components.EmptyState
import com.editnova.app.core.ui.components.FeatureCard
import com.editnova.app.core.ui.components.GradientButton
import com.editnova.app.core.ui.components.PremiumBadge
import com.editnova.app.core.ui.components.SectionHeader
import com.editnova.app.core.ui.components.TemplateCard
import com.editnova.app.domain.media.MediaType
import com.editnova.app.domain.media.SelectedMedia
import com.editnova.app.domain.project.Project
import com.editnova.app.feature.settings.SettingsScreen
import kotlinx.coroutines.launch

/** One AI tool entry point shown in the AI Tools section/tab. */
private data class AiTool(val title: String, val description: String, val icon: ImageVector)

private val aiTools = listOf(
    AiTool("AI Auto Edit", "Automatically cut and pace your footage", Icons.Filled.AutoAwesome),
    AiTool("AI Captions", "Generate accurate captions in seconds", Icons.Filled.Subtitles),
    AiTool("AI Voiceover", "Turn text into natural narration", Icons.Filled.RecordVoiceOver),
    AiTool("AI Thumbnail", "Create a scroll-stopping thumbnail", Icons.Filled.Image)
)

/** Placeholder template names for the Templates section/tab — no real templates exist yet. */
private val templateNames = listOf("Reels Intro", "Vlog Cut", "Product Promo", "Slideshow")

/**
 * HomeScreen — the app's main shell after onboarding.
 *
 * This single composable owns the bottom navigation (Home, Projects, AI Tools,
 * Templates, Settings) as simple in-memory tab state (see [HomeTab]) rather than
 * separate back-stack routes — standard for bottom-nav tabs. The Premium upgrade
 * screen and the Editor Preview screen (Step 3) ARE real navigation destinations,
 * so they're passed in as [onOpenPremium] / [onOpenEditor].
 *
 * Everything else shown is still a UI placeholder: Recent Projects, AI Tools, and
 * Templates do not do any real work yet (see each section's comments).
 */
@Composable
fun HomeScreen(onOpenPremium: () -> Unit, onOpenEditor: (SelectedMedia) -> Unit) {
    var selectedTab by remember { mutableStateOf(HomeTab.HOME) }

    // Reads real (but always-Free-in-Step-2) state from the architecture built in
    // Step 1, so the badge and watermark messaging are honest rather than hardcoded.
    // collectAsState keeps this reactive: if the repository's state ever changes
    // (e.g. once real billing exists), the badge updates automatically.
    val subscriptionState by AppContainer.subscriptionRepository.subscriptionState.collectAsState()
    val isPremium = subscriptionState.isPremium

    // Shared error messaging for the whole Home shell (currently only used by the
    // New Project media picker if the user picks something we can't handle).
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val showError: (String) -> Unit = { message ->
        coroutineScope.launch { snackbarHostState.showSnackbar(message) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            EditNovaTopBar(isPremium = isPremium, onPremiumClick = onOpenPremium)
        },
        bottomBar = {
            EditNovaBottomNavBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { innerPadding ->
        // Crossfade gives a smooth, lightweight transition between tabs instead of an
        // abrupt swap — cheap because only the currently visible tab is composed.
        Crossfade(targetState = selectedTab, label = "home_tab_crossfade") { tab ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = EditNovaSpacing.screenHorizontal)
            ) {
                when (tab) {
                    HomeTab.HOME -> HomeDashboardTab(onOpenEditor = onOpenEditor, onError = showError)
                    HomeTab.PROJECTS -> ProjectsTabContent()
                    HomeTab.AI_TOOLS -> AiToolsTabContent()
                    HomeTab.TEMPLATES -> TemplatesTabContent()
                    HomeTab.SETTINGS -> SettingsScreen(onOpenPremium = onOpenPremium)
                }
            }
        }
    }
}

/** Top app bar: logo + app name on the left, Free/Premium badge on the right. */
@Composable
private fun EditNovaTopBar(isPremium: Boolean, onPremiumClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = EditNovaSpacing.screenHorizontal)
            .padding(top = EditNovaSpacing.sm, bottom = EditNovaSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            EditNovaLogo(size = 36.dp)
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = EditNovaSpacing.sm)
            )
        }
        PremiumBadge(isPremium = isPremium, onClick = onPremiumClick)
    }
}

/**
 * The Home tab: New Project CTA (now functional — opens the system media picker and,
 * on a valid selection, opens the Editor Preview screen) + a preview of each section.
 */
@Composable
private fun HomeDashboardTab(onOpenEditor: (SelectedMedia) -> Unit, onError: (String) -> Unit) {
    // Always empty in Step 2/3 — no project persistence exists yet.
    val recentProjects: List<Project> = emptyList()

    val context = LocalContext.current

    // The modern Android Photo Picker: requires NO storage permission (unlike
    // ACTION_OPEN_DOCUMENT or a READ_MEDIA_* permission request), since the system —
    // not the app — handles the media browsing UI and only grants access to the one
    // item the user actually picks.
    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri == null) {
            // User cancelled the picker — return safely, nothing to do.
            return@rememberLauncherForActivityResult
        }
        val mimeType = context.contentResolver.getType(uri)
        val mediaType = when {
            mimeType?.startsWith("video") == true -> MediaType.VIDEO
            mimeType?.startsWith("image") == true -> MediaType.IMAGE
            else -> null
        }
        if (mediaType == null) {
            onError("Unsupported media type. Please choose a photo or video.")
        } else {
            onOpenEditor(SelectedMedia(uri = uri, type = mediaType))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(EditNovaSpacing.sm))

        GradientButton(
            text = stringResource(R.string.new_project),
            onClick = {
                pickMediaLauncher.launch(
                    PickVisualMediaRequest(mediaType = ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                )
            }
        )

        Spacer(modifier = Modifier.height(EditNovaSpacing.xl))

        SectionHeader(title = stringResource(R.string.recent_projects))
        Spacer(modifier = Modifier.height(EditNovaSpacing.sm))
        if (recentProjects.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.VideoLibrary,
                message = stringResource(R.string.no_recent_projects)
            )
        }

        Spacer(modifier = Modifier.height(EditNovaSpacing.xl))

        SectionHeader(title = stringResource(R.string.ai_tools))
        Spacer(modifier = Modifier.height(EditNovaSpacing.sm))
        AiToolsGrid()

        Spacer(modifier = Modifier.height(EditNovaSpacing.xl))

        SectionHeader(title = stringResource(R.string.templates))
        Spacer(modifier = Modifier.height(EditNovaSpacing.sm))
        TemplatesRow()

        Spacer(modifier = Modifier.height(EditNovaSpacing.xl))
    }
}

/** Full-screen Projects tab — same empty state as the Home preview, shown larger. */
@Composable
private fun ProjectsTabContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(EditNovaSpacing.md))
        SectionHeader(title = stringResource(R.string.recent_projects))
        Spacer(modifier = Modifier.height(EditNovaSpacing.sm))
        EmptyState(
            icon = Icons.Filled.VideoLibrary,
            message = stringResource(R.string.no_recent_projects)
        )
    }
}

/** Full-screen AI Tools tab. */
@Composable
private fun AiToolsTabContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(EditNovaSpacing.md))
        SectionHeader(title = stringResource(R.string.ai_tools))
        Spacer(modifier = Modifier.height(EditNovaSpacing.sm))
        AiToolsGrid()
        Spacer(modifier = Modifier.height(EditNovaSpacing.xl))
    }
}

/** Full-screen Templates tab. */
@Composable
private fun TemplatesTabContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(EditNovaSpacing.md))
        SectionHeader(title = stringResource(R.string.templates))
        Spacer(modifier = Modifier.height(EditNovaSpacing.sm))
        TemplatesRow()
        Spacer(modifier = Modifier.height(EditNovaSpacing.xl))
    }
}

/**
 * The 2-column AI Tools grid, reused by both the Home dashboard preview and the
 * dedicated AI Tools tab so the visual design only needs to be defined once.
 * All 4 tools are disabled — no AI processing exists yet (see project rules).
 */
@Composable
private fun AiToolsGrid() {
    // A simple 2-column layout built from Rows: avoids nesting a scrollable
    // LazyVerticalGrid inside an already-scrolling parent, which Compose disallows
    // without extra height constraints. Four items is small enough that this stays
    // simple and readable.
    aiTools.chunked(2).forEach { rowTools ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(EditNovaSpacing.sm)
        ) {
            rowTools.forEach { tool ->
                FeatureCard(
                    title = tool.title,
                    description = tool.description,
                    icon = tool.icon,
                    enabled = false,
                    modifier = Modifier.weight(1f)
                )
            }
            // Keeps the last row's spacing even when it has only 1 item.
            if (rowTools.size == 1) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
        Spacer(modifier = Modifier.height(EditNovaSpacing.sm))
    }
}

/**
 * The horizontal Templates row, reused by both the Home dashboard preview and the
 * dedicated Templates tab. Purely visual placeholders — no real templates exist yet.
 */
@Composable
private fun TemplatesRow() {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(EditNovaSpacing.sm)) {
        items(templateNames) { name ->
            TemplateCard(name = name)
        }
    }
}
