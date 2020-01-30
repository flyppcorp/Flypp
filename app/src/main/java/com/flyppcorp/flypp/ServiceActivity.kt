package com.flyppcorp.flypp

import android.content.ClipData
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_service.*

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
    private lateinit var mUrl : ArrayList<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service)
        mFirestore = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()
        mUser = User()
        mService = intent.extras?.getParcelable(Constants.KEY.SERVICE_KEY)
        if (mService?.urlService != null && mService?.urlService2 != null){
            mUrl = arrayListOf(
                mService?.urlService.toString(),
                mService?.urlService2.toString()
            )
        }else if (mService?.urlService != null && mService?.urlService2 == null){
            mUrl = arrayListOf(
                mService?.urlService.toString()

            )
        }else if (mService?.urlService == null && mService?.urlService2 != null){
            mUrl = arrayListOf(
                mService?.urlService2.toString()
            )
        }else if (mService?.urlService == null && mService?.urlService2 == null){
            mUrl = arrayListOf(

            )
        }

        val adapter  = PagerAdapterImage (this, mUrl)
        imgServiceView.adapter = adapter
        
        getService()

        btnContratar.setOnClickListener {
            handleContract()
        }
        btnComments?.setOnClickListener {
            val mServico = Servicos()
            mServico.serviceId = mService?.serviceId
            val intent = Intent(this, CommentActivity::class.java)
            intent.putExtra(Constants.KEY.COMMENTS, mServico)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

        //val tb = findViewById<androidx.appcompat.widget.Toolbar>(R.id.includeService)
        //tb.title = ""
        supportActionBar!!.title = "Serviço"
        btnCommentsVisible()
        getIcon()

    }

    private fun btnCommentsVisible() {
        if (mService?.comments != null || mService?.comments!! > 0){
            btnComments.text = "${mService?.comments} comentários"
            btnComments?.visibility = View.VISIBLE
        }
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
            R.id.fav_action -> {
                handleFavorite()
                mFirestore.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
                    .document(mService!!.serviceId!!)
                    .get()
                    .addOnSuccessListener {
                        mFavorito = it.toObject(Servicos::class.java)
                        mFavorito?.let {
                            if (!it.favoritos.containsKey(mAuth.currentUser!!.uid)) menuFAv?.setIcon(
                                R.drawable.ic_favorite_white
                            )
                            else menuFAv?.setIcon(R.drawable.ic_favorite_border_white)
                        }
                    }
            }

           R.id.id_send_message -> {
                val uid = mAuth.currentUser!!.uid
                if (uid != mService!!.uid){
                    handleMessage()
                }else{
                    val mAlert = AlertDialog.Builder(this)
                    mAlert.setMessage("Ops, nós sabemos que as vezes queremos falar com nós mesmos, mas desta vez não vai ser possível.")
                    mAlert.setPositiveButton("Ok", {dialog, which ->  })
                    mAlert.show()
                }

            }

        }

        return super.onOptionsItemSelected(item)
    }

   private fun handleMessage() {
          mFirestore.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
              .document(mService!!.serviceId!!)
              .get()
              .addOnSuccessListener {
                  getMessageAtributes = it.toObject(Servicos::class.java)
                  mFirestore.collection(Constants.COLLECTIONS.USER_COLLECTION)
                      .document(getMessageAtributes!!.uid!!)
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


    fun getService() {
        mFirestore.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
            .whereEqualTo("serviceId", mService?.serviceId)
            .addSnapshotListener { snapshot, exception ->
                snapshot?.let {
                    for (doc in snapshot) {
                        val service = doc.toObject(Servicos::class.java)

                        txtQtdServices.text = "${service.totalServicos} serviços finalizados"
                        txtTituloServices.text = service.nomeService
                        txtDescShort.text = service.shortDesc
                        txtResponde.text = service.tempoResposta
                        val avaliacao: Double =
                            service.avaliacao.toDouble() / service.totalAvaliacao
                        if (service.avaliacao == 0) txtAvaliacao.text =
                            "Sem avaliações"
                        else txtAvaliacao.text = "${avaliacao.toString().substring(
                            0,
                            1
                        )}/5  (${service.totalAvaliacao})"


                        txtPreco.text = "R$ ${service.preco.toString().replace(".",",")}/${service.tipoCobranca}"
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
