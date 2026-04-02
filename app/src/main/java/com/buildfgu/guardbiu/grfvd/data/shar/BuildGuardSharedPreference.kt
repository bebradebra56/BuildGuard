package com.buildfgu.guardbiu.grfvd.data.shar

import android.content.Context
import androidx.core.content.edit

class BuildGuardSharedPreference(context: Context) {
    private val buildGuardPrefs = context.getSharedPreferences("buildGuardSharedPrefsAb", Context.MODE_PRIVATE)

    var buildGuardSavedUrl: String
        get() = buildGuardPrefs.getString(BUILD_GUARD_SAVED_URL, "") ?: ""
        set(value) = buildGuardPrefs.edit { putString(BUILD_GUARD_SAVED_URL, value) }

    var buildGuardExpired : Long
        get() = buildGuardPrefs.getLong(BUILD_GUARD_EXPIRED, 0L)
        set(value) = buildGuardPrefs.edit { putLong(BUILD_GUARD_EXPIRED, value) }

    var buildGuardAppState: Int
        get() = buildGuardPrefs.getInt(BUILD_GUARD_APPLICATION_STATE, 0)
        set(value) = buildGuardPrefs.edit { putInt(BUILD_GUARD_APPLICATION_STATE, value) }

    var buildGuardNotificationRequest: Long
        get() = buildGuardPrefs.getLong(BUILD_GUARD_NOTIFICAITON_REQUEST, 0L)
        set(value) = buildGuardPrefs.edit { putLong(BUILD_GUARD_NOTIFICAITON_REQUEST, value) }


    var buildGuardNotificationState:Int
        get() = buildGuardPrefs.getInt(BUILD_GUARD_NOTIFICATION_STATE, 0)
        set(value) = buildGuardPrefs.edit { putInt(BUILD_GUARD_NOTIFICATION_STATE, value) }

    companion object {
        private const val BUILD_GUARD_NOTIFICATION_STATE = "buildGuardNotificationState"
        private const val BUILD_GUARD_SAVED_URL = "buildGuardSavedUrl"
        private const val BUILD_GUARD_EXPIRED = "buildGuardExpired"
        private const val BUILD_GUARD_APPLICATION_STATE = "buildGuardApplicationState"
        private const val BUILD_GUARD_NOTIFICAITON_REQUEST = "buildGuardNotificationRequest"
    }
}