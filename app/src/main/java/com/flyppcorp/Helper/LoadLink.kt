package com.flyppcorp.Helper

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.flyppcorp.atributesClass.DashBoard
import com.flyppcorp.atributesClass.Servicos
import com.flyppcorp.constants.Constants
import com.flyppcorp.flypp.LoginActivity
import com.flyppcorp.flypp.MainActivity
import com.flyppcorp.flypp.R
import com.flyppcorp.flypp.ServiceActivity
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
                }

            }
            .addOnFailureListener(this) { e -> Log.w("TAG", "getDynamicLink:onFailure", e) }
    }
    private fun handleAnonimo(serviceId : String) {
        mAuth.signInAnonymously()
            .addOnCompleteListener {
                if (it.isSuccessful){
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
                }else {
                    Toast.makeText(this, "Oops! Algo deu errado", Toast.LENGTH_SHORT).show()
                }

            }
    }
}
