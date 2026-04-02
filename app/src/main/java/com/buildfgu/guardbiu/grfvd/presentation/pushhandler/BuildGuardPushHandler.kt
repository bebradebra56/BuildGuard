package com.buildfgu.guardbiu.grfvd.presentation.pushhandler

import android.os.Bundle
import android.util.Log
import com.buildfgu.guardbiu.grfvd.presentation.app.BuildGuardApplication

class BuildGuardPushHandler {
    fun buildGuardHandlePush(extras: Bundle?) {
        Log.d(BuildGuardApplication.BUILD_GUARD_MAIN_TAG, "Extras from Push = ${extras?.keySet()}")
        if (extras != null) {
            val map = buildGuardBundleToMap(extras)
            Log.d(BuildGuardApplication.BUILD_GUARD_MAIN_TAG, "Map from Push = $map")
            map?.let {
                if (map.containsKey("url")) {
                    BuildGuardApplication.BUILD_GUARD_FB_LI = map["url"]
                    Log.d(BuildGuardApplication.BUILD_GUARD_MAIN_TAG, "UrlFromActivity = $map")
                }
            }
        } else {
            Log.d(BuildGuardApplication.BUILD_GUARD_MAIN_TAG, "Push data no!")
        }
    }

    private fun buildGuardBundleToMap(extras: Bundle): Map<String, String?>? {
        val map: MutableMap<String, String?> = HashMap()
        val ks = extras.keySet()
        val iterator: Iterator<String> = ks.iterator()
        while (iterator.hasNext()) {
            val key = iterator.next()
            map[key] = extras.getString(key)
        }
        return map
    }

}