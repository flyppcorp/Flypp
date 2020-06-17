package com.flyppcorp.flypp

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.flyppcorp.atributesClass.LastMessage
import com.flyppcorp.atributesClass.User
import com.flyppcorp.constants.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
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
                .document(item.mLast.toId.toString())
                .get()
                .addOnSuccessListener {
                    val user = it.toObject(User::class.java)
                    val intent = Intent(this, MessageActivity::class.java)
                    intent.putExtra(Constants.KEY.MESSAGE_KEY, user)
                    startActivity(intent)
                }

        }
        mAdapter.setOnItemLongClickListener { item, view ->
            val item : LastMessageItem = item as LastMessageItem
            //Toast.makeText(this, item.mLast.toId, Toast.LENGTH_LONG).show()
            val fromId = mAuth.currentUser?.uid.toString()
            val toId = item.mLast.toId
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle("Apagar conversa")
            alertDialog.setMessage("Você tem certeza que deseja apagar esta convers ? ")
            alertDialog.setNegativeButton("Não", {dialog, which ->  })
            alertDialog.setPositiveButton("Sim", {dialog, which ->
                handleDelete(fromId, toId)
            })
            alertDialog.show()
            return@setOnItemLongClickListener true
        }

        btn_voltar_lm.setOnClickListener {
            finish()
        }

        fetchLastMessage()
    }

    private fun handleDelete(fromId: String, toId: String?) {
        val progressBar = ProgressDialog(this)
        progressBar.setCancelable(false)
        progressBar.setMessage("Apagando conversas")
        progressBar.show()
        mfirestore.collection(Constants.COLLECTIONS.CONVERSATION_COLLETION)
            .document(fromId)
            .collection(toId.toString())
            .get()
            .addOnSuccessListener {
                val batch : WriteBatch = FirebaseFirestore.getInstance().batch()
                val snapshotList: List<DocumentSnapshot> = it.documents
                for (snapshot in snapshotList){
                    batch.delete(snapshot.reference)
                }
                batch.commit()
                    .addOnSuccessListener {
                       mfirestore.collection(Constants.COLLECTIONS.LAST_MESSAGE)
                           .document(fromId)
                           .collection(Constants.COLLECTIONS.CONTACTS)
                           .document(toId.toString())
                           .delete()
                           .addOnSuccessListener {
                               progressBar.hide()
                           }.addOnFailureListener {
                               progressBar.hide()
                           }
                    }.addOnFailureListener {
                        progressBar.hide()
                    }
            }


    }

    private inner class LastMessageItem(val mLast: LastMessage) : Item<GroupieViewHolder>(){
        override fun getLayout(): Int {
            return  R.layout.last_messages_layout
        }

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.txtNomeProfile.text = mLast.name
            Picasso.get().load(mLast.url).resize(300,300).centerCrop().placeholder(R.drawable.btn_select_photo_profile).into(viewHolder.itemView.imgLastProfile)
            viewHolder.itemView.txtLastMessage.text = mLast.text
            if (mLast.unread){
                viewHolder.itemView.dot.visibility = View.VISIBLE
            }else{
                viewHolder.itemView.dot.visibility = View.GONE
            }
        }



    }
    private fun fetchLastMessage() {
        val uid = mAuth.currentUser?.uid
        mfirestore.collection(Constants.COLLECTIONS.LAST_MESSAGE)
            .document(uid.toString())
            .collection(Constants.COLLECTIONS.CONTACTS)
            .orderBy("timestamp", Query.Direction.DESCENDING)
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
