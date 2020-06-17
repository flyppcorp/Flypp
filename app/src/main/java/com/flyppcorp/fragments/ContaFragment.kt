package com.flyppcorp.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.flyppcorp.atributesClass.User
import com.flyppcorp.constants.Constants
import com.flyppcorp.firebase_classes.LoginFirebaseAuth
import com.flyppcorp.flypp.*
import com.flyppcorp.profile.ProfileInformations
import com.flyppcorp.profile_settings.ProfileAdapter
import com.flyppcorp.profile_settings.menuOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_conta.*

import kotlinx.android.synthetic.main.fragment_conta.view.*
import java.util.*

class ContaFragment : Fragment() {

    //objetos com inicio tardio
    private lateinit var mFB: LoginFirebaseAuth
    private lateinit var mFs: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth
    private lateinit var adapter: ProfileAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //iniciando objetos
        mFB = LoginFirebaseAuth(context!!)
        mFs = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()
        adapter = ProfileAdapter(menuOptions())

        //configurações das duas recyclerview
        val view = LayoutInflater.from(activity).inflate(R.layout.fragment_conta, container, false)

        view.recyclerView.adapter = adapter

        adapter.onItemClick = {
            adapter.getItemId(it)
            when {
                it == 0 -> startActivity(Intent(context, ManagerServicesActivity::class.java))
                it == 1 -> {
                    val intent =
                        Intent(Intent.ACTION_VIEW, Uri.parse("http://flyppbrasil.epizy.com/"))
                    startActivity(intent)
                }
                it == 2 -> {
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
                                Intent.EXTRA_TEXT, "Ainda não usa o Flypp para pedir comida ?" +
                                        "\nVenha logo aproveitar, peça rapidinho." +
                                        "\n${shortLink.toString()}"
                            )

                            startActivity(intent)
                        }.addOnFailureListener {

                        }
                }
            }
        }


        fetchUser()
        return view
    }




    //metodo que recupera infirmações do user
    private fun fetchUser() {
        if (mAuth.currentUser?.isAnonymous == false) {
            mFs.collection(Constants.COLLECTIONS.USER_COLLECTION)
                .document(mAuth.currentUser?.uid.toString())
                .get()
                .addOnSuccessListener {
                    val item = it.toObject(User::class.java)
                    txtMyName.text = item?.nome
                    Picasso.get().load(item?.url).resize(200, 200).centerCrop()
                        .placeholder(R.drawable.btn_select_photo_profile).into(photoPerfil)
                    photoPerfil.setOnClickListener {
                        val intent = Intent(context, ProfileInformations::class.java)
                        intent.putExtra(Constants.KEY.PROFILE_KEY, item)
                        startActivity(intent)
                    }


                }

        }
    }
}


