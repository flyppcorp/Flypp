package com.flyppcorp.flypp

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.flyppcorp.Helper.PagerAdapterImage
import com.flyppcorp.atributesClass.Servicos
import com.flyppcorp.atributesClass.User
import com.flyppcorp.constants.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_service.*
import kotlinx.android.synthetic.main.activity_service.view.*
import java.util.*
import kotlin.collections.ArrayList

class ServiceActivity : AppCompatActivity() {
    //mServices pega p parcelable da outra activity
    //mFavorito recupera do banco

    private lateinit var mFirestore: FirebaseFirestore
    private var mService: Servicos? = null
    private var mFavorito: Servicos? = null
    private var getMessageAtributes: Servicos? = null
    private lateinit var mUser: User
    private lateinit var mAuth: FirebaseAuth
    var menuFAv: MenuItem? = null
    private lateinit var mUrl: ArrayList<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service)
        mFirestore = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()
        mUser = User()
        mService = intent.extras?.getParcelable(Constants.KEY.SERVICE_KEY)
        if (mService?.urlService != null && mService?.urlService2 != null) {
            mUrl = arrayListOf(
                mService?.urlService.toString(),
                mService?.urlService2.toString()
            )
        } else if (mService?.urlService != null && mService?.urlService2 == null) {
            mUrl = arrayListOf(
                mService?.urlService.toString()

            )
        } else if (mService?.urlService == null && mService?.urlService2 != null) {
            mUrl = arrayListOf(
                mService?.urlService2.toString()
            )
        } else if (mService?.urlService == null && mService?.urlService2 == null) {
            mUrl = arrayListOf(

            )
        }

        val adapter = PagerAdapterImage(this, mUrl)
        imgServiceView.adapter = adapter

        getService()

        btnContratar.setOnClickListener {
            if (mAuth.currentUser?.isAnonymous == false) {
                handleContract()
            } else {
                val alert = AlertDialog.Builder(this)
                alert.setTitle("Ops!")
                alert.setMessage(
                    "Para contratar um serviço você precisa fazer login ou criar uma conta." +
                            "\nSe preferir salve o produto na sua lista de favoritos antes de sair para login." +
                            "\nDeseja fazer isso agora ?"
                )
                alert.setNegativeButton("Agora não", { dialog, which -> })
                alert.setPositiveButton("Sim", { dialog, which ->
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                })
                alert.show()
            }

        }
        btnComments?.setOnClickListener {
            val mServico = Servicos()
            mServico.serviceId = mService?.serviceId
            val intent = Intent(this, CommentActivity::class.java)
            intent.putExtra(Constants.KEY.COMMENTS, mServico)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.title = mService?.nomeService.toString()
        getIcon()

    }


    private fun getIcon() {
        mFirestore.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
            .document(mService?.serviceId.toString())
            .get()
            .addOnSuccessListener {
                mFavorito = it.toObject(Servicos::class.java)
                getIconResult()
            }
    }

    private fun getIconResult() {
        mFavorito?.let {
            if (it.favoritos.containsKey(mAuth.currentUser!!.uid)) {
                menuFAv?.setIcon(R.drawable.ic_favorite_white)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_favorite_action, menu)
        menuFAv = menu?.findItem(R.id.fav_action)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            android.R.id.home -> {
                finish()
            }
            R.id.fav_action -> {
                handleFavorite()
                mFirestore.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
                    .document(mService?.serviceId.toString())
                    .get()
                    .addOnSuccessListener {
                        mFavorito = it.toObject(Servicos::class.java)
                        mFavorito?.let {
                            if (!it.favoritos.containsKey(mAuth.currentUser?.uid)) menuFAv?.setIcon(
                                R.drawable.ic_favorite_white
                            )
                            else menuFAv?.setIcon(R.drawable.ic_favorite_border_white)
                        }
                    }
            }

            R.id.id_send_message -> {
                val uid = mAuth.currentUser?.uid
                if (uid != mService?.uid) {
                    if (mAuth.currentUser?.isAnonymous == false) {
                        handleMessage()
                    } else {
                        val alert = AlertDialog.Builder(this)
                        alert.setTitle("Ops!")
                        alert.setMessage(
                            "Para enviar mensagens você precisa fazer login ou criar uma conta." +
                                    "\nDeseja fazer isso agora ?"
                        )
                        alert.setNegativeButton("Agora não", { dialog, which -> })
                        alert.setPositiveButton("Sim", { dialog, which ->
                            val intent = Intent(this, LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        })
                        alert.show()
                    }

                } else {
                    val mAlert = AlertDialog.Builder(this)
                    mAlert.setMessage("Ops, nós sabemos que as vezes queremos falar com nós mesmos, mas desta vez não vai ser possível.")
                    mAlert.setPositiveButton("Ok", { dialog, which -> })
                    mAlert.show()
                }

            }

            R.id.share -> {


                val dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                    .setLink(Uri.parse("https://play.google.com/store/apps/details?id=com.flyppcorp.flypp"))
                    .setDomainUriPrefix("https://flyppbrasil.page.link")
                    // Open links with this app on Android
                    .setAndroidParameters(DynamicLink.AndroidParameters.Builder().build())
                    // Open links with com.example.ios on iOS
                    .setIosParameters(DynamicLink.IosParameters.Builder("com.example.ios").build())
                    .buildDynamicLink()

                val dynamicLinkUri = dynamicLink.uri
                Log.i("LINK", dynamicLinkUri.toString())

                val link = "https://flyppbrasil.page.link?" +
                        "apn=com.flyppcorp.flypp" +
                        "&ibi=com.example.ios" +
                        "&link=https%3A%2F%2Fplay.google.com%2Fstore%2Fapps%2Fdetails%3Fid%3Dcom.flyppcorp.flypp" +
                        "&st=${mService?.nomeService}" +
                        "&sd=${mService?.shortDesc}" +
                        "&utm_source=${mService?.serviceId}"

                val shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                    .setLink(Uri.parse(link))
                    .setDomainUriPrefix("https://flyppbrasil.page.link")
                    .buildShortDynamicLink()
                    .addOnSuccessListener { result ->
                        // Short link created
                        val shortLink = result.shortLink
                        val flowchartLink = result.previewLink
                        intent = Intent(Intent.ACTION_SEND)

                        intent.setType("text/plain")

                        intent.putExtra(
                            Intent.EXTRA_TEXT,
                            "${mService?.nomeService} \n${mService?.shortDesc} \n" + shortLink.toString()
                        )

                        startActivity(intent)
                    }.addOnFailureListener {

                    }

            }

        }

        return super.onOptionsItemSelected(item)
    }

    private fun handleMessage() {
        mFirestore.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
            .document(mService?.serviceId.toString())
            .get()
            .addOnSuccessListener {
                getMessageAtributes = it.toObject(Servicos::class.java)
                mFirestore.collection(Constants.COLLECTIONS.USER_COLLECTION)
                    .document(getMessageAtributes?.uid.toString())
                    .get()
                    .addOnSuccessListener {
                        mUser = it.toObject(User::class.java)!!
                        val intent = Intent(this, MessageActivity::class.java)
                        intent.putExtra(Constants.KEY.MESSAGE_KEY, mUser)
                        startActivity(intent)
                    }
            }
    }

    private fun handleFavorite() {
        var tsDoc = mFirestore.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
            .document(mService?.serviceId.toString())
        mFirestore.runTransaction {
            var contentServico = it.get(tsDoc).toObject(Servicos::class.java)
            if (contentServico!!.favoritos.containsKey(mAuth.currentUser?.uid)) {
                contentServico.favoritos.remove(mAuth.currentUser?.uid)

            } else {
                contentServico.favoritos[mAuth.currentUser!!.uid] = true

            }
            it.set(tsDoc, contentServico)

        }
    }


    private fun handleContract() {
        if (mService?.uid == mAuth.currentUser?.uid) {
            val mAlert = AlertDialog.Builder(this)
            mAlert.setMessage(
                "Hey, nós sabemos o quão ótimo(a) você é," +
                        " mas infelizmente você não pode se contratar."
            )
            mAlert.setPositiveButton("OK", { dialogInterface: DialogInterface, i: Int -> })
            mAlert.show()
        } else {

            val intent = Intent(this, ConfirmServiceActivity::class.java)
            intent.putExtra(Constants.KEY.SERVICE_KEY, mService)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)

        }
    }


    fun getService() {
        mFirestore.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
            .whereEqualTo("serviceId", mService?.serviceId)
            .addSnapshotListener { snapshot, exception ->
                snapshot?.let {
                    for (doc in snapshot) {
                        val service = doc.toObject(Servicos::class.java)

                        txtQtdServices.text = "${service.totalServicos} concluídos"
                        txtTituloServices.text = service.nomeService
                        txtDescShort.text = service.shortDesc
                        txtResponde.text = service.tempoResposta
                        //avaliacao
                        val avaliacao: Double =
                            service.avaliacao.toDouble() / service.totalAvaliacao
                        val resultAvaliacao = String.format("%.1f", avaliacao)
                        if (service.avaliacao == 0) {
                            txtAvaliacao.text = "Sem avaliações"
                        } else {
                            txtAvaliacao.text = "${resultAvaliacao}/5"
                        }
                        //preco
                        val result = String.format("%.2f", mService?.preco)
                        txtPrecoContratante.text =
                            "R$ ${result}"


                        txtDetailDesc.text = service.longDesc
                        txtQuality.text = service.qualidadesDiferenciais
                        txtEndereco.text =
                            "${service.rua},  ${service.bairro}, ${service.numero} \n" +
                                    "CEP:${service.cep}, ${service.cidade}, ${service.estado}"
                        if (service.ddd == null && service.telefone == null) {
                            txtTelefone.text = "Sem telefone"
                        } else {
                            txtTelefone.text = "(${service.ddd}) ${service.telefone}"
                        }

                        txtEmail.text = service.email
                        if (service.comments > 0) {
                            btnComments.text = "${mService?.comments} comentários"
                            btnComments?.visibility = View.VISIBLE
                        } else {
                            btnComments.visibility = View.GONE
                        }

                    }
                }

            }
    }

}
