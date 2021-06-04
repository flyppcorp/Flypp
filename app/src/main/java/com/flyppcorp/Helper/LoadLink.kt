package com.flyppcorp.Helper

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.flyppcorp.atributesClass.Servicos
import com.flyppcorp.constants.Constants
import com.flyppcorp.flypp.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_load_link.*

class LoadLink : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mfirestore: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_load_link)
        mAuth = FirebaseAuth.getInstance()
        mfirestore = FirebaseFirestore.getInstance()


        FirebaseDynamicLinks.getInstance()
            .getDynamicLink(intent)
            .addOnSuccessListener(this) { pendingDynamicLinkData ->
                // Get deep link from result (may be null if no link is found)
                var deepLink: Uri? = null
                if (pendingDynamicLinkData != null) {
                    deepLink = pendingDynamicLinkData.link
                    if (deepLink.toString().substringAfter("&uid=")
                            .substring(0, 1) == "-" && deepLink.toString()
                            .substringAfter("&utm_source=") != "-"
                    ) {
                        if (mAuth.currentUser?.uid != null) {
                            mfirestore.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
                                .document(deepLink.toString().substringAfter("&utm_source="))
                                .get()
                                .addOnSuccessListener {
                                    val service = it.toObject(Servicos::class.java)
                                    val intent = Intent(this, ServiceActivity::class.java)
                                    intent.putExtra(Constants.KEY.SERVICE_KEY, service)
                                    progressBarLoad?.visibility = View.GONE
                                    startActivity(intent)
                                    finish()
                                }

                        } else {
                            handleAnonimo(deepLink.toString().substringAfter("&utm_source="))
                        }
                    } else if (deepLink.toString().substringAfter("&uid=")
                            .substring(0, 2) == "--" && deepLink.toString()
                            .substringAfter("&utm_source=") == "-"
                    ) {


                        handleNewUser()

                    }
                    else if(deepLink.toString().substringAfter("&uid=").substring(0, 1) != "-" && deepLink.toString().substringAfter("&utm_source=") == "-"){
                        val deepL = deepLink.toString().substringAfter("&uid=")
                            .substringBefore("&utm_source")
                        goToMarket(deepL)
                    }

                } else {
                    val alert = AlertDialog.Builder(this)
                        .setTitle("Ops!")
                        .setMessage("Por favor, abra novamente o link, você não vai deixar essa chance passar né ?")
                        .setPositiveButton("Ok", { dialogInterface, i ->
                            finish()
                        })
                    alert.show()
                }

            }
            .addOnFailureListener(this) { e ->
                Log.w("TAG", "getDynamicLink:onFailure", e)
                val alert = AlertDialog.Builder(this)
                    .setTitle("Ops!")
                    .setMessage("Por favor, abra novamente o link, você não vai deixar essa chance passar né ?")
                    .setPositiveButton("Ok", { dialogInterface, i ->
                        finish()
                    })
                alert.show()
            }
    }

    private fun handleNewUser() {
        if (mAuth.currentUser?.uid != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun handleAnonimo(serviceId: String) {
        mAuth.signInAnonymously()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    mfirestore.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
                        .document(serviceId)
                        .get()
                        .addOnSuccessListener {
                            val service = it.toObject(Servicos::class.java)
                            val intent = Intent(this, ServiceActivity::class.java)
                            intent.putExtra(Constants.KEY.SERVICE_KEY, service)
                            progressBarLoad?.visibility = View.GONE
                            startActivity(intent)
                            finish()
                        }
                } else {
                    progressBarLoad?.visibility = View.GONE
                    Toast.makeText(this, "Oops! Algo deu errado", Toast.LENGTH_SHORT).show()
                }

            }
    }

    private fun goToMarket(uid: String){
        if (mAuth.currentUser?.uid != null){
            val intent = Intent(this, AllProductsActivity::class.java)
            intent.putExtra(Constants.KEY.ALL_PRODS, uid)
            startActivity(intent)
            finish()
        }else{
            mAuth.signInAnonymously()
                .addOnCompleteListener {
                    if (it.isSuccessful){
                        val intent = Intent(this, AllProductsActivity::class.java)
                        intent.putExtra(Constants.KEY.ALL_PRODS, uid)
                        progressBarLoad?.visibility = View.GONE
                        startActivity(intent)

                        finish()
                    }else{
                        progressBarLoad?.visibility = View.GONE
                        Toast.makeText(this, "Oops! Algo deu errado", Toast.LENGTH_SHORT).show()
                    }
                }
        }

    }
}
