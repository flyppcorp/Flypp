package com.flyppcorp.tabsFragments


import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.flyppcorp.atributesClass.Servicos
import com.flyppcorp.constants.Constants
import com.flyppcorp.flypp.ManagerEditServiceActivity

import com.flyppcorp.flypp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_fragment_todos.view.*
import kotlinx.android.synthetic.main.service_items.*
import kotlinx.android.synthetic.main.service_items.view.*


/**
 * A simple [Fragment] subclass.
 */
class FragmentTodos : Fragment() {

    private lateinit var mFirestore: FirebaseFirestore
    private var uid: String? = null
    lateinit var contentService: ArrayList<Servicos>
    private lateinit var mAdapter : ItemTodos
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mFirestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().currentUser!!.uid
        contentService =  arrayListOf()
        mAdapter = ItemTodos()
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_fragment_todos, container, false)
        view.rv_todos.adapter = mAdapter
        view.rv_todos.layoutManager = LinearLayoutManager(activity)
        view.rv_todos.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
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
                    for (doc in snapshot!!.documents){
                        val item = doc.toObject(Servicos::class.java)
                        contentService.add(item!!)
                    }
                    notifyDataSetChanged()
                }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.service_items, parent, false)
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
            vh.txtNomeServico.text = contentService[position].nomeService
            vh.btnFavorite.visibility = View.GONE
            if (contentService[position].urlService != null) Picasso.get().load(contentService[position].urlService).into(vh.imgServiceMain)
            else vh.imgServiceMain.setImageResource(R.drawable.photo_work)
            if (contentService[position].urlProfile != null) Picasso.get().load(contentService[position].urlProfile).into(vh.imgProfileImgMain)
            else vh.imgProfileImgMain.setCircleBackgroundColorResource(R.color.colorAccent)
            vh.txtNomeUser.text = contentService[position].nome
            vh.txtShortDesc.text = contentService[position].shortDesc
            val avaliacao : Double = contentService[position].avalicao.toDouble()/contentService[position].totalavalicao
            if (contentService[position].avalicao == 0) vh.txtAvaliacao.text = "0/5"
            else vh.txtAvaliacao.text = "${avaliacao.toString().substring(0,3)}/5"

            vh.txtPreco.text = "R$ ${contentService[position].preco}"
            vh.txtduracao.text = "por ${contentService[position].tipoCobranca}"


        }


    }


}
