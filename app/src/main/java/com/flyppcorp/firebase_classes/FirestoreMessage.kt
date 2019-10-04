package com.flyppcorp.firebase_classes

import android.content.Context
import com.flyppcorp.atributesClass.LastMessage
import com.flyppcorp.atributesClass.Message
import com.flyppcorp.constants.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FirestoreMessage (context: Context) {
    private val mMessage : FirebaseFirestore = FirebaseFirestore.getInstance()
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    fun sendMessage(message : Message, toId: String, lastMessage: LastMessage ){

        mMessage.collection(Constants.COLLECTIONS.CONVERSATION_COLLETION)
            .document(mAuth.currentUser!!.uid)
            .collection(toId)
            .add(message)
            .addOnSuccessListener {
                mMessage.collection(Constants.COLLECTIONS.LAST_MESSAGE)
                    .document(mAuth.currentUser!!.uid)
                    .collection(Constants.COLLECTIONS.CONTACTS)
                    .document(toId)
                    .set(lastMessage)
            }

        mMessage.collection(Constants.COLLECTIONS.CONVERSATION_COLLETION)
            .document(toId)
            .collection(mAuth.currentUser!!.uid)
            .add(message)
            .addOnSuccessListener {
                mMessage.collection(Constants.COLLECTIONS.LAST_MESSAGE)
                    .document(toId)
                    .collection(Constants.COLLECTIONS.CONTACTS)
                    .document(mAuth.currentUser!!.uid)
                    .set(lastMessage)
            }
    }
}