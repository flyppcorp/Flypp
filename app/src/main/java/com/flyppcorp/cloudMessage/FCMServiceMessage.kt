package com.flyppcorp.cloudMessage

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.flyppcorp.atributesClass.Servicos
import com.flyppcorp.atributesClass.User
import com.flyppcorp.constants.Constants
import com.flyppcorp.flypp.MessageActivity
import com.flyppcorp.flypp.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FCMServiceMessage : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val data : Map<String, String> = remoteMessage.data
        if (data == null || data.get("sender") == null) return

        val intent  = Intent(this, MessageActivity::class.java)

        FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS.USER_COLLECTION)
            .document(data.get("sender")!!)
            .get()
            .addOnSuccessListener {
                val sender = it.toObject(User::class.java)
                val mService = Servicos()
                mService.uid = sender!!.uid
                mService.urlProfile = sender.url
                mService.nome = sender.nome

                intent.putExtra(Constants.KEY.MESSAGE_KEY, mService)

                val pIntent = PendingIntent.getActivity(applicationContext, 0, intent, 0)

                val notificationManager : NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val notificationChannelId = "my_channel_id_01"
                val builder : NotificationCompat.Builder = NotificationCompat.Builder(applicationContext, notificationChannelId)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    val notificationChannel = NotificationChannel(notificationChannelId, "my notifications", NotificationManager.IMPORTANCE_DEFAULT)

                    notificationChannel.description = "Channel description"
                    notificationChannel.enableLights(true)
                    notificationChannel.lightColor = Color.WHITE
                    notificationManager.createNotificationChannel(notificationChannel)

                    builder.setAutoCancel(true)
                    builder.setSmallIcon(R.drawable.ic_notification_logo)
                    builder.setContentTitle(data.get("title"))
                    builder.setContentText(data.get("body"))
                    builder.setContentIntent(pIntent)
                }

                builder.setAutoCancel(true)
                builder.setSmallIcon(R.drawable.ic_notification_logo)
                builder.setContentTitle(data.get("title"))
                builder.setContentText(data.get("body"))
                builder.setContentIntent(pIntent)

                notificationManager.notify(1, builder.build())
            }
    }
}