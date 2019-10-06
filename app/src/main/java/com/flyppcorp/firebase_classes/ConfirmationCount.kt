package com.flyppcorp.firebase_classes

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import androidx.core.content.ContextCompat.startActivity
import com.flyppcorp.flypp.CreateProfileActivity
import com.flyppcorp.flypp.MainActivity

class ConfirmationCount(private val context: Context) {

    //declaração de objetos
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    val mProgressDialog: ProgressDialog = ProgressDialog(context)

    //função que valida emails
    fun validarEmail() {
        //mostra uma progressDialog enquanto carrega
        mProgressDialog.show()
        //Obtem o usuario atual e verifica a autenticação
        val user = mAuth.currentUser!!
        user.reload()
            .addOnCompleteListener {
                if (it.isSuccessful){
                    if (user.isEmailVerified) {
                        val intent = Intent(context, CreateProfileActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(context, intent, null)
                        mProgressDialog.hide()
                        mProgressDialog.dismiss()
                        //se não tiver verificado, mostra uma mensagem de alerta
                    } else {
                        val alert = AlertDialog.Builder(context)
                        alert.setMessage(
                            "Seu e-mail ainda não foi verificado." +
                                    "\nAguarde uns segundos e clique novamente no botão."
                        )
                        alert.setPositiveButton("Ok", { dialogInterface: DialogInterface, i: Int -> })
                        alert.show()
                        mProgressDialog.hide()
                        mProgressDialog.dismiss()
                    }
                }
            }

    }

}