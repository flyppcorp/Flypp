package com.flyppcorp.firebase_classes

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import com.google.firebase.auth.FirebaseAuth
import androidx.core.content.ContextCompat.startActivity
import com.flyppcorp.flypp.R

class ResetPassword (private val context:Context) {
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val mProgressDialog: ProgressDialog =  ProgressDialog(context)
    private val mAlert = AlertDialog.Builder(context)

    fun resetPassword(email: String){
        mProgressDialog.setCancelable(false)
        mProgressDialog.show()
        mProgressDialog.setContentView(R.layout.progress)
        mProgressDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener {
                    when {

                        it.isSuccessful -> {
                            val intent = Intent(context, LoginFirebaseAuth::class.java)
                            startActivity(context, intent, null)
                        }

                }
        }
    }
}