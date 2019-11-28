package com.flyppcorp.flypp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast

import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.flyppcorp.atributesClass.LastMessage
import com.flyppcorp.atributesClass.User
import com.flyppcorp.constants.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_last_message.*
import kotlinx.android.synthetic.main.last_messages_layout.view.*

class LastMessages : AppCompatActivity() {

    private lateinit var mLastMessage: LastMessage
    private lateinit var mAdapter: GroupAdapter<GroupieViewHolder>
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mfirestore: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_last_message)
        mfirestore = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()
        mAdapter = GroupAdapter()
        mLastMessage = LastMessage()
        rv_last_messages.adapter = mAdapter

        mAdapter.setOnItemClickListener { item, view ->
            val item : LastMessageItem = item as LastMessageItem
            mfirestore.collection(Constants.COLLECTIONS.USER_COLLECTION)
                .document(item.mLast.toId!!)
                .get()
                .addOnSuccessListener {
                    val user = it.toObject(User::class.java)
                    val intent = Intent(this, MessageActivity::class.java)
                    intent.putExtra(Constants.KEY.MESSAGE_KEY, user)
                    startActivity(intent)
                }

        }

        fetchLastMessage()
    }

    private inner class LastMessageItem(val mLast: LastMessage) : Item<GroupieViewHolder>(){
        override fun getLayout(): Int {
            return  R.layout.last_messages_layout
        }

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.txtNomeProfile.text = mLast.name
            Picasso.get().load(mLast.url).into(viewHolder.itemView.imgLastProfile)
            viewHolder.itemView.txtLastMessage.text = mLast.text
        }



    }
    private fun fetchLastMessage() {
        val uid = mAuth.currentUser!!.uid
        mfirestore.collection(Constants.COLLECTIONS.LAST_MESSAGE)
            .document(uid)
            .collection(Constants.COLLECTIONS.CONTACTS)
            .addSnapshotListener { snapshot, exception ->
                snapshot?.let {
                    mAdapter.clear()
                    for (doc in snapshot) {
                        val lm = doc.toObject(LastMessage::class.java)
                        mAdapter.add(LastMessageItem(lm))
                    }
                }
            }
    }




}
