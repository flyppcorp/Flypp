package com.flyppcorp.firebase_classes

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.core.content.ContextCompat.startActivity
import com.flyppcorp.flypp.MainActivity
import com.flyppcorp.flypp.R
import com.google.firebase.auth.FirebaseAuth

class LoginFirebaseAuth(private val context: Context) {

    //declarar e instanciar variaveis
    val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val mAlertDialog = AlertDialog.Builder(context)
    private val mProgressDialog: ProgressDialog = ProgressDialog(context)


    //metodo que realiza o login
    fun loginEmailSenha(email: String, senha: String) {

        //chamada da progress enquanto processa a função

        mProgressDialog.setCancelable(false)
        mProgressDialog.show()
        mProgressDialog.setContentView(R.layout.progress)
        mProgressDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // fim da chamada


        //chamada da operação de login
        mAuth.signInWithEmailAndPassword(email, senha)
            .addOnCompleteListener {
                when {
                    //se tudo der certo entra
                    it.isSuccessful -> {
                        val intent = Intent(context, MainActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(context, intent, null)


                    }
                    //se der errado mostra uma mensagem
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


            }


    }

}
