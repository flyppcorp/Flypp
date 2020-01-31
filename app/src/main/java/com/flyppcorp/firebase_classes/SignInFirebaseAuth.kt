package com.flyppcorp.firebase_classes

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.flyppcorp.atributesClass.User
import com.flyppcorp.constants.Constants
import com.flyppcorp.flypp.ConfirmationActivity
import com.flyppcorp.flypp.CreateProfileActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class SignInFirebaseAuth(private val context: Context) {
    //declarar e instanciar as variaveis
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val mAlertDialog = AlertDialog.Builder(context)
    private val mProgressDialog: ProgressDialog = ProgressDialog(context)
    private val mFirestore : FirebaseFirestore = FirebaseFirestore.getInstance()

    fun signInFirebaseAuth(email: String, senha: String) {
        mProgressDialog.setCancelable(false)
        mProgressDialog.show()
        mAuth.createUserWithEmailAndPassword(email, senha)
            .addOnCompleteListener {

                when {
                    it.isSuccessful -> {
                        //val random = Random().nextInt(4)
                        val intent = Intent(context, CreateProfileActivity::class.java)
                        //intent.putExtra(Constants.KEY.RANDOM_KEY, random.toString())
                        //mAuth.currentUser!!.sendEmailVerification()
                        val user = User()
                        user.nome = "default"
                        user.uid = mAuth.currentUser?.uid
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
                //mProgressDialog.dismiss()
            }
    }
}