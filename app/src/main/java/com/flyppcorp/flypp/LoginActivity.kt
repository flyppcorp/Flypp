package com.flyppcorp.flypp

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.flyppcorp.atributesClass.User
import com.flyppcorp.constants.Constants
import com.flyppcorp.firebase_classes.LoginFirebaseAuth
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    //inicia objetos
    private lateinit var mLoginFirebaseAuth: LoginFirebaseAuth
    private lateinit var mAuth: FirebaseAuth
    private var googleSignInClient : GoogleSignInClient? = null


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
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

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
        btnGoogle.setOnClickListener {
            signInGoogle()
        }
    }

    private fun signInGoogle() {
       val signInIntent = googleSignInClient?.signInIntent
        startActivityForResult(signInIntent, Constants.KEY.GOOGLE_LOGIN_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.KEY.GOOGLE_LOGIN_CODE){
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result!!.isSuccess){
                val account = result.signInAccount
                loginGoogle(account)
            }
        }
    }

    private fun loginGoogle(account: GoogleSignInAccount?) {
        val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener {
                if (it.isSuccessful){
                    if (it.result?.additionalUserInfo!!.isNewUser){
                        val intent = Intent(this, CreateProfileActivity::class.java)
                        val user = User()
                        if (mAuth.currentUser?.displayName != null){
                            user.nome = mAuth.currentUser?.displayName
                        }else {
                            user.nome = "User"
                        }
                        //cria logo um user no banco, caso o user não preencher informações, já fica criado o perfil
                        user.uid = mAuth.currentUser?.uid
                        user.email = mAuth.currentUser?.email
                        val mFirestore = FirebaseFirestore.getInstance()
                        mFirestore.collection(Constants.COLLECTIONS.USER_COLLECTION)
                            .document(mAuth.currentUser?.uid.toString())
                            .set(user)
                        startActivity(intent)
                    }else {
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        ContextCompat.startActivity(this, intent, null)
                    }
                }else {
                    Toast.makeText(this, "Ops! Algo deu errado", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun handleAnonimo() {
        val mProgress = ProgressDialog(this)
        mProgress.setCancelable(false)
        mProgress.show()
        mProgress.setContentView(R.layout.progress)
        mProgress.window?.setBackgroundDrawableResource(android.R.color.transparent)
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



