package com.flyppcorp.flypp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.flyppcorp.Helper.Connection
import com.flyppcorp.atributesClass.*
import com.flyppcorp.constants.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_message2.*
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
    private lateinit var mConnection: Connection
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
        mAdapter = GroupAdapter()
        mConnection = Connection(this)
        recyclerMessages.adapter = mAdapter
        val tb = findViewById<androidx.appcompat.widget.Toolbar>(R.id.tb_message)
        tb.title = ""
        setSupportActionBar(tb)
        btnVoltarTbmessage.setOnClickListener {
            finish()
        }
        txtTitlemessage.text = mUser?.nome

        setListeners()
        getUser()
        
    }


    override fun onResume() {
        super.onResume()
        val uid = mAuth.currentUser?.uid.toString()
        val  tsDoc = mFirestore.collection(Constants.COLLECTIONS.LAST_MESSAGE).document(uid).collection(Constants.COLLECTIONS.CONTACTS).document(mUser!!.uid!!)
        mFirestore.runTransaction {
            val content = it.get(tsDoc).toObject(LastMessage::class.java)

            if (content!!.unread){
                content.unread = content.unread == false
            }

            it.set(tsDoc, content)
        }
    }

    private inner class MessageItem(val message: Message) : Item<GroupieViewHolder>() {
        override fun getLayout(): Int {
            return if (message.fromId == mAuth.currentUser!!.uid)
                R.layout.from_id
            else
                R.layout.to_id
        }

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            if (message.fromId == mAuth.currentUser?.uid) {
                viewHolder.itemView.txt_msg_from.text = message.text
                Picasso.get().load(mMeUser?.url).resize(300,300).centerCrop().placeholder(R.drawable.btn_select_photo_profile).into(viewHolder.itemView.img_profile_from)
                val sdf = SimpleDateFormat("HH:mm dd/MM/yy")
                viewHolder.itemView.hrFrom.text = sdf.format(message.timestampView).toString()
            } else {
                viewHolder.itemView.txt_message_to.text = message.text
                Picasso.get().load(mUser?.url).resize(300,300).centerCrop().placeholder(R.drawable.btn_select_photo_profile).into(viewHolder.itemView.img_profile_to)
                val sdf = SimpleDateFormat("HH:mm dd/MM/yy")
                viewHolder.itemView.hrTo.text = sdf.format(message.timestampView).toString()
            }
        }
    }


    private fun getUser() {
        mFirestoreMessage.collection(Constants.COLLECTIONS.USER_COLLECTION)
            .document(mAuth.currentUser?.uid.toString())
            .get()
            .addOnSuccessListener {
                mMeUser = it.toObject(User::class.java)
                fetchMessages()
            }
    }

    private fun fetchMessages() {
        mMeUser?.let {
            val fromId = it.uid.toString()
            val toId = mUser?.uid
            mFirestore.collection(Constants.COLLECTIONS.CONVERSATION_COLLETION)
                .document(fromId)
                .collection(toId.toString())
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, exception ->
                    snapshot?.let {

                        mAdapter.clear()
                        for (doc in snapshot) {

                            val messeges = doc.toObject(Message::class.java)
                            mAdapter.add(MessageItem(messeges))


                        }
                        recyclerMessages.scrollToPosition(mAdapter.itemCount - 1)
                    }
                }


        }
    }

    private fun setListeners() {

        btnSendMessage.setOnClickListener {
            if (editTextMessage.text.toString() != "") {
                sendMessage()
            }
        }
    }

    private fun sendMessage() {
        mMeUser?.let { task ->
            val toId = mUser?.uid
            val fromId = mAuth.currentUser?.uid
            val timeZone = TimeZone.getTimeZone("America/Sao_Paulo")
            val simpledateFormat = SimpleDateFormat("yyyyMMddHHmmssSS")
            val timeNow = Calendar.getInstance(timeZone)
            val timestamp = simpledateFormat.format(timeNow.time).toLong()
            //val timestamp = Date().time
            var text = editTextMessage.text.toString()

            val message = Message()
            message.text = text
            message.timestamp = timestamp
            message.timestampView = System.currentTimeMillis()
            message.fromId = fromId
            message.toId = toId

            val messageFirestore = FirebaseFirestore.getInstance()
            if (mConnection.validateConection()) {

                if (validate()) {
                    messageFirestore.collection(Constants.COLLECTIONS.CONVERSATION_COLLETION)
                        .document(fromId.toString())
                        .collection(toId.toString())
                        .add(message)
                        .addOnSuccessListener {
                            messageFirestore.collection(Constants.COLLECTIONS.CONVERSATION_COLLETION)
                                .document(toId.toString())
                                .collection(fromId.toString())
                                .add(message)
                                .addOnSuccessListener {
                                    val uid = mAuth.currentUser?.uid
                                    if (uid == fromId){
                                        notificationMessage(mMeUser!!.nome!!, text, toId.toString())
                                    }else{
                                        notificationMessage(mMeUser!!.nome!!, text, fromId.toString())
                                    }

                                    val lastMessages = LastMessage()
                                    lastMessages.name = mUser!!.nome
                                    lastMessages.toId = toId
                                    lastMessages.url = mUser?.url
                                    lastMessages.timestamp = timestamp.toLong()
                                    lastMessages.text = text
                                    lastMessages.unread = false
                                    messageFirestore.collection(Constants.COLLECTIONS.LAST_MESSAGE)
                                        .document(fromId.toString())
                                        .collection(Constants.COLLECTIONS.CONTACTS)
                                        .document(toId.toString())
                                        .set(lastMessages)
                                        .addOnSuccessListener {
                                            val lastMessage = LastMessage()
                                            lastMessage.text = text
                                            lastMessage.timestamp = timestamp.toLong()
                                            lastMessage.url = task.url
                                            lastMessage.toId = fromId
                                            lastMessage.name = task.nome

                                            messageFirestore.collection(Constants.COLLECTIONS.LAST_MESSAGE)
                                                .document(toId.toString())
                                                .collection(Constants.COLLECTIONS.CONTACTS)
                                                .document(fromId.toString())
                                                .set(lastMessage)
                                        }


                                }
                        }
                }


            }
            editTextMessage.setText("")

        }

    }

    private fun validate(): Boolean {
        return editTextMessage.text.toString() != ""
    }

    private fun notificationMessage(nome: String, text: String, uid : String) {
        mFirestore.collection(Constants.COLLECTIONS.USER_COLLECTION)
            .document(uid)
            .get()
            .addOnSuccessListener {
                val itemUser = it.toObject(User::class.java)
                val mNotification = NotificationMessage()
                mNotification.title = "Nova mensagem de $nome"
                mNotification.token = itemUser!!.token
                mNotification.text = text

                val mFireNotification = FirebaseFirestore.getInstance()
                mFireNotification.collection(Constants.COLLECTIONS.NOTIFICATION)
                    .document(itemUser.token!!)
                    .set(mNotification)
            }

    }


}
