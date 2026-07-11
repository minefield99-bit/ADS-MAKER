package com.adsmaker.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.adsmaker.app.AdsMakerApplication
import com.adsmaker.app.data.settings.ApiKeyResolver
import com.adsmaker.app.data.settings.ApiKeyStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** UI state for the API-key settings screen. The full key is never held here. */
data class SettingsUiState(
    val hasSavedKey: Boolean = false,
    val maskedKey: String? = null,
    /** Whether the text field is shown (true when no key saved, or replacing). */
    val editing: Boolean = true,
    val input: String = "",
    val justSaved: Boolean = false,
) {
    val canSave: Boolean get() = input.isNotBlank()
}

class SettingsViewModel(
    private val store: ApiKeyStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private fun initialState(): SettingsUiState {
        val existing = store.getUserKey()
        return SettingsUiState(
            hasSavedKey = existing != null,
            maskedKey = existing?.let { ApiKeyResolver.mask(it) },
            editing = existing == null,
        )
    }

    fun onInputChange(value: String) {
        _uiState.update { it.copy(input = value, justSaved = false) }
    }

    fun save() {
        val key = _uiState.value.input.trim()
        if (key.isEmpty()) return
        store.saveUserKey(key)
        _uiState.update {
            it.copy(
                hasSavedKey = true,
                maskedKey = ApiKeyResolver.mask(key),
                editing = false,
                input = "",
                justSaved = true,
            )
        }
    }

    /** Show the field again to enter a different key. */
    fun startReplacing() {
        _uiState.update { it.copy(editing = true, input = "", justSaved = false) }
    }

    fun clear() {
        store.clearUserKey()
        _uiState.update {
            SettingsUiState(hasSavedKey = false, maskedKey = null, editing = true)
        }
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val app = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as AdsMakerApplication
                @Suppress("UNCHECKED_CAST")
                return SettingsViewModel(app.serviceLocator.apiKeyStore) as T
            }
        }
    }
}
