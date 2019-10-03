package com.flyppcorp.firebase_classes

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.util.Log
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.flyppcorp.atributesClass.User
import com.flyppcorp.constants.Constants
import com.flyppcorp.flypp.MainActivity
import com.flyppcorp.flypp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FirestoreUser(private val context: Context) {

    //declaração e iniciação de objetos
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val mDadosUser: FirebaseFirestore = FirebaseFirestore.getInstance()
    val mProgressDialog = ProgressDialog(context)
    val mAlertDialog = AlertDialog.Builder(context)

    //função que salva usuario no firestore
    fun saveUser(mFirestoreClasses: User) {
        //progress dialog
        mProgressDialog.show()
        val uid = mAuth.currentUser!!.uid

            mDadosUser.collection(Constants.COLLECTIONS.USER_COLLECTION)
                .document(uid)
                .set(mFirestoreClasses)
                .addOnSuccessListener {
                    val intent = Intent(context, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(context, intent, null)
                    mProgressDialog.hide()
                    mProgressDialog.dismiss()

                }.addOnFailureListener {
                   mAlertDialog.setMessage("Algo saiu errado." +
                           "\nTente novamente!")
                    mAlertDialog.setPositiveButton("Ok", { dialogInterface: DialogInterface, i: Int -> })
                    mAlertDialog.show()

                    mProgressDialog.hide()
                    mProgressDialog.dismiss()
                }


    }

}