package com.flyppcorp.FCM

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import com.flyppcorp.atributesClass.Myservice
import com.flyppcorp.constants.Constants
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

        if (data == null || data.get("sender") == null) return

        var intent: Intent = Intent(this, ManagerServicesActivity::class.java)

        firestore.collection(Constants.COLLECTIONS.MY_SERVICE)
            .document(data.get("sender")!!)
            .get()
            .addOnSuccessListener {


                val mMyService: Myservice? = it.toObject(Myservice::class.java)
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

                builder.setAutoCancel(true)
                    .setSmallIcon(R.drawable.ic_notification_logo)
                    .setContentTitle(data.get("title"))
                    .setContentText(data.get("body"))
                    .setContentIntent(pItent)

                val random = java.util.Random().nextInt(500)

                notificationManager.notify(random, builder.build())

            }
    }


}