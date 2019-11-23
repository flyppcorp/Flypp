package com.flyppcorp.managerServices

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.flyppcorp.atributesClass.Myservice
import com.flyppcorp.atributesClass.Servicos
import com.flyppcorp.atributesClass.User
import com.flyppcorp.constants.Constants
import com.flyppcorp.flypp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_andamento.*

class AndamentoActivity : AppCompatActivity() {
    private lateinit var mFirestore: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth
    private var mMyservice: Myservice? = null
    private var mAdress: Myservice? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_andamento)
        mFirestore = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()
        mMyservice = intent.extras?.getParcelable(Constants.KEY.SERVICE_STATUS)
        btnVoltarAndamento.setOnClickListener {
            handleCancel()
        }
        btnFinalizarAndamento.setOnClickListener {
            handleFinalizar()
        }
        supportActionBar!!.title = "Em andamento"
        getEndereco()
        btnText()
    }

    private fun handleFinalizar() {
        val tsDoc = mFirestore.collection(Constants.COLLECTIONS.MY_SERVICE)
            .document(mMyservice!!.documentId!!)
        mFirestore.runTransaction {
            val content = it.get(tsDoc).toObject(Myservice::class.java)
            content?.andamento = false
            content?.finalizado = true
            //Aqui será a chamada para a tela de avaliacao caso seja finalizado pelo contratante
            it.set(tsDoc, content!!)

        }
        val tsServiceDoc = mFirestore.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
            .document(mMyservice!!.serviceId!!)
        mFirestore.runTransaction {
            val contentService = it.get(tsServiceDoc).toObject(Servicos::class.java)

            contentService?.totalServicos = contentService?.totalServicos!!.toInt() + 1
            totalServicosUser()
            it.set(tsServiceDoc, contentService)
        }
        if (mMyservice!!.idContratante == mAuth.currentUser!!.uid) {
            val intent = Intent(this, AvaliationActivity::class.java)
            intent.putExtra(Constants.KEY.SERVICE_STATUS, mMyservice)
            startActivity(intent)
            finish()
        }
        finish()

    }
    private fun totalServicosUser(){
        val tsDoc = mFirestore.collection(Constants.COLLECTIONS.USER_COLLECTION).document(mMyservice!!.idContratado!!)
        mFirestore.runTransaction {
            val content = it.get(tsDoc).toObject(User::class.java)
            content!!.totalServicosFinalizados = content.totalServicosFinalizados + 1
            it.set(tsDoc, content)
        }
    }

    private fun handleCancel() {
        val mAlert = AlertDialog.Builder(this)
        mAlert.setTitle("Você tem certeza disso?")
        if (mMyservice!!.idContratado!! == mAuth.currentUser!!.uid) {
            mAlert.setPositiveButton("Sim") { dialog: DialogInterface?, which: Int ->
                mFirestore.collection(Constants.COLLECTIONS.MY_SERVICE)
                    .document(mMyservice!!.documentId!!)
                    .delete()
                    .addOnSuccessListener {
                        finish()
                    }.addOnFailureListener {
                        Toast.makeText(
                            this,
                            "Ocorreu um erro. Tente novamente!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            mAlert.setNegativeButton("Não") { dialog: DialogInterface?, which: Int -> }
            mAlert.show()
        } else {
            finish()
        }

    }

    private fun btnText() {
        if (mMyservice!!.idContratado!! == mAuth.currentUser!!.uid) btnVoltarAndamento.text =
            "Cancelar"
    }

    private fun getEndereco() {
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
            if (mMyservice?.urlService != null) Picasso.get().load(mMyservice?.urlService).fit().centerCrop().into(
                imgAndamentoAcct
            )
            else imgAndamentoAcct.setImageResource(R.drawable.ic_working)
            txtContratadoAndamentoAcct.text = mMyservice!!.nomeContratado
            txtContratanteAndamentoAcct.text = mMyservice!!.nomeContratante
            txtServiceAndamentoAcct.text = mMyservice!!.serviceNome
            txtObservacaoAndamento.text = mMyservice?.observacao
            txtPrecoAndamentoAcct.text = "R$ ${mMyservice?.preco} por ${mMyservice?.tipoCobranca}"
            txtEnderecoAndamentoAcct.text = "${it.rua}, ${it.bairro}, ${it.numero} \n" +
                    "${it.cidade}, ${it.estado}, ${it.cep}"

        }
    }


}
