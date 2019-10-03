package com.flyppcorp.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import com.flyppcorp.flypp.R
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.flyppcorp.atributesClass.LastMessage
import com.flyppcorp.atributesClass.Message
import com.flyppcorp.atributesClass.Servicos
import com.flyppcorp.constants.Constants
import com.flyppcorp.flypp.LastMessagesActivity
import com.flyppcorp.flypp.MessageActivity
import com.flyppcorp.flypp.ServiceActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.service_items.view.*

class HomeFragment : Fragment() {

    //declaração de objetos com inicio tardio
    private lateinit var mFirestoreService: FirebaseFirestore
    private lateinit var uid: String
    private lateinit var servicos: ArrayList<Servicos>
    private lateinit var mAdapter : DetailRecyclerView

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
        mAdapter = DetailRecyclerView()
        view.recyclerview_main.adapter = mAdapter

        //decoração de borda nas celulas
        view.recyclerview_main.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )
        //configurações e inicio da recyclerview, evento de clique
        view.recyclerview_main.layoutManager = LinearLayoutManager(activity)
        mAdapter.onItemClicked = {
            val intent = Intent(context, ServiceActivity::class.java)
            intent.putExtra(Constants.KEY.SERVICE_KEY, servicos[it])
            startActivity(intent)
        }

        return view
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
            R.id.filter -> Toast.makeText(context, "G", Toast.LENGTH_SHORT).show()

            R.id.mensagem -> {
                val intent = Intent(context, LastMessagesActivity::class.java)
                startActivity(intent)


            }

        }
        return super.onOptionsItemSelected(item)
    }
    //fim da manipulacao de toolbar

    //inicio das funcoes de recyclerview
    inner class DetailRecyclerView : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        // var servicos: ArrayList<Servicos> = arrayListOf()
        var contentUidList: ArrayList<String> = arrayListOf()


        init {
            mFirestoreService.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
                .orderBy("avalicao", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, exception ->
                    servicos.clear()
                    contentUidList.clear()
                    for (doc in snapshot!!.documents) {
                        val item = doc.toObject(Servicos::class.java)
                        servicos.add(item!!)
                        contentUidList.add(doc.id)
                    }
                    notifyDataSetChanged()
                }

        }


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
                viewholder.imgServiceMain.setImageResource(R.drawable.photo_work)
            } else {
                Picasso.get().load(servicos[position].urlService).resize(100, 100)
                    .into(viewholder.imgServiceMain)
            }
            viewholder.txtNomeUser.text = servicos[position].nome
            Picasso.get().load(servicos[position].urlProfile).into(viewholder.imgProfileImgMain)
            viewholder.txtShortDesc.text = servicos[position].shortDesc
            val avaliacao: Double = servicos[position].avalicao.toDouble()/servicos[position].totalavalicao
            viewholder.txtAvaliacao.text = "${avaliacao.toString().substring(0,3)}/5"
            viewholder.txtPreco.text = "R$ ${servicos[position].preco}"
            viewholder.txtduracao.text = "Por ${servicos[position].tipoCobranca}"
            viewholder.btnFavorite.setOnClickListener {
                eventFavorite(position)


            }
            if (servicos[position].favoritos.containsKey(uid)) {
                viewholder.btnFavorite.setImageResource(R.drawable.ic_favorite)

            } else {
                viewholder.btnFavorite.setImageResource(R.drawable.ic_favorite_border)
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