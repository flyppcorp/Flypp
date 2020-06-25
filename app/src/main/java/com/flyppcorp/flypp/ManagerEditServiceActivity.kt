package com.flyppcorp.flypp

import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import com.flyppcorp.atributesClass.DashBoard
import com.flyppcorp.atributesClass.Servicos
import com.flyppcorp.atributesClass.User
import com.flyppcorp.constants.Constants
import com.flyppcorp.managerServices.EditServiceActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_manager_edit_service.*
import java.util.*
import kotlin.collections.ArrayList

class ManagerEditServiceActivity : AppCompatActivity() {

    private var mServicos: Servicos? = null
    private lateinit var mFirestore: FirebaseFirestore
    private var url: String? = null
    private var url2: String? = null
    private var menuPause: MenuItem? = null
    private lateinit var mUrl: ArrayList<String>
    private lateinit var mStorage: FirebaseStorage
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manager_edit_service)
        mServicos = intent.extras?.getParcelable(Constants.KEY.SERVICE_KEY)
        mFirestore = FirebaseFirestore.getInstance()
        mStorage = FirebaseStorage.getInstance()
        val tb = findViewById<Toolbar>(R.id.tb_managerService)
        tb.setTitle("")
        setSupportActionBar(tb)
        fetchServico()
        fetchVisible()
        handleComment()

    }

    private fun handleComment() {
        val mServices = Servicos()
        mServices.serviceId = mServicos?.serviceId
        txtManagerComments.setOnClickListener {
            val intent = Intent(this, CommentActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.putExtra(Constants.KEY.COMMENTS, mServices)
            startActivity(intent)
        }

    }

    private fun fetchVisible() {
        mFirestore.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
            .document(mServicos?.serviceId.toString())
            .get()
            .addOnSuccessListener {
                val service = it.toObject(Servicos::class.java)
                if (service?.visible != true) menuPause?.setIcon(R.drawable.ic_invisible)
            }
    }

    private fun fetchServico() {
        mFirestore.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
            .whereEqualTo("serviceId", mServicos?.serviceId.toString())
            .addSnapshotListener { snapshot, exception ->
                snapshot?.let {
                    for (doc in snapshot) {
                        val serviceItem = doc.toObject(Servicos::class.java)

                        if (serviceItem.urlService != null){
                            Picasso.get().load(serviceItem.urlService).centerCrop().fit().placeholder(R.drawable.ic_working).into(imgServiceManagerView2)
                        }
                        txtQtdServicesView.text =
                            "${serviceItem.totalServicos} concluídos"
                        if (serviceItem.comments > 0) {
                            txtManagerComments?.text = "${serviceItem.comments} comentários"
                        } else {
                            txtManagerComments?.visibility = View.GONE
                        }
                        txtTituloServicesView.text = serviceItem.nomeService
                        if (serviceItem.avaliacao == 0) {
                            txtAvaliacaoView.text = "Sem avaliações"
                        } else {
                            val avaliacao: Double =
                                serviceItem.avaliacao.toDouble() / serviceItem.totalAvaliacao
                            val resultAv = String.format("%.1f", avaliacao)
                            txtAvaliacaoView.text = "${resultAv}/5 (${serviceItem?.totalAvaliacao})".replace(".", ",")
                        }

                        val result = String.format("%.2f", serviceItem.preco)
                        txtPrecoView.text = "R$ ${result}".replace(".", ",")

                        txtDetailDescView.text = serviceItem.longDesc
                        txtEnderecoView.text =
                            "${serviceItem.rua}, ${serviceItem.bairro}, ${serviceItem.numero} \n" +
                                    "${serviceItem.cidade}, ${serviceItem.estado}, ${serviceItem.cep}"

                    }
                }
            }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.manager_services_items_menu, menu)
        menuPause = menu?.findItem(R.id.pausePlayServiceIc)
        return super.onCreateOptionsMenu(menu)
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
                intent.putExtra("url2", url)
                startActivity(intent)
            }

            R.id.pausePlayServiceIc -> {
                //menuPause?.setIcon(R.drawable.ic_invisible)
                handleVisible()
                mFirestore.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
                    .document(mServicos!!.serviceId!!)
                    .get()
                    .addOnSuccessListener {
                        val visible = it.toObject(Servicos::class.java)
                        if (visible?.visible == true) menuPause?.setIcon(R.drawable.ic_invisible)
                        else menuPause?.setIcon(R.drawable.ic_visible)
                    }
            }

            R.id.shareManager -> {
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

                val rand = Random().nextInt(100) + 100
                val link = "https://flyppbrasil.page.link?" +
                        "apn=com.flyppcorp.flypp" +
                        "&ibi=com.example.ios" +
                        "&link=https%3A%2F%2Fplay.google.com%2Fstore%2Fapps%2Fdetails%3Fid%3Dcom.flyppcorp.flypp" +
                        "&st=${mServicos?.nomeService}" +
                        "&sd=${mServicos?.shortDesc}" +
                        "&uid=-"+
                        "&utm_source=${mServicos?.serviceId}"

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
                            Intent.EXTRA_TEXT, "${mServicos?.nomeService} \n${mServicos?.shortDesc} \n"+ shortLink.toString()
                        )

                        startActivity(intent)
                    }.addOnFailureListener {

                    }
            }

        }

        return super.onOptionsItemSelected(item)
    }

    private fun handleVisible() {
        val tsDoc = mFirestore.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
            .document(mServicos!!.serviceId!!)
        mFirestore.runTransaction {
            val content = it.get(tsDoc).toObject(Servicos::class.java)
            if (content?.visible == false) {
                content.visible = true
            } else {
                content?.visible = false
            }
            it.set(tsDoc, content!!)
        }
    }

    private fun deleteService() {
        val mProgress = ProgressDialog(this)
        mProgress.setCancelable(false)
        mProgress.show()
        mProgress.setContentView(R.layout.progress)
        mProgress.window?.setBackgroundDrawableResource(android.R.color.transparent)
        mFirestore.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
            .document(mServicos!!.serviceId!!)
            .delete()
            .addOnSuccessListener {
                servicosAtivos()
                dashBoard()
                deleteImage()
                finish()

            }.addOnFailureListener {
                Toast.makeText(this, "Ocorreu um erro, tente novamente", Toast.LENGTH_SHORT).show()
            }
    }
    private fun deleteImage(){
        val filename = mServicos?.serviceId.toString()
        val ref = mStorage.getReference("image/${filename}")
        ref.delete()
    }

    private fun dashBoard() {
        val tsDoc =
            mFirestore.collection(Constants.DASHBOARD_SERVICE.DASHBOARD_COLLECTION).document(
                Constants.DASHBOARD_SERVICE.DASHBOARD_DOCUMENT
            )
        mFirestore.runTransaction {
            val content = it.get(tsDoc).toObject(DashBoard::class.java)
            content!!.newServices = content.newServices - 1
            it.set(tsDoc, content)
        }
    }


    private fun servicosAtivos() {
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        val tsDoc = mFirestore.collection(Constants.COLLECTIONS.USER_COLLECTION).document(uid)
        mFirestore.runTransaction {
            val content = it.get(tsDoc).toObject(User::class.java)
            content!!.servicosAtivos = content.servicosAtivos - 1
            it.set(tsDoc, content)
        }
    }

}
