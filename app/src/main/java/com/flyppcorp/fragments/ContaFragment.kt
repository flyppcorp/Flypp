package com.flyppcorp.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.flyppcorp.atributesClass.User
import com.flyppcorp.constants.Constants
import com.flyppcorp.firebase_classes.LoginFirebaseAuth
import com.flyppcorp.flypp.*
import com.flyppcorp.profile.ProfileInformations
import com.flyppcorp.profile_settings.ProfileAdapter
import com.flyppcorp.profile_settings.menuOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item

import kotlinx.android.synthetic.main.fragment_conta.view.*
import kotlinx.android.synthetic.main.perfil_items.view.*

class ContaFragment : Fragment() {

    //objetos com inicio tardio
    private lateinit var mAdapter: GroupAdapter<GroupieViewHolder>
    private lateinit var mFB: LoginFirebaseAuth
    private lateinit var mFs : FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth
    private lateinit var adapter: ProfileAdapter



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //iniciando objetos
        mFB = LoginFirebaseAuth(context!!)
        mAdapter = GroupAdapter()
        mFs = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()
        adapter = ProfileAdapter(menuOptions())

        //configurações das duas recyclerview
        val view = LayoutInflater.from(activity).inflate(R.layout.fragment_conta, container, false)


        view.rv_profile.adapter = adapter
        view.rv_profile.layoutManager = LinearLayoutManager(activity)
        view.recyclerView.adapter = mAdapter
        /*mAdapter.setOnItemClickListener { item, view ->
            val intent = Intent(context, ProfileInformations::class.java)
            //val userItem : UserItem = item as UserItem
            //intent.putExtra(Constants.KEY.PROFILE_KEY, userItem.mUser)
            startActivity(intent)
        }*/
        adapter.onItemClick = {
            adapter.getItemId(it)
           when{
               it == 0 -> startActivity(Intent(context, ManagerServicesActivity::class.java))
               it == 1 -> {
                   val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://flyppbrasil.epizy.com/"))
                   startActivity(intent)
               }
               it == 2 -> {
                   val intent = Intent(Intent.ACTION_SEND)
                   intent.setType("text/plain")
                   intent.putExtra(Intent.EXTRA_TEXT, "Use o Flypp para contratar e oferecer serviços de forma rápida, fácil e grátis:  \nhttps://play.google.com/store/apps/details?id=com.flyppcorp.flypp")
                   startActivity(intent)
               }
           }
        }

        fetchUser()

        return view
    }





    //recyclerview local
    private inner class UserItem( val mUser: User): Item<GroupieViewHolder>(){
        private var mUserInfo: User? = null
        override fun getLayout(): Int {
            return R.layout.perfil_items
        }

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
           viewHolder.itemView.txtMyName.text = mUser.nome
            Picasso.get().load(mUser.url).resize(200,200).centerCrop().placeholder(R.drawable.btn_select_photo_profile).into(viewHolder.itemView.photoPerfil)
            viewHolder.itemView.photoPerfil.setOnClickListener {
                 FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS.USER_COLLECTION)
                     .document(mAuth.currentUser!!.uid)
                     .get()
                     .addOnSuccessListener {
                         mUserInfo = it.toObject(User::class.java)
                         val intent = Intent(context, ProfileInformations::class.java)
                         intent.putExtra(Constants.KEY.PROFILE_KEY, mUser)
                         startActivity(intent)
                     }
            }
        }


    }

    //metodo que recupera infirmações do user
    private fun fetchUser(){
        mFs.collection(Constants.COLLECTIONS.USER_COLLECTION)
            .whereEqualTo("uid", mAuth.currentUser?.uid)
            .addSnapshotListener { snapshot, exception ->
                mAdapter.clear()
                snapshot?.let {
                    for (doc in snapshot){
                        val user = doc.toObject(User::class.java)
                        mAdapter.add(UserItem(user))

                    }
                }
            }
    }


}