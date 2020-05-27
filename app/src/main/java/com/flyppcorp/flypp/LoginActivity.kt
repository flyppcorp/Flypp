package com.flyppcorp.flypp

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.flyppcorp.atributesClass.DashBoard
import com.flyppcorp.constants.Constants
import com.flyppcorp.firebase_classes.LoginFirebaseAuth
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    //inicia objetos
    private lateinit var mLoginFirebaseAuth: LoginFirebaseAuth
    private lateinit var mAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        //instancia
        mLoginFirebaseAuth = LoginFirebaseAuth(this)
        mAuth = FirebaseAuth.getInstance()
        //setListeners possui os Onclick
        setListeners()
        //moveMain leva para a main caso user esteja conectado
        //moveMainPage()

    }

    private fun setListeners() {
        btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        btnLogin.setOnClickListener {
            handleLogin()

        }
        esqueceuSenha.setOnClickListener {
            startActivity(Intent(this, ResetPasswordActivity::class.java))
        }

        txtAnonimo.setOnClickListener {
            handleAnonimo()
        }
    }

    private fun handleAnonimo() {
        val mProgress = ProgressDialog(this)
        mProgress.setCancelable(false)
        mProgress.show()
        mAuth.signInAnonymously()
            .addOnCompleteListener {

                if (it.isSuccessful){
                    val intent = Intent(this,MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }else {
                    Toast.makeText(this, "Oops! Algo deu errado", Toast.LENGTH_SHORT).show()
                    mProgress.hide()
                }

            }
    }

    private fun handleLogin() {
        val email = editEmail.text.toString()
        val senha = editSenha.text.toString()
        if (validateEmail() && validateSenha()) {
            mLoginFirebaseAuth.loginEmailSenha(email, senha)

        }


    }

    //funções que validam os campos
    private fun validateEmail(): Boolean {
        val email = editEmail.text.toString()
        if (email.isEmpty()) {
            editEmailLayout.error = "E-mail não pode ser vazio"
            return false
        } else {

            editEmailLayout.error = null
            //progressBar2.visibility = View.VISIBLE
            return true
        }
    }

    private fun validateSenha(): Boolean {
        val senha = editSenha.text.toString()
        if (senha.isEmpty()) {
            editSenhaLayout.error = "Senha não pode ser vazio"
            return false
        } else {
            editSenhaLayout.error = null
            //progressBar2.visibility = View.VISIBLE
            return true
        }
    }


}



