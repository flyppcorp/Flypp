package com.flyppcorp.firebase_classes

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.flyppcorp.atributesClass.Myservice
import com.flyppcorp.constants.Constants
import com.flyppcorp.flypp.MainActivity
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.properties.Delegates

class FirestoreContract (var context: Context) {
    private val mFirestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    var mProgress : Boolean = true



    fun confirmServiceContract(myservice: Myservice, documentId: String){
        mFirestore.collection(Constants.COLLECTIONS.MY_SERVICE)
            .document(documentId)
            .set(myservice)
            .addOnSuccessListener {
                mProgress
                val intent = Intent(context, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(context, intent, null)


            }.addOnFailureListener {
                Toast.makeText(context, "Algo saiu errado, tente outra vez.", Toast.LENGTH_SHORT).show()
            }

    }
}