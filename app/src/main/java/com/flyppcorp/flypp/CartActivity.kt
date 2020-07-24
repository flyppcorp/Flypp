package com.flyppcorp.flypp

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.flyppcorp.atributesClass.Myservice
import com.flyppcorp.atributesClass.Servicos
import com.flyppcorp.constants.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.xwray.groupie.Group
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_cart.*
import kotlinx.android.synthetic.main.service_items_all.view.*
import java.util.*

class CartActivity : AppCompatActivity() {
    private lateinit var mAdapter: GroupAdapter<GroupieViewHolder>
    private lateinit var mFirestore: FirebaseFirestore
    private var mCart : Myservice? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)
        mAdapter = GroupAdapter()
        mFirestore = FirebaseFirestore.getInstance()
        mCart = intent.extras?.getParcelable(Constants.KEY.CART)
        rv_cart.adapter = mAdapter


        mAdapter.setOnItemClickListener { item, view ->
            val item : ItemCart = item as ItemCart
            val intent = Intent(this, ConfirmServiceActivity::class.java)
            intent.putExtra(Constants.KEY.SERVICE_KEY, item.cart)
            startActivity(intent)
        }

        btnGoToHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        fetchProdutos()
    }

    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
        super.onBackPressed()
    }

    private fun fetchProdutos() {
        mFirestore.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
            .whereEqualTo("uid", mCart?.idContratado)
            .addSnapshotListener { snapshot, exception ->
                mAdapter.clear()
                snapshot?.let {
                    for(doc in snapshot.documents){
                        val items = doc.toObject(Servicos::class.java)
                        val calendar = Calendar.getInstance().get(Calendar.DAY_OF_WEEK).toString()
                        if (items?.visible != false && items?.dias!!.contains(calendar) && items.serviceId != mCart?.serviceId){
                            mAdapter.add(ItemCart(items))
                        }

                    }

                }
                if (mAdapter.itemCount == 0){
                    btnGoToHome.text = "Nenhum outro produto, clique para sair"
                }
                mAdapter.notifyDataSetChanged()
            }
    }

    inner class ItemCart(val cart : Servicos) : Item<GroupieViewHolder>(){
        override fun getLayout(): Int {
            return R.layout.service_items_all
        }

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            val vh = viewHolder.itemView
            vh.txtNomeServicoList.text = cart.nomeService
            Picasso.get().load(cart.urlService).resize(300, 300)
                .centerCrop().placeholder(R.drawable.photo_work)
                .into(vh.imgServiceMainList)
            Picasso.get().load(cart.urlProfile).resize(300, 300)
                .centerCrop().placeholder(R.drawable.btn_select_photo_profile)
                .into(vh.imgProfileImgMainList)
            vh.txtNomeUserList.text = cart.nome

            if (cart.tempoEntrega != null) vh.txtPreparoList.text =
                " ${cart.tempoEntrega}" else vh.txtPreparoList.text =
                " ?"
            vh.txtShortDescList.text = cart.shortDesc

            val avaliacao: Double =
                cart.avaliacao.toDouble() / cart.totalAvaliacao
            val resultAvaliacao = String.format("%.1f", avaliacao)
            if (cart.avaliacao == 0){
                vh.txtAvaliacaoList.text =
                    "-"
            }else{
                vh.txtAvaliacaoList.text =
                    "${resultAvaliacao}".replace(".", ",")
            }

            //preço
            val result = String.format("%.2f", cart.preco)
            vh.txtPrecoList.text =
                "R$ ${result}".replace(".", ",")

            //fim preço

            vh.btnFavoriteList?.visibility = View.GONE

        }


    }
}