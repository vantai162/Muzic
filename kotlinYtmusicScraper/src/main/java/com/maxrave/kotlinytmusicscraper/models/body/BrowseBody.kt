package com.maxrave.kotlinytmusicscraper.models.body

import com.maxrave.kotlinytmusicscraper.models.Context
import com.maxrave.kotlinytmusicscraper.models.WatchEndpoint
import kotlinx.serialization.Serializable

@Serializable
data class BrowseBody(
    val context: Context,
    val browseId: String? = null,
    val params: String? = null,
    val formData: FormData? = null,
    val enablePersistentPlaylistPanel: Boolean? = null,
    val isAudioOnly: Boolean? = null,
    val tunerSettingValue: String? = null,
    val playlistId: String? = null,
    val continuation: String? = null,
    val watchEndpointMusicSupportedConfigs: WatchEndpoint.WatchEndpointMusicSupportedConfigs? = null,
)