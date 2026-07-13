@file:OptIn(ExperimentalMaterial3Api::class)

package com.adsmaker.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var revealed by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("API key") },
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
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                "Ads Maker generates videos with the fal.ai (Seedance) service, which needs your own API key.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                "Get a free key at fal.ai/dashboard/keys, then paste it below. It's encrypted and stored only on this phone.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (state.hasSavedKey && !state.editing) {
                SavedKeyCard(
                    maskedKey = state.maskedKey.orEmpty(),
                    justSaved = state.justSaved,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = viewModel::startReplacing,
                        modifier = Modifier.weight(1f).height(50.dp),
                    ) { Text("Replace key") }
                    OutlinedButton(
                        onClick = viewModel::clear,
                        modifier = Modifier.weight(1f).height(50.dp),
                    ) { Text("Remove key") }
                }
            } else {
                OutlinedTextField(
                    value = state.input,
                    onValueChange = viewModel::onInputChange,
                    label = { Text("fal.ai API key") },
                    singleLine = true,
                    visualTransformation = if (revealed) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { revealed = !revealed }) {
                            Icon(
                                if (revealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (revealed) "Hide key" else "Show key",
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                Button(
                    onClick = viewModel::save,
                    enabled = state.canSave,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                ) { Text("Save key") }
                if (state.hasSavedKey) {
                    TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                        Text("Cancel")
                    }
                }
            }

            OwnerModeCard(
                ownerMode = state.ownerMode,
                onOwnerModeChange = viewModel::setOwnerMode,
            )
        }
    }
}

@Composable
private fun OwnerModeCard(ownerMode: Boolean, onOwnerModeChange: (Boolean) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(Modifier.weight(1f)) {
                Text("Owner mode", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Clean videos with no watermark and no free-trial cap. " +
                        "Off = free-trial behaviour: one watermarked video.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = ownerMode, onCheckedChange = onOwnerModeChange)
        }
    }
}

@Composable
private fun SavedKeyCard(maskedKey: String, justSaved: Boolean) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                if (justSaved) "Key saved" else "Key on file",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                maskedKey,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
