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
import com.flyppcorp.managerServices.PendenteActivity

import com.flyppcorp.flypp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item

import kotlinx.android.synthetic.main.fragment_fragment_pendentes.view.*
import kotlinx.android.synthetic.main.manager_service_items.view.*

/**
 * A simple [Fragment] subclass.
 */
class FragmentPendentes : Fragment() {
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
        val view = inflater.inflate(R.layout.fragment_fragment_pendentes, container, false)
        view.recyclerPendente.adapter = mAdapter
        mAdapter.setOnItemClickListener { item, view ->
            val intent = Intent(context, PendenteActivity::class.java)
            val itemPendente: ItemPendente = item as ItemPendente
            intent.putExtra(Constants.KEY.SERVICE_STATUS, itemPendente.mMyservice)
            startActivity(intent)
        }
        fetchPendente()
        return view


    }

    private inner class ItemPendente(val mMyservice: Myservice) : Item<GroupieViewHolder>() {
        override fun getLayout(): Int {
            return R.layout.manager_service_items
        }

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.txtNomeServiceManager.text = mMyservice.serviceNome
            if (mMyservice.urlService != null) Picasso.get().load(mMyservice.urlService).resize(100, 100).centerCrop().placeholder(R.drawable.photo_work).into(
                viewHolder.itemView.imgServiceManager
            )
            if (mMyservice.urlContratado != null) Picasso.get().load(mMyservice.urlContratado).placeholder(R.drawable.btn_select_photo_profile).into(
                viewHolder.itemView.imgProfileImgManager
            )
            viewHolder.itemView.txtNomeContratado.text = mMyservice.nomeContratado
            viewHolder.itemView.txtNomeContratante.text = mMyservice.nomeContratante
            viewHolder.itemView.txtPrecoManager.text =
                "R$ ${mMyservice.preco.toString().replace(".",",")} por ${mMyservice.tipoCobranca}"

        }


    }

    private fun fetchPendente() {
        mFirestore.collection(Constants.COLLECTIONS.MY_SERVICE)
            .whereEqualTo("id.${mAuth.currentUser!!.uid}", true)
            .whereEqualTo("pendente", true)
            .addSnapshotListener { snapshot, exception ->
                mAdapter.clear()
                exception?.let {
                    Log.i("EXCEPTION", it.toString())
                }
                snapshot?.let {
                    for (doc in snapshot) {
                        val item = doc.toObject(Myservice::class.java)
                        mAdapter.add(ItemPendente(item))
                    }
                    mAdapter.notifyDataSetChanged()
                }
            }

    }


}
