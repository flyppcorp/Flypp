package com.flyppcorp.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.flyppcorp.Helper.Connection
import com.flyppcorp.Helper.SharedFilter
import com.flyppcorp.atributesClass.Servicos
import com.flyppcorp.constants.Constants
import com.flyppcorp.flypp.R
import com.flyppcorp.flypp.ServiceActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.fragment_search.view.*
import kotlinx.android.synthetic.main.service_items_all.view.*

class SearchFragment : Fragment() {

    //declaração de objetos com inicio tardio
    private lateinit var mFirestore: FirebaseFirestore
    lateinit var contentServicesearch: ArrayList<Servicos>
    lateinit var contentUidList: ArrayList<String>
    private lateinit var mAdapter: SearchRecyclerView
    private lateinit var uid: String
    private lateinit var mConnection: Connection
    private lateinit var mCity: SharedFilter

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
        mCity = SharedFilter(context!!)

        //primeiras configurações e manipulações da recyclerview
        val view = LayoutInflater.from(activity).inflate(R.layout.fragment_search, container, false)
        view.recyclerSearch.adapter = mAdapter
        view.btnSearch.setOnClickListener {
            if (!editSearch.text.toString().isEmpty()) {
                if (mConnection.validateConection()) {
                    get()
                    val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.windowToken,0)

                }

            }

        }
        view.editSearch.setOnEditorActionListener { v, actionId, event ->
           if (actionId == EditorInfo.IME_ACTION_SEARCH){
               if (!editSearch.text.toString().isEmpty()) {
                   if (mConnection.validateConection()) {
                       get()
                       val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                       imm.hideSoftInputFromWindow(view.windowToken,0)
                   }

               }
           }
            true
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
    fun get() {

        mFirestore.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
            //.whereEqualTo("tags.${search}", true)
            .addSnapshotListener { snapshot, exception ->

                contentServicesearch.clear()
                contentUidList.clear()
                if (snapshot == null) return@addSnapshotListener
                for (doc in snapshot.documents) {
                    val item = doc.toObject(Servicos::class.java)
                    val prefix = editSearch?.text.toString().toLowerCase()

                    if ((item?.nacional == true || item?.cityName == mCity.getFilter(Constants.KEY.CITY_NAME)) && item.visible) {
                        for (key in item.tags) {
                            if (key.toString().startsWith(prefix)) {
                                contentServicesearch.add(item)
                                contentUidList.add(doc.id)
                                break
                            }

                        }

                    }


                }
                if (contentServicesearch.size == 0) framesearch?.visibility = View.VISIBLE
                if (contentServicesearch.size > 0) framesearch?.visibility = View.GONE
                mAdapter.notifyDataSetChanged()

            }


    }


    //Todas as configurações da recycler view
    inner class SearchRecyclerView : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.service_items_all, parent, false)
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
                Picasso.get().load(contentServicesearch[position].urlService).resize(100, 100)
                    .centerCrop().placeholder(R.drawable.photo_work)
                    .into(viewholder.imgServiceMainList)
            }
            viewholder.txtNomeUserList.text = contentServicesearch[position].nome
            Picasso.get().load(contentServicesearch[position].urlProfile).resize(300, 300)
                .centerCrop().placeholder(R.drawable.btn_select_photo_profile)
                .into(viewholder.imgProfileImgMainList)
            viewholder.txtShortDescList.text = contentServicesearch[position].shortDesc
            val avaliacao: Double =
                contentServicesearch[position].avaliacao.toDouble() / contentServicesearch[position].totalAvaliacao
            if (contentServicesearch[position].avaliacao == 0) viewholder.txtAvaliacaoList.text =
                "${contentServicesearch[position].avaliacao}/5"
            else viewholder.txtAvaliacaoList.text = "${avaliacao.toString().substring(0, 3)}/5"
            viewholder.txtPrecoList.text =
                "R$ ${contentServicesearch[position].preco.toString().replace(
                    ".",
                    ","
                )} Por ${contentServicesearch[position].tipoCobranca}"

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