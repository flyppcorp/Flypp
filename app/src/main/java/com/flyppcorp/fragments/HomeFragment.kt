package com.flyppcorp.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.flyppcorp.flypp.R
import android.view.MenuItem
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.view.isEmpty
import androidx.core.view.isNotEmpty
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.flyppcorp.Helper.SharedFilter
import com.flyppcorp.atributesClass.LastMessage
import com.flyppcorp.atributesClass.Servicos
import com.flyppcorp.atributesClass.User
import com.flyppcorp.constants.Constants
import com.flyppcorp.flypp.LastMessages
import com.flyppcorp.flypp.ServiceActivity
import com.flyppcorp.managerServices.FilterActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.service_items.*
import kotlinx.android.synthetic.main.service_items.view.*
import java.lang.IndexOutOfBoundsException

class HomeFragment : Fragment() {

    //declaração de objetos com inicio tardio
    private lateinit var mFirestoreService: FirebaseFirestore
    private lateinit var uid: String
    private lateinit var servicos: ArrayList<Servicos>
    private lateinit var contentUidList: ArrayList<String>
    private lateinit var mAdapter: DetailRecyclerView
    private lateinit var mSharedFilter: SharedFilter
    private var mUser: User? = null
    private lateinit var mCity: SharedFilter
    private var cityOther: User? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //iniciando todos os objetos
        mFirestoreService = FirebaseFirestore.getInstance()
        val view = LayoutInflater.from(activity).inflate(R.layout.fragment_home, container, false)
        uid = FirebaseAuth.getInstance().currentUser!!.uid
        servicos = arrayListOf()
        contentUidList = arrayListOf()
        mAdapter = DetailRecyclerView()
        mSharedFilter = SharedFilter(context!!)
        mCity = SharedFilter(context!!)
        view.recyclerview_main.adapter = mAdapter


        //configurações e inicio da recyclerview, evento de clique
        view.recyclerview_main.layoutManager = LinearLayoutManager(activity)
        mAdapter.onItemClicked = {
            val intent = Intent(context, ServiceActivity::class.java)
            intent.putExtra(Constants.KEY.SERVICE_KEY, servicos[it])
            startActivity(intent)
        }




