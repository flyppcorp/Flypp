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
import com.flyppcorp.flypp.LoginActivity
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
                    progressBarLoad?.visibility = View.GONE
                    if (mAuth.currentUser?.uid != null) {
                        mfirestore.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
                            .document(deepLink.toString().substringAfter("&utm_source="))
                            .get()
                            .addOnSuccessListener {
                                val service = it.toObject(Servicos::class.java)
                                val intent = Intent(this, ServiceActivity::class.java)
                                intent.putExtra(Constants.KEY.SERVICE_KEY, service)
                                startActivity(intent)
                                finish()
                            }

                    } else {
                        val alert = AlertDialog.Builder(this)
                        alert.setTitle("Ops! Você não está logado")
                            .setCancelable(false)
                            .setMessage("Você precisa fazer o login ou criar uma conta para prosseguir.")
                            .setPositiveButton("Fazer Login", { dialog, which ->
                                startActivity(Intent(this, LoginActivity::class.java))
                                finish()
                            })
                        alert.show()
                    }
                }

                // Handle the deep link. For example, open the linked
                // content, or apply promotional credit to the user's
                // account.
                // ...

                // ...
            }
            .addOnFailureListener(this) { e -> Log.w("TAG", "getDynamicLink:onFailure", e) }
    }
}
