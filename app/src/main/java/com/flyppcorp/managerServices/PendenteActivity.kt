package com.flyppcorp.managerServices

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.flyppcorp.atributesClass.Myservice
import com.flyppcorp.atributesClass.Notification
import com.flyppcorp.atributesClass.User
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

        supportActionBar!!.title = "Pendente "
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
                notification()
                it.set(tsDoc, content!!)
            }
            finish()
        }
    }
    private fun notification(){
        mFirestore.collection(Constants.COLLECTIONS.USER_COLLECTION)
            .document(mMyService!!.idContratante!!)
            .get()
            .addOnSuccessListener { info ->

                val user: User? = info.toObject(User::class.java)
                val notification = Notification()
                notification.serviceId = mMyService!!.serviceId
                notification.text = "${mMyService!!.nomeContratante} aceitou sua solicitação de trabalho (${mMyService!!.serviceNome})"
                notification.title = "Nova atualização de serviço"

                mFirestore.collection(Constants.COLLECTIONS.NOTIFICATION_SERVICE)
                    .document(user!!.token!!)
                    .set(notification)

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
                    if (mMyService!!.idContratante == mAuth.currentUser!!.uid){
                        notificationDesistence(mMyService!!.idContratado!!, "desistiu")
                    }else{
                        notificationDesistence(mMyService!!.idContratante!!, "rejeitou")
                    }
                    finish()
                }.addOnFailureListener {
                    Toast.makeText(this, "Ocorreu um erro. Tente novamente!", Toast.LENGTH_SHORT).show()
                }
        }
        mDialog.setNegativeButton("Não"){dialog: DialogInterface?, which: Int ->  }
        mDialog.show()
    }

    private fun notificationDesistence(uid: String, status: String){
        mFirestore.collection(Constants.COLLECTIONS.USER_COLLECTION)
            .document(uid)
            .get()
            .addOnSuccessListener { info ->

                val user: User? = info.toObject(User::class.java)
                val notification = Notification()
                notification.serviceId = mMyService!!.serviceId
                notification.text = "${mMyService!!.nomeContratante} $status sua solicitação de trabalho (${mMyService!!.serviceNome})"
                notification.title = "Nova atualização de serviço"

                mFirestore.collection(Constants.COLLECTIONS.NOTIFICATION_SERVICE)
                    .document(user!!.token!!)
                    .set(notification)

            }
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
            else Picasso.get().load(mMyService?.urlService).fit().centerCrop().into(imgServiceAcct)
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
