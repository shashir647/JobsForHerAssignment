package com.shashi.jobsforherassignment

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.shashi.jobsforherassignment.activity.Activity1
import com.shashi.jobsforherassignment.activity.Activity2
import com.shashi.jobsforherassignment.activity.LoginActivity
import com.shashi.jobsforherassignment.activity.MainActivity
import java.io.IOException
import java.net.URL

class MyFirebaseMessagingService : FirebaseMessagingService(){
    companion object{
        private val FCM_PARAM = "picture"
        private val CHANNEL_NAME = "FCM"
        private val CHANNEL_DESC = "Firebase Cloud Messaging"
    }
    private var numMessages = 0

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val notification = remoteMessage.notification
        val data = remoteMessage.data
        Log.d("FROM", remoteMessage.from!!)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            sendNotification(notification, data)
        }
    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        Log.d("NEW_TOKEN", p0)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun sendNotification(
        notification: RemoteMessage.Notification?,
        data: Map<String, String>
    ) {
        var intent: Intent
        when(notification?.clickAction){
            "Open_Activity1" -> Intent(this, Activity1::class.java).also { intent = it }
            "Open_Activity2" -> Intent(this, Activity2::class.java).also { intent = it }
            else -> { // Note the block
                Intent(this, MainActivity::class.java).also { intent = it }
            }
        }
        intent.putExtra("click_action", notification?.clickAction);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        val pendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        val notificationBuilder =
            NotificationCompat.Builder(this, getString(R.string.notification_channel_id))
                .setContentTitle(notification!!.title)
                .setContentText(notification.body)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent)
                .setContentInfo("Hello")
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
                .setColor(getColor(R.color.colorAccent))
                .setLights(Color.RED, 1000, 300)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setNumber(++numMessages)
                .setSmallIcon(R.drawable.ic_notification)
        try {
            val picture = data[FCM_PARAM]
            if (picture != null && "" != picture) {
                val url = URL(picture)
                val bigPicture = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                notificationBuilder.setStyle(
                    NotificationCompat.BigPictureStyle().bigPicture(bigPicture)
                        .setSummaryText(notification.body)
                )
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                getString(R.string.notification_channel_id),
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = CHANNEL_DESC
            channel.setShowBadge(true)
            channel.canShowBadge()
            channel.enableLights(true)
            channel.lightColor = Color.RED
            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500)
            notificationManager.createNotificationChannel(channel)
        }
        if (false) {
            error("Assertion failed")
        }
        notificationManager.notify(0, notificationBuilder.build())
    }
}