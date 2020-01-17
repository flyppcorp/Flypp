package com.flyppcorp.managerServices

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.flyppcorp.Helper.SharedFilter
import com.flyppcorp.atributesClass.Servicos
import com.flyppcorp.atributesClass.User
import com.flyppcorp.constants.Constants
import com.flyppcorp.firebase_classes.FirestoreService
import com.flyppcorp.flypp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_add.*
import kotlinx.android.synthetic.main.activity_edit_service.*
import kotlinx.android.synthetic.main.activity_edit_service.EditBairroService
import kotlinx.android.synthetic.main.activity_edit_service.btnSelectPhotoService
import kotlinx.android.synthetic.main.activity_edit_service.btnSelectPhotoService2
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
import kotlinx.android.synthetic.main.activity_edit_service.imgService2
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class EditServiceActivity : AppCompatActivity() {

    private var mService: Servicos? = null
    private var mUri: Uri? = null
    private var mUri2: Uri? = null
    private lateinit var mStorage: FirebaseStorage
    private lateinit var mFirestore: FirebaseFirestore
    private lateinit var mServiceAtributes: Servicos
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mFirestoreService: FirestoreService
    private var mGetService: Servicos? = null
    private var mProfile: User? = null
    private lateinit var mCity: SharedFilter
    private lateinit var mProgress: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_service)
        mService = intent.extras?.getParcelable(Constants.KEY.SERVICE_KEY)
        mFirestore = FirebaseFirestore.getInstance()
        mStorage = FirebaseStorage.getInstance()
        mServiceAtributes = Servicos()
        mAuth = FirebaseAuth.getInstance()
        mProgress = ProgressDialog(this)
        mFirestoreService = FirestoreService(this)
        mCity = SharedFilter(this)
        btnSelectPhotoService.setOnClickListener {
            handleSelectPhoto()
        }
        btnSelectPhotoService2.setOnClickListener {
            handleSelect2()
        }
        btnUpdateService.setOnClickListener {
            fetchUp()
        }
        btnVoltarService.setOnClickListener {
            finish()
        }
        fetch()


    }


    private fun fetchUp() {
        mFirestore.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
            .document(mService!!.serviceId!!)
            .get()
            .addOnSuccessListener {
                mGetService = it.toObject(Servicos::class.java)
                handleSaveService()
            }
    }

    private fun handleSelectPhoto() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, Constants.KEY.REQUEST_CODE)
    }
    private fun handleSelect2() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, Constants.KEY.REQUEST_CODE2)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.KEY.REQUEST_CODE) {
            mUri = data?.data

            if (mUri != null) {
                Picasso.get().load(mUri.toString()).resize(300, 300).centerCrop().into(imgService)
                btnSelectPhotoService.alpha = 0f
            }


        }
        if (requestCode == 2) {
            mUri2 = data?.data

            if (mUri2 != null) {
                Picasso.get().load(mUri2.toString()).resize(300,300).centerCrop().into(imgService2)
                btnSelectPhotoService2.alpha = 0f
            }
        }
    }

    private fun fetch() {
        mFirestore.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
            .whereEqualTo("serviceId", mService!!.serviceId)
            .addSnapshotListener { snapshot, exception ->
                snapshot?.let {
                    for (doc in snapshot) {
                        val serviceItem = doc.toObject(Servicos::class.java)
                        if (serviceItem.urlService != null) {
                            Picasso.get().load(serviceItem.urlService)
                                .placeholder(R.drawable.photo_work).resize(150, 150).centerCrop()
                                .into(imgService)
                            btnSelectPhotoService.alpha = 0f
                        }
                        if (mUri != null) {
                            Picasso.get().load(mUri.toString()).placeholder(R.drawable.photo_work)
                                .resize(150, 150).centerCrop().into(imgService)
                            btnSelectPhotoService.alpha = 0f
                        }

                        if (serviceItem.urlService2 != null) {
                            Picasso.get().load(serviceItem.urlService2)
                                .placeholder(R.drawable.photo_work).resize(150, 150).centerCrop()
                                .into(imgService2)
                            btnSelectPhotoService2.alpha = 0f
                        }
                        if (mUri2 != null) {
                            Picasso.get().load(mUri.toString()).placeholder(R.drawable.photo_work)
                                .resize(150, 150).centerCrop().into(imgService2)
                            btnSelectPhotoService2.alpha = 0f
                        }
                        if (serviceItem.nome != null) {
                            editEmpresaUpdate.setText(serviceItem.nome)
                        }
                        editService.setText(serviceItem.nomeService)
                        editDescCurta.setText(serviceItem.shortDesc)
                        editDescDetalhada.setText(serviceItem.longDesc)
                        editPreco.setText(serviceItem.preco.toString().replace(".", ","))
                        editDuracaoEdit.setText(serviceItem.tipoCobranca)
                        if (serviceItem.tempoResposta != null) respondeEm.setText(serviceItem.tempoResposta)
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

        if (validateConection()) {
            if (validate()) {
                mFirestoreService.mDialog.setCancelable(false)
                mFirestoreService.mDialog.show()
                //definindo valores para a classe servico
                mGetService?.let {
                    val filename = SimpleDateFormat("yMdMs", Locale.getDefault()).format(Date())
                    val ref = mStorage.getReference("image/${filename}")
                    mServiceAtributes.totalAvaliacao = mGetService!!.totalAvaliacao
                    mServiceAtributes.totalServicos = mGetService!!.totalServicos
                    mServiceAtributes.avaliacao = mGetService!!.avaliacao
                    mServiceAtributes.favoritos = mGetService!!.favoritos
                    mServiceAtributes.uid = mService?.uid
                    mServiceAtributes.visible = mService!!.visible
                    mServiceAtributes.uidProfile[mService?.uid.toString()] = true

                    ////////////////////////////////////////////////////////////
                    mServiceAtributes.urlProfile = mGetService?.urlProfile
                    if (mCity.getFilter(Constants.KEY.CITY_NAME) != "") mServiceAtributes.cityName =
                        mCity.getFilter(Constants.KEY.CITY_NAME)
                    else mServiceAtributes.cityName = editCidadeService.text.toString()
                    mServiceAtributes.nome = editEmpresaUpdate.text.toString()
                    mServiceAtributes.ddd = mGetService?.ddd
                    mServiceAtributes.telefone = mGetService?.telefone
                    mServiceAtributes.comments = mGetService!!.comments
                            ///////////////////////////////////////////////////////////

                    mServiceAtributes.nomeService = editService.text.toString()
                    mServiceAtributes.shortDesc = editDescCurta.text.toString()
                    mServiceAtributes.longDesc = editDescDetalhada.text.toString()
                    mServiceAtributes.preco = editPreco.text.toString().replace(",", ".").toFloat()
                    mServiceAtributes.tipoCobranca = editDuracaoEdit.text.toString()
                    mServiceAtributes.qualidadesDiferenciais =
                        editQualidadesDiferenciais.text.toString()
                    mServiceAtributes.tempoResposta = respondeEm.text.toString()
                    mServiceAtributes.cep = editCep.text.toString()
                    mServiceAtributes.estado = editEstadoEdit.text.toString()
                    mServiceAtributes.cidade = editCidadeService.text.toString().trimEnd()
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
                        tags[tag.trimStart().trimEnd()] = true
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

                                        mProgress.show()
                                        mFirestore.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
                                            .document(mService!!.serviceId!!)
                                            .set(mServiceAtributes)
                                            .addOnFailureListener {
                                                mProgress.hide()
                                                secondPhoto(mService!!.serviceId!!, filename)
                                            }
                                        finish()
                                    }
                            }

                    }
                    //savando no db caso não haja uma url
                    if (mUri == null) {
                        mProgress.show()
                        val extras = intent.extras
                        if (extras != null) mServiceAtributes.urlService = extras.getString("url")
                        if (extras != null && extras.getString("url2") != ""){
                            secondPhoto(mService!!.serviceId!!, filename)
                        }

                        mFirestore.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
                            .document(mService!!.serviceId!!)
                            .set(mServiceAtributes)
                            .addOnFailureListener {
                                mProgress.hide()
                            }
                        finish()

                    }
                }

            } else {
                Toast.makeText(
                    this,
                    "Verifique se os campos nome empresa/negócio, nome serviço, preço e tags estão preenchidos",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


    }

    private fun secondPhoto( document: String, filename : String){
        val ref = mStorage.getReference("image/${filename}2")
        mUri2?.let {
            ref.putFile(it)
                .addOnSuccessListener {
                    ref.downloadUrl
                        .addOnSuccessListener {
                            mFirestore.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
                                .document(document)
                                .update("urlService2", it.toString())
                        }
                }
        }

    }

    //função de validacao
    fun validate(): Boolean {
        return editTags.text.toString() != "" &&
                editService.text.toString() != "" &&
                editPreco.text.toString() != "" &&
                editEmpresaUpdate.text.toString() != ""

    }

    fun validateConection(): Boolean {
        val cm = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = cm.activeNetworkInfo
        if (networkInfo != null && networkInfo.isConnected) {
            return true
        } else {

            Toast.makeText(this, "Você não possui conexão com a internet", Toast.LENGTH_SHORT)
                .show()
            return false
        }

    }


}
