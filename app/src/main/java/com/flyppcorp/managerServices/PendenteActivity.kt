package com.flyppcorp.managerServices

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.flyppcorp.atributesClass.Myservice
import com.flyppcorp.constants.Constants
import com.flyppcorp.flypp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_pendente.*

class PendenteActivity : AppCompatActivity() {

    private var mMyService: Myservice? = null
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mFirestore: FirebaseFirestore
    private var mAdress: Myservice? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pendente)
        mMyService = intent.extras?.getParcelable(Constants.KEY.SERVICE_STATUS)
        mAuth = FirebaseAuth.getInstance()
        mFirestore = FirebaseFirestore.getInstance()
        btnRecusarDesistirPendente.setOnClickListener {
            handleDesistirRecusar()
        }
        btnAceitarVoltarPendente.setOnClickListener {
            handleAceitarVoltar()
        }


        getEndereco()
        handleTextButton()


    }

    private fun handleAceitarVoltar() {
        if (mMyService!!.idContratante == mAuth.currentUser!!.uid){
            finish()
        }else if (mMyService!!.idContratado == mAuth.currentUser!!.uid ){
              val tsDoc = mFirestore.collection(Constants.COLLECTIONS.MY_SERVICE).document(mMyService!!.documentId!!)
            mFirestore.runTransaction {
                val content = it.get(tsDoc).toObject(Myservice::class.java)
                content?.pendente = false
                content?.andamento = true
                it.set(tsDoc, content!!)
            }
            finish()
        }
    }

    private fun handleDesistirRecusar() {
       val mDialog = AlertDialog.Builder(this)
        mDialog.setTitle("Você tem certeza disso?")
        mDialog.setPositiveButton("Sim"){dialog: DialogInterface?, which: Int ->
            mFirestore.collection(Constants.COLLECTIONS.MY_SERVICE)
                .document(mMyService!!.documentId!!)
                .delete()
                .addOnSuccessListener {
                    finish()
                }.addOnFailureListener {
                    Toast.makeText(this, "Ocorreu um erro. Tente novamente!", Toast.LENGTH_SHORT).show()
                }
        }
        mDialog.setNegativeButton("Não"){dialog: DialogInterface?, which: Int ->  }
        mDialog.show()
    }


    private fun handleTextButton() {
        if (mMyService!!.idContratado == mAuth.currentUser!!.uid){
            btnAceitarVoltarPendente.text = "Aceitar"
            btnRecusarDesistirPendente.text = "Recusar"
        }
        else {
            btnAceitarVoltarPendente.text = "Voltar"
            btnRecusarDesistirPendente.text = "Desistir"
        }
    }

    private fun getEndereco() {
        mFirestore.collection(Constants.COLLECTIONS.MY_SERVICE)
            .document(mMyService!!.documentId!!)
            .get()
            .addOnSuccessListener {
                mAdress = it.toObject(Myservice::class.java)
                fetchPendente()
            }
    }

    private fun fetchPendente() {
        mAdress?.let {
            if (mMyService?.urlService == null) imgServiceAcct.setImageResource(R.drawable.ic_working)
            else Picasso.get().load(mMyService?.urlService).into(imgServiceAcct)
            txtContratanteAcct.text = mMyService!!.nomeContratante
            txtContratadoAcct.text = mMyService!!.nomeContratado
            txtServicoAcct.text = mMyService!!.serviceNome
            txtObservacao.text = mMyService?.observacao
            txtPrecoAcct.text = "R$ ${mMyService?.preco} por ${mMyService?.tipoCobranca}"
            txtEnderecoAcct.text = "${it.rua}, ${it.bairro}, ${it.numero} \n" +
                    "${it.cidade}, ${it.estado}, ${it.cep}"
        }


    }
}
