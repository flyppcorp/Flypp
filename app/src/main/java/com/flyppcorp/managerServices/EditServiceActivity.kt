package com.flyppcorp.managerServices

import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
import kotlinx.android.synthetic.main.activity_edit_service.editCep
import kotlinx.android.synthetic.main.activity_edit_service.editCidadeService
import kotlinx.android.synthetic.main.activity_edit_service.editDescCurta
import kotlinx.android.synthetic.main.activity_edit_service.editDescDetalhada
import kotlinx.android.synthetic.main.activity_edit_service.editNumService
import kotlinx.android.synthetic.main.activity_edit_service.editObservacao
import kotlinx.android.synthetic.main.activity_edit_service.editPreco
import kotlinx.android.synthetic.main.activity_edit_service.editRuaService
import kotlinx.android.synthetic.main.activity_edit_service.editService
import kotlinx.android.synthetic.main.activity_edit_service.editTags
import kotlinx.android.synthetic.main.activity_edit_service.imgService
import kotlinx.android.synthetic.main.activity_edit_service.spinnerPreparo
import kotlinx.android.synthetic.main.activity_edit_service.spinnerResposta
import kotlinx.android.synthetic.main.dialog_fr2.view.*
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class EditServiceActivity : AppCompatActivity() {

    private var mService: Servicos? = null
    private var mUri: Uri? = null
    private lateinit var mStorage: FirebaseStorage
    private lateinit var mFirestore: FirebaseFirestore
    private lateinit var mServiceAtributes: Servicos
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mFirestoreService: FirestoreService
    private var mGetService: Servicos? = null
    private var mProfile: User? = null
    private lateinit var mCity: SharedFilter
    private lateinit var mProgress: ProgressDialog
    private var horario1: String? = null
    private var horario2: String? = null
    private lateinit var mExp: SharedFilter
    private lateinit var mDiasExpediente: ArrayList<String>
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
        mExp = SharedFilter(this)
        mDiasExpediente = arrayListOf()
        btnSelectPhotoService.setOnClickListener {
            handleSelectPhoto()
        }
        btnUpdateService.setOnClickListener {
            fetchUp()
        }
        btnVoltarService.setOnClickListener {
            finish()
        }
        fetch()
        fetchCategoria()
        handleDialog()


    }

    private fun handleDialog() {
        btnExpedienteEdit.setOnClickListener {
            val vd = layoutInflater.inflate(R.layout.dialog_fr2, null)
            val alert = AlertDialog.Builder(this)
            alert.setView(vd)
            if (horario1 != null) {
                vd.btnInicio.text = horario1
            } else {
                vd.btnInicio.text = mService?.horario?.substringBefore("-")
            }
            if (horario2 != null) {
                vd.btnFim.text = horario2
            } else {
                vd.btnFim.text = mService?.horario?.substringAfter("-")
            }
            vd.btnInicio.setOnClickListener {

                val calendar = Calendar.getInstance()
                val timeSetListener =
                    TimePickerDialog.OnTimeSetListener { timepicker: TimePicker?, hourOfDay: Int, minute: Int ->
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)
                        horario1 = SimpleDateFormat("HH:mm").format(calendar.time)
                        vd.btnInicio.text = horario1


                    }
                TimePickerDialog(
                    this,
                    timeSetListener,
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            }
            vd.btnFim.setOnClickListener {

                val calendar = Calendar.getInstance()
                val timeSetListener =
                    TimePickerDialog.OnTimeSetListener { timepicker: TimePicker?, hourOfDay: Int, minute: Int ->
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)
                        horario2 = SimpleDateFormat("HH:mm").format(calendar.time)
                        vd.btnFim.text = horario2


                    }
                TimePickerDialog(
                    this,
                    timeSetListener,
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            }
            val keyExp = mService?.dias.toString()
            if (keyExp.contains("1")) {
                vd.checkBox1domingo.isChecked = true
            }
            if (keyExp.contains("2")) {
                vd.checkBox2segunda.isChecked = true
            }
            if (keyExp.contains("3")) {
                vd.checkBox3terca.isChecked = true
            }
            if (keyExp.contains("4")) {
                vd.checkBox4quarta.isChecked = true
            }
            if (keyExp.contains("5")) {
                vd.checkBox5quinta.isChecked = true
            }
            if (keyExp.contains("6")) {
                vd.checkBox6sexta.isChecked = true
            }
            if (keyExp.contains("7")) {
                vd.checkBox7sabado.isChecked = true
            }


            /*if (horario1 != null){
                vd.btnInicio.text = horario1
            }
            if (horario2 != null){
                vd.btnFim.text = horario2
            }*/


            alert.setPositiveButton("Salvar", { dialog, which ->
                if (vd.checkBox1domingo.isChecked) {
                    mDiasExpediente.add("1")
                } else {
                    mDiasExpediente.remove("1")
                }
                if (vd.checkBox2segunda.isChecked) {
                    mDiasExpediente.add("2")
                } else {
                    mDiasExpediente.remove("2")
                }
                if (vd.checkBox3terca.isChecked) {
                    mDiasExpediente.add("3")
                } else {
                    mDiasExpediente.remove("3")
                }
                if (vd.checkBox4quarta.isChecked) {
                    mDiasExpediente.add("4")
                } else {
                    mDiasExpediente.remove("4")
                }
                if (vd.checkBox5quinta.isChecked) {
                    mDiasExpediente.add("5")
                } else {
                    mDiasExpediente.remove("5")
                }
                if (vd.checkBox6sexta.isChecked) {
                    mDiasExpediente.add("6")
                } else {
                    mDiasExpediente.remove("6")
                }
                if (vd.checkBox7sabado.isChecked) {
                    mDiasExpediente.add("7")
                } else {
                    mDiasExpediente.remove("7")
                }
                mExp.saveFilter(Constants.KEY.EXPEDIENTE, mDiasExpediente.toString())
                mDiasExpediente.clear()
                horario1?.replace("null", mService?.horario!!.substringBefore("-"))
                horario2?.replace("null", mService?.horario!!.substringAfter("-"))
                btnExpedienteEdit.text = "Alterar Expediente"

            })
            val ad = alert.create()
            ad.show()
        }
    }

    private fun fetchCategoria() {
        val categoria = listOf<String>(
            mService?.categoria.toString(),
            "Lanches",
            "Pizza",
            "Burguer",
            "Carnes",
            "Frangos e Aves",
            "Peixes",
            "Bolos",
            "Porções",
            "Marmitex",
            "Massas",
            "Salgados",
            "Doces",
            "Saudáveis",
            "Veganos",
            "Bebidas",
            "Nordestina",
            "Japonês",
            "Churrasco",
            "Outros",
            "Todos"
        )
        spinnerCategoriaEdit.adapter =
            ArrayAdapter(this, android.R.layout.simple_list_item_1, categoria)

        val resposta = listOf<String>(
            mService?.tempoResposta.toString(),
            "1–5 minutos",
            "10–20 minutos",
            "30–40 minutos",
            "50–60 minutos"
        )
        spinnerResposta.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, resposta)

        val entrega = listOf<String>(
            mService?.tempoEntrega.toString(),
            "5 min",
            "10 min",
            "15 min",
            "30 min",
            "45 min",
            "60 min",
            "90 min",
            "120 min",
            "Sob encomenda"
        )
        spinnerPreparo.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, entrega)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.KEY.REQUEST_CODE) {
            mUri = data?.data

            if (mUri != null) {
                Picasso.get().load(mUri.toString()).resize(300, 300).centerCrop().into(imgService)
                btnSelectPhotoService.alpha = 0f
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

                        if (serviceItem.nome != null) {
                            editEmpresaUpdate.setText(serviceItem.nome)
                        }
                        if (serviceItem.delivery) {
                            simDeliveryEdit.isChecked = true
                        } else {
                            naoDeliveryEdit.isChecked = true
                        }
                        editService.setText(serviceItem.nomeService)
                        editDescCurta.setText(serviceItem.shortDesc)
                        editDescDetalhada.setText(serviceItem.longDesc)
                        editPreco.setText(serviceItem.preco.toString().replace(".", ","))
                        editCep.setText(serviceItem.cep)
                        editEstadoEdit.setText(serviceItem.estado)
                        editCidadeService.setText(serviceItem.cidade)
                        editRuaService.setText(serviceItem.rua)
                        EditBairroService.setText(serviceItem.bairro)
                        editNumService.setText(serviceItem.numero)
                        editTags.setText(serviceItem.tagsStr)
                        if (serviceItem.sabor != null) {
                            editObservacao.setText(serviceItem.sabor)
                        }

                        /*if(serviceItem.nacional){
                            nacionalEdit.isChecked = true
                        }else if (!serviceItem.nacional){
                            localEdit.isChecked = true
                        }*/

                    }
                }
            }

    }


    //------------------------------------------------------------------------------------------------------------------------
    private fun handleSaveService() {
        Toast.makeText(this, "Salvando atualizações", Toast.LENGTH_SHORT).show()
        if (validateConection()) {
            if (validate()) {
                mFirestoreService.mDialog.setCancelable(false)
                mFirestoreService.mDialog.show()
                mFirestoreService.mDialog.setContentView(R.layout.progress)
                mFirestoreService.mDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
                //definindo valores para a classe servico
                mGetService?.let {

                    val filename = mService?.serviceId
                    val ref = mStorage.getReference("image/${filename}")
                    mServiceAtributes.totalAvaliacao = mGetService!!.totalAvaliacao
                    mServiceAtributes.totalServicos = mGetService!!.totalServicos
                    mServiceAtributes.avaliacao = mGetService!!.avaliacao
                    mServiceAtributes.favoritos = mGetService!!.favoritos
                    mServiceAtributes.uid = mService?.uid
                    mServiceAtributes.visible = mService!!.visible
                    mServiceAtributes.uidProfile[mService?.uid.toString()] = true
                    mServiceAtributes.tempoEntrega = spinnerPreparo.selectedItem.toString()


                    ////////////////////////////////////////////////////////////
                    mServiceAtributes.urlProfile = mGetService?.urlProfile
                    if (mCity.getFilter(Constants.KEY.CITY_NAME) != "") mServiceAtributes.cityName =
                        mCity.getFilter(Constants.KEY.CITY_NAME)
                    else mServiceAtributes.cityName = editCidadeService.text.toString()
                    mServiceAtributes.nome = editEmpresaUpdate.text.toString()
                    mServiceAtributes.comments = mGetService!!.comments
                    ///////////////////////////////////////////////////////////

                    //expediente
                    mServiceAtributes.dias = mExp.getFilter(Constants.KEY.EXPEDIENTE)
                    if (horario1 == null) {
                        horario1 = mService?.horario?.substringBefore("-")
                    }
                    if (horario2 == null) {
                        horario2 = mService?.horario?.substringAfter("-")
                    }

                    mServiceAtributes.horario = "${horario1}-${horario2}"

                    //expediente

                    mServiceAtributes.nomeService = editService.text.toString()
                    mServiceAtributes.shortDesc = editDescCurta.text.toString()
                    mServiceAtributes.longDesc = editDescDetalhada.text.toString()
                    mServiceAtributes.preco = editPreco.text.toString().replace(",", ".").toFloat()
                    if (editObservacao.text.toString() != "") {
                        mServiceAtributes.sabor = editObservacao.text.toString()
                    }

                    if (simDeliveryEdit.isChecked) {
                        mServiceAtributes.delivery = true
                    } else if (naoDeliveryEdit.isChecked) {
                        mServiceAtributes.delivery = false
                    }

                    mServiceAtributes.categoria = spinnerCategoriaEdit.selectedItem.toString()

                    mServiceAtributes.tempoResposta = spinnerResposta?.selectedItem.toString()
                    mServiceAtributes.cep = editCep.text.toString()
                    mServiceAtributes.estado = editEstadoEdit.text.toString()
                    mServiceAtributes.cidade = editCidadeService.text.toString().trimEnd()
                    mServiceAtributes.bairro = EditBairroService.text.toString()
                    mServiceAtributes.rua = editRuaService.text.toString()
                    mServiceAtributes.numero = editNumService.text.toString()
                    mServiceAtributes.serviceId = mService?.serviceId
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
                        var bitmap: Bitmap? = null
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(contentResolver, it)
                        } catch (e: Exception) {
                            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
                        }
                        val bytes = ByteArrayOutputStream()
                        bitmap?.compress(Bitmap.CompressFormat.JPEG, 50, bytes)
                        val fileInBytes = bytes.toByteArray()
                        ref.putBytes(fileInBytes)
                            .addOnSuccessListener {
                                ref.downloadUrl
                                    .addOnSuccessListener {
                                        mServiceAtributes.urlService = it.toString()

                                        //salvando no db caso haja uma url

                                        mProgress.setCancelable(false)
                                        mProgress.show()
                                        mProgress.setContentView(R.layout.progress)
                                        mProgress.window?.setBackgroundDrawableResource(android.R.color.transparent)
                                        mFirestore.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
                                            .document(mService?.serviceId.toString())
                                            .set(mServiceAtributes)
                                            .addOnFailureListener {
                                                mProgress.hide()
                                            }
                                        finish()
                                    }
                            }

                    }
                    //savando no db caso não haja uma url
                    if (mUri == null) {
                        mProgress.setCancelable(false)
                        mProgress.show()
                        mProgress.setContentView(R.layout.progress)
                        mProgress.window?.setBackgroundDrawableResource(android.R.color.transparent)
                        if (mGetService?.urlService != null) mServiceAtributes.urlService =
                            mGetService?.urlService

                        mFirestore.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
                            .document(mService?.serviceId.toString())
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