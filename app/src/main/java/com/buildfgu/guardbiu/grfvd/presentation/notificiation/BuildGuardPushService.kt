package com.buildfgu.guardbiu.grfvd.presentation.notificiation

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.os.bundleOf
import com.buildfgu.guardbiu.BuildGuardActivity
import com.buildfgu.guardbiu.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.buildfgu.guardbiu.grfvd.presentation.app.BuildGuardApplication

private const val BUILD_GUARD_CHANNEL_ID = "build_guard_notifications"
private const val BUILD_GUARD_CHANNEL_NAME = "BuildGuard Notifications"
private const val BUILD_GUARD_NOT_TAG = "BuildGuard"

class BuildGuardPushService : FirebaseMessagingService(){
    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Обработка notification payload
        remoteMessage.notification?.let {
            if (remoteMessage.data.contains("url")) {
                buildGuardShowNotification(it.title ?: BUILD_GUARD_NOT_TAG, it.body ?: "", data = remoteMessage.data["url"])
            } else {
                buildGuardShowNotification(it.title ?: BUILD_GUARD_NOT_TAG, it.body ?: "", data = null)
            }
        }

        // Обработка data payload
        if (remoteMessage.data.isNotEmpty()) {
            buildGuardHandleDataPayload(remoteMessage.data)
        }
    }

    private fun buildGuardShowNotification(title: String, message: String, data: String?) {
        val buildGuardNotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Создаем канал уведомлений для Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                BUILD_GUARD_CHANNEL_ID,
                BUILD_GUARD_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            buildGuardNotificationManager.createNotificationChannel(channel)
        }

        val buildGuardIntent = Intent(this, BuildGuardActivity::class.java).apply {
            putExtras(bundleOf(
                "url" to data
            ))
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val buildGuardPendingIntent = PendingIntent.getActivity(
            this,
            0,
            buildGuardIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val buildGuardNotification = NotificationCompat.Builder(this, BUILD_GUARD_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.build_guard_noti_ic)
            .setAutoCancel(true)
            .setContentIntent(buildGuardPendingIntent)
            .build()

        buildGuardNotificationManager.notify(System.currentTimeMillis().toInt(), buildGuardNotification)
    }

    private fun buildGuardHandleDataPayload(data: Map<String, String>) {
        data.forEach { (key, value) ->
            Log.d(BuildGuardApplication.BUILD_GUARD_MAIN_TAG, "Data key=$key value=$value")
        }
    }
}