        fetchServices()
        locationOther()
        return view
    }


    //funcao que obtem servicos com e sem filtro
    private fun fetchServices() {
        val filter = mSharedFilter.getFilter(Constants.KEY.FILTER_KEY)
        if (filter != "") {
            if (filter.equals(Constants.FILTERS_VALUES.MENOR_PRECO)) {
                mFirestoreService.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
                    .orderBy("preco", Query.Direction.ASCENDING)
                    .addSnapshotListener { snapshot, exception ->
                        servicos.clear()
                        contentUidList.clear()
                        if (snapshot == null) return@addSnapshotListener
                        for (doc in snapshot.documents) {
                            val item = doc.toObject(Servicos::class.java)
                            if (item?.cityName == mCity.getFilter(Constants.KEY.CITY_NAME) && item.visible) {
                                servicos.add(item)
                                contentUidList.add(doc.id)

                            }

                        }
                        if (servicos.size == 0) framelayoutEmpty?.visibility = View.VISIBLE
                        if (servicos.size > 0) framelayoutEmpty?.visibility = View.GONE
                        mAdapter.notifyDataSetChanged()
                    }
            } else if (filter.equals(Constants.FILTERS_VALUES.MAIOR_PRECO)) {
                mFirestoreService.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
                    .orderBy("preco", Query.Direction.DESCENDING)
                    .addSnapshotListener { snapshot, exception ->
                        servicos.clear()
                        contentUidList.clear()
                        if (snapshot == null) return@addSnapshotListener
                        for (doc in snapshot.documents) {
                            val item = doc.toObject(Servicos::class.java)
                            if (item?.cityName == mCity.getFilter(Constants.KEY.CITY_NAME) && item.visible) {
                                servicos.add(item)
                                contentUidList.add(doc.id)

                            }

                        }
                        if (servicos.size == 0) framelayoutEmpty?.visibility = View.VISIBLE
                        if (servicos.size > 0) framelayoutEmpty?.visibility = View.GONE
                        mAdapter.notifyDataSetChanged()
                    }
            } else if (filter.equals(Constants.FILTERS_VALUES.MENOR_RELEVANCIA)) {
                mFirestoreService.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
                    .orderBy("totalServicos", Query.Direction.ASCENDING)
                    .addSnapshotListener { snapshot, exception ->
                        servicos.clear()
                        contentUidList.clear()
                        if (snapshot == null) return@addSnapshotListener
                        for (doc in snapshot.documents) {
                            val item = doc.toObject(Servicos::class.java)
                            if (item?.cityName == mCity.getFilter(Constants.KEY.CITY_NAME) && item.visible) {
                                servicos.add(item)
                                contentUidList.add(doc.id)
                            }

                        }
                        if (servicos.size == 0) framelayoutEmpty?.visibility = View.VISIBLE
                        if (servicos.size > 0) framelayoutEmpty?.visibility = View.GONE
                        mAdapter.notifyDataSetChanged()
                    }
            } else if (filter.equals(Constants.FILTERS_VALUES.MAIOR_RELEVANCIA)) {
                mFirestoreService.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
                    .orderBy("totalServicos", Query.Direction.DESCENDING)
                    .addSnapshotListener { snapshot, exception ->
                        servicos.clear()
                        contentUidList.clear()
                        if (snapshot == null) return@addSnapshotListener
                        for (doc in snapshot.documents) {
                            val item = doc.toObject(Servicos::class.java)
                            if (item?.cityName == mCity.getFilter(Constants.KEY.CITY_NAME) && item.visible) {

                                servicos.add(item)
                                contentUidList.add(doc.id)

                            }

                        }
                        if (servicos.size == 0) framelayoutEmpty?.visibility = View.VISIBLE
                        if (servicos.size > 0) framelayoutEmpty?.visibility = View.GONE
                        mAdapter.notifyDataSetChanged()
                    }
            } else if (filter.equals(Constants.FILTERS_VALUES.MENOS_AVALIADO)) {
                mFirestoreService.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
                    .orderBy("avaliacao", Query.Direction.ASCENDING)
                    .addSnapshotListener { snapshot, exception ->
                        servicos.clear()
                        contentUidList.clear()
                        if (snapshot == null) return@addSnapshotListener
                        for (doc in snapshot.documents) {
                            val item = doc.toObject(Servicos::class.java)
                            if (item?.cityName == mCity.getFilter(Constants.KEY.CITY_NAME) && item.visible) {

                                servicos.add(item)
                                contentUidList.add(doc.id)

                            }
                        }
                        if (servicos.size == 0) framelayoutEmpty?.visibility = View.VISIBLE
                        if (servicos.size > 0) framelayoutEmpty?.visibility = View.GONE
                        mAdapter.notifyDataSetChanged()
                    }
            } else if (filter.equals(Constants.FILTERS_VALUES.MAIS_AVALIADO)) {
                mFirestoreService.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
                    .orderBy("avaliacao", Query.Direction.DESCENDING)
                    .addSnapshotListener { snapshot, exception ->
                        servicos.clear()
                        contentUidList.clear()
                        if (snapshot == null) return@addSnapshotListener
                        for (doc in snapshot.documents) {
                            val item = doc.toObject(Servicos::class.java)
                            if (item?.cityName == mCity.getFilter(Constants.KEY.CITY_NAME) && item.visible) {

                                servicos.add(item)
                                contentUidList.add(doc.id)

                            }

                        }
                        if (servicos.size == 0) framelayoutEmpty?.visibility = View.VISIBLE
                        if (servicos.size > 0) framelayoutEmpty?.visibility = View.GONE
                        mAdapter.notifyDataSetChanged()
                    }
            }

        } else {
            mFirestoreService.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
                .orderBy("totalServicos", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, exception ->
                    servicos.clear()
                    contentUidList.clear()
                    if (snapshot == null) return@addSnapshotListener
                    for (doc in snapshot.documents) {
                        val item = doc.toObject(Servicos::class.java)

                        servicos.add(item!!)
                        contentUidList.add(doc.id)

                    }
                    if (servicos.size == 0) framelayoutEmpty?.visibility = View.VISIBLE
                    if (servicos.size > 0) framelayoutEmpty?.visibility = View.GONE
                    mAdapter.notifyDataSetChanged()
                }
        }


    }

    private fun locationOther() {
        if (mSharedFilter.getFilter(Constants.KEY.CITY_NAME) == "") {
            mFirestoreService.collection(Constants.COLLECTIONS.USER_COLLECTION)
                .document(uid)
                .get()
                .addOnSuccessListener {
                    cityOther = it.toObject(User::class.java)
                    if (cityOther?.cidade != null) {
                        mSharedFilter.saveFilter(Constants.KEY.CITY_NAME, cityOther?.cidade!!)
                    } else {
                        return@addOnSuccessListener
                    }
                }
        }
    }


    //funcões de manipulaçao de toolbar
    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.bg_filter, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.filter -> startActivity(Intent(context, FilterActivity::class.java))
            R.id.mensagem -> startActivity(Intent(context, LastMessages::class.java))
        }
        return super.onOptionsItemSelected(item)
    }
    //fim da manipulacao de toolbar

    //inicio das funcoes de recyclerview
    inner class DetailRecyclerView : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        // var servicos: ArrayList<Servicos> = arrayListOf()


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view =
                LayoutInflater.from(parent.context).inflate(R.layout.service_items, parent, false)
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)


        override fun getItemCount(): Int {

            return servicos.size
        }

        var onItemClicked: ((Int) -> Unit)? = null

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            holder.itemView.setOnClickListener {
                onItemClicked?.invoke(position)
            }
            var viewholder = (holder as CustomViewHolder).itemView


            viewholder.txtNomeServico.text = servicos[position].nomeService
            if (servicos[position].urlService == null) {
                viewholder.imgServiceMain.setImageResource(R.drawable.ic_working)
            } else {

                Picasso.get().load(servicos[position].urlService).placeholder(R.drawable.ic_working)
                    .fit().centerCrop().into(viewholder.imgServiceMain)

            }
            if (servicos[position].urlProfile != null) {
                Picasso.get().load(servicos[position].urlProfile)
                    .resize(300,300)
                    .placeholder(R.drawable.btn_select_photo_profile).centerCrop()
                    .into(viewholder.imgProfileImgMain)
            } else {
                viewholder.imgProfileImgMain.setImageResource(R.drawable.btn_select_photo_profile)
            }

            viewholder.txtNomeUser.text = servicos[position].nome
            viewholder.txtShortDesc.text = servicos[position].shortDesc
            val avaliacao: Double =
                servicos[position].avaliacao.toDouble() / servicos[position].totalAvaliacao
            if (servicos[position].avaliacao == 0) viewholder.txtAvaliacao.text =
                "${servicos[position].avaliacao}/5"
            else viewholder.txtAvaliacao.text = "${avaliacao.toString().substring(0, 3)}/5"
            viewholder.txtPreco.text =
                "R$ ${servicos[position].preco.toString().replace(
                    ".",
                    ","
                )} por ${servicos[position].tipoCobranca}"


            viewholder.btnFavorite.setOnClickListener {
                eventFavorite(position)


            }
            if (servicos[position].favoritos.containsKey(uid)) {
                viewholder.btnFavorite.setImageResource(R.drawable.ic_favorite)

            } else {
                viewholder.btnFavorite.setImageResource(R.drawable.ic_favorite_border)
            }

                if ((servicos != null && servicos.size > 0) && (contentUidList != null && contentUidList.size > 0)  || (servicos != null && servicos.size > 1) && (contentUidList != null && contentUidList.size > 1) ){
                    if (servicos[position].uidProfile.containsKey(uid) ) {
                        updateInfo(
                            servicos[position].nome!!,
                            servicos[position].urlProfile.toString(),
                            position
                        )
                    }
                }


            updateLocation()

        }

        private fun updateLocation() {

            val mFirestoreService = FirebaseFirestore.getInstance()
            mFirestoreService.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
                .whereEqualTo("uid", uid)
                .addSnapshotListener { snapshot, exception ->
                    if (snapshot == null) return@addSnapshotListener
                    snapshot?.let {
                        for (doc in snapshot) {
                            val serviceLocation = doc.toObject(Servicos::class.java)
                            val tsDoc =
                                mFirestoreService.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
                                    .document(serviceLocation.serviceId!!)
                            mFirestoreService.runTransaction {
                                val content = it.get(tsDoc).toObject(Servicos::class.java)
                                if (content?.cityName != mCity.getFilter(Constants.KEY.CITY_NAME) && mCity.getFilter(
                                        Constants.KEY.CITY_NAME
                                    ) != ""
                                ) {
                                    content?.cityName = mCity.getFilter(Constants.KEY.CITY_NAME)
                                }
                                it.set(tsDoc, content!!)
                            }

                        }
                    }
                }
        }

        private fun updateInfo(nome: String, url: String, position: Int) {

                val user = FirebaseFirestore.getInstance()
                user.collection(Constants.COLLECTIONS.USER_COLLECTION)
                    .document(uid)
                    .get()
                    .addOnSuccessListener {
                        mUser = it.toObject(User::class.java)
                        if (contentUidList != null && contentUidList.size > 0){
                            val tsDoc =
                                mFirestoreService.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
                                    .document(contentUidList[position])
                            mFirestoreService.runTransaction {
                                val userUp = it.get(tsDoc).toObject(Servicos::class.java)
                                if (userUp!!.uidProfile.containsKey(uid)) {
                                    if (nome != mUser!!.nome) {
                                        userUp.nome = mUser!!.nome
                                    }
                                    if (url != mUser?.url) {
                                        userUp.urlProfile = mUser?.url
                                    }

                                }

                                it.set(tsDoc, userUp)
                            }
                        }else{
                            return@addOnSuccessListener
                        }

                    }


        }

        fun eventFavorite(position: Int) {

            var tsDoc = mFirestoreService.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
                .document(contentUidList[position])
            mFirestoreService.runTransaction { transaction ->

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