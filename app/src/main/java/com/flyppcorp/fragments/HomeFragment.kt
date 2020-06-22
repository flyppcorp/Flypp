package com.flyppcorp.fragments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.flyppcorp.flypp.R
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.flyppcorp.Helper.SharedFilter
import com.flyppcorp.atributesClass.Servicos
import com.flyppcorp.atributesClass.User
import com.flyppcorp.constants.Constants
import com.flyppcorp.flypp.LastMessages
import com.flyppcorp.flypp.ManagerServicesActivity
import com.flyppcorp.flypp.ServiceActivity
import com.flyppcorp.managerServices.FilterActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.service_items.view.*
import java.util.*
import kotlin.collections.ArrayList


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
    //Fim da desclaração das variáveis

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //iniciando todos os objetos
        mFirestoreService = FirebaseFirestore.getInstance()
        val view = LayoutInflater.from(activity).inflate(R.layout.fragment_home, container, false)
        uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
        servicos = arrayListOf()
        contentUidList = arrayListOf()
        mAdapter = DetailRecyclerView()
        mSharedFilter = SharedFilter(context!!)
        mCity = SharedFilter(context!!)
        view.recyclerview_main.adapter = mAdapter
        //Fim da iniciação das variáveis

        //configurações e inicio da recyclerview, evento de clique
        view.recyclerview_main.layoutManager = LinearLayoutManager(activity)
        mAdapter.onItemClicked = {
            val intent = Intent(context, ServiceActivity::class.java)
            intent.putExtra(Constants.KEY.SERVICE_KEY, servicos[it])
            startActivity(intent)
        }
        //Fim do inicio e click da recyclerview

        //call das funções
        fetchServices()
        //locationOther()
        //fim da call das funções
        return view
    }


    //funcao que obtem servicos com e sem filtro
    private fun fetchServices() {
        //pegando o sharedPreference
        val filter = mSharedFilter.getFilter(Constants.KEY.FILTER_KEY)
        //verificando se o filtro existe
        if (filter != "") {
            //Filtrando por menor preco
            if (filter.equals(Constants.FILTERS_VALUES.MENOR_PRECO)) {
                mFirestoreService.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
                    .orderBy("preco", Query.Direction.ASCENDING)
                    .whereEqualTo("cityName", mCity.getFilter(Constants.KEY.CITY_NAME))
                    .whereEqualTo("visible", true)
                    .addSnapshotListener { snapshot, exception ->
                        servicos.clear()
                        contentUidList.clear()
                        snapshot?.let {
                            for (doc in snapshot.documents) {
                                val item = doc.toObject(Servicos::class.java)
                                if (mSharedFilter.getFilter(Constants.FILTERS_VALUES.CATEGORIA) == item?.categoria){
                                    servicos.add(item)
                                    contentUidList.add(doc.id)
                                }else if (mSharedFilter.getFilter(Constants.FILTERS_VALUES.CATEGORIA) == ""){
                                    servicos.add(item!!)
                                    contentUidList.add(doc.id)
                                }

                            }


                        }
                        exception?.let {
                            Toast.makeText(context, it.toString(), Toast.LENGTH_SHORT).show()
                        }
                        if (servicos.size == 0) framelayoutEmpty?.visibility = View.VISIBLE
                        if (servicos.size > 0) framelayoutEmpty?.visibility = View.GONE
                        mAdapter.notifyDataSetChanged()
                    }
                //Fim das filtragens

                //Inicio do Filtro de maior preço
            } else if (filter.equals(Constants.FILTERS_VALUES.MAIOR_PRECO)) {
                mFirestoreService.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
                    .orderBy("preco", Query.Direction.DESCENDING)
                    .whereEqualTo("cityName", mCity.getFilter(Constants.KEY.CITY_NAME))
                    .whereEqualTo("visible", true)
                    .addSnapshotListener { snapshot, exception ->
                        servicos.clear()
                        contentUidList.clear()
                        snapshot?.let {
                            for (doc in snapshot.documents) {
                                val item = doc.toObject(Servicos::class.java)
                                if (mSharedFilter.getFilter(Constants.FILTERS_VALUES.CATEGORIA) == item?.categoria){
                                    servicos.add(item)
                                    contentUidList.add(doc.id)
                                }else if (mSharedFilter.getFilter(Constants.FILTERS_VALUES.CATEGORIA) == ""){
                                    servicos.add(item!!)
                                    contentUidList.add(doc.id)
                                }
                            }


                        }
                        exception?.let {
                            Toast.makeText(context, it.toString(), Toast.LENGTH_SHORT).show()
                        }
                        if (servicos.size == 0) framelayoutEmpty?.visibility = View.VISIBLE
                        if (servicos.size > 0) framelayoutEmpty?.visibility = View.GONE
                        mAdapter.notifyDataSetChanged()
                    }
                //fim do filtro de maior preco

                //filtro menor relevancia
            } else if (filter.equals(Constants.FILTERS_VALUES.MENOR_RELEVANCIA)) {
                mFirestoreService.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
                    .orderBy("totalServicos", Query.Direction.ASCENDING)
                    .whereEqualTo("cityName", mCity.getFilter(Constants.KEY.CITY_NAME))
                    .whereEqualTo("visible", true)
                    .addSnapshotListener { snapshot, exception ->
                        servicos.clear()
                        contentUidList.clear()
                        snapshot?.let {
                            for (doc in snapshot.documents) {
                                val item = doc.toObject(Servicos::class.java)
                                if (mSharedFilter.getFilter(Constants.FILTERS_VALUES.CATEGORIA) == item?.categoria){
                                    servicos.add(item)
                                    contentUidList.add(doc.id)
                                }else if (mSharedFilter.getFilter(Constants.FILTERS_VALUES.CATEGORIA) == ""){
                                    servicos.add(item!!)
                                    contentUidList.add(doc.id)
                                }
                            }


                        }
                        exception?.let {
                            Toast.makeText(context, it.toString(), Toast.LENGTH_SHORT).show()
                        }
                        if (servicos.size == 0) framelayoutEmpty?.visibility = View.VISIBLE
                        if (servicos.size > 0) framelayoutEmpty?.visibility = View.GONE
                        mAdapter.notifyDataSetChanged()
                    }
                //fim do filtro

                //filtro maior relevancia
            } else if (filter.equals(Constants.FILTERS_VALUES.MAIOR_RELEVANCIA)) {
                mFirestoreService.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
                    .orderBy("totalServicos", Query.Direction.DESCENDING)
                    .whereEqualTo("cityName", mCity.getFilter(Constants.KEY.CITY_NAME))
                    .whereEqualTo("visible", true)
                    .addSnapshotListener { snapshot, exception ->
                        servicos.clear()
                        contentUidList.clear()
                        snapshot?.let {
                            for (doc in snapshot.documents) {
                                val item = doc.toObject(Servicos::class.java)
                                if (mSharedFilter.getFilter(Constants.FILTERS_VALUES.CATEGORIA) == item?.categoria){
                                    servicos.add(item)
                                    contentUidList.add(doc.id)
                                }else if (mSharedFilter.getFilter(Constants.FILTERS_VALUES.CATEGORIA) == ""){
                                    servicos.add(item!!)
                                    contentUidList.add(doc.id)
                                }
                            }


                        }
                        exception?.let {
                            Toast.makeText(context, it.toString(), Toast.LENGTH_SHORT).show()
                        }
                        if (servicos.size == 0) framelayoutEmpty?.visibility = View.VISIBLE
                        if (servicos.size > 0) framelayoutEmpty?.visibility = View.GONE
                        mAdapter.notifyDataSetChanged()
                    }
                //fim do filtro maior relevancia

                //filtro do menos avaliado
            } else if (filter.equals(Constants.FILTERS_VALUES.MENOS_AVALIADO)) {
                mFirestoreService.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
                    .orderBy("avaliacao", Query.Direction.ASCENDING)
                    .whereEqualTo("cityName", mCity.getFilter(Constants.KEY.CITY_NAME))
                    .whereEqualTo("visible", true)
                    .addSnapshotListener { snapshot, exception ->
                        servicos.clear()
                        contentUidList.clear()
                        snapshot?.let {
                            for (doc in snapshot.documents) {
                                val item = doc.toObject(Servicos::class.java)
                                if (mSharedFilter.getFilter(Constants.FILTERS_VALUES.CATEGORIA) == item?.categoria){
                                    servicos.add(item)
                                    contentUidList.add(doc.id)
                                }else if (mSharedFilter.getFilter(Constants.FILTERS_VALUES.CATEGORIA) == ""){
                                    servicos.add(item!!)
                                    contentUidList.add(doc.id)
                                }
                            }


                        }
                        exception?.let {
                            Toast.makeText(context, it.toString(), Toast.LENGTH_SHORT).show()
                        }
                        if (servicos.size == 0) framelayoutEmpty?.visibility = View.VISIBLE
                        if (servicos.size > 0) framelayoutEmpty?.visibility = View.GONE
                        mAdapter.notifyDataSetChanged()
                    }
                //fim do filtro menos avaliada

                //filtro mais avaliado
            } else if (filter.equals(Constants.FILTERS_VALUES.MAIS_AVALIADO)) {
                mFirestoreService.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
                    .orderBy("avaliacao", Query.Direction.DESCENDING)
                    .whereEqualTo("cityName", mCity.getFilter(Constants.KEY.CITY_NAME))
                    .whereEqualTo("visible", true)
                    .addSnapshotListener { snapshot, exception ->
                        servicos.clear()
                        contentUidList.clear()
                        snapshot?.let {
                            for (doc in snapshot.documents) {
                                val item = doc.toObject(Servicos::class.java)
                                if (mSharedFilter.getFilter(Constants.FILTERS_VALUES.CATEGORIA) == item?.categoria){
                                    servicos.add(item)
                                    contentUidList.add(doc.id)
                                }else if (mSharedFilter.getFilter(Constants.FILTERS_VALUES.CATEGORIA) == ""){
                                    servicos.add(item!!)
                                    contentUidList.add(doc.id)
                                }
                            }


                        }
                        exception?.let {
                            Toast.makeText(context, it.toString(), Toast.LENGTH_SHORT).show()
                        }
                        if (servicos.size == 0) framelayoutEmpty?.visibility = View.VISIBLE
                        if (servicos.size > 0) framelayoutEmpty?.visibility = View.GONE
                        mAdapter.notifyDataSetChanged()
                    }
            }
            //fim do filtro mais avaliado

            // sem filtro escolhido mas pega por maior avaliação
        } else {
            mFirestoreService.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
                .orderBy("avaliacao", Query.Direction.DESCENDING)
                .whereEqualTo("cityName", mCity.getFilter(Constants.KEY.CITY_NAME))
                .whereEqualTo("visible", true)
                .addSnapshotListener { snapshot, exception ->
                    servicos.clear()
                    contentUidList.clear()
                    snapshot?.let {
                        for (doc in snapshot.documents) {
                            val item = doc.toObject(Servicos::class.java)
                            servicos.add(item!!)
                            contentUidList.add(doc.id)
                        }


                    }
                    exception?.let {
                        Toast.makeText(context, it.toString(), Toast.LENGTH_SHORT).show()
                    }
                    if (servicos.size == 0) framelayoutEmpty?.visibility = View.VISIBLE
                    if (servicos.size > 0) framelayoutEmpty?.visibility = View.GONE
                    mAdapter.notifyDataSetChanged()
                }
            //fim do filtro
        }


    }
    //fim da função

    //Esta função verifica sem existe uma localização salva no dispositivo e se não existir verifica se há uma no banco de dados, caso tenha, salva no dispositivo
    private fun locationOther() {
        mFirestoreService.collection(Constants.COLLECTIONS.USER_COLLECTION)
            .document(uid)
            .get()
            .addOnSuccessListener {
                cityOther = it.toObject(User::class.java)
                if (mCity.getFilter(Constants.KEY.CITY_NAME) == "") {
                    if (cityOther?.cidade != null) {
                        mSharedFilter.saveFilter(
                            Constants.KEY.CITY_NAME,
                            cityOther?.cidade.toString()
                        )
                    } else {
                        return@addOnSuccessListener
                    }

                } else {
                    return@addOnSuccessListener
                }
            }

    }
    //fim das função de localização de correção

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
            R.id.work -> startActivity(Intent(context, ManagerServicesActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }
    //fim da manipulacao de toolbar


    //inicio das funcoes de recyclerview
    inner class DetailRecyclerView : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view =
                LayoutInflater.from(parent.context).inflate(R.layout.service_items, parent, false)
            return CustomViewHolder(view)
        }


        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)


        override fun getItemCount(): Int {

            return servicos.size
        }

        //clique da celular de rv
        var onItemClicked: ((Int) -> Unit)? = null

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            //implementeção da interface de click
            holder.itemView.setOnClickListener {
                onItemClicked?.invoke(position)
            }
            //fim do click

            //variavel que cuida das informações
            var viewholder = (holder as CustomViewHolder).itemView



            val calendar = Calendar.getInstance()
            if (servicos[position].dias.contains(calendar.get(Calendar.DAY_OF_WEEK).toString())){
                viewholder.txtHorarioFunc.text = servicos[position].horario
            }else {
                viewholder.txtHorarioFunc.setTextColor(Color.RED)
                viewholder.txtHorarioFunc.text = "Fechado"
            }

            //nome do produto
            viewholder.txtNomeServico.text = servicos[position].nomeService

            //imagem do produto
            if (servicos[position].urlService == null) {
                viewholder.imgServiceMain.setImageResource(R.drawable.ic_working)
            } else {

                Picasso.get().load(servicos[position].urlService).placeholder(R.drawable.ic_working)
                    .fit().centerCrop().into(viewholder.imgServiceMain)

            }
            //fim

            //imagem do perfil
            if (servicos[position].urlProfile != null) {
                Picasso.get().load(servicos[position].urlProfile)
                    .resize(300, 300)
                    .placeholder(R.drawable.btn_select_photo_profile).centerCrop()
                    .into(viewholder.imgProfileImgMain)
            } else {
                viewholder.imgProfileImgMain.setImageResource(R.drawable.btn_select_photo_profile)
            }
            //fim

            //nome estabelecimento
            viewholder.txtNomeUser.text = servicos[position].nome
            //descrição curta
            viewholder.txtShortDesc.text = servicos[position].shortDesc
            //tempo de preparo
            if (servicos[position].tempoEntrega != null) viewholder.txtPreparo.text =
                servicos[position].tempoEntrega else viewholder.txtPreparo.text = "?"
            //avaliação
            val avaliacao: Double =
                servicos[position].avaliacao.toDouble() / servicos[position].totalAvaliacao
            val resultAvaliacao = String.format("%.1f", avaliacao)
            if (servicos[position].avaliacao == 0){
                viewholder.txtAvaliacao.text =
                    "0/5"
            }else{
                viewholder.txtAvaliacao.text =
                    "${resultAvaliacao}/5".replace(".", ",")
            }

            //fim avaliação


            //preço

            val result = String.format("%.2f", servicos[position].preco)
            viewholder.txtPrecoContratante.text =
                "R$ ${result}".replace(".", ",")
            //fim preço


            //ação para favoritar
            viewholder.btnFavorite.setOnClickListener {
                eventFavorite(position)


            }
            if (servicos[position].favoritos.containsKey(uid)) {
                viewholder.btnFavorite.setImageResource(R.drawable.ic_favorite)

            } else {
                viewholder.btnFavorite.setImageResource(R.drawable.ic_favorite_border)
            }
            //fim ação
        }

        //função que salva o id do usuario no produto e favorita
        private fun eventFavorite(position: Int) {

            //pega o nome do documento
            var tsDoc = mFirestoreService.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
                .document(contentUidList[position])
            //inicio da firestore trtansaction
            mFirestoreService.runTransaction { transaction ->

                //variavel que aponta para o documento no banco
                val contentServico = transaction.get(tsDoc).toObject(Servicos::class.java)

                //se ao clicar o id estiver no documento, então remove o id
                if (contentServico!!.favoritos.containsKey(uid)) {
                    contentServico.favoritos.remove(uid)
                    //senão adiciona ao documento
                } else {
                    contentServico.favoritos[uid] = true

                }
                //completa a transaction
                transaction.set(tsDoc, contentServico)

            }

        }


    }


}