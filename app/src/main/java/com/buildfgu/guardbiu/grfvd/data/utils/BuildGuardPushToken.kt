package com.buildfgu.guardbiu.grfvd.data.utils

import android.util.Log
import com.buildfgu.guardbiu.grfvd.presentation.app.BuildGuardApplication
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class BuildGuardPushToken {

    suspend fun buildGuardGetToken(
        buildGuardMaxAttempts: Int = 3,
        buildGuardDelayMs: Long = 1500
    ): String {

        repeat(buildGuardMaxAttempts - 1) {
            try {
                val buildGuardToken = FirebaseMessaging.getInstance().token.await()
                return buildGuardToken
            } catch (e: Exception) {
                Log.e(BuildGuardApplication.BUILD_GUARD_MAIN_TAG, "Token error (attempt ${it + 1}): ${e.message}")
                delay(buildGuardDelayMs)
            }
        }

        return try {
            FirebaseMessaging.getInstance().token.await()
        } catch (e: Exception) {
            Log.e(BuildGuardApplication.BUILD_GUARD_MAIN_TAG, "Token error final: ${e.message}")
            "null"
        }
    }


}