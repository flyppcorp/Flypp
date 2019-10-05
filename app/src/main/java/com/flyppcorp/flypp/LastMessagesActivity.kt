package com.flyppcorp.flypp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import com.flyppcorp.atributesClass.LastMessage
import com.flyppcorp.atributesClass.Servicos
import com.flyppcorp.constants.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_last_messages.*
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.last_messages_layout.*
import kotlinx.android.synthetic.main.last_messages_layout.view.*

class LastMessagesActivity : AppCompatActivity() {

    private lateinit var mFirestore: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mAdapter: GroupAdapter<ViewHolder>
    private lateinit var mServices: Servicos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_last_messages)
        mFirestore = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()
        mAdapter = GroupAdapter()
        mServices = Servicos()
        rv_last_message.adapter = mAdapter
        rv_last_message.addItemDecoration( DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        mAdapter.setOnItemClickListener { item, view ->
            val intent = Intent(this, MessageActivity::class.java)
            val userItem : ItemLastMessage = item as ItemLastMessage
            mServices.uid = userItem.mLastMessage.toId
            mServices.urlProfile = userItem.mLastMessage.url
            mServices.nome = userItem.mLastMessage.name
            intent.putExtra(Constants.KEY.MESSAGE_KEY, mServices)
            startActivity(intent)



        }
        fetchMessages()
    }



    private inner class ItemLastMessage(val mLastMessage: LastMessage): Item<ViewHolder>() {
        override fun getLayout(): Int {
            return R.layout.last_messages_layout
        }

        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.txtNomeProfile.text = mLastMessage.name
            viewHolder.itemView.txtLastMessage.text = mLastMessage.lastMessage
            Picasso.get().load(mLastMessage.url).into(viewHolder.itemView.imgLastProfile)
        }
    }
    private fun fetchMessages() {
        val uid = mAuth.currentUser!!.uid
          mFirestore.collection(Constants.COLLECTIONS.LAST_MESSAGE)
              .document(uid)
              .collection(Constants.COLLECTIONS.CONTACTS)
              .addSnapshotListener { snapshot, firestoreException ->
                  val changes = snapshot?.documentChanges
                  changes?.let {
                      for (doc in it){
                          when(doc.type){
                              DocumentChange.Type.ADDED -> {
                                  val contact = doc.document.toObject(LastMessage::class.java)
                                  mAdapter.add(ItemLastMessage(contact))
                              }
                          }
                      }
                  }
              }
    }
}
