package com.flyppcorp.flypp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.SyncStateContract
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.flyppcorp.atributesClass.LastMessage
import com.flyppcorp.atributesClass.Message
import com.flyppcorp.atributesClass.NotificationMessage
import com.flyppcorp.atributesClass.User
import com.flyppcorp.constants.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item

import kotlinx.android.synthetic.main.activity_andamento.*
import kotlinx.android.synthetic.main.activity_message2.*
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.from_id.view.*
import kotlinx.android.synthetic.main.to_id.view.*
import java.text.SimpleDateFormat
import java.util.*

class MessageActivity : AppCompatActivity() {

    //mUser é a variavel que obtem o parcelable
    private var mUser: User? = null
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mFirestore: FirebaseFirestore
    private lateinit var mFirestoreMessage: FirebaseFirestore
    private lateinit var mMessage: Message
    private lateinit var mLastMessage: LastMessage
    var mMeUser: User? = null
    private lateinit var mAdapter: GroupAdapter<GroupieViewHolder>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message2)
        mUser = intent.extras?.getParcelable(Constants.KEY.MESSAGE_KEY)
        mAuth = FirebaseAuth.getInstance()
        mFirestore = FirebaseFirestore.getInstance()
        mFirestoreMessage = FirebaseFirestore.getInstance()
        mLastMessage = LastMessage()
        mMessage = Message()
        mAdapter =GroupAdapter()
        recyclerMessages.adapter = mAdapter
        supportActionBar?.title = mUser?.nome
        setListeners()
        getUser()
    }

    private inner class MessageItem(val message: Message ) : Item<GroupieViewHolder>(){
        override fun getLayout(): Int {
            return if (message.fromId == mAuth.currentUser!!.uid)
                R.layout.from_id
            else
                R.layout.to_id
        }

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            if (message.fromId == mAuth.currentUser!!.uid ){
                viewHolder.itemView.txt_msg_from.text = message.text
                Picasso.get().load(mMeUser?.url).into(viewHolder.itemView.img_profile_from)
            }else {
                viewHolder.itemView.txt_message_to.text = message.text
                Picasso.get().load(mUser?.url).into(viewHolder.itemView.img_profile_to)
            }
        }
    }




    private fun getUser() {
        mFirestoreMessage.collection(Constants.COLLECTIONS.USER_COLLECTION)
            .document(mAuth.currentUser!!.uid)
            .get()
            .addOnSuccessListener {
                mMeUser = it.toObject(User::class.java)
                fetchMessages()
            }
    }

    private fun fetchMessages() {
        mMeUser?.let {
            val fromId = it.uid.toString()
            val toId = mUser!!.uid

            mFirestoreMessage.collection(Constants.COLLECTIONS.CONVERSATION_COLLETION)
                .document(fromId)
                .collection(toId!!)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, exception ->
                    snapshot?.documentChanges?.let {
                        for (doc in it){
                            when (doc.type){
                                DocumentChange.Type.ADDED -> {
                                    val messagem = doc.document.toObject(Message::class.java)
                                    mAdapter.add(MessageItem(messagem))
                                }
                            }
                        }
                    }
                }
        }
    }

    private fun setListeners() {

        btnSendMessage.setOnClickListener {
              if (editTextMessage.text.toString() != ""){
                  sendMessage()
              }
        }
    }

    private fun sendMessage(){
        val timestamp = SimpleDateFormat("yMdHMs", Locale.getDefault()).format(Date()).toLong()
        var txtMessage = editTextMessage.text.toString()

            mMessage.fromId = mAuth.currentUser!!.uid
            mMessage.toId = mUser!!.uid
            mMessage.timestamp = timestamp
            mMessage.text = txtMessage

            val fromId = mAuth.currentUser!!.uid
            val toId = mUser!!.uid

            mFirestore.collection(Constants.COLLECTIONS.CONVERSATION_COLLETION)
                .document(fromId)
                .collection(toId!!)
                .add(mMessage)
                .addOnSuccessListener {
                    mLastMessage.timestamp = timestamp
                    mLastMessage.url = mUser?.url
                    mLastMessage.name = mUser!!.nome
                    mLastMessage.toId = mUser!!.uid
                    mLastMessage.lastMessage = txtMessage

                    mFirestoreMessage.collection(Constants.COLLECTIONS.LAST_MESSAGE)
                        .document(fromId)
                        .collection(Constants.COLLECTIONS.CONTACTS)
                        .document(toId)
                        .set(mLastMessage)

                }

            mFirestore.collection(Constants.COLLECTIONS.CONVERSATION_COLLETION)
                .document(toId!!)
                .collection(fromId)
                .add(mMessage)
                .addOnSuccessListener {
                    mLastMessage.timestamp = timestamp
                    mLastMessage.url = mMeUser?.url
                    mLastMessage.name = mMeUser!!.nome
                    mLastMessage.toId = mMeUser!!.uid
                    mLastMessage.lastMessage = txtMessage

                    mFirestoreMessage.collection(Constants.COLLECTIONS.LAST_MESSAGE)
                        .document(toId)
                        .collection(Constants.COLLECTIONS.CONTACTS)
                        .document(fromId)
                        .set(mLastMessage)
                }

            editTextMessage.setText("")
        }


}
