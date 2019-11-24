package com.flyppcorp.firebase_classes

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.isVisible
import com.flyppcorp.flypp.LoginActivity
import com.flyppcorp.flypp.MainActivity
import com.flyppcorp.flypp.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.view.*
import kotlin.properties.Delegates

class LoginFirebaseAuth(private val context: Context) {

    //declarar e instanciar variaveis
    val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val mAlertDialog = AlertDialog.Builder(context)
    private val mProgressDialog: ProgressDialog = ProgressDialog(context)


    //metodo que realiza o login
    fun loginEmailSenha(email: String, senha: String) {

        //chamada da progress enquanto processa a função
        mProgressDialog.show()

        // fim da chamada


        //chamada da operação de login
        mAuth.signInWithEmailAndPassword(email, senha)
            .addOnCompleteListener {
                when {
                    it.isSuccessful -> {
                        val intent = Intent(context, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(context, intent, null)

                    }
                    !it.isSuccessful -> {
                        mAlertDialog.setMessage(
                            "Parece que seu e-mail e/ou senha estão incorretos.\n" +
                                    "Verifique-os e tente novamente."
                        )
                        mAlertDialog.setPositiveButton("Ok") { dialogInterface: DialogInterface, i: Int ->
                        }
                        mAlertDialog.show()


                    }


                }

                mProgressDialog.hide()
                //mProgressDialog.dismiss()



            }


    }

}
