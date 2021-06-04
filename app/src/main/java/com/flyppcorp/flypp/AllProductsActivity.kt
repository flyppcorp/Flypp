package com.flyppcorp.flypp

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.flyppcorp.atributesClass.Servicos
import com.flyppcorp.atributesClass.User
import com.flyppcorp.constants.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_all_products.*
import kotlinx.android.synthetic.main.service_items.view.*
import kotlinx.android.synthetic.main.service_items_all.view.*
import java.util.*
import kotlinx.android.synthetic.main.service_items.view.txtHorarioFunc as txtHorarioFunc1

class AllProductsActivity : AppCompatActivity() {
    private lateinit var mAdapter: GroupAdapter<GroupieViewHolder>
    private lateinit var mFirestore: FirebaseFirestore
    private var mGet: String? = null
    private var extras: Bundle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_products)
        mFirestore = FirebaseFirestore.getInstance()
        mAdapter = GroupAdapter()
        rv_all_prods.adapter = mAdapter

        extras = intent?.extras
        if (extras != null) {
            mGet = extras?.getString(Constants.KEY.ALL_PRODS)
        }
        mAdapter.setOnItemClickListener { item, view ->
            val progressBar = ProgressDialog(this)
            progressBar.setCancelable(false)
            progressBar.show()
            progressBar.setContentView(R.layout.progress)
            progressBar.window?.setBackgroundDrawableResource(android.R.color.transparent)
            val item : ItemProds = item as ItemProds
            val intent = Intent(this, ServiceActivity::class.java)
            intent.putExtra(Constants.KEY.SERVICE_KEY, item.prods)
            progressBar.hide()
            startActivity(intent)
        }
        fetchProds()
    }

    private fun fetchProds() {
        mFirestore.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
            .whereEqualTo("uid", mGet)
            .addSnapshotListener { snapshot, exception ->
                mAdapter.clear()
                snapshot?.let {
                    for(doc in snapshot.documents){
                        val items = doc.toObject(Servicos::class.java)
                        if (items?.visible != false ){
                            mAdapter.add(ItemProds(items!!))
                        }
                    }
                }
                mAdapter.notifyDataSetChanged()
            }

    }

    inner class ItemProds(val prods: Servicos): Item<GroupieViewHolder>(){
        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            val vh = viewHolder.itemView
            vh.txtNomeServicoList.text = prods.nomeService
            val calendar = Calendar.getInstance().get(Calendar.DAY_OF_WEEK).toString()
            if (prods.dias.contains(calendar)){
                vh.txtHorarioFunc.text = prods.horario
                vh.txtHorarioFunc.setTextColor(Color.rgb(30, 130, 76))

            }else{
                vh.txtHorarioFunc.text = "Fechado"
                vh.txtHorarioFunc.setTextColor(Color.RED)
            }

            Picasso.get().load(prods.urlService).resize(300, 300)
                .centerCrop().placeholder(R.drawable.photo_work)
                .into(vh.imgServiceMainList)
            Picasso.get().load(prods.urlProfile).resize(300, 300)
                .centerCrop().placeholder(R.drawable.btn_select_photo_profile)
                .into(vh.imgProfileImgMainList)
            vh.txtNomeUserList.text = prods.nome
            if (prods.tempoEntrega != null) vh.txtPreparoList.text =
                " ${prods.tempoEntrega}" else vh.txtPreparoList.text =
                " ?"
            vh.txtShortDescList.text = prods.shortDesc

            val avaliacao: Double =
                prods.avaliacao.toDouble() / prods.totalAvaliacao
            val resultAvaliacao = String.format("%.1f", avaliacao)
            if (prods.avaliacao == 0){
                vh.txtAvaliacaoList.text =
                    "-"
            }else{
                vh.txtAvaliacaoList.text =
                    "${resultAvaliacao}".replace(".", ",")
            }

            //preço
            val result = String.format("%.2f", prods.preco)
            vh.txtPrecoList.text =
                "R$ ${result}".replace(".", ",")

            //fim preço



            vh.btnFavoriteList?.visibility = View.GONE

        }

        override fun getLayout(): Int {
            return R.layout.service_items_all
        }

    }

}