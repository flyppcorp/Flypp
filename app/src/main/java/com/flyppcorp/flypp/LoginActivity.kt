package com.flyppcorp.flypp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.flyppcorp.firebase_classes.LoginFirebaseAuth
import com.google.firebase.auth.FirebaseAuth
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



