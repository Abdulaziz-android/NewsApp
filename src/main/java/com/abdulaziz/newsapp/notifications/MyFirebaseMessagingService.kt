package com.abdulaziz.newsapp.notifications

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.abdulaziz.newsapp.MainActivity
import com.abdulaziz.newsapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.squareup.picasso.Picasso

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            updateToken(token)
        }
    }

    private fun updateToken(refreshToken: String) {

        val user = FirebaseAuth.getInstance().currentUser

        val reference = FirebaseDatabase.getInstance().getReference("Tokens")
        val token = Token(refreshToken, user!!.uid)
        reference.child(user.uid).setValue(token)

    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        if (remoteMessage.data.isNotEmpty()) {
            createNotification(remoteMessage)
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun createNotification(remoteMessage: RemoteMessage) {
        val intent = Intent(remoteMessage.notification?.clickAction, null, this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)


        intent.putExtra("noti", "noti")
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        var builder = NotificationCompat.Builder(applicationContext, "channel_id")
            .setSmallIcon(R.drawable.ic_home)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)

        builder = builder.setContent(getRemoteView(remoteMessage))

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel("channel_id",
                "channel_name",
                NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(notificationChannel)

        }

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())

    }

    @SuppressLint("RemoteViewLayout")
    private fun getRemoteView(remoteMessage: RemoteMessage): RemoteViews {
        val remoteView = RemoteViews("com.abdulaziz.newsapp", R.layout.item_notification)

        val title = remoteMessage.data["title"]
        val category = remoteMessage.data["category"]
        val imageUrl = remoteMessage.data["image_url"]

        val bitmap = Picasso.get().load(imageUrl).get()

        remoteView.setImageViewBitmap(R.id.image_view, bitmap)
        remoteView.setTextViewText(R.id.title_tv, title)
        remoteView.setTextViewText(R.id.category_tv, category)

        return remoteView
    }


}