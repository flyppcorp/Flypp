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
import kotlinx.android.synthetic.main.from_id.view.*
import kotlinx.android.synthetic.main.to_id.view.*

class MessageActivity : AppCompatActivity() {

    var mUser: Servicos? = null
    private lateinit var mMensagens: Message
    private lateinit var mFirestoreMessage: FirestoreMessage
    private lateinit var mAuth: FirebaseAuth
    private var mMe: User? = null
    private lateinit var mFirestore : FirebaseFirestore
    private lateinit var mLastMessage: LastMessage
    private lateinit var mAdapter : GroupAdapter<ViewHolder>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)
        mAdapter = GroupAdapter()
        rv_message.adapter = mAdapter
        mUser = intent.extras?.getParcelable(Constants.KEY.MESSAGE_KEY)
        //mLast = intent.extras?.getParcelable(Constants.KEY.LAST_MESSAGE_KEY)
        mMensagens = Message()
        mLastMessage = LastMessage()
        mAuth = FirebaseAuth.getInstance()
        mFirestore = FirebaseFirestore.getInstance()
        mFirestoreMessage = FirestoreMessage(this)

            supportActionBar?.title = mUser?.nome


        btnSend.setOnClickListener {
            handleSend()
            if (!validateConection()){
                return@setOnClickListener
            }


        }
        mFirestore.collection(Constants.COLLECTIONS.USER_COLLECTION)
            .document(mAuth.currentUser!!.uid)
            .get()
            .addOnSuccessListener {
                mMe = it.toObject(User::class.java)
                fetchMessages()
            }




    }

    private inner class MessageItem(private val mMessage: Message) : Item<ViewHolder>(){

        override fun getLayout(): Int {
            return if (mMessage.fromId == mAuth.currentUser!!.uid)
                R.layout.from_id
            else
                R.layout.to_id
        }

        override fun bind(viewHolder: ViewHolder, position: Int) {
            if (mMessage.fromId == mAuth.currentUser?.uid){
                viewHolder.itemView.txt_msg_from.text = mMessage.text
                Picasso.get().load(mUser?.urlProfile).into(viewHolder.itemView.img_profile_from)

            }else{
                viewHolder.itemView.txt_message_to.text = mMessage.text
                Picasso.get().load(mUser?.urlProfile).into(viewHolder.itemView.img_profile_to)


            }
        }


    }

    private fun fetchMessages() {
        mMe?.let {
            val fromId = it.uid.toString()
            val toId = mUser!!.uid.toString()



            mFirestore.collection(Constants.KEY.CONVERSATION_KEY)
                .document(fromId)
                .collection(toId!!)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, exception ->
                    //mAdapter.clear()
                    snapshot?.documentChanges?.let {
                        for (doc in it){
                            when(doc.type){
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

    private fun handleSend() {
        val text = editMessage.text.toString()
        if (validate()){
            mMensagens.text = text
            mMensagens.toId = mUser!!.uid

            mMensagens.fromId = mAuth.currentUser!!.uid
            mMensagens.timestamp = System.currentTimeMillis()


            mLastMessage.name = mUser?.nome
            mLastMessage.lastMessage = text
            mLastMessage.uid = mUser?.uid
            mLastMessage.url = mUser?.urlProfile


            mLastMessage.timestamp = System.currentTimeMillis()
            mFirestoreMessage.sendMessage(mMensagens, mUser!!.uid.toString(), mLastMessage)


           editMessage.setText("")

        }
    }

    private fun validate(): Boolean{
        return  editMessage.text.toString() != ""
    }
    private fun validateConection(): Boolean{
        val cm = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = cm.activeNetworkInfo
        if (networkInfo != null && networkInfo.isConnected){
            return true
        }else{
            progressBar.visibility = View.GONE
            Toast.makeText(this, "Você não possui conexão com a internet", Toast.LENGTH_SHORT).show()
            return false
        }

    }
}
