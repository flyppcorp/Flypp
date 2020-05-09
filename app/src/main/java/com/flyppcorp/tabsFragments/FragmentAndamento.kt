package com.flyppcorp.tabsFragments


import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.flyppcorp.atributesClass.Myservice
import com.flyppcorp.constants.Constants
import com.flyppcorp.managerServices.AndamentoActivity

import com.flyppcorp.flypp.R
import com.flyppcorp.managerServices.PendenteActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.fragment_fragment_andamento.view.*
import kotlinx.android.synthetic.main.manager_service_items.view.*
import java.text.SimpleDateFormat
import java.util.ArrayList

/**
 * A simple [Fragment] subclass.
 */
class FragmentAndamento : Fragment() {
    private lateinit var mFirestore: FirebaseFirestore
    private lateinit var mAdapter: AndamentoRecyclerView
    private lateinit var mAuth: FirebaseAuth
    private lateinit var servicos: ArrayList<Myservice>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mFirestore = FirebaseFirestore.getInstance()
        mAdapter = AndamentoRecyclerView()
        servicos = arrayListOf()
        mAuth = FirebaseAuth.getInstance()
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_fragment_andamento, container, false)
        view.recyclerAndamento.adapter = mAdapter
        mAdapter.onItemClicked = {
            val intent = Intent(context, AndamentoActivity::class.java)
            intent.putExtra(Constants.KEY.SERVICE_STATUS, servicos[it])
            startActivity(intent)
        }

        fetchAndamento()
        return view

    }

    private inner class AndamentoRecyclerView: RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view =
                LayoutInflater.from(parent.context).inflate(R.layout.manager_service_items, parent, false)
            return CustomViewHolderAndamento(view)
        }

        inner class CustomViewHolderAndamento(view: View) : RecyclerView.ViewHolder(view) {

        }

        override fun getItemCount(): Int {
            return  servicos.size
        }
        var onItemClicked: ((Int) -> Unit)? = null

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            holder.itemView.setOnClickListener {
                onItemClicked?.invoke(position)
            }
            var viewholder = (holder as CustomViewHolderAndamento).itemView

            viewholder.txtNomeServiceManager.text = servicos[position].serviceNome
            if (servicos[position].urlService != null) Picasso.get().load(servicos[position].urlService).resize(200,200).centerCrop().placeholder(R.drawable.photo_work).into(
                viewholder.imgServiceManager
            )
            if (servicos[position].urlContratado != null) Picasso.get().load(servicos[position].urlContratado).resize(200,200).centerCrop().placeholder(R.drawable.btn_select_photo_profile).into(
                viewholder.imgProfileImgManager
            )
            viewholder.txtNomeContratado.text = servicos[position].nomeContratado
            viewholder.txtNomeContratante.text = servicos[position].nomeContratante
            val precoQtd = servicos[position].preco!! * servicos[position].quantidate.toFloat()

            if (precoQtd.toString().substringAfter(".").length == 1){
                viewholder.txtPrecoManager.text =
                    "R$ ${servicos[position].preco.toString().replace(
                        ".",
                        ","
                    )}${"0"}"
            }else{
                viewholder.txtPrecoManager.text =
                    "R$ ${precoQtd.toString().replace(
                        ".",
                        ","
                    )}"
            }

            val sdfHora = SimpleDateFormat("HH:mm dd/MM/yyyy").format(servicos[position].timestamp)
            viewholder.txtHora.text = sdfHora
        }


    }


    private fun fetchAndamento() {
        mFirestore.collection(Constants.COLLECTIONS.MY_SERVICE)
            .whereEqualTo("id.${mAuth.currentUser?.uid}", true)
            .addSnapshotListener { snapshot, exception ->
                servicos.clear()
                exception?.let {
                    Log.i("EXCEPTION", it.toString())
                }
                snapshot?.let {
                    for (doc in snapshot) {
                        val item = doc.toObject(Myservice::class.java)
                        if (item.id.containsKey(mAuth.currentUser?.uid) && item.andamento){
                            servicos.add(item)
                        }

                    }
                    servicos.sortWith(kotlin.Comparator { o1, o2 ->
                        if (o1.timestamp < o2.timestamp) -1 else if (o1.timestamp > o2.timestamp) 1 else 0
                    })
                    mAdapter.notifyDataSetChanged()
                }
            }

    }

}
