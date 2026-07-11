package com.adsmaker.app.ui.create

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.adsmaker.app.AdsMakerApplication
import com.adsmaker.app.core.AppResult
import com.adsmaker.app.data.repository.AdRepository
import com.adsmaker.app.data.video.VideoProgress
import com.adsmaker.app.domain.AdInputs
import com.adsmaker.app.util.MediaUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Drives the create → confirm cost → generate → preview flow. Activity-scoped so
 * the create and preview screens share one instance.
 */
class CreateAdViewModel(
    app: Application,
    private val repository: AdRepository,
    private val apiKeyMissing: Boolean,
) : AndroidViewModel(app) {

    private var imageUri: Uri? = null
    private var videoUri: Uri? = null
    private var productNotes: String? = null

    private val _uiState = MutableStateFlow(CreateAdUiState(apiKeyMissing = apiKeyMissing))
    val uiState: StateFlow<CreateAdUiState> = _uiState.asStateFlow()

    fun onImagePicked(uri: Uri?) {
        imageUri = uri
        _uiState.update { it.copy(hasImage = uri != null) }
    }

    fun onVideoPicked(uri: Uri?) {
        videoUri = uri
        _uiState.update { it.copy(hasVideo = uri != null) }
    }

    fun onTextFilePicked(uri: Uri?, displayName: String?) {
        if (uri == null) return
        viewModelScope.launch {
            val text = MediaUtils.readTextFile(getApplication(), uri)
            productNotes = text
            _uiState.update {
                it.copy(
                    notesFileName = displayName,
                    notesPreview = text.take(280),
                )
            }
        }
    }

    fun clearNotes() {
        productNotes = null
        _uiState.update { it.copy(notesFileName = null, notesPreview = null) }
    }

    /** User tapped Generate — surface the cost confirmation first. */
    fun requestGenerate() {
        if (!_uiState.value.canGenerate) return
        _uiState.update { it.copy(showCostDialog = true) }
    }

    fun dismissCostDialog() {
        _uiState.update { it.copy(showCostDialog = false) }
    }

    /** User confirmed the cost — actually call the paid API. */
    fun confirmAndGenerate() {
        val state = _uiState.value
        if (state.phase is CreateAdUiState.Phase.Generating) return

        _uiState.update {
            it.copy(
                showCostDialog = false,
                phase = CreateAdUiState.Phase.Generating(null),
                errorMessage = null,
                generatedAd = null,
            )
        }

        viewModelScope.launch {
            val inputs = AdInputs(
                imageUri = imageUri,
                videoUri = videoUri,
                productNotes = productNotes,
                platform = state.platform,
            )
            val result = repository.generateAd(inputs) { progress ->
                _uiState.update { it.copy(phase = CreateAdUiState.Phase.Generating(progress)) }
            }
            when (result) {
                is AppResult.Success -> _uiState.update {
                    it.copy(phase = CreateAdUiState.Phase.Done, generatedAd = result.data)
                }
                is AppResult.Failure -> _uiState.update {
                    it.copy(phase = CreateAdUiState.Phase.Idle, errorMessage = result.message)
                }
            }
        }
    }

    fun consumeError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /** Reset the generation phase when returning to the create screen to regenerate. */
    fun prepareForRegenerate() {
        _uiState.update {
            it.copy(phase = CreateAdUiState.Phase.Idle, generatedAd = null, errorMessage = null)
        }
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras,
            ): T {
                val app = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as AdsMakerApplication
                @Suppress("UNCHECKED_CAST")
                return CreateAdViewModel(
                    app = app,
                    repository = app.serviceLocator.adRepository,
                    apiKeyMissing = !app.serviceLocator.hasApiKey,
                ) as T
            }
        }
    }
}
