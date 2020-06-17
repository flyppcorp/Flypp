package com.flyppcorp.firebase_classes

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.core.content.ContextCompat.startActivity
import com.flyppcorp.atributesClass.NotificationMessage
import com.flyppcorp.atributesClass.User
import com.flyppcorp.constants.Constants
import com.flyppcorp.flypp.CreateProfileActivity
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import java.util.*

class SignInFirebaseAuth(private val context: Context) {
    //declarar e instanciar as variaveis
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val mAlertDialog = AlertDialog.Builder(context)
    private val mProgressDialog: ProgressDialog = ProgressDialog(context)
    private val mFirestore : FirebaseFirestore = FirebaseFirestore.getInstance()
    private val mRemote: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()

    //funcao de cadastro de usuário
    fun signInFirebaseAuth(email: String, senha: String, uid: String?) {
        mProgressDialog.setCancelable(false)
        mProgressDialog.show()

        //se o usuario for nulo, ou seja, não estiver logado ou não for um user anonimo
        if (mAuth.currentUser?.uid == null){

            //criar user
            mAuth.createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener {

                    when {
                        it.isSuccessful -> {
                            //val random = Random().nextInt(4)
                            val intent = Intent(context, CreateProfileActivity::class.java)
                            //intent.putExtra(Constants.KEY.RANDOM_KEY, random.toString())
                            //mAuth.currentUser!!.sendEmailVerification()
                            val user = User()
                            if (mAuth.currentUser?.displayName != null){
                                user.nome = mAuth.currentUser?.displayName
                            }else {
                                user.nome = "User"
                            }
                            //cria logo um user no banco, caso o user não preencher informações, já fica criado o perfil
                            user.uid = mAuth.currentUser?.uid
                            user.email = mAuth.currentUser?.email
                            mFirestore.collection(Constants.COLLECTIONS.USER_COLLECTION)
                                .document(mAuth.currentUser?.uid.toString())
                                .set(user)
                            //fim do firestore

                            //caso o usuário tenha se cadastrado por um link, fazer uma transacao para criar a contagem de desconto
                            mRemote.fetch(0).addOnCompleteListener {
                                if (it.isSuccessful){
                                    mRemote.fetchAndActivate()
                                    val ativo = mRemote.getBoolean(Constants.KEY.ACTIVATE_SHARE)
                                    if (uid != null && ativo){
                                        handleOFF(uid)
                                    }
                                }
                            }

                            startActivity(context, intent, null)

                        }
                        !it.isSuccessful ->
                            try {
                                throw it.exception!!
                            } catch (e: FirebaseAuthUserCollisionException) {
                                mAlertDialog.setMessage(
                                    "Parece que esse e-mail já está em uso.\n" +
                                            "por favor, tente outro e-mail."
                                )
                                mAlertDialog.setPositiveButton("Ok") { dialogInterface: DialogInterface, i: Int ->
                                }
                                mAlertDialog.show()

                            } catch (e: FirebaseAuthWeakPasswordException) {
                                mAlertDialog.setMessage(
                                    "A senha que você está tentando usar é inválida ou muito fraca.\n" +
                                            "Por favor, tente outra senha."
                                )
                                mAlertDialog.setPositiveButton(
                                    "Ok",
                                    { dialogInterface: DialogInterface, i: Int -> })
                                mAlertDialog.show()

                            } catch (e: FirebaseAuthInvalidCredentialsException) {
                                mAlertDialog.setMessage(
                                    "O e-mail que você está tentando não é válido.\n" +
                                            "Por favor, tente um e-mail válido."
                                )
                                mAlertDialog.setPositiveButton(
                                    "Ok",
                                    { dialogInterface: DialogInterface, i: Int -> })
                                mAlertDialog.show()

                            }
                    }
                    mProgressDialog.hide()
                    //mProgressDialog.dismiss()
                }

        }else if (mAuth.currentUser?.isAnonymous == true){
            signAnonimo(email, senha)
        }

    }

