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
        view.recycler_favorite.addItemDecoration(
            DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        )
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
                LayoutInflater.from(parent.context).inflate(R.layout.service_items, parent, false)
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
            viewholder.txtNomeServico.text = contentServicos[position].nomeService
            if (contentServicos[position].urlService == null) {
                viewholder.imgServiceMain.setImageResource(R.drawable.photo_work)
            } else {
                Picasso.get().load(contentServicos[position].urlService).resize(100, 100)
                    .into(viewholder.imgServiceMain)
            }
            viewholder.txtNomeUser.text = contentServicos[position].nome
            Picasso.get().load(contentServicos[position].urlProfile)
                .into(viewholder.imgProfileImgMain)
            viewholder.txtShortDesc.text = contentServicos[position].shortDesc
            val avaliacao : Double = contentServicos[position].avalicao.toDouble()/contentServicos[position].totalavalicao
            viewholder.txtAvaliacao.text = "${avaliacao.toString().substring(0,3)}/5"
            viewholder.txtPreco.text = "R$ ${contentServicos[position].preco}"
            viewholder.txtduracao.text = "Por ${contentServicos[position].tipoCobranca}"

            viewholder.btnFavorite.setOnClickListener {
                eventFavorite(position)
            }
            if (contentServicos[position].favoritos.containsKey(uid)) {
                viewholder.btnFavorite.setImageResource(R.drawable.ic_favorite)
            } else {
                viewholder.btnFavorite.setImageResource(R.drawable.ic_favorite_border)

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