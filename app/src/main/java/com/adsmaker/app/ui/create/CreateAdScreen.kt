@file:OptIn(ExperimentalMaterial3Api::class)

package com.adsmaker.app.ui.create

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adsmaker.app.data.video.VideoProgress
import com.adsmaker.app.domain.CostEstimator

@Composable
fun CreateAdScreen(
    viewModel: CreateAdViewModel,
    onGenerated: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHost = remember { SnackbarHostState() }

    // Navigate to preview once a video is ready.
    LaunchedEffect(state.phase) {
        if (state.phase is CreateAdUiState.Phase.Done && state.generatedAd != null) {
            onGenerated()
        }
    }

    // Surface generation errors without crashing.
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHost.showSnackbar(it)
            viewModel.consumeError()
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri -> viewModel.onImagePicked(uri) }

    val videoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri -> viewModel.onVideoPicked(uri) }

    val textPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        viewModel.onTextFilePicked(uri, uri?.let { queryDisplayName(context, it) })
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) },
        topBar = {
            TopAppBar(
                title = { Text("Ads Maker") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "Turn your product into a TikTok ad",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    "Add a picture (and optionally a clip or a notes file). We'll generate a ~15s vertical ad.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (state.apiKeyMissing) ApiKeyMissingCard()

                UploadCard(
                    icon = Icons.Default.Image,
                    title = "Product picture",
                    subtitle = "Required — the main image for your ad",
                    selected = state.hasImage,
                    onClick = {
                        imagePicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
                    },
                )
                UploadCard(
                    icon = Icons.Default.Movie,
                    title = "Video clip",
                    subtitle = "Optional — extra footage to draw from",
                    selected = state.hasVideo,
                    onClick = {
                        videoPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly),
                        )
                    },
                )
                UploadCard(
                    icon = Icons.Default.Description,
                    title = state.notesFileName ?: "Product notes",
                    subtitle = state.notesPreview?.let { "\"$it…\"" }
                        ?: "Optional — a .txt describing the product / selling points",
                    selected = state.notesFileName != null,
                    onClick = { textPicker.launch(arrayOf("text/plain", "text/*")) },
                )

                PlatformStyleCard()

                Spacer(Modifier.height(4.dp))
                Button(
                    onClick = viewModel::requestGenerate,
                    enabled = state.canGenerate,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                ) {
                    Text("Generate ad  ·  ${state.costEstimate.formatted}")
                }
                Text(
                    "Estimated ${state.costEstimate.seconds}s × $${CostEstimator.USD_PER_SECOND}/s. You'll confirm before we charge.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(20.dp))
            }

            if (state.phase is CreateAdUiState.Phase.Generating) {
                GeneratingOverlay((state.phase as CreateAdUiState.Phase.Generating).progress)
            }
        }
    }

    if (state.showCostDialog) {
        CostConfirmDialog(
            estimate = state.costEstimate,
            onConfirm = viewModel::confirmAndGenerate,
            onDismiss = viewModel::dismissCostDialog,
        )
    }
}

@Composable
private fun UploadCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                )
            }
            if (selected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun PlatformStyleCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("TikTok · 9:16 · ~15s", style = MaterialTheme.typography.titleMedium)
            Text(
                "Style: sharp product shot → fades to a soft blur while bold text and voice-over take over.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ApiKeyMissingCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF3A1D2A)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("API key needed", style = MaterialTheme.typography.titleMedium)
            Text(
                "Add FAL_API_KEY to local.properties to enable generation. See README.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CostConfirmDialog(
    estimate: CostEstimator.Estimate,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm generation") },
        text = {
            Text(
                "This will generate a ${estimate.seconds}s video and costs about " +
                    "${estimate.formatted} in API charges. Each attempt (including retries) " +
                    "is billed. Continue?",
            )
        },
        confirmButton = {
            Button(onClick = onConfirm) { Text("Generate · ${estimate.formatted}") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun GeneratingOverlay(progress: VideoProgress?) {
    Box(
        Modifier.fillMaxSize().padding(0.dp),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.padding(32.dp),
        ) {
            Column(
                Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                CircularProgressIndicator(Modifier.size(48.dp))
                Text(stageLabel(progress), style = MaterialTheme.typography.titleMedium)
                Text(
                    "This can take a couple of minutes. Please keep the app open.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Normal,
                )
            }
        }
    }
}

private fun stageLabel(progress: VideoProgress?): String = when (progress?.stage) {
    VideoProgress.Stage.UPLOADING -> "Preparing your image…"
    VideoProgress.Stage.QUEUED -> progress.queuePosition
        ?.let { "Queued (position $it)…" } ?: "Queued…"
    VideoProgress.Stage.GENERATING -> "Generating your ad…"
    VideoProgress.Stage.DOWNLOADING -> "Finishing up…"
    null -> "Starting…"
}

/** Resolves a human-readable file name from a content Uri. */
private fun queryDisplayName(context: android.content.Context, uri: Uri): String? {
    return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (idx >= 0 && cursor.moveToFirst()) cursor.getString(idx) else null
    }
}
