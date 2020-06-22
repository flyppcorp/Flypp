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
import com.flyppcorp.atributesClass.DashBoard
import com.flyppcorp.atributesClass.Notification
import com.flyppcorp.atributesClass.User


class FirestoreContract(var context: Context) {
    //variaveis e objetos
    private val mFirestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    val progress = ProgressDialog(context)
    //lateinit var mIntertial: InterstitialAd

    @SuppressLint("ResourceAsColor")

    //função que salva no banco de dados
    fun confirmServiceContract(myservice: Myservice, documentId: String, token: String, notification: Notification
    ) {
        progress.setCancelable(false)
        progress.show()
        mFirestore.collection(Constants.COLLECTIONS.MY_SERVICE)
            .document(documentId)
            .set(myservice)
            .addOnSuccessListener {

                dashBoard()
                sendNotification(token, notification)
                toast()
                progress.dismiss()
                moveAndShowAd()


            }.addOnFailureListener {
                Toast.makeText(context, "Algo saiu errado, tente outra vez.", Toast.LENGTH_SHORT)
                    .show()
                progress.dismiss()
            }

    }

    //função que envia uma notificação para o contratado
    private fun sendNotification(token: String, notification: Notification) {

            mFirestore.collection(Constants.COLLECTIONS.NOTIFICATION_SERVICE)
                .document(token)
                .set(notification)


    }

    fun tsFirstCompra(uid: String){
        val tsDoc = mFirestore.collection(Constants.COLLECTIONS.USER_COLLECTION).document(uid)
        mFirestore.runTransaction {
            val content = it.get(tsDoc).toObject(User::class.java)
            content?.primeiraCompra = true

            it.set(tsDoc, content!!)
        }
    }



    private fun toast() {
        val toast = Toast.makeText(context, "Serviço solicitado com sucesso", Toast.LENGTH_LONG)
        var view = toast.view
        view?.setBackgroundColor(Color.rgb(242, 120, 75))
        view?.setPadding(20, 20, 20, 20)
        toast.show()
    }

    //função que mostra um anuncio
    private fun moveAndShowAd() {
        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(context, intent, null)
        /*if (mIntertial.isLoaded) {
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
        }*/
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
