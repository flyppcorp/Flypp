package com.flyppcorp.managerServices

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.flyppcorp.atributesClass.Servicos
import com.flyppcorp.constants.Constants
import com.flyppcorp.firebase_classes.FirestoreService
import com.flyppcorp.flypp.R
import com.flyppcorp.fragments.HomeFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_edit_service.*
import kotlinx.android.synthetic.main.activity_edit_service.EditBairroService
import kotlinx.android.synthetic.main.activity_edit_service.btnSelectPhotoService
import kotlinx.android.synthetic.main.activity_edit_service.editCep
import kotlinx.android.synthetic.main.activity_edit_service.editCidadeService
import kotlinx.android.synthetic.main.activity_edit_service.editDescCurta
import kotlinx.android.synthetic.main.activity_edit_service.editDescDetalhada
import kotlinx.android.synthetic.main.activity_edit_service.editNumService
import kotlinx.android.synthetic.main.activity_edit_service.editPreco
import kotlinx.android.synthetic.main.activity_edit_service.editQualidadesDiferenciais
import kotlinx.android.synthetic.main.activity_edit_service.editRuaService
import kotlinx.android.synthetic.main.activity_edit_service.editService
import kotlinx.android.synthetic.main.activity_edit_service.editTags
import kotlinx.android.synthetic.main.activity_edit_service.imgService
import kotlinx.android.synthetic.main.activity_edit_service.progressBar5
import kotlinx.android.synthetic.main.activity_edit_service.spinnerDuracaoService
import kotlinx.android.synthetic.main.activity_edit_service.spinnerEstadoService
import kotlinx.android.synthetic.main.fragment_add.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class EditServiceActivity : AppCompatActivity() {

    private  var mService: Servicos? = null
    private var mUri: Uri? = null
    private lateinit var mStorage: FirebaseStorage
    private lateinit var mFirestore: FirebaseFirestore
    private lateinit var mServiceAtributes: Servicos
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mFirestoreService: FirestoreService
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_service)
        mService = intent.extras?.getParcelable(Constants.KEY.SERVICE_KEY)
        mFirestore = FirebaseFirestore.getInstance()
        mStorage = FirebaseStorage.getInstance()
        mServiceAtributes = Servicos()
        mAuth = FirebaseAuth.getInstance()
        mFirestoreService = FirestoreService(this)
        btnSelectPhotoService.setOnClickListener {
            handleSelectPhoto()
        }
        btnUpdateService.setOnClickListener {
            handleSaveService()
        }
        btnVoltarService.setOnClickListener {
            finish()
        }
        fetch()

    }

    private fun handleSelectPhoto() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, Constants.KEY.REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.KEY.REQUEST_CODE){
            mUri = data?.data

            imgService.setImageURI(mUri)
            btnSelectPhotoService.alpha = 0f
        }
    }

    private fun fetch() {
        mFirestore.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
            .whereEqualTo("serviceId", mService!!.serviceId)
            .addSnapshotListener { snapshot, exception ->
                snapshot?.let {
                    for (doc in snapshot){
                        val serviceItem = doc.toObject(Servicos::class.java)
                        if (serviceItem.urlService != null){
                            Picasso.get().load(serviceItem.urlService).into(imgService)
                            btnSelectPhotoService.alpha = 0f
                        }
                        if (mUri != null){
                            imgService.setImageURI(mUri)
                            btnSelectPhotoService.alpha = 0f
                        }
                        editService.setText(serviceItem.nomeService)
                        editDescCurta.setText(serviceItem.shortDesc)
                        editDescDetalhada.setText(serviceItem.longDesc)
                        editPreco.setText(serviceItem.preco)
                        editDuracaoEdit.setText(serviceItem.tipoCobranca)
                        editQualidadesDiferenciais.setText(serviceItem.qualidadesDiferenciais)
                        editCep.setText(serviceItem.cep)
                        editEstadoEdit.setText(serviceItem.estado)
                        editCidadeService.setText(serviceItem.cidade)
                        editRuaService.setText(serviceItem.rua)
                        EditBairroService.setText(serviceItem.bairro)
                        editNumService.setText(serviceItem.numero)
                        editTags.setText(serviceItem.tagsStr)

                    }
                }
            }

    }


    //------------------------------------------------------------------------------------------------------------------------
    private fun handleSaveService() {
        //definindo valores para a classe servico
        val timestamp = SimpleDateFormat("aaaaMMdd", Locale("EUA")).format(Date())
        val ref = mStorage.getReference("/ServicesImages/${timestamp}")
        mServiceAtributes.nome = mService?.nome
        mServiceAtributes.uid = mService?.uid
        mServiceAtributes.urlProfile = mService?.urlProfile
        mServiceAtributes.telefone = mService?.telefone
        mServiceAtributes.nomeService = editService.text.toString()
        mServiceAtributes.shortDesc = editDescCurta.text.toString()
        mServiceAtributes.longDesc = editDescDetalhada.text.toString()
        mServiceAtributes.preco = editPreco.text.toString()
        mServiceAtributes.tipoCobranca = editDuracaoEdit.text.toString()
        mServiceAtributes.qualidadesDiferenciais = editQualidadesDiferenciais.text.toString()
        mServiceAtributes.cep = editCep.text.toString()
        mServiceAtributes.estado = editEstadoEdit.text.toString()
        mServiceAtributes.cidade = editCidadeService.text.toString()
        mServiceAtributes.bairro = EditBairroService.text.toString()
        mServiceAtributes.rua = editRuaService.text.toString()
        mServiceAtributes.numero = editNumService.text.toString()
        mServiceAtributes.email = mAuth.currentUser!!.email
        mServiceAtributes.serviceId = mService!!.serviceId
        val tagInput = editTags.text.toString().toLowerCase()
        mServiceAtributes.tagsStr = tagInput
        val tagArray: Array<String> = tagInput.split(",").toTypedArray()
        val tags: MutableMap<String, Boolean> = HashMap()
        for (tag in tagArray) {
            tags[tag] = true
        }
        if (tags.isEmpty()) tags[""] = false
        mServiceAtributes.tags = tags
        //fim

        //obtendo url da imagem no firestorage
        mUri?.let {
            ref.putFile(it)
                .addOnSuccessListener {
                    ref.downloadUrl
                        .addOnSuccessListener {
                            mServiceAtributes.urlService = it.toString()

                            //salvando no db caso haja uma url
                            if (validate()) {
                                mFirestore.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
                                    .document(mService!!.serviceId!!)
                                    .set(mServiceAtributes)
                                finish()

                            } else {
                                Toast.makeText(
                                    this,
                                    "Por favor, preencha o nome do serviço e/ou as TAGS",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                }

        }
        //savando no db caso não haja uma url
        if (mUri == null) {
            if (validate() && validateConection()) {
                val extras = intent.extras
                if (extras != null) mServiceAtributes.urlService = extras.getString("url")

                mFirestore.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
                    .document(mService!!.serviceId!!)
                    .set(mServiceAtributes)
                finish()

            } else {
                Toast.makeText(
                    this,
                    "Por favor, preencha o nome do serviço e/ou as TAGS",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


    }

    //função de validacao
    fun validate(): Boolean {
        return editTags.text.toString() != "" &&
                editService.text.toString() != ""

    }
    fun validateConection(): Boolean{
        val cm = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = cm.activeNetworkInfo
        if (networkInfo != null && networkInfo.isConnected){
            return true
        }else{
            progressBar5.visibility = View.GONE
            Toast.makeText(this, "Você não possui conexão com a internet", Toast.LENGTH_SHORT).show()
            return false
        }

    }


}
