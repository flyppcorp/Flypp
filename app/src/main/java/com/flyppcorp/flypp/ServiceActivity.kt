package com.flyppcorp.flypp

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import android.widget.Toolbar
import androidx.appcompat.app.AlertDialog
import com.flyppcorp.atributesClass.Servicos
import com.flyppcorp.constants.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_service.*

class ServiceActivity : AppCompatActivity() {

    private lateinit var mFirestore: FirebaseFirestore
    private var mService: Servicos? = null
    private var mFavorito: Servicos? = null
    private lateinit var mAuth: FirebaseAuth
    var menuFAv : MenuItem? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service)
        mFirestore = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()
        mService = intent.extras?.getParcelable(Constants.KEY.SERVICE_KEY)
        getService()

        btnContratar.setOnClickListener {
            handleContract()
        }

        val tb = findViewById<androidx.appcompat.widget.Toolbar>(R.id.includeService)
        tb.title = ""
        setSupportActionBar(tb)
        getIcon()
    }

    private fun getIcon() {
        mFirestore.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
            .document(mService!!.serviceId!!)
            .get()
            .addOnSuccessListener {
                mFavorito = it.toObject(Servicos::class.java)
                getIconResult()
            }
    }

    private fun getIconResult() {
        mFavorito?.let {
            if (it.favoritos.containsKey(mAuth.currentUser!!.uid)){
                menuFAv?.setIcon( R.drawable.ic_favorite_white)
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
            R.id.fav_action -> {
                handleFavorite()
                mFirestore.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
                    .document(mService!!.serviceId!!)
                    .get()
                    .addOnSuccessListener {
                        mFavorito = it.toObject(Servicos::class.java)
                        mFavorito?.let {
                            if (!it.favoritos.containsKey(mAuth.currentUser!!.uid)) menuFAv?.setIcon(R.drawable.ic_favorite_white)
                            else menuFAv?.setIcon(R.drawable.ic_favorite_border_white)
                        }
                    }
            }
            R.id.id_send_message -> {
                handleMessage()
            }

        }

        return super.onOptionsItemSelected(item)
    }

    private fun handleFavorite() {
        var tsDoc = mFirestore.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
            .document(mService!!.serviceId!!)
        mFirestore.runTransaction {
            var contentServico = it.get(tsDoc).toObject(Servicos::class.java)
            if (contentServico!!.favoritos.containsKey(mAuth.currentUser!!.uid)) {
                contentServico.favoritos.remove(mAuth.currentUser!!.uid)

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
                        " mas infelizmente você não pode se contratar"
            )
            mAlert.setPositiveButton("OK", { dialogInterface: DialogInterface, i: Int -> })
            mAlert.show()
        } else {
            val intent = Intent(this, ConfirmServiceActivity::class.java)
            intent.putExtra(Constants.KEY.SERVICE_KEY, mService)
            startActivity(intent)
        }
    }

    private fun handleMessage() {
        if (mService?.uid == mAuth.currentUser?.uid) {
            val mAlert = AlertDialog.Builder(this)
            mAlert.setMessage(
                "Hey, nós sabemos que as vezes bate aquela vontade de conversar com a gente mesmo" +
                        " mas infelizmente você não pode fazer isso"
            )
            mAlert.setPositiveButton("OK", { dialogInterface: DialogInterface, i: Int -> })
            mAlert.show()
        } else {
            val intent = Intent(this, MessageActivity::class.java)
            intent.putExtra(Constants.KEY.MESSAGE_KEY, mService)
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
                        if (service.urlService == null) {
                            imgServiceView.setImageResource(R.drawable.ic_working)
                        } else {
                            Picasso.get().load(service.urlService).into(imgServiceView)
                        }
                        txtQtdServices.text = "${service.totalServicos} serviços finalizados"
                        txtTituloServices.text = service.nomeService
                        txtDescShort.text = service.shortDesc
                        val avaliacao: Double = service.avalicao.toDouble() / service.totalavalicao
                        if (service.avalicao == 0) txtAvaliacao.text = "Avaliado em ${service.avalicao}/5 segundo os usuários "
                        else txtAvaliacao.text = "Avaliado em ${avaliacao.toString().substring(0, 1)}/5 segundo os usuários "


                        txtPreco.text = "R$ ${service.preco} por ${service.tipoCobranca}"
                        txtDetailDesc.text = service.longDesc
                        txtQuality.text = service.qualidadesDiferenciais
                        txtEndereco.text =
                            "${service.rua},  ${service.bairro}, ${service.numero} \n" +
                                    "CEP:${service.cep}, ${service.cidade}, ${service.estado}"
                        txtTelefone.text = "(${service.ddd}) ${service.telefone}"
                        txtEmail.text = service.email


                    }
                }

            }
    }

}
