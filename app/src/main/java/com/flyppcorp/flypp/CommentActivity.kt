package com.flyppcorp.flypp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import com.flyppcorp.atributesClass.Comentarios
import com.flyppcorp.atributesClass.Servicos
import com.flyppcorp.constants.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_comment.*
import kotlinx.android.synthetic.main.comment_layout.view.*

class CommentActivity : AppCompatActivity() {

    private lateinit var mFirestore: FirebaseFirestore
    private  var mService : Servicos? = null
    private lateinit var mAdapter: GroupAdapter<GroupieViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)

        mFirestore = FirebaseFirestore.getInstance()
        mService = intent.extras?.getParcelable(Constants.KEY.COMMENTS)
        mAdapter = GroupAdapter()
        rv_comments.adapter = mAdapter
        rv_comments.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        btnVoltarTb.setOnClickListener {
            finish()
        }
        fetchComments()
    }

    inner class ItemComments(val mComments : Comentarios): Item<GroupieViewHolder>(){
        override fun getLayout(): Int {
            return R.layout.comment_layout
        }

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.txtNomeComment.text = mComments.nomeContratante
            viewHolder.itemView.txtComment.text = mComments.comentario
            if (mComments.urlContratante != null) Picasso.get().load(mComments.urlContratante).resize(300,300).centerCrop().into(viewHolder.itemView.img_profile_comment)

        }


    }

    private fun fetchComments(){
        mFirestore.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
            .document(mService!!.serviceId!!)
            .collection(Constants.COLLECTIONS.COMMENTS)
            .addSnapshotListener { snapshot, exception ->
                snapshot?.let {
                    for( doc in snapshot){
                        val item = doc.toObject(Comentarios::class.java)
                        mAdapter.add(ItemComments(item))
                    }
                }
            }

    }
}