    //funcao que registra o cadastro
    fun handleOFF(uid: String?) {
        val mFirestore = FirebaseFirestore.getInstance()
        val tsDoc = mFirestore.collection(Constants.COLLECTIONS.USER_COLLECTION).document(uid.toString())
        mFirestore.runTransaction {
            val content = it.get(tsDoc).toObject(User::class.java)
            if (content?.desconto == 9){
                content.desconto = 0
                content.primeiraCompraConcluida = false
                content.primeiraCompra = false
            }else {
                content?.desconto = content!!.desconto + 1
            }
            it.set(tsDoc, content)
        }.addOnSuccessListener {
            handleNotification(uid)
        }


    }

    //funcao que envia uma notificacao
    private fun handleNotification(uid: String?) {
        val mFirestore = FirebaseFirestore.getInstance()
        mFirestore.collection(Constants.COLLECTIONS.USER_COLLECTION)
            .document(uid.toString())
            .get()
            .addOnSuccessListener {
                val itemUser = it.toObject(User::class.java)
                val mNotification = NotificationMessage()
                mNotification.title = "Uhuuul"
                mNotification.token = itemUser?.token.toString()
                val total = 10 - itemUser?.desconto!!
                //se for igual a zero, entao 10 pessoas se cadastraram
                if (itemUser.desconto == 0){
                    mNotification.text = "Alguém se cadastrou usando seu link, 10 pessoas se cadastraram e você ganhou desconto "
                    //senão, falta completar para dar o desconto
                }else{
                    mNotification.text = "Alguém se cadastrou usando seu link, mais ${total} pessoas e você ganha um desconto "
                }


                val mFireNotification = FirebaseFirestore.getInstance()
                mFireNotification.collection(Constants.COLLECTIONS.NOTIFICATION_DESCONTO)
                    .document(itemUser.token.toString())
                    .set(mNotification)
            }
    }

    //entrar anonimamente
    fun signAnonimo(email: String, senha: String){
        val credential = EmailAuthProvider.getCredential(email, senha)

        mAuth.currentUser?.linkWithCredential(credential)
            ?.addOnCompleteListener {
                when {
                    it.isSuccessful -> {
                        val intent = Intent(context, CreateProfileActivity::class.java)
                        val user = User()
                        if (mAuth.currentUser?.displayName != null){
                            user.nome = mAuth.currentUser?.displayName
                        }else {
                            user.nome = "User"
                        }
                        user.uid = mAuth.currentUser?.uid
                        user.email = mAuth.currentUser?.email
                        mFirestore.collection(Constants.COLLECTIONS.USER_COLLECTION)
                            .document(mAuth.currentUser?.uid.toString())
                            .set(user)
                        startActivity(context, intent, null)

                    }
                    !it.isSuccessful ->
                        try {
                            throw it.exception!!
                        } catch (e: FirebaseAuthUserCollisionException) {
                            mAlertDialog.setMessage(
                                "Parece que esse e-mail já está em uso.\n" +
                                        "por favor, tente outro e-mail."
                            )
                            mAlertDialog.setPositiveButton("Ok") { dialogInterface: DialogInterface, i: Int ->
                            }
                            mAlertDialog.show()

                        } catch (e: FirebaseAuthWeakPasswordException) {
                            mAlertDialog.setMessage(
                                "A senha que você está tentando usar é inválida ou muito fraca.\n" +
                                        "Por favor, tente outra senha."
                            )
                            mAlertDialog.setPositiveButton(
                                "Ok",
                                { dialogInterface: DialogInterface, i: Int -> })
                            mAlertDialog.show()

                        } catch (e: FirebaseAuthInvalidCredentialsException) {
                            mAlertDialog.setMessage(
                                "O e-mail que você está tentando não é válido.\n" +
                                        "Por favor, tente um e-mail válido."
                            )
                            mAlertDialog.setPositiveButton(
                                "Ok",
                                { dialogInterface: DialogInterface, i: Int -> })
                            mAlertDialog.show()

                        }
                }
                mProgressDialog.hide()

            }
            }

    }
