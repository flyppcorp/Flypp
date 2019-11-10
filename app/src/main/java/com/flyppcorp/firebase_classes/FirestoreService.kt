package com.flyppcorp.firebase_classes

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.flyppcorp.atributesClass.Servicos
import com.flyppcorp.constants.Constants
import com.flyppcorp.flypp.MainActivity
import com.google.firebase.firestore.FirebaseFirestore

class FirestoreService (private val context: Context) {

    //declaração e inicialização de objeetos
    private val mServices: FirebaseFirestore = FirebaseFirestore.getInstance()
    val mDialog =  ProgressDialog(context)
    private val mAlertDialog = AlertDialog.Builder(context)

    //função que salva serviços
    fun servicos(mService: Servicos, serviceId :String){
        //progress dialog exibido
        //mDialog.show()

        mServices.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
            .document(serviceId)
            .set(mService)
            .addOnSuccessListener {
                mDialog.hide()
                //mDialog.dismiss()

            }.addOnFailureListener {
                mAlertDialog.setMessage("Algo deu errado, tente novamente")
                mAlertDialog.setPositiveButton("Ok", { dialogInterface: DialogInterface, i: Int -> })
                mAlertDialog.show()
                mDialog.hide()


            }
    }

}