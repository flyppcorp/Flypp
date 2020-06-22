package com.flyppcorp.profile


import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.flyppcorp.atributesClass.User
import com.flyppcorp.constants.Constants
import com.flyppcorp.flypp.ProfileActivity
import com.flyppcorp.flypp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_profile.*

class ProfileFragment : Fragment() {

    private lateinit var mFirestore: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth
    private var mUser: User? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mFirestore = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()

        fetchUsers()
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        return view

    }


    private fun fetchUsers() {
        val uid = mAuth.currentUser?.uid
        if (uid != null){
            mFirestore.collection(Constants.COLLECTIONS.USER_COLLECTION)
                .document(uid.toString())
                .get()
                .addOnSuccessListener {
                    mUser = it.toObject(User::class.java)
                    if (mUser?.url != null) Picasso.get().load(mUser?.url).resize(300,300).centerCrop().placeholder(R.drawable.btn_select_photo_profile).into(imgManagerProfile)
                    txtManagerNome.text = mUser?.nome
                    txtMAnagerEnderecoProfile.text =
                        "${mUser?.rua}, ${mUser?.bairro}, ${mUser?.numero} \n" +
                                "${mUser?.cidade}, ${mUser?.estado}, ${mUser?.cep}"

                    txtContatosManagerProfile.text = "E-mail: ${mUser?.email} \n" +
                            "Telefone: (${mUser?.ddd}) ${mUser?.telefone}"
                    if (mUser?.servicosAtivos != null){
                        if (mUser!!.servicosAtivos < 1000){
                            txtAtivoProfile.text = mUser!!.servicosAtivos.toString()
                        }else if (mUser!!.servicosAtivos > 1000){
                            txtAtivoProfile.text = "${mUser!!.servicosAtivos.toString().substring(0,1)}K"
                        }
                    }

                    if (mUser?.totalServicosFinalizados != null){
                        if (mUser!!.totalServicosFinalizados < 1000){
                            txtFinalizadoProfile.text = mUser!!.totalServicosFinalizados.toString()
                        }else if (mUser!!.totalServicosFinalizados > 1000){
                            txtAtivoProfile.text = "${mUser!!.totalServicosFinalizados.toString().substring(0,1)}K"
                        }
                    }


                    if (mUser?.avaliacao == 0) {
                        txtAvaliacaoProfile.text = mUser?.avaliacao.toString()
                    } else {
                        val media : Double = mUser?.avaliacao!!.toDouble() / mUser!!.totalAvaliacao
                        val result = String.format("%.1f", media)
                        txtAvaliacaoProfile.text = result.replace(".", ",")

                    }
                    profileGo()

                }
        }

    }

    private fun profileGo() {
        mUser?.let { information ->
            btnEditProfileGo.setOnClickListener {
                val intent = Intent(context, ProfileActivity::class.java)
                intent.putExtra(Constants.KEY.PROFILE_KEY, mUser)
                startActivity(intent)
            }

        }
    }


}



