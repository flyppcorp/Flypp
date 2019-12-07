package com.flyppcorp.flypp

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import com.flyppcorp.Helper.RedimensionImage
import com.flyppcorp.firebase_classes.LoginFirebaseAuth
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login.view.*
import java.io.File

class LoginActivity : AppCompatActivity() {
    //inicia objetos
    private lateinit var mLoginFirebaseAuth: LoginFirebaseAuth
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mSize : RedimensionImage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        //instancia
        mLoginFirebaseAuth = LoginFirebaseAuth(this)
        mAuth = FirebaseAuth.getInstance()
        mSize = RedimensionImage()
        //setListeners possui os Onclick
        setListeners()
        //moveMain leva para a main caso user esteja conectado
        //moveMainPage()
        //image()
    }

    private fun image() {
        //Picasso.get().load(R.drawable.logo).placeholder(R.drawable.logo).resize(500, 500).centerInside().into(imageViewLogo)
        imageViewLogo.setImageBitmap(mSize.redimensionarResource(resources,R.drawable.logo, 300, 130))
    }

    private fun setListeners() {
        txtRegister.setOnClickListener {
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



