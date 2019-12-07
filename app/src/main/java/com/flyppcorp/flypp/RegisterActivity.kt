package com.flyppcorp.flypp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.flyppcorp.Helper.RedimensionImage
import com.flyppcorp.firebase_classes.SignInFirebaseAuth
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var mSignInFirebaseAuth: SignInFirebaseAuth
    private lateinit var mSize : RedimensionImage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        mSignInFirebaseAuth = SignInFirebaseAuth(this)
        mSize = RedimensionImage()

        setListeners()
    }

    private fun setListeners() {
        btnCad.setOnClickListener {
            handleRegister()
        }
        //loadImage()
    }

    private fun loadImage() {
        //Picasso.get().load(R.drawable.logo).resize(500,500).centerInside().into(imageView2)
        imageView2.setImageBitmap(mSize.redimensionarResource(resources,R.drawable.logo, 350, 130))
    }

    private fun handleRegister() {
        val email = editEmailCad.text.toString()
        val senha = editSenhaCad.text.toString()
        val confirmSenha = editSenhaConfirmCad.text.toString()

        if (validateEmail() && validateSenha() && validateConfirmSenha()){
            mSignInFirebaseAuth.signInFirebaseAuth(email, senha)

        }
    }

    private fun validateEmail(): Boolean {
        val email = editEmailCad.text.toString()
        if (email.isEmpty()){
            editEmailCadLayout.error = "Email não pode ser vazio."
            return false
        }else{
            editEmailCadLayout.error = null
            return true
        }
    }

    private fun validateSenha(): Boolean{
        val senha = editSenhaCad.text.toString()
        if (senha.isEmpty()){
            editSenhaCadLayout.error = "Senha não pode ser vazio."
            return false
        }else{
            editSenhaCadLayout.error = null
            return true
        }
    }
    private fun validateConfirmSenha(): Boolean{
        val senha = editSenhaCad.text.toString()
        val confirmSenha = editSenhaConfirmCad.text.toString()

        if (confirmSenha.isEmpty()){
            editSenhaConfirmCadLayout.error = "Confirmação de senha não pode ser vazio."
            return false
        }else if (confirmSenha != senha){
            editSenhaConfirmCadLayout.error = "Senhas não coincidem."
            return false
        }else {
            editSenhaConfirmCadLayout.error = null
            return true
        }
    }
}
