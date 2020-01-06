package com.flyppcorp.FCM

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.flyppcorp.atributesClass.Myservice
import com.flyppcorp.constants.Constants
import com.flyppcorp.flypp.LastMessages
import com.flyppcorp.flypp.ManagerServicesActivity
import com.flyppcorp.flypp.R
import com.flyppcorp.managerServices.PendenteActivity
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

class FCMServiceNotification : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val data: MutableMap<String, String> = remoteMessage.data
        val firestore = FirebaseFirestore.getInstance()

        if (remoteMessage.data.size > 0){
            val payload : MutableMap<String, String> = remoteMessage.data
            messageNotification(payload)
            serviceNotification(payload)
        }


    }

    private fun messageNotification(payload: MutableMap<String, String>) {
        var intent: Intent = Intent(this, LastMessages::class.java)
        val pItent = PendingIntent.getActivity(applicationContext, 0, intent, 0)

        val notificationManager1 =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationChannelId1 = "Flypp"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val notificationChannel1 = NotificationChannel(
                notificationChannelId1,
                "Flypp",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            notificationChannel1.description = "Você receberá notificações de Flypp "
            notificationChannel1.lightColor = Color.WHITE
            notificationChannel1.enableLights(true)

            notificationManager1.createNotificationChannel(notificationChannel1)

        }

        val builder: NotificationCompat.Builder =
            NotificationCompat.Builder(applicationContext, notificationChannelId1)

        val alarm = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        builder.setAutoCancel(true)
            .setSmallIcon(R.drawable.ic_notification_logo)
            .setStyle(NotificationCompat.BigTextStyle()
                .setBigContentTitle(payload.get("titleKey"))
                .bigText(payload.get("bodyKey")))
            .setSound(alarm)
            .setContentIntent(pItent)

        val random = java.util.Random().nextInt(500)

        if (payload.get("senderKey") == null){
            return
        }else{
            notificationManager1.notify(random, builder.build())
        }



    }

    private fun serviceNotification(payload: MutableMap<String, String>) {
        var intent: Intent = Intent(this, ManagerServicesActivity::class.java)
        val pItent = PendingIntent.getActivity(applicationContext, 0, intent, 0)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationChannelId = "Flypp"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val notificationChannel = NotificationChannel(
                notificationChannelId,
                "Flypp",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            notificationChannel.description = "Você receberá notificações de Flypp "
            notificationChannel.lightColor = Color.WHITE
            notificationChannel.enableLights(true)

            notificationManager.createNotificationChannel(notificationChannel)

        }

        val builder: NotificationCompat.Builder =
            NotificationCompat.Builder(applicationContext, notificationChannelId)

        val alarm = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        builder.setAutoCancel(true)
            .setSmallIcon(R.drawable.ic_notification_logo)
            .setStyle(NotificationCompat.BigTextStyle()
                .setBigContentTitle(payload.get("title"))
                .bigText(payload.get("body")))
            .setSound(alarm)
            .setContentIntent(pItent)

        val random = java.util.Random().nextInt(500)

        if (payload.get("sender") == null){
            return
        }else{
            notificationManager.notify(random, builder.build())
        }



    }
}


