package com.flyppcorp.firebase_classes

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.flyppcorp.atributesClass.Myservice
import com.flyppcorp.constants.Constants
import com.flyppcorp.flypp.MainActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.ads.*
import androidx.appcompat.app.AppCompatActivity
import com.flyppcorp.atributesClass.DashBoard
import com.flyppcorp.atributesClass.Notification



class FirestoreContract(var context: Context) {
    private val mFirestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    var mProgress: Boolean = true
    val progress = ProgressDialog(context)
    lateinit var mIntertial: InterstitialAd

    @SuppressLint("ResourceAsColor")

    fun confirmServiceContract(myservice: Myservice, documentId: String, token: String, notification: Notification ) {
        progress.show()
        mFirestore.collection(Constants.COLLECTIONS.MY_SERVICE)
            .document(documentId)
            .set(myservice)
            .addOnSuccessListener {

                progress.dismiss()
                moveAndShowAd()
                dashBoard()
                sendNotification(token, notification)
                toast()


            }.addOnFailureListener {
                Toast.makeText(context, "Algo saiu errado, tente outra vez.", Toast.LENGTH_SHORT)
                    .show()
            }

    }
    private fun sendNotification(token : String, notification: Notification){
        mFirestore.collection(Constants.COLLECTIONS.NOTIFICATION_SERVICE)
            .document(token)
            .set(notification)
    }



    private fun toast() {
        val toast = Toast.makeText(context, "Serviço solicitado com sucesso", Toast.LENGTH_LONG)
        var view = toast.view
        view.setBackgroundColor(Color.rgb(103, 58, 183))
        view.setPadding(20, 20, 20, 20)
        toast.show()
    }

    private fun moveAndShowAd() {
        if (mIntertial.isLoaded) {
            mIntertial.show()
            mIntertial.adListener = object : AdListener() {
                override fun onAdClosed() {
                    mIntertial.loadAd(AdRequest.Builder().build())
                    val intent = Intent(context, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(context, intent, null)
                }
            }
        } else {
            val intent = Intent(context, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(context, intent, null)
        }
    }

    private fun dashBoard() {
        val tsDoc = mFirestore.collection(Constants.DASHBOARD_SERVICE.DASHBOARD_COLLECTION)
            .document(Constants.DASHBOARD_SERVICE.DASHBOARD_DOCUMENT)
        mFirestore.runTransaction {
            val content = it.get(tsDoc).toObject(DashBoard::class.java)
            content!!.contractService = content.contractService + 1
            it.set(tsDoc, content)
        }
    }


}
