package com.flyppcorp.flypp

import android.content.Context
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.flyppcorp.atributesClass.LastMessage
import com.flyppcorp.atributesClass.Message
import com.flyppcorp.atributesClass.Servicos
import com.flyppcorp.atributesClass.User
import com.flyppcorp.constants.Constants
import com.flyppcorp.firebase_classes.FirestoreMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_confirm_service.*
import kotlinx.android.synthetic.main.activity_message.*
import kotlinx.android.synthetic.main.from_id.*
import kotlinx.android.synthetic.main.from_id.view.*
import kotlinx.android.synthetic.main.to_id.*
import kotlinx.android.synthetic.main.to_id.view.*

class MessageActivity : AppCompatActivity() {

    private lateinit var mFirestore: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth
    private var mUser: Servicos? = null
    private var mMe: User? = null
    private lateinit var mMessage: Message
    private lateinit var mAdapter: GroupAdapter<ViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)
        mFirestore = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()
        mMessage = Message()
        mAdapter = GroupAdapter()
        mUser = intent.extras?.getParcelable(Constants.KEY.MESSAGE_KEY)
        btnSend.setOnClickListener {
            handleSend()
        }
        rv_message.adapter = mAdapter
        getDados()

    }


    private fun getDados() {
        mFirestore.collection(Constants.COLLECTIONS.USER_COLLECTION)
            .document(mAuth.currentUser!!.uid)
            .get()
            .addOnSuccessListener {
                mMe = it.toObject(User::class.java)
                fetchMessage()
            }
    }

    private inner class MensagemItem(val message: Message): Item<ViewHolder>(){
        override fun getLayout(): Int {
            return if (message.fromId == mAuth.currentUser!!.uid) R.layout.from_id
            else R.layout.to_id
        }

        override fun bind(viewHolder: ViewHolder, position: Int) {
            if (message.fromId == mAuth.currentUser!!.uid){
                viewHolder.itemView.txt_msg_from.text = message.text
                Picasso.get().load(mMe?.url).into(viewHolder.itemView.img_profile_from)
            }else{
                viewHolder.itemView.txt_message_to.text = message.text
                Picasso.get().load(mUser?.urlProfile).into(viewHolder.itemView.img_profile_to)
            }
        }


    }

    private fun fetchMessage() {
        mMe?.let {
            val fromId = it.uid
            val toId = mUser!!.uid

            mFirestore.collection(Constants.COLLECTIONS.CONVERSATION_COLLETION)
                .document(fromId!!)
                .collection(toId!!)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, exception ->
                    snapshot?.documentChanges?.let {
                        for (doc in it){
                            when(doc.type){
                                DocumentChange.Type.ADDED ->{
                                    val  mensagem = doc.document.toObject(Message::class.java)
                                    mAdapter.add(MensagemItem(mensagem))
                                }
                            }

                        }
                    }
                }
        }
    }

    private fun handleSend() {
        val message = editMessage.text.toString()
        mMessage.toId = mUser!!.uid
        mMessage.fromId = mAuth.currentUser!!.uid
        mMessage.timestamp = System.currentTimeMillis()
        mMessage.text = message
        editMessage.setText("")

        mFirestore.collection(Constants.COLLECTIONS.CONVERSATION_COLLETION)
            .document(mUser!!.uid!!)
            .collection(mAuth.currentUser!!.uid)
            .add(mMessage)
            .addOnSuccessListener {
                val mLastMessage : LastMessage = LastMessage()
                mLastMessage.lastMessage = message
                mLastMessage.name = mUser?.nome
                mLastMessage.url = mUser?.urlProfile
                mLastMessage.toId = mUser?.uid
                mLastMessage.timestamp = System.currentTimeMillis()

                mFirestore.collection(Constants.COLLECTIONS.LAST_MESSAGE)
                    .document(mAuth.currentUser!!.uid)
                    .collection(Constants.COLLECTIONS.CONTACTS)
                    .document(mUser!!.uid!!)
                    .set(mLastMessage)
            }

        mFirestore.collection(Constants.COLLECTIONS.CONVERSATION_COLLETION)
            .document(mAuth.currentUser!!.uid)
            .collection(mUser!!.uid!!)
            .add(mMessage)
            .addOnSuccessListener {
                val mLastMessage : LastMessage = LastMessage()
                mLastMessage.lastMessage = message
                mLastMessage.name = mMe?.nome
                mLastMessage.url = mMe?.url
                mLastMessage.toId = mMe?.uid
                mLastMessage.timestamp = System.currentTimeMillis()

                mFirestore.collection(Constants.COLLECTIONS.LAST_MESSAGE)
                    .document(mUser!!.uid!!)
                    .collection(Constants.COLLECTIONS.CONTACTS)
                    .document(mAuth.currentUser!!.uid)
                    .set(mLastMessage)
            }
    }
}
