package com.example.muzic.service

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.audiofx.AudioEffect
import android.media.audiofx.LoudnessEnhancer
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaItem.EMPTY
import androidx.media3.common.MediaItem.SubtitleConfiguration
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.SessionCommand
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.placeholder
import coil3.toBitmap
import com.maxrave.kotlinytmusicscraper.models.sponsorblock.SkipSegments
import com.example.muzic.R
import com.example.muzic.common.ASC
import com.example.muzic.common.DESC
import com.example.muzic.common.LOCAL_PLAYLIST_ID
import com.example.muzic.common.LOCAL_PLAYLIST_ID_SAVED_QUEUE
import com.example.muzic.common.MEDIA_CUSTOM_COMMAND
import com.example.muzic.common.SPONSOR_BLOCK
import com.example.muzic.data.dataStore.DataStoreManager
import com.example.muzic.data.dataStore.DataStoreManager.Settings.TRUE
import com.example.muzic.data.db.Converters
import com.example.muzic.data.db.entities.NewFormatEntity
import com.example.muzic.data.db.entities.SongEntity
import com.example.muzic.data.model.browse.album.Track
import com.example.muzic.data.model.searchResult.songs.Artist
import com.example.muzic.data.repository.MainRepository
import com.example.muzic.extension.connectArtists
import com.example.muzic.extension.getScreenSize
import com.example.muzic.extension.isVideo
import com.example.muzic.extension.toArrayListTrack
import com.example.muzic.extension.toListName
import com.example.muzic.extension.toMediaItem
import com.example.muzic.extension.toSongEntity
import com.example.muzic.extension.toTrack
import com.example.muzic.service.test.source.MergingMediaSourceFactory
import com.example.muzic.ui.widget.BasicWidget
import com.example.muzic.utils.Resource
import com.example.muzic.viewModel.FilterState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime
import kotlin.math.pow

