package com.flyppcorp.tabsFragments


import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.flyppcorp.Helper.Connection
import com.flyppcorp.Helper.SharedFilter
import com.flyppcorp.atributesClass.Servicos
import com.flyppcorp.constants.Constants
import com.flyppcorp.flypp.ManagerEditServiceActivity
import com.flyppcorp.flypp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_add.*
import kotlinx.android.synthetic.main.dialog_fr2.view.*
import kotlinx.android.synthetic.main.fragment_fragment_todos.*
import kotlinx.android.synthetic.main.fragment_fragment_todos.view.*
import kotlinx.android.synthetic.main.service_items_all.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


/**
 * A simple [Fragment] subclass.
 */
class FragmentTodos : Fragment() {

    private lateinit var mFirestore: FirebaseFirestore
    private var uid: String? = null
    lateinit var contentService: ArrayList<Servicos>
    lateinit var contentId: ArrayList<String>
    private lateinit var mAdapter: ItemTodos
    private lateinit var mDiasExpediente: ArrayList<String>
    private lateinit var mExp: SharedFilter
    private var horario1: String? = null
    private var horario2: String? = null
    private lateinit var mInicioFim: SharedFilter
    private lateinit var mConnect: Connection

    private lateinit var expedilist: ArrayList<String>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mFirestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().currentUser!!.uid
        contentService = arrayListOf()
        contentId = arrayListOf()
        mAdapter = ItemTodos()
        mDiasExpediente = arrayListOf()
        mExp = SharedFilter(context!!)
        mInicioFim = SharedFilter(context!!)
        mConnect = Connection(context!!)

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_fragment_todos, container, false)
        view.rv_todos.adapter = mAdapter
        view.rv_todos.layoutManager = LinearLayoutManager(activity)
        mAdapter.onItemClick = {
            val intent = Intent(context, ManagerEditServiceActivity::class.java)
            intent.putExtra(Constants.KEY.SERVICE_KEY, contentService[it])
            startActivity(intent)
        }

        return view
    }



    inner class ItemTodos : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


        init {
            mFirestore.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
                .whereEqualTo("uid", FirebaseAuth.getInstance().currentUser!!.uid)
                .addSnapshotListener { snapshot, exception ->
                    contentService.clear()
                    if (snapshot == null) return@addSnapshotListener
                    for (doc in snapshot.documents) {
                        val item = doc.toObject(Servicos::class.java)
                        contentService.add(item!!)
                    }
                    notifyDataSetChanged()
                }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.service_items_all, parent, false)
            return ViewholderCustom(view)
        }

        inner class ViewholderCustom(view: View) : RecyclerView.ViewHolder(view)


        override fun getItemCount(): Int {
            return contentService.size
        }

        var onItemClick: ((Int) -> Unit)? = null

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            holder.itemView.setOnClickListener {
                onItemClick?.invoke(position)
            }
            var vh = (holder as ViewholderCustom).itemView
            vh.txtNomeServicoList.text = contentService[position].nomeService
            vh.btnFavoriteList.visibility = View.GONE
            if (contentService[position].tempoEntrega != null) vh.txtPreparoList.text =
                contentService[position].tempoEntrega
            else vh.txtPreparoList.text = "?"
            if (contentService[position].urlService != null) Picasso.get()
                .load(contentService[position].urlService).placeholder(R.drawable.photo_work)
                .resize(150, 150).centerCrop().into(vh.imgServiceMainList)
            else vh.imgServiceMainList.setImageResource(R.drawable.photo_work)
            if (contentService[position].urlProfile != null) Picasso.get()
                .load(contentService[position].urlProfile).resize(150, 150).centerCrop()
                .placeholder(R.drawable.btn_select_photo_profile).into(vh.imgProfileImgMainList)

            vh.txtNomeUserList.text = contentService[position].nome
            vh.txtShortDescList.text = contentService[position].shortDesc
            val avaliacao: Double =
                contentService[position].avaliacao.toDouble() / contentService[position].totalAvaliacao
            val resultAv = String.format("%.1f", avaliacao)
            if (contentService[position].avaliacao == 0) vh.txtAvaliacaoList.text = "0/5"
            else vh.txtAvaliacaoList.text = "${resultAv}/5".replace(".", ",")

            val result = String.format("%.2f", contentService[position].preco)
            vh.txtPrecoList.text =
                "R$ ${result}".replace(".", ",")


        }


    }


}

