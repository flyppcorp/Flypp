package com.flyppcorp.fragments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.flyppcorp.atributesClass.Servicos
import com.flyppcorp.constants.Constants
import com.flyppcorp.flypp.R
import com.flyppcorp.flypp.ServiceActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_fav.view.*
import kotlinx.android.synthetic.main.service_items_all.view.*

class FavFragment : Fragment() {
    //objetos com inicio tardio
    private lateinit var mFirestore: FirebaseFirestore
    private lateinit var uid: String
    private lateinit var mAdapter: FavoriteRecyclerView
    lateinit var contentServicos: ArrayList<Servicos>
    //fim das declarações
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //iniciando objetos antes tardios
        mFirestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().currentUser!!.uid
        contentServicos = arrayListOf()
        mAdapter = FavoriteRecyclerView()
        //fim do iniciar

        //configurações da recyclerview
        val view = LayoutInflater.from(activity).inflate(R.layout.fragment_fav, container, false)
        view.recycler_favorite.adapter = mAdapter
        view.recycler_favorite.layoutManager = LinearLayoutManager(activity)
        mAdapter.onItemClick = {
            val intent = Intent(context, ServiceActivity::class.java)
            intent.putExtra(Constants.KEY.SERVICE_KEY, contentServicos[it])
            startActivity(intent)
        }
        //fim das config
        return view
    }

    //todos os metodos da recyclerview
    inner class FavoriteRecyclerView : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        var contentUidList: ArrayList<String> = arrayListOf()

        //iniciar a rv com todos os produtos que possuem o uid do usuario
        init {
            mFirestore.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
                .whereEqualTo("favoritos.${uid}", true)
                .addSnapshotListener { snapshot, exception ->
                    contentServicos.clear()
                    contentUidList.clear()
                    if (snapshot == null) return@addSnapshotListener
                    for (doc in snapshot.documents) {
                        val item = doc.toObject(Servicos::class.java)
                        contentServicos.add(item!!)
                        contentUidList.add(doc.id)
                    }
                    notifyDataSetChanged()
                }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.service_items_all, parent, false)
            return CustomView(view)
        }

        inner class CustomView(view: View) : RecyclerView.ViewHolder(view)


        override fun getItemCount(): Int {
            return contentServicos.size
        }

        //ação de click
        var onItemClick: ((Int) -> Unit)? = null

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            //ação de click
            holder.itemView.setOnClickListener {
                onItemClick?.invoke(position)
            }
            val viewholder = (holder as CustomView).itemView

            //nome do produto
            viewholder.txtNomeServicoList.text = contentServicos[position].nomeService
            //foto do produto
            if (contentServicos[position].urlService == null) {
                viewholder.imgServiceMainList.setImageResource(R.drawable.photo_work)
            } else {
                Picasso.get().load(contentServicos[position].urlService).resize(300,300).centerCrop().placeholder(R.drawable.photo_work).into(viewholder.imgServiceMainList)
            }
            //fim nome do produto

            //nome do estabelecimento
            viewholder.txtNomeUserList.text = contentServicos[position].nome
            //foto do perfil do estabelecimento
            Picasso.get().load(contentServicos[position].urlProfile).resize(300,300).centerCrop().placeholder(R.drawable.btn_select_photo_profile)
                .into(viewholder.imgProfileImgMainList)
            //fim foto

            //desc short
            viewholder.txtShortDescList.text = contentServicos[position].shortDesc
            //tempo de entrega
            if (contentServicos[position].tempoEntrega != null) viewholder.txtPreparoList.text = " ${contentServicos[position].tempoEntrega}" else viewholder.txtPreparoList.text = " ?"

            //avaliação
            val avaliacao : Double = contentServicos[position].avaliacao.toDouble()/contentServicos[position].totalAvaliacao
            if (contentServicos[position].avaliacao == 0) viewholder.txtAvaliacaoList.text =
                "${contentServicos[position].avaliacao}/5"
            else viewholder.txtAvaliacaoList.text = "${avaliacao.toString().substring(0, 3)}/5"
            //fim avaliação


            //preço
            if (contentServicos[position].preco.toString().substringAfter(".").length == 1){
                viewholder.txtPrecoList.text =
                    "R$ ${contentServicos[position].preco.toString().replace(
                        ".",
                        ","
                    )}${"0"}"
            }else{
                viewholder.txtPrecoList.text =
                    "R$ ${contentServicos[position].preco.toString().replace(
                        ".",
                        ","
                    )}"
            }
            //fim preco


            //event favorite
            viewholder.btnFavoriteList.setOnClickListener {
                eventFavorite(position)
            }
            if (contentServicos[position].favoritos.containsKey(uid)) {
                viewholder.btnFavoriteList.setImageResource(R.drawable.ic_favorite)
            } else {
                viewholder.btnFavoriteList.setImageResource(R.drawable.ic_favorite_border)

            }

        }
        //fim do event

        //metodo que salva nos favoritos
        private fun eventFavorite(position: Int) {

            var tsDoc = mFirestore.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
                .document(contentUidList[position])
            mFirestore.runTransaction { transaction ->

                var contentServico = transaction.get(tsDoc).toObject(Servicos::class.java)

                if (contentServico!!.favoritos.containsKey(uid)) {
                    contentServico.favoritos.remove(uid)
                } else {
                    contentServico.favoritos[uid] = true
                }
                transaction.set(tsDoc, contentServico)
            }
        }


    }
}