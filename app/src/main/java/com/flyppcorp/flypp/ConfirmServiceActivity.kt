package com.flyppcorp.flypp

import android.content.Context
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.flyppcorp.atributesClass.Myservice
import com.flyppcorp.atributesClass.Notification
import com.flyppcorp.atributesClass.Servicos
import com.flyppcorp.atributesClass.User
import com.flyppcorp.constants.Constants
import com.flyppcorp.firebase_classes.FirestoreContract
import com.google.android.gms.ads.InterstitialAd
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_confirm_service.*
import java.util.*
import com.google.android.gms.ads.*

class ConfirmServiceActivity : AppCompatActivity() {
    //private var mService: Servicos? = null
    private var mServices: Servicos? = null
    private lateinit var mFirestore: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth
    private var mUser: User? = null
    private lateinit var mFirestoreContract: FirestoreContract
    private lateinit var mMyservice: Myservice
    private var user: User? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm_service)
        mServices = intent.extras?.getParcelable(Constants.KEY.SERVICE_KEY)
        mFirestore = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()
        mFirestoreContract = FirestoreContract(this)
        mFirestoreContract.mIntertial = InterstitialAd(applicationContext)
        MobileAds.initialize(this)
        mFirestoreContract.mIntertial.adUnitId = getString(R.string.ads_intertitial_id)
        mFirestoreContract.mIntertial.loadAd(AdRequest.Builder().build())


        mMyservice = Myservice()
        getDataService()
        btnConfirmContract.setOnClickListener {
            if (validateConection()) {
                getToSave()
            }
        }


        mFirestore.collection(Constants.COLLECTIONS.USER_COLLECTION)
            .document(mAuth.currentUser!!.uid)
            .get()
            .addOnSuccessListener {
                mUser = it.toObject(User::class.java)
                getDataService()

            }

    }

    fun getToSave() {
        mFirestore.collection(Constants.COLLECTIONS.USER_COLLECTION)
            .document(mAuth.currentUser!!.uid)
            .get()
            .addOnSuccessListener {
                mUser = it.toObject(User::class.java)
                handleConfirm()
            }
    }

    private fun handleConfirm() {
        mUser?.let {
            mMyservice.idContratante = it.uid
            mMyservice.idContratado = mServices!!.uid
            mMyservice.id[it.uid.toString()] = true
            mMyservice.id[mServices!!.uid.toString()] = true
            mMyservice.timestamp = System.currentTimeMillis()
            mMyservice.serviceId = mServices!!.serviceId
            mMyservice.urlService = mServices?.urlService
            mMyservice.serviceNome = mServices?.nomeService
            mMyservice.urlContratante = it.url
            mMyservice.urlContratado = mServices?.urlProfile
            mMyservice.preco = mServices?.preco
            mMyservice.tipoCobranca = mServices?.tipoCobranca
            mMyservice.nomeContratado = mServices?.nome
            mMyservice.nomeContratante = it.nome
            mMyservice.shortDesc = mServices?.shortDesc
            mMyservice.cep = it.cep
            mMyservice.estado = it.estado
            mMyservice.cidade = it.cidade
            mMyservice.bairro = it.bairro
            mMyservice.rua = it.rua
            mMyservice.numero = it.numero
            mMyservice.pendente = true
            mMyservice.finalizado = false
            mMyservice.andamento = false
            mMyservice.observacao = editObservacao.text.toString()
            val documentId = UUID.randomUUID().toString() + it.uid
            mMyservice.documentId = documentId

            mFirestore.collection(Constants.COLLECTIONS.USER_COLLECTION)
                .document(mMyservice.idContratado!!)
                .get()
                .addOnSuccessListener { info ->

                    val user: User? = info.toObject(User::class.java)
                    val notification = Notification()
                    notification.serviceId = documentId
                    notification.text =
                        "${mMyservice.nomeContratante} está solicitando um serviço (${mMyservice!!.serviceNome})"
                    notification.title = "Novo serviço solicitado"

                    mFirestoreContract.confirmServiceContract(
                        mMyservice, documentId, user!!.token!!, notification
                    )
                }


        }
    }

    private fun getDataService() {
        mUser?.let {
            nomeContratante.text = it.nome
            txtServicoContratar.text = mServices?.nomeService
            txtPrecoContratante.text = "R$ ${mServices?.preco} por  ${mServices?.tipoCobranca}"
            txtEnderecoContratante.text =
                "${it.rua}, ${it.bairro}, ${it.numero} \n ${it.cidade}, ${it.estado}, ${it.cep}"

        }
    }

    private fun validateConection(): Boolean {
        val cm = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = cm.activeNetworkInfo
        if (networkInfo != null && networkInfo.isConnected) {
            return true
        } else {
            progressBar.visibility = View.GONE
            Toast.makeText(this, "Você não possui conexão com a internet", Toast.LENGTH_SHORT)
                .show()
            return false
        }

    }
}
