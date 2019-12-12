package com.flyppcorp.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
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
import kotlinx.android.synthetic.main.service_items.view.*
import kotlinx.android.synthetic.main.service_items.view.btnFavorite
import kotlinx.android.synthetic.main.service_items.view.imgProfileImgMain
import kotlinx.android.synthetic.main.service_items.view.imgServiceMain
import kotlinx.android.synthetic.main.service_items.view.txtAvaliacao
import kotlinx.android.synthetic.main.service_items.view.txtNomeServico
import kotlinx.android.synthetic.main.service_items.view.txtNomeUser
import kotlinx.android.synthetic.main.service_items.view.txtPreco
import kotlinx.android.synthetic.main.service_items.view.txtShortDesc
import kotlinx.android.synthetic.main.service_items_all.view.*

class FavFragment : Fragment() {
    //objetos com inicio tardio
    private lateinit var mFirestore: FirebaseFirestore
    private lateinit var uid: String
    private lateinit var mAdapter: FavoriteRecyclerView
    lateinit var contentServicos: ArrayList<Servicos>
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

        //configurações da recyclerview
        val view = LayoutInflater.from(activity).inflate(R.layout.fragment_fav, container, false)
        view.recycler_favorite.adapter = mAdapter
        view.recycler_favorite.layoutManager = LinearLayoutManager(activity)
        mAdapter.onItemClick = {
            val intent = Intent(context, ServiceActivity::class.java)
            intent.putExtra(Constants.KEY.SERVICE_KEY, contentServicos[it])
            startActivity(intent)
        }
        return view
    }

    //todos os metodos da recyclerview
    inner class FavoriteRecyclerView : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        var contentUidList: ArrayList<String> = arrayListOf()

        init {
            mFirestore.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
                .whereEqualTo("favoritos.${uid}", true)
                .addSnapshotListener { snapshot, exception ->
                    contentServicos.clear()
                    contentUidList.clear()
                    for (doc in snapshot!!.documents) {
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

        var onItemClick: ((Int) -> Unit)? = null

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            holder.itemView.setOnClickListener {
                onItemClick?.invoke(position)
            }
            val viewholder = (holder as CustomView).itemView
            viewholder.txtNomeServicoList.text = contentServicos[position].nomeService
            if (contentServicos[position].urlService == null) {
                viewholder.imgServiceMainList.setImageResource(R.drawable.photo_work)
            } else {
                Picasso.get().load(contentServicos[position].urlService).fit().centerCrop().placeholder(R.drawable.photo_work).into(viewholder.imgServiceMainList)
            }
            viewholder.txtNomeUserList.text = contentServicos[position].nome
            Picasso.get().load(contentServicos[position].urlProfile).placeholder(R.drawable.btn_select_photo_profile)
                .into(viewholder.imgProfileImgMainList)
            viewholder.txtShortDescList.text = contentServicos[position].shortDesc
            val avaliacao : Double = contentServicos[position].avaliacao.toDouble()/contentServicos[position].totalAvaliacao
            if (contentServicos[position].avaliacao == 0) viewholder.txtAvaliacaoList.text =
                "${contentServicos[position].avaliacao}/5"
            else viewholder.txtAvaliacaoList.text = "${avaliacao.toString().substring(0, 3)}/5"
            viewholder.txtPrecoList.text = "R$ ${contentServicos[position].preco.toString().replace(".",",")} Por ${contentServicos[position].tipoCobranca}"


            viewholder.btnFavoriteList.setOnClickListener {
                eventFavorite(position)
            }
            if (contentServicos[position].favoritos.containsKey(uid)) {
                viewholder.btnFavoriteList.setImageResource(R.drawable.ic_favorite)
            } else {
                viewholder.btnFavoriteList.setImageResource(R.drawable.ic_favorite_border)

            }

        }

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