@UnstableApi
class SimpleMediaServiceHandler(
    val player: ExoPlayer,
    mediaSessionCallback: SimpleMediaSessionCallback,
    private val dataStoreManager: DataStoreManager,
    private val mainRepository: MainRepository,
    private val coroutineScope: CoroutineScope,
    private val context: Context,
) : Player.Listener {
    var setNotificationLayout: ((List<CommandButton>) -> Unit)? = null

    @Suppress("ktlint:standard:property-naming")
    private val TAG = "SimpleMediaServiceHandler"
    private val converter = Converters()

    private var loudnessEnhancer: LoudnessEnhancer? = null

    private var volumeNormalizationJob: Job? = null

    private var sleepTimerJob: Job? = null

    private var downloadImageForWidgetJob: Job? = null

    private val basicWidget = BasicWidget.instance

    private val _simpleMediaState = MutableStateFlow<SimpleMediaState>(SimpleMediaState.Initial)
    val simpleMediaState = _simpleMediaState.asStateFlow()

    private var _nowPlaying = MutableStateFlow(player.currentMediaItem)
    val nowPlaying = _nowPlaying.asStateFlow()

    private var _queueData = MutableStateFlow<QueueData?>(null)
    val queueData = _queueData.asStateFlow()

    private var _controlState =
        MutableStateFlow(
            ControlState(
                isPlaying = player.isPlaying,
                isShuffle = player.shuffleModeEnabled,
                repeatState =
                    when (player.repeatMode) {
                        Player.REPEAT_MODE_ONE -> RepeatState.One
                        Player.REPEAT_MODE_ALL -> RepeatState.All
                        Player.REPEAT_MODE_OFF -> RepeatState.None
                        else -> {
                            RepeatState.None
                        }
                    },
                isLiked = false,
                isNextAvailable = player.hasNextMediaItem(),
                isPreviousAvailable = player.hasPreviousMediaItem(),
            ),
        )
    val controlState = _controlState.asStateFlow()

    private val isFading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val shouldFadeAudio = dataStoreManager.fadeVolume.map { it > 0 }.stateIn(coroutineScope, SharingStarted.WhileSubscribed(5000L), false)

    private var _stateFlow = MutableStateFlow<StateSource>(StateSource.STATE_CREATED)
    val stateFlow = _stateFlow.asStateFlow()
    private var _currentSongIndex = MutableStateFlow<Int>(player.currentMediaItemIndex)
    val currentSongIndex = _currentSongIndex.asSharedFlow()

    private var _nowPlayingState = MutableStateFlow(NowPlayingTrackState.initial())
    val nowPlayingState = _nowPlayingState.asStateFlow()

    private var _sleepTimerState = MutableStateFlow<SleepTimerState>(SleepTimerState(false, 0))
    val sleepTimerState = _sleepTimerState.asStateFlow()

    // SponsorBlock
    private var _skipSegments: MutableStateFlow<List<SkipSegments>?> = MutableStateFlow(null)
    val skipSegments: StateFlow<List<SkipSegments>?> = _skipSegments

    private var getSkipSegmentsJob: Job? = null

    private var _format: MutableStateFlow<NewFormatEntity?> = MutableStateFlow(null)
    val format: StateFlow<NewFormatEntity?> = _format

    private var getFormatJob: Job? = null

    private var skipSilent = false

    private var normalizeVolume = false

    private var progressJob: Job? = null

    private var bufferedJob: Job? = null

    private var fadeJob: Job? = null

    private var updateNotificationJob: Job? = null

    private var toggleLikeJob: Job? = null

    private var loadJob: Job? = null

    private var songEntityJob: Job? = null

    private var watchTimeList: ArrayList<Float> = arrayListOf()

    private var jobWatchtime: Job? = null

    init {
        player.addListener(this)
        progressJob = Job()
        bufferedJob = Job()
        sleepTimerJob = Job()
        volumeNormalizationJob = Job()
        updateNotificationJob = Job()
        toggleLikeJob = Job()
        loadJob = Job()
        songEntityJob = Job()
        downloadImageForWidgetJob = Job()
        getSkipSegmentsJob = Job()
        getFormatJob = Job()
        jobWatchtime = Job()
        skipSilent = runBlocking { dataStoreManager.skipSilent.first() == TRUE }
        normalizeVolume =
            runBlocking { dataStoreManager.normalizeVolume.first() == TRUE }
        if (runBlocking { dataStoreManager.saveStateOfPlayback.first() } == TRUE) {
            Log.d("CHECK INIT", "TRUE")
            val shuffleKey = runBlocking { dataStoreManager.shuffleKey.first() }
            val repeatKey = runBlocking { dataStoreManager.repeatKey.first() }
            Log.d("CHECK INIT", "Shuffle: $shuffleKey")
            Log.d("CHECK INIT", "Repeat: $repeatKey")
            player.shuffleModeEnabled = shuffleKey == TRUE
            player.repeatMode =
                when (repeatKey) {
                    DataStoreManager.REPEAT_ONE -> Player.REPEAT_MODE_ONE
                    DataStoreManager.REPEAT_ALL -> Player.REPEAT_MODE_ALL
                    DataStoreManager.REPEAT_MODE_OFF -> Player.REPEAT_MODE_OFF
                    else -> {
                        Player.REPEAT_MODE_OFF
                    }
                }
        }
        _nowPlaying.value = player.currentMediaItem
        mediaSessionCallback.apply {
            toggleLike = ::toggleLike
        }
        mayBeRestoreQueue()
        coroutineScope.launch {
            val shouldFadeJob =
                launch {
                    shouldFadeAudio.collectLatest {
                        if (!it) {
                            fadeJob?.cancel()
                            isFading.value = false
                            player.volume = 1f
                        }
                    }
                }
            val fadeJob =
                launch {
                    combine(
                        simpleMediaState
                            .filter { it is SimpleMediaState.Progress }
                            .map {
                                val current = (it as SimpleMediaState.Progress).progress
                                val duration = player.duration
                                if (duration > 0L) {
                                    Pair(current, player.duration)
                                } else {
                                    Pair(-1L, player.duration)
                                }
                            }.filter { it.first >= 0 && it.second > 0 },
                        isFading,
                    ) { (current, duration), fading ->
                        Triple(current, duration, fading)
                    }.collectLatest { (current, duration, fading) ->
                        if (shouldFadeAudio.value) {
                            val fadeDuration =
                                runBlocking {
                                    dataStoreManager.fadeVolume.first()
                                }
                            if (duration - current <= fadeDuration && !fading) {
                                Log.w(TAG, "Fade out $current $duration $fadeDuration $fading")
                                isFading.value = true
                                startFadeAnimator(
                                    (duration - current),
                                    20,
                                    false,
                                ) {
                                    isFading.value = false
                                }
                            }
                        }
                    }
                }
            val skipSegmentsJob =
                launch {
                    simpleMediaState
                        .filter { it is SimpleMediaState.Progress }
                        .map {
                            val current = (it as SimpleMediaState.Progress).progress
                            val duration = player.duration
                            if (duration > 0L) {
                                (current.toFloat() / player.duration) * 100
                            } else {
                                -1f
                            }
                        }.filter { it >= 0f }
                        .distinctUntilChanged()
                        .collect { current ->
                            if (dataStoreManager.sponsorBlockEnabled.first() == TRUE) {
                                if (player.duration > 0L) {
                                    val skipSegments = skipSegments.value
                                    val listCategory = dataStoreManager.getSponsorBlockCategories()
                                    if (skipSegments != null) {
                                        for (skip in skipSegments) {
                                            if (listCategory.contains(skip.category)) {
                                                val firstPart = ((skip.segment[0] / skip.videoDuration) * 100).toFloat()
                                                val secondPart =
                                                    ((skip.segment[1] / skip.videoDuration) * 100).toFloat()
                                                if (current in firstPart..secondPart) {
                                                    Log.w(
                                                        "Seek to",
                                                        secondPart.toString(),
                                                    )
                                                    Log.d("Seek to", "Cr: $current, First: $firstPart, Second: $secondPart")
                                                    skipSegment((secondPart * player.duration).toLong() / 100)
                                                    Toast
                                                        .makeText(
                                                            context,
                                                            context.getString(
                                                                R.string.sponsorblock_skip_segment,
                                                                context
                                                                    .getString(
                                                                        SPONSOR_BLOCK.listName.get(
                                                                            SPONSOR_BLOCK.list.indexOf(skip.category),
                                                                        ),
                                                                    ).lowercase(),
                                                            ),
                                                            Toast.LENGTH_SHORT,
                                                        ).show()
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                }
            val playbackJob =
                launch {
                    format.collectLatest { formatTemp ->
                        if (dataStoreManager.sendBackToGoogle.first() == TRUE) {
                            if (formatTemp != null) {
                                println("format in viewModel: $formatTemp")
                                Log.d(TAG, "Collect format ${formatTemp.videoId}")
                                Log.w(TAG, "Format expire at ${formatTemp.expiredTime}")
                                Log.i(TAG, "AtrUrl ${formatTemp.playbackTrackingAtrUrl}")
                                initPlayback(
                                    formatTemp.playbackTrackingVideostatsPlaybackUrl,
                                    formatTemp.playbackTrackingAtrUrl,
                                    formatTemp.playbackTrackingVideostatsWatchtimeUrl,
                                    formatTemp.cpn,
                                )
                            }
                        }
                    }
                }
            val playbackSpeedPitchJob =
                launch {
                    combine(dataStoreManager.playbackSpeed, dataStoreManager.pitch) { speed, pitch ->
                        Pair(speed, pitch)
                    }.collectLatest { pair ->
                        Log.w(TAG, "Playback speed: ${pair.first}, Pitch: ${pair.second}")
                        player.playbackParameters =
                            PlaybackParameters(
                                pair.first,
                                2f.pow(pair.second.toFloat() / 12),
                            )
                        Log.w(TAG, "Playback current speed: ${player.playbackParameters.speed}, Pitch: ${player.playbackParameters.pitch}")
                    }
                }
            skipSegmentsJob.join()
            playbackJob.join()
            playbackSpeedPitchJob.join()
            shouldFadeJob.join()
            fadeJob.join()
        }
    }

    private var getDataOfNowPlayingTrackStateJob: Job? = null

    private fun getDataOfNowPlayingState(mediaItem: MediaItem) {
        val videoId =
            if (mediaItem.isVideo()) {
                mediaItem.mediaId.removePrefix(MergingMediaSourceFactory.isVideo)
            } else {
                mediaItem.mediaId
            }
        val track = queueData.value?.listTracks?.find { it.videoId == videoId }
        _nowPlayingState.update {
            it.copy(
                mediaItem = mediaItem,
                track = track,
            )
        }
        updateWidget(mediaItem)
        _format.value = null
        _skipSegments.value = null
        getDataOfNowPlayingTrackStateJob?.cancel()
        getDataOfNowPlayingTrackStateJob =
            coroutineScope.launch {
                Log.w(TAG, "getDataOfNowPlayingState: $videoId")
                mainRepository.getSongById(videoId).cancellable().singleOrNull().let { songEntity ->
                    if (songEntity != null) {
                        _controlState.update { it.copy(isLiked = songEntity.liked) }
                        var thumbUrl =
                            track?.thumbnails?.lastOrNull()?.url
                                ?: "http://i.ytimg.com/vi/${songEntity.videoId}/maxresdefault.jpg"
                        if (thumbUrl.contains("w120")) {
                            thumbUrl = Regex("([wh])120").replace(thumbUrl, "$1544")
                        }
                        if (songEntity.thumbnails != thumbUrl) {
                            mainRepository.updateThumbnailsSongEntity(thumbUrl, songEntity.videoId).singleOrNull()?.let {
                                Log.w(TAG, "getDataOfNowPlayingState: Updated thumbs $it")
                            }
                        }
                        mainRepository.updateSongInLibrary(LocalDateTime.now(), songEntity.videoId).singleOrNull().let {
                            Log.w(TAG, "getDataOfNowPlayingState: $it")
                        }
                        mainRepository.updateListenCount(songEntity.videoId)
                    } else {
                        _controlState.update { it.copy(isLiked = false) }
                        mainRepository
                            .insertSong(
                                track?.toSongEntity() ?: mediaItem.toSongEntity()!!,
                            ).singleOrNull()
                            ?.let {
                                Log.w(TAG, "getDataOfNowPlayingState: $it")
                            }
                    }
                    Log.w(TAG, "getDataOfNowPlayingState: $songEntity")
                    Log.w(TAG, "getDataOfNowPlayingState: $track")
                    _nowPlayingState.update {
                        it.copy(
                            songEntity = songEntity ?: track?.toSongEntity() ?: mediaItem.toSongEntity(),
                        )
                    }
                    Log.w(TAG, "getDataOfNowPlayingState: ${nowPlayingState.value}")
                }
                songEntityJob?.cancel()
                songEntityJob =
                    coroutineScope.launch {
                        mainRepository.getSongAsFlow(videoId).cancellable().filterNotNull().collectLatest { songEntity ->
                            _nowPlayingState.update {
                                it.copy(
                                    songEntity = songEntity,
                                )
                            }
                            _controlState.update {
                                it.copy(
                                    isLiked = songEntity.liked,
                                )
                            }
                        }
                    }
                if (dataStoreManager.sponsorBlockEnabled.first() == TRUE) {
                    getSkipSegments(videoId)
                }
                if (dataStoreManager.sendBackToGoogle.first() == TRUE) {
                    getFormat(videoId)
                }
            }
    }

    private fun getSkipSegments(videoId: String) {
        _skipSegments.value = null
        coroutineScope.launch {
            mainRepository.getSkipSegments(videoId).collect { segments ->
                if (segments != null) {
                    Log.w("Check segments $videoId", segments.toString())
                    _skipSegments.value = segments
                } else {
                    _skipSegments.value = null
                }
            }
        }
    }

    private fun getFormat(mediaId: String?) {
        getFormatJob?.cancel()
        getFormatJob =
            coroutineScope.launch {
                if (mediaId != null) {
                    mainRepository.getFormatFlow(mediaId).cancellable().collectLatest { f ->
                        Log.w(TAG, "Get format for " + mediaId.toString() + ": " + f.toString())
                        if (f != null) {
                            _format.emit(f)
                        } else {
                            _format.emit(null)
                        }
                    }
                }
            }
    }

    private fun initPlayback(
        playback: String?,
        atr: String?,
        watchTime: String?,
        cpn: String?,
    ) {
        jobWatchtime?.cancel()
        coroutineScope.launch {
            if (playback != null && atr != null && watchTime != null && cpn != null) {
                watchTimeList.clear()
                mainRepository
                    .initPlayback(playback, atr, watchTime, cpn, queueData.value?.playlistId)
                    .collect {
                        if (it.first == 204) {
                            Log.d("Check initPlayback", "Success")
                            watchTimeList.add(0f)
                            watchTimeList.add(5.54f)
                            watchTimeList.add(it.second)
                            updateWatchTime()
                        }
                    }
            }
        }
    }

    private fun updateWatchTime() {
        coroutineScope.launch {
            jobWatchtime =
                launch {
                    simpleMediaState.collect { state ->
                        if (state is SimpleMediaState.Progress) {
                            val value = state.progress
                            if (value > 0 && watchTimeList.isNotEmpty()) {
                                val second = (value / 1000).toFloat()
                                if (second in watchTimeList.last()..watchTimeList.last() + 1.2f) {
                                    val watchTimeUrl =
                                        _format.value?.playbackTrackingVideostatsWatchtimeUrl
                                    val cpn = _format.value?.cpn
                                    if (second + 20.23f < (player.duration / 1000).toFloat()) {
                                        watchTimeList.add(second + 20.23f)
                                        if (watchTimeUrl != null && cpn != null) {
                                            mainRepository
                                                .updateWatchTime(
                                                    watchTimeUrl,
                                                    watchTimeList,
                                                    cpn,
                                                    queueData.value?.playlistId,
                                                ).collect { response ->
                                                    if (response == 204) {
                                                        Log.d("Check updateWatchTime", "Success")
                                                    }
                                                }
                                        }
                                    } else {
                                        watchTimeList.clear()
                                        if (watchTimeUrl != null && cpn != null) {
                                            mainRepository
                                                .updateWatchTimeFull(
                                                    watchTimeUrl,
                                                    cpn,
                                                    queueData.value?.playlistId,
                                                ).collect { response ->
                                                    if (response == 204) {
                                                        Log.d("Check updateWatchTimeFull", "Success")
                                                    }
                                                }
                                        }
                                    }
                                    Log.w("Check updateWatchTime", watchTimeList.toString())
                                }
                            }
                        }
                    }
                }
            jobWatchtime?.join()
        }
    }

    private fun toggleLike() {
        Log.w(TAG, "toggleLike: ${nowPlayingState.value.mediaItem.mediaId}")
        toggleLikeJob?.cancel()
        toggleLikeJob =
            coroutineScope.launch {
                var id = (player.currentMediaItem?.mediaId ?: "")
                if (id.contains("Video")) {
                    id = id.removePrefix("Video")
                }
                mainRepository.updateLikeStatus(
                    id,
                    if (!(controlState.first().isLiked)) 1 else 0,
                )
                delay(200)
                updateNotification()
            }
    }

    fun like(liked: Boolean) {
        _controlState.value = _controlState.value.copy(isLiked = liked)
        updateNotification()
    }

    fun resetSongAndQueue() {
        player.clearMediaItems()
        _queueData.value = null
    }

    // Set sleep timer
    fun sleepStart(minutes: Int) {
        sleepTimerJob?.cancel()
        sleepTimerJob =
            coroutineScope.launch(Dispatchers.Main) {
                _sleepTimerState.update {
                    it.copy(isDone = false, timeRemaining = minutes)
                }
                var count = minutes
                while (count > 0) {
                    delay(60 * 1000L)
                    count--
                    _sleepTimerState.update {
                        it.copy(isDone = false, timeRemaining = count)
                    }
                }
                player.pause()
                _sleepTimerState.update {
                    it.copy(isDone = true, timeRemaining = 0)
                }
            }
    }

    fun sleepStop() {
        sleepTimerJob?.cancel()
        _sleepTimerState.value = SleepTimerState(false, 0)
    }

    private fun updateNextPreviousTrackAvailability() {
        _controlState.value =
            _controlState.value.copy(
                isNextAvailable = player.hasNextMediaItem(),
                isPreviousAvailable = player.hasPreviousMediaItem(),
            )
    }

    private fun getMediaItemWithIndex(index: Int): MediaItem = player.getMediaItemAt(index)

    fun removeMediaItem(position: Int) {
        player.removeMediaItem(position)
        val temp = _queueData.value?.listTracks?.toMutableList()
        temp?.removeAt(position)
        _queueData.value =
            _queueData.value?.copy(
                listTracks = temp ?: arrayListOf(),
            )
        _currentSongIndex.value = player.currentMediaItemIndex
    }

    fun addMediaItem(
        mediaItem: MediaItem,
        playWhenReady: Boolean = true,
    ) {
        player.clearMediaItems()
        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = playWhenReady
    }

    private fun addMediaItemNotSet(mediaItem: MediaItem) {
        player.addMediaItem(mediaItem)
        if (player.mediaItemCount == 1) {
            player.prepare()
            player.playWhenReady = true
        }
        updateNextPreviousTrackAvailability()
    }

    fun addMediaItemNotSet(
        mediaItem: MediaItem,
        index: Int,
    ) {
        player.addMediaItem(index, mediaItem)
        if (player.mediaItemCount == 1) {
            player.prepare()
            player.playWhenReady = true
        }
        updateNextPreviousTrackAvailability()
    }

    fun clearMediaItems() {
        player.clearMediaItems()
    }

    fun addMediaItemList(mediaItemList: List<MediaItem>) {
        for (mediaItem in mediaItemList) {
            addMediaItemNotSet(mediaItem)
        }
        Log.d("Media Item List", "addMediaItemList: ${player.mediaItemCount}")
    }

    fun playMediaItemInMediaSource(index: Int) {
        player.seekTo(index, 0)
        player.prepare()
        player.playWhenReady = true
    }

    fun currentSongIndex(): Int = player.currentMediaItemIndex

    private fun moveMediaItem(
        fromIndex: Int,
        newIndex: Int,
    ) {
        player.moveMediaItem(fromIndex, newIndex)
        _currentSongIndex.value = player.currentMediaItemIndex
    }

    suspend fun swap(
        from: Int,
        to: Int,
    ) {
        if (from < to) {
            for (i in from until to) {
                moveItemDown(i)
            }
        } else {
            for (i in from downTo to + 1) {
                moveItemUp(i)
            }
        }
    }

    suspend fun onPlayerEvent(playerEvent: PlayerEvent) {
        when (playerEvent) {
            PlayerEvent.Backward -> player.seekBack()
            PlayerEvent.Forward -> player.seekForward()
            PlayerEvent.PlayPause -> {
                if (player.isPlaying) {
                    player.pause()
                    stopProgressUpdate()
                } else {
                    player.play()
                    startProgressUpdate()
                }
            }

            PlayerEvent.Next -> player.seekToNext()
            PlayerEvent.Previous -> player.seekToPrevious()
            PlayerEvent.Stop -> {
                stopProgressUpdate()
                player.stop()
                _nowPlayingState.value = NowPlayingTrackState.initial()
            }

            is PlayerEvent.UpdateProgress -> player.seekTo((player.duration * playerEvent.newProgress / 100).toLong())
            PlayerEvent.Shuffle -> {
                if (player.shuffleModeEnabled) {
                    player.shuffleModeEnabled = false
                    _controlState.value = _controlState.value.copy(isShuffle = false)
                } else {
                    player.shuffleModeEnabled = true
                    _controlState.value = _controlState.value.copy(isShuffle = true)
                }
            }

            PlayerEvent.Repeat -> {
                when (player.repeatMode) {
                    ExoPlayer.REPEAT_MODE_OFF -> {
                        player.repeatMode = ExoPlayer.REPEAT_MODE_ALL
                        _controlState.value = _controlState.value.copy(repeatState = RepeatState.All)
                    }

                    ExoPlayer.REPEAT_MODE_ONE -> {
                        player.repeatMode = ExoPlayer.REPEAT_MODE_OFF
                        _controlState.value = _controlState.value.copy(repeatState = RepeatState.None)
                    }

                    ExoPlayer.REPEAT_MODE_ALL -> {
                        player.repeatMode = ExoPlayer.REPEAT_MODE_ONE
                        _controlState.value = _controlState.value.copy(repeatState = RepeatState.One)
                    }

                    else -> {
                        when (controlState.first().repeatState) {
                            RepeatState.None -> {
                                player.repeatMode = ExoPlayer.REPEAT_MODE_ALL
                                _controlState.value = _controlState.value.copy(repeatState = RepeatState.All)
                            }

                            RepeatState.One -> {
                                player.repeatMode = ExoPlayer.REPEAT_MODE_ALL
                                _controlState.value = _controlState.value.copy(repeatState = RepeatState.All)
                            }

                            RepeatState.All -> {
                                player.repeatMode = ExoPlayer.REPEAT_MODE_ONE
                                _controlState.value = _controlState.value.copy(repeatState = RepeatState.One)
                            }
                        }
                    }
                }
            }

            PlayerEvent.ToggleLike -> {
                toggleLike()
            }
        }
    }

    override fun onEvents(
        player: Player,
        events: Player.Events,
    ) {
        val shouldBePlaying = !(player.playbackState == Player.STATE_ENDED || !player.playWhenReady)
        if (events.containsAny(
                Player.EVENT_PLAYBACK_STATE_CHANGED,
                Player.EVENT_PLAY_WHEN_READY_CHANGED,
                Player.EVENT_IS_PLAYING_CHANGED,
                Player.EVENT_POSITION_DISCONTINUITY,
            )
        ) {
            if (shouldBePlaying) {
                sendOpenEqualizerIntent()
            } else {
                sendCloseEqualizerIntent()
            }
        }
    }

    override fun onTracksChanged(tracks: Tracks) {
        Log.d("Tracks", "onTracksChanged: ${tracks.groups.size}")
        super.onTracksChanged(tracks)
    }

    override fun onPlayerError(error: PlaybackException) {
        when (error.errorCode) {
            PlaybackException.ERROR_CODE_TIMEOUT -> {
                Log.e("Player Error", "onPlayerError: ${error.message}")
                Toast
                    .makeText(
                        context,
                        context.getString(R.string.time_out_check_internet_connection_or_change_piped_instance_in_settings),
                        Toast.LENGTH_LONG,
                    ).show()
                player.pause()
            }

            else -> {
                Log.e("Player Error", "onPlayerError: ${error.message}")
                Toast
                    .makeText(
                        context,
                        context.getString(R.string.time_out_check_internet_connection_or_change_piped_instance_in_settings),
                        Toast.LENGTH_LONG,
                    ).show()
                player.pause()
            }
        }
    }

    override fun onMediaItemTransition(
        mediaItem: MediaItem?,
        reason: Int,
    ) {
        Log.w(TAG, "Smooth Switching Transition Current Position: ${player.currentPosition}")
        mayBeNormalizeVolume()
        Log.w(TAG, "REASON onMediaItemTransition: $reason")
        Log.d(TAG, "Media Item Transition Media Item: ${mediaItem?.mediaMetadata?.title}")
        if (mediaItem?.mediaId != _nowPlaying.value?.mediaId) {
            _nowPlaying.value = mediaItem
        }
        if (mediaItem?.mediaId != nowPlayingState.value.mediaItem.mediaId) {
            Log.w(TAG, "onMediaItemTransition: ${mediaItem?.mediaId}")
            if (mediaItem != null) {
                getDataOfNowPlayingState(mediaItem)
            } else {
                _nowPlayingState.update { NowPlayingTrackState.initial() }
            }
        }
        _queueData.value?.listTracks?.let { list ->
            if ((list.size > 3 || runBlocking { dataStoreManager.endlessQueue.first() == TRUE }) &&
                list.size - player.currentMediaItemIndex < 3 &&
                list.size - player.currentMediaItemIndex >= 0 &&
                _stateFlow.value == StateSource.STATE_INITIALIZED
            ) {
                Log.d("Check loadMore", "loadMore")
                loadMore()
            }
        }
        updateNextPreviousTrackAvailability()
        updateNotification()
        mayBeFadeInVolume()
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        super.onPlaybackStateChanged(playbackState)
        val loaded =
            player.bufferedPosition.let {
                if (it > 0) {
                    it
                } else {
                    0
                }
            }
        val current =
            player.currentPosition.let {
                if (it > 0) {
                    it
                } else {
                    0
                }
            }
        when (playbackState) {
            Player.STATE_IDLE -> {
                _simpleMediaState.value = SimpleMediaState.Initial
                Log.d(TAG, "onPlaybackStateChanged: Idle")
            }
            Player.STATE_ENDED -> {
                _simpleMediaState.value = SimpleMediaState.Ended
                Log.d(TAG, "onPlaybackStateChanged: Ended")
            }
            Player.STATE_READY -> {
                Log.d(TAG, "onPlaybackStateChanged: Ready")
                _simpleMediaState.value = SimpleMediaState.Ready(player.duration)
            }
            else -> {
                if (current >= loaded) {
                    _simpleMediaState.value = SimpleMediaState.Buffering(player.currentPosition)
                    Log.d(TAG, "onPlaybackStateChanged: Buffering")
                }
            }
        }
    }

    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
        when (shuffleModeEnabled) {
            true -> {
                _controlState.value = _controlState.value.copy(isShuffle = true)
            }

            false -> {
                _controlState.value = _controlState.value.copy(isShuffle = false)
            }
        }
        updateNextPreviousTrackAvailability()
    }

    override fun onRepeatModeChanged(repeatMode: Int) {
        when (repeatMode) {
            ExoPlayer.REPEAT_MODE_OFF -> _controlState.value = _controlState.value.copy(repeatState = RepeatState.None)
            ExoPlayer.REPEAT_MODE_ONE -> _controlState.value = _controlState.value.copy(repeatState = RepeatState.One)
            ExoPlayer.REPEAT_MODE_ALL -> _controlState.value = _controlState.value.copy(repeatState = RepeatState.All)
        }
        updateNextPreviousTrackAvailability()
        updateNotification()
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        _controlState.value = _controlState.value.copy(isPlaying = isPlaying)
        basicWidget.updatePlayingState(
            context,
            isPlaying,
        )
        if (isPlaying) {
            startProgressUpdate()
        } else {
            stopProgressUpdate()
            mayBeSaveRecentSong()
            mayBeSavePlaybackState()
        }
        updateNextPreviousTrackAvailability()
    }

    private fun startProgressUpdate() {
        progressJob =
            coroutineScope.launch {
                while (true) {
                    delay(100)
                    _simpleMediaState.value = SimpleMediaState.Progress(player.currentPosition)
                }
            }
    }

    private fun startBufferedUpdate() {
        bufferedJob =
            coroutineScope.launch {
                while (true) {
                    delay(500)
                    _simpleMediaState.value =
                        SimpleMediaState.Loading(player.bufferedPercentage, player.duration)
                }
            }
    }

    fun shufflePlaylist(randomTrackIndex: Int = 0) {
        val playlistId = _queueData.value?.playlistId ?: return
        val firstPlayedTrack = _queueData.value?.firstPlayedTrack ?: return
        coroutineScope.launch {
            if (playlistId.startsWith(LOCAL_PLAYLIST_ID)) {
                _stateFlow.value = StateSource.STATE_INITIALIZING
                mainRepository.insertSong(firstPlayedTrack.toSongEntity()).collect {
                    Log.w(TAG, "Inserted song: ${firstPlayedTrack.title}")
                }
                clearMediaItems()
                firstPlayedTrack.durationSeconds?.let {
                    mainRepository.updateDurationSeconds(it, firstPlayedTrack.videoId)
                }
                addMediaItem(firstPlayedTrack.toMediaItem(), playWhenReady = true)
                val longId = playlistId.replace(LOCAL_PLAYLIST_ID, "").toLong()
                val localPlaylist = mainRepository.getLocalPlaylist(longId).singleOrNull()
                if (localPlaylist != null) {
                    Log.w(TAG, "shufflePlaylist: Local playlist track size ${localPlaylist.tracks?.size}")
                    val trackCount = localPlaylist.tracks?.size ?: return@launch
                    val listPosition =
                        (0 until trackCount).toMutableList().apply {
                            remove(randomTrackIndex)
                        }
                    if (listPosition.size <= 0) return@launch
                    listPosition.shuffle()
                    _queueData.update {
                        it?.copy(
                            // After shuffle prefix is offset and list position
                            continuation = "SHUFFLE0_${converter.fromListIntToString(listPosition)}",
                        )
                    }
                    loadMore()
                }
            }
        }
    }

    fun loadMore() {
        // Separate local and remote data
        // Local Add Prefix to PlaylistID to differentiate between local and remote
        // Local: LC-PlaylistID
        val playlistId = _queueData.value?.playlistId ?: return
        Log.w("Check loadMore", playlistId.toString())
        val continuation = _queueData.value?.continuation
        Log.w("Check loadMore", continuation.toString())
        if (continuation != null) {
            if (playlistId.startsWith(LOCAL_PLAYLIST_ID)) {
                coroutineScope.launch {
                    _stateFlow.value = StateSource.STATE_INITIALIZING
                    val longId =
                        try {
                            playlistId.replace(LOCAL_PLAYLIST_ID, "").toLong()
                        } catch (e: NumberFormatException) {
                            return@launch
                        }
                    Log.w("Check loadMore", longId.toString())
                    if (continuation.startsWith("SHUFFLE")) {
                        val regex = Regex("(?<=SHUFFLE)\\d+(?=_)")
                        var offset = regex.find(continuation)?.value?.toInt() ?: return@launch
                        val posString = continuation.removePrefix("SHUFFLE${offset}_")
                        val listPosition = converter.fromStringToListInt(posString) ?: return@launch
                        val theLastLoad = 50 * (offset + 1) >= listPosition.size
                        mainRepository
                            .getPlaylistPairSongByListPosition(
                                longId,
                                listPosition.subList(50 * offset, if (theLastLoad) listPosition.size else 50 * (offset + 1)),
                            ).singleOrNull()
                            ?.let { pair ->
                                Log.w("Check loadMore response", pair.size.toString())
                                mainRepository.getSongsByListVideoId(pair.map { it.songId }).single().let { songs ->
                                    if (songs.isNotEmpty()) {
                                        delay(300)
                                        loadMoreCatalog(songs.toArrayListTrack())
                                        offset++
                                        _queueData.update {
                                            if (!theLastLoad) {
                                                it?.copy(
                                                    continuation = "SHUFFLE${offset}_$posString",
                                                )
                                            } else {
                                                it?.copy(
                                                    continuation = null,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                    } else if (continuation.startsWith(ASC) || continuation.startsWith(DESC)) {
                        val filter = if (continuation.startsWith(ASC)) FilterState.OlderFirst else FilterState.NewerFirst
                        val offset =
                            if (filter == FilterState.OlderFirst) {
                                continuation
                                    .removePrefix(
                                        ASC,
                                    ).toInt()
                            } else {
                                continuation.removePrefix(DESC).toInt()
                            }
                        val total =
                            mainRepository
                                .getLocalPlaylist(longId)
                                .firstOrNull()
                                ?.tracks
                                ?.size ?: 0
                        mainRepository
                            .getPlaylistPairSongByOffset(
                                longId,
                                offset,
                                filter,
                                total,
                            ).singleOrNull()
                            ?.let { pair ->
                                Log.w("Check loadMore response", pair.size.toString())
                                mainRepository.getSongsByListVideoId(pair.map { it.songId }).single().let { songs ->
                                    if (songs.isNotEmpty()) {
                                        delay(300)
                                        loadMoreCatalog(songs.toArrayListTrack())
                                        _queueData.value =
                                            _queueData.value?.copy(
                                                continuation =
                                                    if (filter ==
                                                        FilterState.OlderFirst
                                                    ) {
                                                        ASC + (offset + 1)
                                                    } else {
                                                        DESC + (offset + 1).toString()
                                                    },
                                            )
                                    } else {
                                        _stateFlow.value = StateSource.STATE_INITIALIZED
                                    }
                                }
                            }
                    }
                }
            } else {
                coroutineScope.launch {
                    _stateFlow.value = StateSource.STATE_INITIALIZING
                    Log.w(TAG, "Check loadMore continuation $continuation")
                    mainRepository
                        .getContinueTrack(playlistId, continuation)
                        .singleOrNull()
                        .let { response ->
                            val list = response?.first
                            if (list != null) {
                                Log.w(TAG, "Check loadMore response $response")
                                loadMoreCatalog(list)
                                _queueData.value =
                                    _queueData.value?.copy(
                                        continuation = response.second,
                                    )
                            } else {
                                _queueData.update {
                                    it?.copy(
                                        continuation = null,
                                    )
                                }
                                if (runBlocking { dataStoreManager.endlessQueue.first() } == TRUE) {
                                    Log.w(TAG, "loadMore: Endless Queue")
                                    val lastTrack = queueData.value?.listTracks?.lastOrNull() ?: return@launch
                                    _queueData.update {
                                        it?.copy(
                                            playlistId = "RDAMVM${lastTrack.videoId}",
                                        )
                                    }
                                    Log.d("Check loadMore", "queueData: ${queueData.value}")
                                    getRelated(lastTrack.videoId)
                                }
                            }
                        }
                }
            }
        } else if (runBlocking { dataStoreManager.endlessQueue.first() } == TRUE) {
            Log.w(TAG, "loadMore: Endless Queue")
            val lastTrack = queueData.value?.listTracks?.lastOrNull() ?: return
            _queueData.update {
                it?.copy(
                    playlistId = "RDAMVM${lastTrack.videoId}",
                )
            }
            Log.d("Check loadMore", "queueData: ${queueData.value}")
            getRelated(lastTrack.videoId)
        }
    }

    fun getRelated(videoId: String) {
        coroutineScope.launch {
            mainRepository.getRelatedData(videoId).collect { response ->
                when (response) {
                    is Resource.Success -> {
                        loadMoreCatalog(response.data?.first ?: arrayListOf())
                        _queueData.value =
                            _queueData.value?.copy(
                                continuation = response.data?.second,
                            )
                    }
                    is Resource.Error -> {
                        Log.d("Check Related", "getRelated: ${response.message}")
                        _queueData.value =
                            _queueData.value?.copy(
                                continuation = null,
                            )
                    }
                }
            }
        }
    }

    fun setQueueData(queueData: QueueData) {
        _queueData.value = queueData
    }

    fun mediaListSize(): Int = player.mediaItemCount

    fun getCurrentMediaItem(): MediaItem? = player.currentMediaItem

    private fun stopProgressUpdate() {
        progressJob?.cancel()
        Log.w(TAG, "stopProgressUpdate: ${progressJob?.isActive}")
    }

    private fun stopBufferedUpdate() {
        bufferedJob?.cancel()
        _simpleMediaState.value =
            SimpleMediaState.Loading(player.bufferedPercentage, player.duration)
    }

    override fun onIsLoadingChanged(isLoading: Boolean) {
        super.onIsLoadingChanged(isLoading)
        _simpleMediaState.value =
            SimpleMediaState.Loading(player.bufferedPercentage, player.duration)
        if (isLoading) {
            startBufferedUpdate()
        } else {
            stopBufferedUpdate()
        }
    }

    private fun mayBeFadeInVolume() {
        if (shouldFadeAudio.value) {
            startFadeAnimator(
                runBlocking { dataStoreManager.fadeVolume.first() }.toLong(),
                20,
                true,
            )
        }
    }

    private fun mayBeNormalizeVolume() {
        runBlocking {
            normalizeVolume = dataStoreManager.normalizeVolume.first() == TRUE
        }
        if (!normalizeVolume) {
            loudnessEnhancer?.enabled = false
            loudnessEnhancer?.release()
            loudnessEnhancer = null
            volumeNormalizationJob?.cancel()
            player.volume = 1f
            return
        }

        if (loudnessEnhancer == null && player.audioSessionId != C.AUDIO_SESSION_ID_UNSET) {
            loudnessEnhancer = LoudnessEnhancer(player.audioSessionId)
        }

        player.currentMediaItem?.mediaId?.let { songId ->
            val videoId =
                if (songId.contains("Video")) {
                    songId.removePrefix("Video")
                } else {
                    songId
                }
            volumeNormalizationJob?.cancel()
            volumeNormalizationJob =
                coroutineScope.launch(Dispatchers.Main) {
                    fun Float?.toMb() = ((this ?: 0f) * 100).toInt()
                    mainRepository
                        .getFormatFlow(videoId)
                        .cancellable()
                        .distinctUntilChanged()
                        .collectLatest { format ->
                            if (format != null) {
                                val loudnessMb =
                                    format.loudnessDb.toMb().let {
                                        if (it !in -2000..2000) {
                                            0
                                        } else {
                                            it
                                        }
                                    }
                                Log.d(TAG, "Loudness: ${format.loudnessDb} db, $loudnessMb")
                                try {
                                    loudnessEnhancer?.setTargetGain(0f.toMb() - loudnessMb)
                                    loudnessEnhancer?.enabled = true
                                    Log.w(
                                        TAG,
                                        "mayBeNormalizeVolume: ${loudnessEnhancer?.targetGain}",
                                    )
                                } catch (e: Exception) {
                                    Log.e(TAG, "mayBeNormalizeVolume: ${e.message}")
                                    e.printStackTrace()
                                }
                            }
                        }
                }
        }
    }

    private fun maybeSkipSilent() {
        skipSilent = runBlocking { dataStoreManager.skipSilent.first() } == TRUE
        player.skipSilenceEnabled = skipSilent
    }

    private fun mayBeRestoreQueue() {
        coroutineScope.launch {
            if (dataStoreManager.saveRecentSongAndQueue.first() == TRUE) {
                val currentPlayingTrack = mainRepository.getSongById(dataStoreManager.recentMediaId.first()).singleOrNull()?.toTrack()
                if (currentPlayingTrack != null) {
                    val queue = mainRepository.getSavedQueue().singleOrNull()
                    setQueueData(
                        QueueData(
                            listTracks = queue?.firstOrNull()?.listTrack?.toCollection(arrayListOf()) ?: arrayListOf(currentPlayingTrack),
                            firstPlayedTrack = currentPlayingTrack,
                            playlistId = LOCAL_PLAYLIST_ID_SAVED_QUEUE,
                            playlistName = dataStoreManager.playlistFromSaved.first(),
                            playlistType = PlaylistType.PLAYLIST,
                            continuation = null,
                        ),
                    )
                    var index =
                        queue?.firstOrNull()?.listTrack?.map { it.videoId }?.indexOf(
                            currentPlayingTrack.videoId,
                        )
                    if (index == null || index == -1) index = 0
                    addMediaItem(currentPlayingTrack.toMediaItem(), playWhenReady = false)
                    seekTo(dataStoreManager.recentPosition.first())
                    loadPlaylistOrAlbum(index = index)
                }
            }
        }
    }

    private fun updateWidget(nowPlaying: MediaItem) {
        basicWidget.performUpdate(
            context,
            this,
            null,
        )
        downloadImageForWidgetJob?.cancel()
        downloadImageForWidgetJob =
            coroutineScope.launch {
                val p = getScreenSize(context)
                val widgetImageSize = p.x.coerceAtMost(p.y)
                val imageRequest =
                    ImageRequest
                        .Builder(context)
                        .data(nowPlaying.mediaMetadata.artworkUri)
                        .size(widgetImageSize)
                        .placeholder(R.drawable.holder_video)
                        .target(
                            onSuccess = { drawable ->
                                basicWidget.updateImage(
                                    context,
                                    drawable.toBitmap(
                                        widgetImageSize,
                                        widgetImageSize,
                                    ),
                                )
                            },
                            onStart = { holder ->
                                if (holder != null) {
                                    basicWidget.updateImage(
                                        context,
                                        holder.toBitmap(
                                            widgetImageSize,
                                            widgetImageSize,
                                        ),
                                    )
                                }
                            },
                            onError = {
                                AppCompatResources
                                    .getDrawable(
                                        context,
                                        R.drawable.holder_video,
                                    )?.let { it1 ->
                                        basicWidget.updateImage(
                                            context,
                                            it1.toBitmap(
                                                widgetImageSize,
                                                widgetImageSize,
                                            ),
                                        )
                                    }
                            },
                        ).build()
                context.imageLoader.execute(imageRequest)
            }
    }

    fun mayBeSaveRecentSong() {
        coroutineScope.launch {
            if (dataStoreManager.saveRecentSongAndQueue.first() == TRUE) {
                dataStoreManager.saveRecentSong(
                    nowPlayingState.value.songEntity?.videoId ?: "",
                    player.contentPosition,
                )
                dataStoreManager.setPlaylistFromSaved(queueData.value?.playlistName ?: "")
                Log.d(
                    "Check saved",
                    player.currentMediaItem
                        ?.mediaMetadata
                        ?.title
                        .toString(),
                )
                val temp: ArrayList<Track> = ArrayList()
                temp.clear()
                temp.addAll(_queueData.value?.listTracks ?: arrayListOf())
                Log.w("Check recover queue", temp.toString())
                mainRepository.recoverQueue(temp)
            }
        }
    }

    fun mayBeSavePlaybackState() {
        if (runBlocking { dataStoreManager.saveStateOfPlayback.first() } == TRUE) {
            runBlocking {
                dataStoreManager.recoverShuffleAndRepeatKey(
                    player.shuffleModeEnabled,
                    player.repeatMode,
                )
            }
        }
    }

    fun editSkipSilent(skip: Boolean) {
        skipSilent = skip
        maybeSkipSilent()
    }

    fun editNormalizeVolume(normalize: Boolean) {
        normalizeVolume = normalize
    }

    private fun seekTo(position: String) {
        player.seekTo(position.toLong())
        Log.d("Check seek", "seekTo: ${player.currentPosition}")
    }

    private fun skipSegment(position: Long) {
        if (position in 0..player.duration) {
            player.seekTo(position)
        } else if (position > player.duration) {
            player.seekToNext()
        }
    }

    private fun sendOpenEqualizerIntent() {
        context.sendBroadcast(
            Intent(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION).apply {
                putExtra(AudioEffect.EXTRA_AUDIO_SESSION, player.audioSessionId)
                putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.packageName)
                putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
            },
        )
    }

    private fun sendCloseEqualizerIntent() {
        context.sendBroadcast(
            Intent(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION).apply {
                putExtra(AudioEffect.EXTRA_AUDIO_SESSION, player.audioSessionId)
            },
        )
    }

    fun release() {
        player.stop()
        player.playWhenReady = false
        player.removeListener(this)
        sendCloseEqualizerIntent()
        if (progressJob?.isActive == true) {
            progressJob?.cancel()
            progressJob = null
        }
        if (bufferedJob?.isActive == true) {
            bufferedJob?.cancel()
            bufferedJob = null
        }
        if (sleepTimerJob?.isActive == true) {
            sleepTimerJob?.cancel()
            sleepTimerJob = null
        }
        if (volumeNormalizationJob?.isActive == true) {
            volumeNormalizationJob?.cancel()
            volumeNormalizationJob = null
        }
        if (toggleLikeJob?.isActive == true) {
            toggleLikeJob?.cancel()
            toggleLikeJob = null
        }
        if (updateNotificationJob?.isActive == true) {
            updateNotificationJob?.cancel()
            updateNotificationJob = null
        }
        if (loadJob?.isActive == true) {
            loadJob?.cancel()
            loadJob = null
        }
        if (coroutineScope.isActive) {
            coroutineScope.cancel()
        }
        Log.w("Service", "scope is active: ${coroutineScope.isActive}")
    }

    @SuppressLint("PrivateResource")
    private fun updateNotification() {
        updateNotificationJob?.cancel()
        updateNotificationJob =
            coroutineScope.launch {
                var id = (player.currentMediaItem?.mediaId ?: "")
                if (id.contains("Video")) {
                    id = id.removePrefix("Video")
                }
                val liked =
                    mainRepository
                        .getSongById(id)
                        .singleOrNull()
                        ?.liked ?: false
                Log.w("Check liked", liked.toString())
                _controlState.value = _controlState.value.copy(isLiked = liked)
                setNotificationLayout?.invoke(
                    listOf(
                        CommandButton
                            .Builder()
                            .setDisplayName(
                                if (liked) {
                                    context.getString(R.string.liked)
                                } else {
                                    context.getString(
                                        R.string.like,
                                    )
                                },
                            ).setIconResId(if (liked) R.drawable.baseline_favorite_24 else R.drawable.baseline_favorite_border_24)
                            .setSessionCommand(SessionCommand(MEDIA_CUSTOM_COMMAND.LIKE, Bundle()))
                            .build(),
                        CommandButton
                            .Builder()
                            .setDisplayName(
                                when (player.repeatMode) {
                                    Player.REPEAT_MODE_ONE -> context.getString(R.string.repeat_one)

                                    Player.REPEAT_MODE_ALL -> context.getString(R.string.repeat_all)

                                    else -> context.getString(R.string.repeat_off)
                                },
                            ).setSessionCommand(
                                SessionCommand(
                                    MEDIA_CUSTOM_COMMAND.REPEAT,
                                    Bundle(),
                                ),
                            ).setIconResId(
                                when (player.repeatMode) {
                                    Player.REPEAT_MODE_ONE -> R.drawable.baseline_repeat_one_24
                                    Player.REPEAT_MODE_ALL -> R.drawable.repeat_on
                                    else -> R.drawable.baseline_repeat_24_enable
                                },
                            ).build(),
                    ),
                )
            }
    }

    fun getPlayerDuration(): Long = player.duration

    fun getProgress(): Long = player.currentPosition

//    fun changeAddedState() {
//        added.value = false
//    }
//
//    fun addFirstMetadata(it: Track) {
//        added.value = true
//        catalogMetadata.add(0, it)
//        Log.d("MusicSource", "addFirstMetadata: ${it.title}, ${catalogMetadata.size}")
//    }

    @UnstableApi
    suspend fun moveItemUp(position: Int) {
        moveMediaItem(position, position - 1)
        queueData.first()?.listTracks?.toMutableList()?.let { list ->
            val temp = list[position]
            list[position] = list[position - 1]
            list[position - 1] = temp
            _queueData.update {
                it?.copy(
                    listTracks = list,
                )
            }
        }
        _currentSongIndex.value = player.currentMediaItemIndex
    }

    @UnstableApi
    suspend fun moveItemDown(position: Int) {
        moveMediaItem(position, position + 1)
        queueData.first()?.listTracks?.toMutableList()?.let { list ->
            val temp = list[position]
            list[position] = list[position + 1]
            list[position + 1] = temp
            _queueData.update {
                it?.copy(
                    listTracks = list,
                )
            }
        }
        _currentSongIndex.value = player.currentMediaItemIndex
    }

    @UnstableApi
    fun addFirstMediaItemToIndex(
        mediaItem: MediaItem?,
        index: Int,
    ) {
        if (mediaItem != null) {
            Log.d("MusicSource", "addFirstMediaItem: ${mediaItem.mediaId}")
            moveMediaItem(0, index)
        }
    }

    fun reset() {
        _currentSongIndex.value = 0
        _stateFlow.value = StateSource.STATE_CREATED
    }

    @UnstableApi
    suspend fun load(
        downloaded: Int = 0,
        index: Int? = null,
    ) {
        updateCatalog(downloaded, index).let {
            if (index != 0 && index != null) {
                moveMediaItem(0, index)
            }
            _stateFlow.value = StateSource.STATE_INITIALIZED
        }
    }

    suspend fun loadMoreCatalog(
        listTrack: ArrayList<Track>,
        isAddToQueue: Boolean = false,
    ) {
        Log.d("Queue", listTrack.map { it.title }.toString())
        _stateFlow.value = StateSource.STATE_INITIALIZING
        val catalogMetadata: ArrayList<Track> = arrayListOf()
        for (i in 0 until listTrack.size) {
            val track = listTrack[i]
            var thumbUrl =
                track.thumbnails?.lastOrNull()?.url
                    ?: "http://i.ytimg.com/vi/${track.videoId}/maxresdefault.jpg"
            if (thumbUrl.contains("w120")) {
                thumbUrl = Regex("([wh])120").replace(thumbUrl, "$1544")
            }
            val artistName: String = track.artists.toListName().connectArtists()
            val isSong =
                (
                    track.thumbnails?.lastOrNull()?.height != 0 &&
                        track.thumbnails?.lastOrNull()?.height == track.thumbnails?.lastOrNull()?.width &&
                        track.thumbnails?.lastOrNull()?.height != null
                ) &&
                    (
                        !thumbUrl
                            .contains("hq720") &&
                            !thumbUrl
                                .contains("maxresdefault") &&
                            !thumbUrl.contains("sddefault")
                    )
            if (track.artists.isNullOrEmpty()) {
                mainRepository
                    .getSongInfo(track.videoId)
                    .singleOrNull()
                    .let { songInfo ->
                        if (songInfo != null) {
                            catalogMetadata.add(
                                track.copy(
                                    artists =
                                        listOf(
                                            Artist(
                                                songInfo.authorId,
                                                songInfo.author ?: "",
                                            ),
                                        ),
                                ),
                            )
                            addMediaItemNotSet(
                                MediaItem
                                    .Builder()
                                    .setUri(track.videoId)
                                    .setMediaId(track.videoId)
                                    .setCustomCacheKey(track.videoId)
                                    .setMediaMetadata(
                                        MediaMetadata
                                            .Builder()
                                            .setTitle(track.title)
                                            .setArtist(songInfo.author)
                                            .setArtworkUri(thumbUrl.toUri())
                                            .setAlbumTitle(track.album?.name)
                                            .setDescription(if (isSong) "Song" else "Video")
                                            .build(),
                                    ).build(),
                            )
                        } else {
                            val mediaItem =
                                MediaItem
                                    .Builder()
                                    .setMediaId(track.videoId)
                                    .setUri(track.videoId)
                                    .setCustomCacheKey(track.videoId)
                                    .setMediaMetadata(
                                        MediaMetadata
                                            .Builder()
                                            .setArtworkUri(thumbUrl.toUri())
                                            .setAlbumTitle(track.album?.name)
                                            .setTitle(track.title)
                                            .setArtist("Various Artists")
                                            .setDescription(if (isSong) "Song" else "Video")
                                            .build(),
                                    ).build()
                            addMediaItemNotSet(mediaItem)
                            catalogMetadata.add(
                                track.copy(
                                    artists = listOf(Artist("", "Various Artists")),
                                ),
                            )
                        }
                    }
            } else {
                addMediaItemNotSet(
                    MediaItem
                        .Builder()
                        .setUri(track.videoId)
                        .setMediaId(track.videoId)
                        .setCustomCacheKey(track.videoId)
                        .setMediaMetadata(
                            MediaMetadata
                                .Builder()
                                .setTitle(track.title)
                                .setArtist(artistName)
                                .setArtworkUri(thumbUrl.toUri())
                                .setAlbumTitle(track.album?.name)
                                .setDescription(if (isSong) "Song" else "Video")
                                .build(),
                        ).build(),
                )
                catalogMetadata.add(track)
            }
            Log.d(
                "MusicSource",
                "updateCatalog: ${track.title}, ${catalogMetadata.size}",
            )
            Log.d("MusicSource", "updateCatalog: ${track.title}")
        }
        if (!player.isPlaying && isAddToQueue) {
            player.playWhenReady = false
        }
        _queueData.value = _queueData.value?.addTrackList(catalogMetadata)
        _stateFlow.value = StateSource.STATE_INITIALIZED
    }

    @UnstableApi
    suspend fun updateCatalog(
        downloaded: Int = 0,
        index: Int? = null,
    ): Boolean {
        _stateFlow.value = StateSource.STATE_INITIALIZING
        val tempQueue: ArrayList<Track> = arrayListOf()
        tempQueue.addAll(_queueData.value?.listTracks ?: arrayListOf())
        val chunkedList = tempQueue.chunked(100)
        // Reset queue
        _queueData.update {
            it?.copy(
                listTracks = arrayListOf(),
            )
        }
        val current = if (index != null) tempQueue.getOrNull(index) else null
        chunkedList.forEach { list ->
            val catalogMetadata: ArrayList<Track> = arrayListOf()
            Log.w("SimpleMediaServiceHandler", "Catalog size: ${tempQueue.size}")
            Log.w("SimpleMediaServiceHandler", "Skip index: $index")
            for (i in list.indices) {
                val track = list[i]
                if (track == current) continue
                var thumbUrl =
                    track.thumbnails?.lastOrNull()?.url
                        ?: "http://i.ytimg.com/vi/${track.videoId}/maxresdefault.jpg"
                if (thumbUrl.contains("w120")) {
                    thumbUrl = Regex("([wh])120").replace(thumbUrl, "$1544")
                }
                val isSong =
                    (
                        track.thumbnails?.lastOrNull()?.height != 0 &&
                            track.thumbnails?.lastOrNull()?.height == track.thumbnails?.lastOrNull()?.width &&
                            track.thumbnails?.lastOrNull()?.height != null
                    ) &&
                        (
                            !thumbUrl
                                .contains("hq720") &&
                                !thumbUrl
                                    .contains("maxresdefault") &&
                                !thumbUrl.contains("sddefault")
                        )
                if (downloaded == 1) {
                    if (track.artists.isNullOrEmpty()) {
                        mainRepository.getSongInfo(track.videoId).singleOrNull().let { songInfo ->
                            if (songInfo != null) {
                                val mediaItem =
                                    MediaItem
                                        .Builder()
                                        .setMediaId(track.videoId)
                                        .setUri(track.videoId)
                                        .setCustomCacheKey(track.videoId)
                                        .setMediaMetadata(
                                            MediaMetadata
                                                .Builder()
                                                .setArtworkUri(thumbUrl.toUri())
                                                .setAlbumTitle(track.album?.name)
                                                .setTitle(track.title)
                                                .setArtist(songInfo.author)
                                                .setDescription(if (isSong) "Song" else "Video")
                                                .build(),
                                        ).build()
                                addMediaItemNotSet(mediaItem)
                                catalogMetadata.add(
                                    track.copy(
                                        artists =
                                            listOf(
                                                Artist(
                                                    songInfo.authorId,
                                                    songInfo.author ?: "",
                                                ),
                                            ),
                                    ),
                                )
                            } else {
                                val mediaItem =
                                    MediaItem
                                        .Builder()
                                        .setMediaId(track.videoId)
                                        .setUri(track.videoId)
                                        .setCustomCacheKey(track.videoId)
                                        .setMediaMetadata(
                                            MediaMetadata
                                                .Builder()
                                                .setArtworkUri(thumbUrl.toUri())
                                                .setAlbumTitle(track.album?.name)
                                                .setTitle(track.title)
                                                .setArtist("Various Artists")
                                                .setDescription(if (isSong) "Song" else "Video")
                                                .build(),
                                        ).build()
                                addMediaItemNotSet(mediaItem)
                                catalogMetadata.add(
                                    track.copy(
                                        artists = listOf(Artist("", "Various Artists")),
                                    ),
                                )
                            }
                        }
                    } else {
                        val mediaItem =
                            MediaItem
                                .Builder()
                                .setMediaId(track.videoId)
                                .setUri(track.videoId)
                                .setCustomCacheKey(track.videoId)
                                .setMediaMetadata(
                                    MediaMetadata
                                        .Builder()
                                        .setArtworkUri(thumbUrl.toUri())
                                        .setAlbumTitle(track.album?.name)
                                        .setTitle(track.title)
                                        .setArtist(track.artists.toListName().connectArtists())
                                        .setDescription(if (isSong) "Song" else "Video")
                                        .build(),
                                ).build()
                        addMediaItemNotSet(mediaItem)
                        catalogMetadata.add(track)
                    }
                    Log.d("MusicSource", "updateCatalog: ${track.title}, ${catalogMetadata.size}")
                } else {
                    val artistName: String = track.artists.toListName().connectArtists()
                    if (track.artists.isNullOrEmpty()) {
                        mainRepository
                            .getSongInfo(track.videoId)
                            .cancellable()
                            .first()
                            .let { songInfo ->
                                if (songInfo != null) {
                                    catalogMetadata.add(
                                        track.copy(
                                            artists =
                                                listOf(
                                                    Artist(
                                                        songInfo.authorId,
                                                        songInfo.author ?: "",
                                                    ),
                                                ),
                                        ),
                                    )
                                    addMediaItemNotSet(
                                        MediaItem
                                            .Builder()
                                            .setUri(track.videoId)
                                            .setMediaId(track.videoId)
                                            .setCustomCacheKey(track.videoId)
                                            .setMediaMetadata(
                                                MediaMetadata
                                                    .Builder()
                                                    .setTitle(track.title)
                                                    .setArtist(songInfo.author)
                                                    .setArtworkUri(thumbUrl.toUri())
                                                    .setDescription(if (isSong) "Song" else "Video")
                                                    .setAlbumTitle(track.album?.name)
                                                    .build(),
                                            ).build(),
                                    )
                                } else {
                                    val mediaItem =
                                        MediaItem
                                            .Builder()
                                            .setMediaId(track.videoId)
                                            .setUri(track.videoId)
                                            .setCustomCacheKey(track.videoId)
                                            .setMediaMetadata(
                                                MediaMetadata
                                                    .Builder()
                                                    .setArtworkUri(thumbUrl.toUri())
                                                    .setAlbumTitle(track.album?.name)
                                                    .setTitle(track.title)
                                                    .setDescription(if (isSong) "Song" else "Video")
                                                    .setArtist("Various Artists")
                                                    .build(),
                                            ).build()
                                    addMediaItemNotSet(mediaItem)
                                    catalogMetadata.add(
                                        track.copy(
                                            artists = listOf(Artist("", "Various Artists")),
                                        ),
                                    )
                                }
                            }
                    } else {
                        addMediaItemNotSet(
                            MediaItem
                                .Builder()
                                .setUri(track.videoId)
                                .setMediaId(track.videoId)
                                .setCustomCacheKey(track.videoId)
                                .setMediaMetadata(
                                    MediaMetadata
                                        .Builder()
                                        .setTitle(track.title)
                                        .setArtist(artistName)
                                        .setArtworkUri(thumbUrl.toUri())
                                        .setAlbumTitle(track.album?.name)
                                        .setDescription(if (isSong) "Song" else "Video")
                                        .build(),
                                ).build(),
                        )
                        catalogMetadata.add(track)
                    }
                    Log.d(
                        "MusicSource",
                        "updateCatalog: ${track.title}, ${catalogMetadata.size}",
                    )
                    Log.d("MusicSource", "updateCatalog: ${track.title}")
                }
            }
            _queueData.update {
                it?.addTrackList(catalogMetadata)
            }
            delay(200)
        }
        if (current != null && index != null) {
            _queueData.update {
                it?.addToIndex(current, index)
            }
        }
        Log.w("SimpleMediaServiceHandler", "current queue: ${player.mediaItemCount}")
        return true
    }

    fun addQueueToPlayer() {
        loadJob?.cancel()
        loadJob =
            coroutineScope.launch {
                load()
            }
    }

    fun loadPlaylistOrAlbum(index: Int? = null) {
        loadJob?.cancel()
        loadJob =
            coroutineScope.launch {
                load(index = index)
            }
    }

    fun setCurrentSongIndex(index: Int) {
        _currentSongIndex.value = index
    }

    fun updateSubtitle(url: String) {
        val index = player.currentMediaItemIndex
        val mediaItem = player.currentMediaItem
        Log.w("Subtitle", "updateSubtitle: $url")
        val subtitle =
            SubtitleConfiguration
                .Builder(url.plus("&fmt=ttml").toUri())
                .setId(mediaItem?.mediaId)
                .setMimeType(MimeTypes.APPLICATION_TTML)
                .setLanguage("en")
                .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                .build()
        val new = mediaItem?.buildUpon()?.setSubtitleConfigurations(listOf(subtitle))?.build()
        if (new != null) {
            player.replaceMediaItem(
                index,
                new,
            )
            println(
                "update subtitle" +
                    player.currentMediaItem
                        ?.localConfiguration
                        ?.subtitleConfigurations
                        ?.firstOrNull()
                        ?.uri
                        .toString(),
            )
        }
    }

    suspend fun playNext(track: Track) {
        _stateFlow.value = StateSource.STATE_INITIALIZING
        val catalogMetadata: ArrayList<Track> = queueData.first()?.listTracks?.toCollection(arrayListOf()) ?: arrayListOf()
        var thumbUrl =
            track.thumbnails?.lastOrNull()?.url
                ?: "http://i.ytimg.com/vi/${track.videoId}/maxresdefault.jpg"
        if (thumbUrl.contains("w120")) {
            thumbUrl = Regex("([wh])120").replace(thumbUrl, "$1544")
        }
        val artistName: String = track.artists.toListName().connectArtists()
        val isSong =
            (
                track.thumbnails?.lastOrNull()?.height != 0 &&
                    track.thumbnails?.lastOrNull()?.height == track.thumbnails?.lastOrNull()?.width &&
                    track.thumbnails?.lastOrNull()?.height != null
            ) &&
                (
                    !thumbUrl
                        .contains("hq720") &&
                        !thumbUrl
                            .contains("maxresdefault") &&
                        !thumbUrl.contains("sddefault")
                )
        if ((player.currentMediaItemIndex + 1 in 0..(queueData.first()?.listTracks?.size ?: 0))) {
            if (track.artists.isNullOrEmpty()) {
                mainRepository.getSongInfo(track.videoId).cancellable().first().let { songInfo ->
                    if (songInfo != null) {
                        catalogMetadata.add(
                            player.currentMediaItemIndex + 1,
                            track.copy(
                                artists =
                                    listOf(
                                        Artist(
                                            songInfo.authorId,
                                            songInfo.author ?: "",
                                        ),
                                    ),
                            ),
                        )
                        addMediaItemNotSet(
                            MediaItem
                                .Builder()
                                .setUri(track.videoId)
                                .setMediaId(track.videoId)
                                .setCustomCacheKey(track.videoId)
                                .setMediaMetadata(
                                    MediaMetadata
                                        .Builder()
                                        .setTitle(track.title)
                                        .setArtist(songInfo.author)
                                        .setArtworkUri(thumbUrl.toUri())
                                        .setAlbumTitle(track.album?.name)
                                        .setDescription(if (isSong) "Song" else "Video")
                                        .build(),
                                ).build(),
                            player.currentMediaItemIndex + 1,
                        )
                    } else {
                        val mediaItem =
                            MediaItem
                                .Builder()
                                .setMediaId(track.videoId)
                                .setUri(track.videoId)
                                .setCustomCacheKey(track.videoId)
                                .setMediaMetadata(
                                    MediaMetadata
                                        .Builder()
                                        .setArtworkUri(thumbUrl.toUri())
                                        .setAlbumTitle(track.album?.name)
                                        .setTitle(track.title)
                                        .setArtist("Various Artists")
                                        .setDescription(if (isSong) "Song" else "Video")
                                        .build(),
                                ).build()
                        addMediaItemNotSet(mediaItem, player.currentMediaItemIndex + 1)
                        catalogMetadata.add(
                            player.currentMediaItemIndex + 1,
                            track.copy(
                                artists = listOf(Artist("", "Various Artists")),
                            ),
                        )
                    }
                }
            } else {
                addMediaItemNotSet(
                    MediaItem
                        .Builder()
                        .setUri(track.videoId)
                        .setMediaId(track.videoId)
                        .setCustomCacheKey(track.videoId)
                        .setMediaMetadata(
                            MediaMetadata
                                .Builder()
                                .setTitle(track.title)
                                .setArtist(artistName)
                                .setArtworkUri(thumbUrl.toUri())
                                .setAlbumTitle(track.album?.name)
                                .setDescription(if (isSong) "Song" else "Video")
                                .build(),
                        ).build(),
                    player.currentMediaItemIndex + 1,
                )
                catalogMetadata.add(player.currentMediaItemIndex + 1, track)
            }
            Log.d(
                "MusicSource",
                "updateCatalog: ${track.title}, ${catalogMetadata.size}",
            )
            Log.d("MusicSource", "updateCatalog: ${track.title}")
            _queueData.value =
                queueData.first()?.copy(
                    listTracks = catalogMetadata,
                )
        }
        _stateFlow.value = StateSource.STATE_INITIALIZED
    }

    private fun startFadeAnimator(
        duration: Long,
        steps: Int = 10,
        fadeIn: Boolean,
        callback: suspend () -> Unit = {},
    ) {
        fadeJob?.cancel()
        fadeJob =
            coroutineScope.launch(Dispatchers.Main) {
                Log.w(TAG, "startFadeAnimator")
                val delay = duration / steps
                if (duration == 0L) {
                    callback.invoke()
                    return@launch
                }
                var startValue = if (fadeIn) 0f else 1f
                val endValue = if (fadeIn) 1f else 0f
                if (fadeIn) player.volume = startValue
                var currentAnim = 0f
                while (currentAnim <= duration) {
                    if (fadeIn && player.currentPosition > duration) {
                        player.volume = endValue
                        Log.w(TAG, "startFadeAnimator current value: $endValue")
                        callback.invoke()
                        return@launch
                    } else if (!fadeIn && (player.duration - player.currentPosition) > duration) {
                        player.volume = endValue
                        Log.w(TAG, "startFadeAnimator current value: $endValue")
                        callback.invoke()
                        return@launch
                    }
                    currentAnim += delay
                    Log.w(TAG, "startFadeAnimator anim $currentAnim")
                    startValue =
                        if (fadeIn) {
                            (currentAnim / duration).toFloat()
                        } else {
                            1f - (currentAnim / duration).toFloat()
                        }
                    player.volume =
                        if (startValue > 1f) {
                            1f
                        } else if (startValue < 0f) {
                            0f
                        } else {
                            startValue
                        }
                    Log.w(TAG, "startFadeAnimator current value: $startValue")
                    delay(delay)
                }
                player.volume = endValue
                callback.invoke()
            }
    }
}

sealed class RepeatState {
    data object None : RepeatState()

    data object All : RepeatState()

    data object One : RepeatState()
}

sealed class PlayerEvent {
    data object PlayPause : PlayerEvent()

    data object Backward : PlayerEvent()

    data object Forward : PlayerEvent()

    data object Stop : PlayerEvent()

    data object Next : PlayerEvent()

    data object Previous : PlayerEvent()

    data object Shuffle : PlayerEvent()

    data object Repeat : PlayerEvent()

    data class UpdateProgress(
        val newProgress: Float,
    ) : PlayerEvent()

    data object ToggleLike : PlayerEvent()
}

sealed class SimpleMediaState {
    data object Initial : SimpleMediaState()

    data object Ended : SimpleMediaState()

    data class Ready(
        val duration: Long,
    ) : SimpleMediaState()

    data class Loading(
        val bufferedPercentage: Int,
        val duration: Long,
    ) : SimpleMediaState()

    data class Progress(
        val progress: Long,
    ) : SimpleMediaState()

    data class Buffering(
        val position: Long,
    ) : SimpleMediaState()
}

data class NowPlayingTrackState(
    val mediaItem: MediaItem,
    val track: Track?,
    val songEntity: SongEntity?,
) {
    fun isNotEmpty(): Boolean = this != initial()

    companion object {
        fun initial(): NowPlayingTrackState =
            NowPlayingTrackState(
                mediaItem = EMPTY,
                track = null,
                songEntity = null,
            )
    }
}

enum class StateSource {
    STATE_CREATED,
    STATE_INITIALIZING,
    STATE_INITIALIZED,
    STATE_ERROR,
}

data class ControlState(
    val isPlaying: Boolean,
    val isShuffle: Boolean,
    val repeatState: RepeatState,
    val isLiked: Boolean,
    val isNextAvailable: Boolean,
    val isPreviousAvailable: Boolean,
)

data class QueueData(
    val listTracks: List<Track> = arrayListOf(),
    val firstPlayedTrack: Track? = null,
    val playlistId: String? = null,
    val playlistName: String? = null,
    val playlistType: PlaylistType? = null,
    val continuation: String? = null,
) {
    fun addTrackList(tracks: Collection<Track>): QueueData {
        val temp = listTracks.toMutableList()
        temp.addAll(tracks)
        return this.copy(
            listTracks = temp,
        )
    }

    fun addToIndex(
        track: Track,
        index: Int,
    ): QueueData {
        val temp = listTracks.toMutableList()
        temp.add(index, track)
        return this.copy(
            listTracks = temp,
        )
    }

    fun removeFirstTrackForPlaylistAndAlbum(): QueueData {
        val temp = listTracks.toMutableList()
        temp.removeAt(0)
        return this.copy(
            listTracks = temp,
        )
    }

    fun removeTrackWithIndex(index: Int): QueueData {
        val temp = listTracks.toMutableList()
        temp.removeAt(index)
        return this.copy(
            listTracks = temp,
        )
    }

    fun setContinuation(continuation: String): QueueData =
        this.copy(
            continuation = continuation,
        )

    fun isLocalPlaylist(): Boolean = playlistType == PlaylistType.LOCAL_PLAYLIST

    fun isRadio(): Boolean = playlistType == PlaylistType.RADIO

    fun isPlaylist(): Boolean = playlistType == PlaylistType.PLAYLIST
}

/**
 * @param isDone whether the timer is done to make a notification
 * @param timeRemaining the time remaining in minutes
 */

data class SleepTimerState(
    val isDone: Boolean,
    val timeRemaining: Int,
)

enum class PlaylistType {
    PLAYLIST,
    LOCAL_PLAYLIST,
    RADIO,
}