package com.flyppcorp.flypp

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.flyppcorp.atributesClass.User
import com.flyppcorp.constants.Constants
import com.flyppcorp.firebase_classes.SignInFirebaseAuth
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var mSignInFirebaseAuth: SignInFirebaseAuth
    private lateinit var mAuth: FirebaseAuth
    var extras: Bundle? = null
    var dataGet : String? = null
    private var googleSignInClient : GoogleSignInClient? = null
    private lateinit var mRemote: FirebaseRemoteConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        mSignInFirebaseAuth = SignInFirebaseAuth(this)
        mAuth = FirebaseAuth.getInstance()
        mRemote = FirebaseRemoteConfig.getInstance()
        extras = intent?.extras
        if (extras != null){
            dataGet = extras?.getString(Constants.KEY.ID_DESCONTO)
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)


        setListeners()
    }

    private fun setListeners() {
        btnCad.setOnClickListener {
            handleRegister()
        }
        btnTermos.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("http://flyppbrasil.epizy.com/termosdeuso.html")
            )
            startActivity(intent)
        }

        btnRegisterGoogle.setOnClickListener {
            handleRegisterLogin()
        }

    }

    private fun handleRegisterLogin() {
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

                        mRemote.fetch(0).addOnCompleteListener {
                            if (it.isSuccessful){
                                mRemote.fetchAndActivate()
                                val ativo = mRemote.getBoolean(Constants.KEY.ACTIVATE_SHARE)
                                if (dataGet != null && ativo){
                                   mSignInFirebaseAuth.handleOFF(dataGet)
                                }
                            }
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


    private fun handleRegister() {
        val email = editEmailCad.text.toString()
        val senha = editSenhaCad.text.toString()
        val confirmSenha = editSenhaConfirmCad.text.toString()

        if (validateEmail() && validateSenha() && validateConfirmSenha()) {
            mSignInFirebaseAuth.signInFirebaseAuth(email, senha, dataGet)


        }
    }


    private fun validateEmail(): Boolean {
        val email = editEmailCad.text.toString()
        if (email.isEmpty()) {
            editEmailCadLayout.error = "Email não pode ser vazio."
            return false
        } else {
            editEmailCadLayout.error = null
            return true
        }
    }

    private fun validateSenha(): Boolean {
        val senha = editSenhaCad.text.toString()
        if (senha.isEmpty()) {
            editSenhaCadLayout.error = "Senha não pode ser vazio."
            return false
        } else {
            editSenhaCadLayout.error = null
            return true
        }
    }

    private fun validateConfirmSenha(): Boolean {
        val senha = editSenhaCad.text.toString()
        val confirmSenha = editSenhaConfirmCad.text.toString()

        if (confirmSenha.isEmpty()) {
            editSenhaConfirmCadLayout.error = "Confirmação de senha não pode ser vazio."
            return false
        } else if (confirmSenha != senha) {
            editSenhaConfirmCadLayout.error = "Senhas não coincidem."
            return false
        } else {
            editSenhaConfirmCadLayout.error = null
            return true
        }
    }
}
