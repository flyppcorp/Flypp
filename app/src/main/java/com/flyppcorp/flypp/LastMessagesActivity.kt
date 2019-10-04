package com.flyppcorp.flypp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
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
    private lateinit var mUser: FirebaseFirestore
    private lateinit var mAdapter : GroupAdapter<ViewHolder>
    private lateinit var mServicos: Servicos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_last_messages)
        mFirestore = FirebaseFirestore.getInstance()
        mUser = FirebaseFirestore.getInstance()
        mServicos = Servicos()

        mAdapter = GroupAdapter()
        rv_last_message.adapter = mAdapter
        mAdapter.setOnItemClickListener { item, view ->
            val intent = Intent(this, MessageActivity::class.java)
            val userItem : ContactItem = item as ContactItem
            mServicos.nome = userItem.mLastMessage.name
            mServicos.urlProfile = userItem.mLastMessage.url
            mServicos.uid = userItem.mLastMessage.uid
            intent.putExtra(Constants.KEY.MESSAGE_KEY, mServicos)
            startActivity(intent)

        }
        fetchLast()




    }


    private fun fetchLast() {
        val uid = FirebaseAuth.getInstance().currentUser!!.uid

        mFirestore.collection(Constants.COLLECTIONS.LAST_MESSAGE)
            .document(uid)
            .collection(Constants.COLLECTIONS.CONTACTS)
            .addSnapshotListener { snapshot, exception ->
                val changes = snapshot?.documentChanges
                changes?.let{
                    for (doc in it){
                        when(doc.type){
                            DocumentChange.Type.ADDED ->{
                                val contact = doc.document.toObject(LastMessage::class.java)
                                mAdapter.add(ContactItem((contact)))



                            }
                        }
                    }
                }
            }
    }

    private inner class ContactItem(val mLastMessage: LastMessage): Item<ViewHolder>(){
        override fun getLayout(): Int {
            return R.layout.last_messages_layout

        }

        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.txtNomeProfile.text = mLastMessage.name
            viewHolder.itemView.txtLastMessage.text = mLastMessage.lastMessage
            if (mLastMessage.url == null){
                imgLastProfile.setImageResource(R.drawable.ic_work)
            }else{
                Picasso.get().load(mLastMessage.url).into(viewHolder.itemView.imgLastProfile)
            }


        }


    }
}
