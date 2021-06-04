package com.flyppcorp.profile


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.flyppcorp.atributesClass.User
import com.flyppcorp.constants.Constants
import com.flyppcorp.flypp.ProfileActivity
import com.flyppcorp.flypp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.view.*
import java.util.*

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
        view.btnLink.setOnClickListener {
              shareBusiness()
        }

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

                    if (mUser?.servicosAtivos == 0){
                        btnLink?.visibility = View.GONE
                    }else{
                        btnLink?.visibility = View.VISIBLE
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

    private fun shareBusiness(){
        val dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
            .setLink(Uri.parse("https://play.google.com/store/apps/details?id=com.flyppcorp.flypp"))
            .setDomainUriPrefix("https://flyppbrasil.page.link")
            // Open links with this app on Android
            .setAndroidParameters(DynamicLink.AndroidParameters.Builder().build())
            // Open links with com.example.ios on iOS
            .setIosParameters(
                DynamicLink.IosParameters.Builder("com.example.ios").build()
            )
            .buildDynamicLink()

        val dynamicLinkUri = dynamicLink.uri
        Log.i("LINK", dynamicLinkUri.toString())

        val rand = Random().nextInt(100) + 100
        val link = "https://flyppbrasil.page.link?" +
                "apn=com.flyppcorp.flypp" +
                "&ibi=com.example.ios" +
                "&link=https%3A%2F%2Fplay.google.com%2Fstore%2Fapps%2Fdetails%3Fid%3Dcom.flyppcorp.flypp" +
                "&uid=${mAuth.currentUser?.uid}" +
                "&utm_source=-"

        val shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
            .setLink(Uri.parse(link))
            .setDomainUriPrefix("https://flyppbrasil.page.link")
            .buildShortDynamicLink()
            .addOnSuccessListener { result ->
                // Short link created
                val shortLink = result.shortLink
                val flowchartLink = result.previewLink
                val intent = Intent(Intent.ACTION_SEND)

                intent.setType("text/plain")

                intent.putExtra(
                    Intent.EXTRA_TEXT, "Hey, veja todos os meus produtos" +
                            "\nVenha logo aproveitar, peça rapidinho." +
                            "\n${shortLink.toString()}"
                )

                startActivity(intent)
            }
    }


}



