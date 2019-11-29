package com.flyppcorp.fragments

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.flyppcorp.Helper.Connection
import com.flyppcorp.atributesClass.Servicos
import com.flyppcorp.constants.Constants
import com.flyppcorp.flypp.R
import com.flyppcorp.flypp.ServiceActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.fragment_search.view.*
import kotlinx.android.synthetic.main.service_items.view.*
import kotlinx.android.synthetic.main.service_items_all.view.*

class SearchFragment : Fragment() {

    //declaração de objetos com inicio tardio
    private lateinit var mFirestore: FirebaseFirestore
    lateinit var contentServicesearch: ArrayList<Servicos>
    lateinit var contentUidList: ArrayList<String>
    private lateinit var mAdapter: SearchRecyclerView
    private lateinit var uid: String
    private lateinit var mConnection: Connection

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //iniciando objetos declarado antes da onCreate
        mFirestore = FirebaseFirestore.getInstance()
        mAdapter = SearchRecyclerView()
        contentServicesearch = arrayListOf()
        contentUidList = arrayListOf()
        uid = FirebaseAuth.getInstance().currentUser!!.uid
        mConnection = Connection(context!!)

        //primeiras configurações e manipulações da recyclerview
        val view = LayoutInflater.from(activity).inflate(R.layout.fragment_search, container, false)
        view.recyclerSearch.adapter = mAdapter
        view.btnSearch.setOnClickListener {
            if (!editSearch.text.toString().isEmpty()){
                if (mConnection.validateConection()){
                    get(editSearch.text.toString().toLowerCase())
                    editSearch.onEditorAction(EditorInfo.IME_ACTION_DONE)
                }

            }

        }
        view.recyclerSearch.layoutManager = LinearLayoutManager(activity)
        mAdapter.onItemClick = {
            val intent = Intent(context, ServiceActivity::class.java)
            intent.putExtra(Constants.KEY.SERVICE_KEY, contentServicesearch[it])
            startActivity(intent)
        }

        return view
    }

    //função que busca os resultados no banco de dados
    fun get(search: String) {

        mFirestore.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
            .whereEqualTo("tags.${search}", true)
            .addSnapshotListener { snapshot, exception ->
                contentServicesearch.clear()
                contentUidList.clear()
                for (doc in snapshot!!.documents) {
                    val item = doc.toObject(Servicos::class.java)
                    contentServicesearch.add(item!!)
                    contentUidList.add(doc.id)
                }
                mAdapter.notifyDataSetChanged()

            }


    }


    //Todas as configurações da recycler view
    inner class SearchRecyclerView : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.service_items_all, parent, false)
            return CustomViewholder(view)
        }

        inner class CustomViewholder(view: View) : RecyclerView.ViewHolder(view)


        override fun getItemCount(): Int {
            return contentServicesearch.size
        }

        var onItemClick: ((Int) -> Unit)? = null

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            holder.itemView.setOnClickListener {
                onItemClick?.invoke(position)
            }
            val viewholder = (holder as CustomViewholder).itemView
            viewholder.txtNomeServicoList.text = contentServicesearch[position].nomeService
            if (contentServicesearch[position].urlService == null) {
                viewholder.imgServiceMainList.setImageResource(R.drawable.photo_work)
            } else {
                Picasso.get().load(contentServicesearch[position].urlService).fit().centerCrop()
                    .into(viewholder.imgServiceMainList)
            }
            viewholder.txtNomeUserList.text = contentServicesearch[position].nome
            Picasso.get().load(contentServicesearch[position].urlProfile)
                .into(viewholder.imgProfileImgMainList)
            viewholder.txtShortDescList.text = contentServicesearch[position].shortDesc
            val avaliacao : Double = contentServicesearch[position].avaliacao.toDouble()/contentServicesearch[position].totalAvaliacao
            if (contentServicesearch[position].avaliacao == 0) viewholder.txtAvaliacaoList.text =
                "${contentServicesearch[position].avaliacao}/5"
            else viewholder.txtAvaliacaoList.text = "${avaliacao.toString().substring(0, 3)}/5"
            viewholder.txtPrecoList.text = "R$ ${contentServicesearch[position].preco} Por ${contentServicesearch[position].tipoCobranca}"

            viewholder.btnFavoriteList.setOnClickListener {
                favoriteEvent(position)
            }
            if (contentServicesearch[position].favoritos.containsKey(uid)) {
                viewholder.btnFavoriteList.setImageResource(R.drawable.ic_favorite)
            } else {
                viewholder.btnFavoriteList.setImageResource(R.drawable.ic_favorite_border)
            }
        }

        //função que salva o id do user como favorito
        private fun favoriteEvent(position: Int) {
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