package com.liskovsoft.youtubeapi.common.helpers

internal enum class PostDataType { Default, Player, Browse }

internal class QueryBuilder(val client: AppClient) {
    private var type: PostDataType? = null
    private var acceptLanguage: String? = null
    private var acceptRegion: String? = null
    private var utcOffsetMinutes: Int? = null
    private var visitorData: String? = null
    private var cpn: String? = null
    private var videoId: String? = null
    private var clickTrackingParams: String? = null
    private var poToken: String? = null
    private var signatureTimestamp: Int? = null
    private var isWebEmbedded: Boolean = false

    fun setType(type: PostDataType) = apply { this.type = type }
    fun setLanguage(lang: String?) = apply { acceptLanguage = lang }
    fun setCountry(country: String?) = apply { acceptRegion = country }
    fun setUtcOffsetMinutes(offset: Int?) = apply { utcOffsetMinutes = offset }
    fun setVideoId(videoId: String?) = apply { this.videoId = videoId }
    fun setPoToken(poToken: String?) = apply { this.poToken = poToken }
    fun setClientPlaybackNonce(cpn: String?) = apply { this.cpn = cpn }
    fun setSignatureTimestamp(timestamp: Int?) = apply { signatureTimestamp = timestamp }
    fun setClickTrackingParams(params: String?) = apply { clickTrackingParams = params }
    fun setVisitorData(visitorData: String?) = apply { this.visitorData = visitorData }
    fun setAsWebEmbedded(isWebEmbedded: Boolean) = apply { this.isWebEmbedded = isWebEmbedded }

    fun build(): String {
        val json = """
             {
                "context": {
                     ${createClientChunk()}
                     ${createClickTrackingChunk() ?: ""}
                     ${createUserChunk()}
                     ${createWebEmbeddedChunk() ?: ""}
                },
                "racyCheckOk": true,
                "contentCheckOk": true,
                ${createCheckParamsChunk() ?: ""}
                ${createPotChunk() ?: ""}
                ${createVideoIdChunk() ?: ""}
             }
        """

        // Remove all indentations
        val result = buildString {
            json.lineSequence().forEach { append(it.trim()) }
        }

        return result
    }

    private fun createClientChunk(): String {
        val clientVars = """
            "clientName": "${client.clientName}",
            "clientVersion": "${client.clientVersion}",
            "clientScreen": "${client.clientScreen}",
            "userAgent": "${client.userAgent}",
        """
        val postVars = client.postData
        val browseVars = if (requireNotNull(type) == PostDataType.Browse)
            """
                "tvAppInfo": { 
                    "appQuality": "TV_APP_QUALITY_FULL_ANIMATION",
                    "zylonLeftNav": true
                },
                "browserName": "Cobalt",
                "webpSupport": false,
                "animatedWebpSupport": true,
            """ // Include Shorts: "browserName":"Cobalt"
            else null
        val regionVars = """
            "acceptLanguage": "${requireNotNull(acceptLanguage)}",
            "acceptRegion": "${requireNotNull(acceptRegion)}",
            "utcOffsetMinutes": "${requireNotNull(utcOffsetMinutes)}",
        """
        val visitorVar = visitorData?.let { """ "visitorData": "$visitorData" """ }
        return """
             "client": {
                $clientVars
                ${postVars ?: ""}
                ${browseVars ?: ""}
                $regionVars
                ${visitorVar ?: ""}
             },
        """
    }

    private fun createClickTrackingChunk(): String? {
        return clickTrackingParams?.let {
            """
                "clickTracking": {
                    "clickTrackingParams": "$it"
                },
            """
        }
    }

    private fun createWebEmbeddedChunk(): String? {
        return if (isWebEmbedded)
            """
                "thirdParty": {
                    "embedUrl": "https://www.youtube.com/embed/${requireNotNull(videoId)}"
                },
            """
           else null
    }

    private fun createUserChunk(): String {
        return """
           "user":{
                "enableSafetyMode": false,
                "lockedSafetyMode":false
           }, 
        """
    }

    private fun createPotChunk(): String? {
        return poToken?.let {
            """
               "serviceIntegrityDimensions": {
                    "poToken": "$it"
               }, 
            """
        }
    }

    private fun createVideoIdChunk(): String? {
        return videoId?.let {
            """
                "videoId": "$it",
                "cpn": "${requireNotNull(cpn)}",
            """
        }
    }

    private fun createCheckParamsChunk(): String? {
        return signatureTimestamp?.let {
            """
                "playbackContext": {
                    "contentPlaybackContext": {
                        "html5Preference": "HTML5_PREF_WANTS",
                        "lactMilliseconds": 60000,
                        "signatureTimestamp": $it
                    }
                },
            """
        }
    }
}