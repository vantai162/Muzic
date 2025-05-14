package com.maxrave.simpmusic.viewModel

import android.app.Application
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.maxrave.kotlinytmusicscraper.pages.BrowseResult
import com.maxrave.simpmusic.viewModel.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@UnstableApi
class MoreAlbumsViewModel(
    application: Application,
) : BaseViewModel(application) {
    private var _browseResult: MutableStateFlow<BrowseResult?> = MutableStateFlow(null)
    val browseResult: StateFlow<BrowseResult?> = _browseResult

    fun getAlbumMore(id: String) {
        viewModelScope.launch {
            _browseResult.value = null
            mainRepository.getAlbumMore(id, ALBUM_PARAM).collect { data ->
                _browseResult.value = data
            }
        }
    }

    fun getSingleMore(id: String) {
        viewModelScope.launch {
            _browseResult.value = null
            mainRepository.getAlbumMore(id, SINGLE_PARAM).collect { data ->
                _browseResult.value = data
            }
        }
    }

    companion object {
        const val ALBUM_PARAM = "ggMIegYIARoCAQI%3D"
        const val SINGLE_PARAM = "ggMIegYIAhoCAQI%3D"
    }
}