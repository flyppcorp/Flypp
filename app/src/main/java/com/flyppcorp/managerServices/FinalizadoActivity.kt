package com.flyppcorp.managerServices

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.flyppcorp.atributesClass.Myservice
import com.flyppcorp.atributesClass.Servicos
import com.flyppcorp.constants.Constants
import com.flyppcorp.flypp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_finalizado.*

class FinalizadoActivity : AppCompatActivity() {
   private lateinit var mFirestore: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth
    private var mMyservice: Myservice? = null
    private var mAdress: Myservice? = null
    private var mServices: Myservice? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finalizado)
        mFirestore = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()
        mMyservice = intent.extras?.getParcelable(Constants.KEY.SERVICE_STATUS)
        btnAvaliar.setOnClickListener {
            val intent = Intent(this, AvaliationActivity::class.java)
            intent.putExtra(Constants.KEY.SERVICE_STATUS, mMyservice)
            startActivity(intent)
            finish()
        }

        getadress()
        getAvaliationStatus()
    }

    private fun getAvaliationStatus() {
        mFirestore.collection(Constants.COLLECTIONS.MY_SERVICE)
            .document(mMyservice!!.documentId!!)
            .get()
            .addOnSuccessListener {
                mServices = it.toObject(Myservice::class.java)
                mServices?.let {
                   if (it.idAvaliador.containsKey(mMyservice!!.idContratante) || mMyservice!!.idContratado == mAuth.currentUser!!.uid) btnAvaliar.visibility = View.GONE
                }

            }
    }

    private fun getadress() {
        mFirestore.collection(Constants.COLLECTIONS.MY_SERVICE)
            .document(mMyservice!!.documentId!!)
            .get()
            .addOnSuccessListener {
                mAdress = it.toObject(Myservice::class.java)
                fetchService()
            }
    }

    private fun fetchService() {
        mAdress?.let {
            if(mMyservice?.urlService !=  null) Picasso.get().load(mMyservice?.urlService).into(imgServiceFinalizadoAcct)
            else imgServiceFinalizadoAcct.setImageResource(R.drawable.ic_working)
            txtContratadoFinalizadoAcct.text = mMyservice!!.nomeContratante
            txtContratanteFinalizadoAcct.text = mMyservice!!.nomeContratado
            txtServicoFinalizadoAcct.text = mMyservice!!.serviceNome
            txtObservacaoFinalizado.text = mMyservice?.observacao
            txtPrecoFinalizadoAcct.text = "R$ ${mMyservice?.preco} por ${mMyservice?.tipoCobranca}"
            txtEnderecoFinalizadoAcct.text = "${it.rua}, ${it.bairro}, ${it.numero} \n" +
                    "${it.cidade}, ${it.estado}, ${it.cep}"

        }
    }
}
