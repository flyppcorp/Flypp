package com.flyppcorp.flypp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import com.flyppcorp.atributesClass.LastMessage
import com.flyppcorp.atributesClass.User
import com.flyppcorp.constants.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_last_message.*
import kotlinx.android.synthetic.main.last_messages_layout.view.*

class LastMessages : AppCompatActivity() {

    private lateinit var mLastMessage: LastMessage
    private lateinit var mAdapter: GroupAdapter<ViewHolder>
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mfirestore: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_last_message)
        mLastMessage = LastMessage()
        mAdapter = GroupAdapter()
        mAuth = FirebaseAuth.getInstance()
        mfirestore = FirebaseFirestore.getInstance()
        rv_last_messages.adapter = mAdapter
        rv_last_messages.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        mAdapter.setOnItemClickListener { item, view ->
            val intent = Intent(this, MessageActivity::class.java)
            val userItem : ContactItem = item as ContactItem
            mfirestore.collection(Constants.COLLECTIONS.USER_COLLECTION)
                .document(userItem.contact.toId!!)
                .get()
                .addOnSuccessListener {
                    val user = it.toObject(User::class.java)
                    intent.putExtra(Constants.KEY.MESSAGE_KEY, user)
                    startActivity(intent)
                }
        }



        fetchLastMessage()

    }

    inner class ContactItem(val contact: LastMessage) : Item<ViewHolder>() {
        override fun getLayout(): Int {
            return R.layout.last_messages_layout
        }

        override fun bind(viewHolder: ViewHolder, position: Int) {
            val myItem = viewHolder.itemView
            myItem.txtNomeProfile.text = contact.name
            myItem.txtLastMessage.text = contact.lastMessage
            Picasso.get().load(contact.url).into(myItem.imgLastProfile)
        }

    }


    private fun fetchLastMessage() {
        val uid = mAuth.currentUser!!.uid
        mfirestore.collection(Constants.COLLECTIONS.LAST_MESSAGE)
            .document(uid)
            .collection(Constants.COLLECTIONS.CONTACTS)
            .addSnapshotListener { snapshot, exception ->
                val changes = snapshot?.documentChanges
                changes?.let {
                    for (doc in it) {
                        when (doc.type) {
                            DocumentChange.Type.ADDED -> {
                                val last = doc.document.toObject(LastMessage::class.java)
                                mAdapter.add(ContactItem(last))
                            }
                        }
                    }
                }


            }
    }
}
