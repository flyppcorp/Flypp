package com.flyppcorp.tabsFragments


import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import com.flyppcorp.atributesClass.Myservice
import com.flyppcorp.constants.Constants
import com.flyppcorp.managerServices.AndamentoActivity

import com.flyppcorp.flypp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.fragment_fragment_andamento.view.*
import kotlinx.android.synthetic.main.manager_service_items.view.*

/**
 * A simple [Fragment] subclass.
 */
class FragmentAndamento : Fragment() {
    private lateinit var mFirestore: FirebaseFirestore
    private lateinit var mAdapter: GroupAdapter<GroupieViewHolder>
    private lateinit var mAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mFirestore = FirebaseFirestore.getInstance()
        mAdapter = GroupAdapter()
        mAuth = FirebaseAuth.getInstance()
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_fragment_andamento, container, false)
        view.recyclerAndamento.adapter = mAdapter
        mAdapter.setOnItemClickListener { item, view ->
            val intent = Intent(context, AndamentoActivity::class.java)
            val userItem: ItemAndamento = item as ItemAndamento
            intent.putExtra(Constants.KEY.SERVICE_STATUS, userItem.mMyservice)
            startActivity(intent)
        }
        fetchAndamento()
        return view

    }

    private inner class ItemAndamento(val mMyservice: Myservice) : Item<GroupieViewHolder>() {
        override fun getLayout(): Int {
            return R.layout.manager_service_items
        }

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.txtNomeServiceManager.text = mMyservice.serviceNome
            if (mMyservice.urlService != null) Picasso.get().load(mMyservice.urlService).resize(100,100).centerCrop().placeholder(R.drawable.photo_work).into(
                viewHolder.itemView.imgServiceManager
            )
            if (mMyservice.urlContratado != null) Picasso.get().load(mMyservice.urlContratado).resize(100,100).centerCrop().placeholder(R.drawable.btn_select_photo_profile).into(
                viewHolder.itemView.imgProfileImgManager
            )
            viewHolder.itemView.txtNomeContratado.text = mMyservice.nomeContratado
            viewHolder.itemView.txtNomeContratante.text = mMyservice.nomeContratante


            if (mMyservice.preco.toString().substringAfter(".").length == 1){
                viewHolder.itemView.txtPrecoManager.text =
                    "R$ ${mMyservice.preco.toString().replace(
                        ".",
                        ","
                    )}${"0"} Por ${mMyservice.tipoCobranca}"
            }else{
                viewHolder.itemView.txtPrecoManager.text =
                    "R$ ${mMyservice.preco.toString().replace(
                        ".",
                        ","
                    )} Por ${mMyservice.tipoCobranca}"
            }


        }


    }

    private fun fetchAndamento() {
        mFirestore.collection(Constants.COLLECTIONS.MY_SERVICE)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, exception ->
                mAdapter.clear()
                exception?.let {
                    Log.i("EXCEPTION", it.toString())
                }
                snapshot?.let {
                    for (doc in snapshot) {
                        val item = doc.toObject(Myservice::class.java)
                        if (item.id.containsKey(mAuth.currentUser?.uid) && item.andamento){
                            mAdapter.add(ItemAndamento(item))
                        }

                    }
                    mAdapter.notifyDataSetChanged()
                }
            }

    }

}
