package com.flyppcorp.managerServices

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
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
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.title = "Finalizado"
        getadress()
        getAvaliationStatus()

    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getAvaliationStatus() {
        mFirestore.collection(Constants.COLLECTIONS.MY_SERVICE)
            .document(mMyservice?.documentId.toString())
            .get()
            .addOnSuccessListener {
                mServices = it.toObject(Myservice::class.java)
                mServices?.let {
                    if (it.idAvaliador.containsKey(mMyservice!!.idContratante) || mMyservice!!.idContratado == mAuth.currentUser!!.uid) btnAvaliar.visibility =
                        View.GONE
                }

            }
    }

    private fun getadress() {
        mFirestore.collection(Constants.COLLECTIONS.MY_SERVICE)
            .document(mMyservice?.documentId.toString())
            .get()
            .addOnSuccessListener {
                mAdress = it.toObject(Myservice::class.java)
                fetchService()
            }
    }

    private fun fetchService() {
        mAdress?.let {
            if (mMyservice?.urlService != null) Picasso.get().load(mMyservice?.urlService)
                .placeholder(R.drawable.ic_working).fit().centerCrop()
                .into(imgServiceFinalizadoAcct)
            else imgServiceFinalizadoAcct.setImageResource(R.drawable.ic_working)
            txtContratadoFinalizadoAcct.text = mMyservice?.nomeContratante
            txtContratanteFinalizadoAcct.text = mMyservice?.nomeContratado
            txtServicoFinalizadoAcct.text = mMyservice?.serviceNome
            txtObservacaoFinalizado.text = mMyservice?.observacao

            val result = String.format("%.2f", mMyservice?.preco)
            txtPrecoFinalizadoAcct.text = "R$ ${result}"


            txtEnderecoFinalizadoAcct.text = "${it.rua}, ${it.bairro}, ${it.numero} \n" +
                    "${it.cidade}, ${it.estado}, ${it.cep}"


        }
    }
}
