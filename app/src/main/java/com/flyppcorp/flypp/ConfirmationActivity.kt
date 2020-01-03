package com.flyppcorp.flypp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.flyppcorp.Helper.RedimensionImage
import com.flyppcorp.constants.Constants
import com.flyppcorp.firebase_classes.ConfirmationCount
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_confirmation.*

class ConfirmationActivity : AppCompatActivity() {

    //inicio tardio e declaração dos objetos
    private lateinit var mConfirmationCount: ConfirmationCount
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mSize : RedimensionImage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirmation)

        //instancia dos objetos
        mAuth = FirebaseAuth.getInstance()
        mConfirmationCount = ConfirmationCount(this)
        mSize = RedimensionImage()
        handleImg()
        showMessage()
        //botao que inicia funcao de validar email
        btnConfirm.setOnClickListener {
            mConfirmationCount.validarEmail()
        }
        txtSendNewEmail.setOnClickListener {
            mAuth.currentUser!!.sendEmailVerification()
                .addOnCompleteListener {
                    if (it.isSuccessful){
                        Toast.makeText(this, "E-mail reenviado ", Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(this, "Falha ao reenviar e-mail, tente novamente ", Toast.LENGTH_SHORT).show()
                    }
                }
        }

    }


    //função que mostra mensagem com email do usuario
    private fun showMessage() {
        val emailUser = mAuth.currentUser?.email
        txtMessageConfirm.text =
            "Olá, um e-mail foi enviado para ${emailUser}, vá até ele e confirme clicando no link." +
                    "\n(O e-mail pode estar em Promoções, Atualizações ou até mesmo em Spam)" +
                    "\nApós seguir os passos, clique no botão abaixo"
    }

    //função que mostra uma imagem aleatoria toda vez que um usuario for autenticar
    private fun handleImg() {
        val extras = intent.extras
        if (extras != null) {
            val imgRandomico = extras.getString(Constants.KEY.RANDOM_KEY)

            if (imgRandomico.equals("0")) {
                imgRandom.setImageResource(R.drawable.ic_email_one)

            } else if (imgRandomico.equals("1")) {
                imgRandom.setImageResource(R.drawable.ic_email_two)
            } else if (imgRandomico.equals("2")) {
                imgRandom.setImageResource(R.drawable.ic_email_three)
            } else if (imgRandomico.equals("3")) {
                imgRandom.setImageResource(R.drawable.ic_email_four)
            }
        }
    }
}
