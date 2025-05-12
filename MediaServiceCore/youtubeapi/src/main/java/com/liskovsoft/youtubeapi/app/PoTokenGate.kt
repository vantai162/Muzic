package com.liskovsoft.youtubeapi.app

import android.annotation.TargetApi
import android.os.Build.VERSION
import com.liskovsoft.sharedutils.helpers.DeviceHelpers
import com.liskovsoft.youtubeapi.app.potokencloud.PoTokenCloudService
import com.liskovsoft.youtubeapi.app.potokennp2.PoTokenProviderImpl
import com.liskovsoft.youtubeapi.app.potokennp2.misc.PoTokenResult

internal object PoTokenGate {
    private var mNpPoToken: PoTokenResult? = null
    private var mCacheResetTimeMs: Long = -1

    @TargetApi(19)
    @JvmStatic
    fun getContentPoToken(videoId: String): String? {
        if (mNpPoToken?.videoId == videoId) {
            return mNpPoToken?.playerRequestPoToken
        }

        mNpPoToken = if (supportsNpPot())
            PoTokenProviderImpl.getWebClientPoToken(videoId)
        else null

        return mNpPoToken?.playerRequestPoToken
    }

    @JvmStatic
    fun getSessionPoToken(): String? {
        return if (supportsNpPot()) {
            if (mNpPoToken == null)
                mNpPoToken = PoTokenProviderImpl.getWebClientPoToken("")
            mNpPoToken?.streamingDataPoToken
        } else PoTokenCloudService.getPoToken()
    }

    @JvmStatic
    fun updatePoToken() {
        if (supportsNpPot()) {
            //mNpPoToken = null // only refresh
            mNpPoToken = PoTokenProviderImpl.getWebClientPoToken("") // refresh and preload
        } else {
            PoTokenCloudService.updatePoToken()
        }
    }

    @JvmStatic
    fun getVisitorData(): String? {
        return mNpPoToken?.visitorData
    }

    @JvmStatic
    fun supportsNpPot() = VERSION.SDK_INT >= 19 && DeviceHelpers.supportsWebView() && !isWebViewBroken()

    private fun isWebViewBroken(): Boolean = VERSION.SDK_INT == 19 && DeviceHelpers.isTCL() // "TCL TV - Harman"

    @TargetApi(19)
    @JvmStatic
    fun resetCache(): Boolean {
        if (System.currentTimeMillis() < mCacheResetTimeMs)
            return false

        if (supportsNpPot()) {
            mNpPoToken = null
            //PoTokenProviderImpl.resetCache()
        } else
            PoTokenCloudService.resetCache()

        mCacheResetTimeMs = System.currentTimeMillis() + 60_000

        return true
    }
}