package com.flyppcorp.flypp

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import com.flyppcorp.atributesClass.Servicos
import com.flyppcorp.atributesClass.User
import com.flyppcorp.constants.Constants
import com.flyppcorp.managerServices.EditServiceActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_manager_edit_service.*

class ManagerEditServiceActivity : AppCompatActivity() {

    private var mServicos: Servicos? = null
    private lateinit var mFirestore: FirebaseFirestore
    private var url: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manager_edit_service)
        mServicos = intent.extras?.getParcelable(Constants.KEY.SERVICE_KEY)
        mFirestore = FirebaseFirestore.getInstance()
        url = mServicos?.urlService
        val tb = findViewById<Toolbar>(R.id.tb_managerService)
        tb.setTitle("")
        setSupportActionBar(tb)
        fetchServico()

    }

    private fun fetchServico() {
        mFirestore.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
            .whereEqualTo("serviceId", mServicos!!.serviceId!!)
            .addSnapshotListener { snapshot, exception ->
                snapshot?.let {
                    for (doc in snapshot) {
                        val serviceItem = doc.toObject(Servicos::class.java)
                        if (serviceItem.urlService != null) Picasso.get().load(serviceItem.urlService).fit().centerCrop().into(
                            imgServiceManagerView
                        )
                        else imgServiceManagerView.setImageResource(R.drawable.ic_working)
                        txtQtdServicesView.text =
                            "${serviceItem.totalServicos} serviços finalizados"
                        txtTituloServicesView.text = serviceItem.nomeService
                        txtDescShortView.text = serviceItem.shortDesc
                        val avaliacao: Double =
                            serviceItem.avaliacao.toDouble() / serviceItem.totalAvaliacao
                        txtAvaliacaoView.text = "Avaliado em ${avaliacao.toString().substring(
                            0,
                            3
                        )}/5 segundo ${serviceItem.totalAvaliacao} usuários"
                        txtPrecoView.text =
                            "R$ ${serviceItem.preco} por ${serviceItem.tipoCobranca}"
                        txtDetailDescView.text = serviceItem.longDesc
                        txtQualityView.text = serviceItem.qualidadesDiferenciais
                        txtEnderecoView.text =
                            "${serviceItem.rua}, ${serviceItem.bairro}, ${serviceItem.numero} \n" +
                                    "${serviceItem.cidade}, ${serviceItem.estado}, ${serviceItem.cep}"
                        txtTelefoneView.text = "(${serviceItem.ddd}) ${serviceItem.telefone}"
                        txtEmailView.text = serviceItem.email
                    }
                }
            }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.manager_services_items_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.deleteServiceIc -> {
                val mAlert = AlertDialog.Builder(this)
                mAlert.setMessage("Você tem certeza que deseja excluir este serviço?")
                mAlert.setPositiveButton("Sim") { dialog: DialogInterface?, which: Int -> deleteService() }
                mAlert.setNegativeButton("Não") { dialog: DialogInterface?, which: Int -> }
                mAlert.show()
            }
            R.id.editServiceIc -> {
                val intent = Intent(this, EditServiceActivity::class.java)
                intent.putExtra(Constants.KEY.SERVICE_KEY, mServicos)
                intent.putExtra("url", url)
                startActivity(intent)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun deleteService() {
        mFirestore.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
            .document(mServicos!!.serviceId!!)
            .delete()
            .addOnSuccessListener {
                servicosAtivos()
                finish()

            }.addOnFailureListener {
                Toast.makeText(this, "Ocorreu um erro, tente novamente", Toast.LENGTH_SHORT).show()
            }
    }

    private fun servicosAtivos(){
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        val tsDoc = mFirestore.collection(Constants.COLLECTIONS.USER_COLLECTION).document(uid)
        mFirestore.runTransaction {
            val content = it.get(tsDoc).toObject(User::class.java)
            content!!.servicosAtivos = content.servicosAtivos - 1
            it.set(tsDoc, content)
        }
    }

}
