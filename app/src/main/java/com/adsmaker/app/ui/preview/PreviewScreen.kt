package com.adsmaker.app.ui.preview

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.adsmaker.app.domain.GeneratedAd
import com.adsmaker.app.util.MediaUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewScreen(
    ad: GeneratedAd,
    onBack: () -> Unit,
    onRegenerate: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHost = remember { SnackbarHostState() }
    var saving by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) },
        topBar = {
            TopAppBar(
                title = { Text("Preview") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(9f / 16f)
                    .clip(RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center,
            ) {
                com.adsmaker.app.ui.components.VideoPlayer(
                    uri = ad.file.toUri(),
                    modifier = Modifier.fillMaxSize(),
                )
            }

            Text(
                "${ad.mode.label} · ${ad.platform.displayName} · ${ad.durationSeconds}s · " +
                    "${ad.mode.resolution.apiValue} · cost ~$${"%.2f".format(ad.estimatedCostUsd)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Button(
                onClick = {
                    saving = true
                    scope.launch {
                        val uri = MediaUtils.exportToGallery(
                            context, ad.file, ad.file.name,
                        )
                        saving = false
                        if (uri != null) {
                            saved = true
                            snackbarHost.showSnackbar("Saved to Movies/AdsMaker")
                        } else {
                            snackbarHost.showSnackbar("Couldn't save the video. Please try again.")
                        }
                    }
                },
                enabled = !saving,
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                Icon(Icons.Default.Download, contentDescription = null)
                Spacer(Modifier.height(0.dp))
                Text(if (saved) "  Saved to gallery" else "  Save to gallery")
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = {
                        val shareUri = MediaUtils.shareableUri(context, ad.file)
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "video/mp4"
                            putExtra(Intent.EXTRA_STREAM, shareUri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share ad"))
                    },
                    modifier = Modifier.weight(1f).height(52.dp),
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Text("  Share")
                }
                OutlinedButton(
                    onClick = onRegenerate,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.secondary,
                    ),
                    modifier = Modifier.weight(1f).height(52.dp),
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Text("  Regenerate")
                }
            }
            Text(
                "Regenerating starts a new paid generation (~$${"%.2f".format(ad.estimatedCostUsd)}).",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
