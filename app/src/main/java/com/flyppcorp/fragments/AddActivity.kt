package com.flyppcorp.fragments

import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.flyppcorp.Helper.Contact
import com.flyppcorp.Helper.SharedFilter
import com.flyppcorp.atributesClass.DashBoard
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
import kotlinx.android.synthetic.main.activity_add.EditBairroService
import kotlinx.android.synthetic.main.activity_add.btnSelectPhotoService
import kotlinx.android.synthetic.main.activity_add.editCep
import kotlinx.android.synthetic.main.activity_add.editCidadeService
import kotlinx.android.synthetic.main.activity_add.editDescCurta
import kotlinx.android.synthetic.main.activity_add.editDescDetalhada
import kotlinx.android.synthetic.main.activity_add.editNumService
import kotlinx.android.synthetic.main.activity_add.editObservacao
import kotlinx.android.synthetic.main.activity_add.editPreco
import kotlinx.android.synthetic.main.activity_add.editRuaService
import kotlinx.android.synthetic.main.activity_add.editService
import kotlinx.android.synthetic.main.activity_add.editTags
import kotlinx.android.synthetic.main.activity_add.imgService
import kotlinx.android.synthetic.main.activity_add.spinnerPreparo
import kotlinx.android.synthetic.main.activity_add.spinnerResposta
import kotlinx.android.synthetic.main.activity_confirm_service.*
import kotlinx.android.synthetic.main.activity_edit_service.*
import kotlinx.android.synthetic.main.dialog_fr2.*
import kotlinx.android.synthetic.main.dialog_fr2.view.*
import kotlinx.android.synthetic.main.dialog_fr2.view.checkBox1domingo
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class AddActivity : AppCompatActivity() {
    //declaração das variaveis e objetos
    private var mUri: Uri? = null
    private var mUri2: Uri? = null
    private lateinit var mFirestoreService: FirestoreService
    private lateinit var mStorage: FirebaseStorage
    private lateinit var mServiceAtributes: Servicos
    private lateinit var mFirestore: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mUser: User
    private lateinit var mCity: SharedFilter
    private lateinit var mExp: SharedFilter
    private var horario1: String? = null
    private var horario2: String? = null
    private lateinit var mDiasExpediente: ArrayList<String>
    //fim da inicialização

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)

        //iniciando objetos
        mFirestoreService = FirestoreService(this)
        mStorage = FirebaseStorage.getInstance()
        mFirestore = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()
        mServiceAtributes = Servicos()
        mUser = User()
        mCity = SharedFilter(this)
        mDiasExpediente = arrayListOf()
        mExp = SharedFilter(this)
        //fim
        val tb = findViewById<androidx.appcompat.widget.Toolbar>(R.id.tb_add)
        tb?.title = ""
        setSupportActionBar(tb)
        btnVoltarTbadd.setOnClickListener {
            finish()
        }
        txtTitleadd.text = "Adicionar produto"

        //ações de click
        btnSelectPhotoService.setOnClickListener { handleSelect() }
        btnSaveService.setOnClickListener { handleSaveService() }
        btnExpediente.setOnClickListener {
            handleDialog()
        }
        //fim

        //chamando funcao que preenche dadas existentes da colecao user na service
        getInfo()
    }

    private fun handleDialog() {
        val vd = layoutInflater.inflate(R.layout.dialog_fr2, null)
        val alert = AlertDialog.Builder(this)
        alert.setView(vd)
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
        val keyExp = mExp.getFilter(Constants.KEY.EXPEDIENTE)
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

        if (horario1 != null) {
            vd.btnInicio.text = horario1
        }
        if (horario2 != null) {
            vd.btnFim.text = horario2
        }


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
            btnExpediente.text = "Alterar Expediente"

        })
        val ad = alert.create()
        ad.show()
    }


    //função que preenche os campos com informações do usuário
    private fun getInfo() {
        val uid = mAuth.currentUser?.uid
        mFirestore.collection(Constants.COLLECTIONS.USER_COLLECTION)
            .whereEqualTo("uid", "${uid}")
            .addSnapshotListener { snapshot, exception ->
                snapshot?.let {
                    for (doc in snapshot) {
                        mUser = doc.toObject(User::class.java)
                        editCep.setText(mUser.cep)
                        editCidadeService.setText(mUser.cidade)
                        editNumService.setText(mUser.numero)
                        editCidadeService.setText(mUser.cidade)
                        editRuaService.setText(mUser.rua)
                        EditBairroService.setText(mUser.bairro)
                        editEstadosAdd.setText(mUser.estado)
                        if (mUser.nomeEmpresa != null) {
                            editEmpresaAdd.setText(mUser.nomeEmpresa)
                        }
                        if (!mUser.autorized){
                            val alertDialog = AlertDialog.Builder(this)
                                .setCancelable(false)
                                .setTitle("Você precisa de autorização para adicionar seus produtos")
                                .setMessage("Entre em contato conosco para começar fazer parte da família Flypp." +
                                        "\nBasta clicar no botão abaixo")
                                .setNegativeButton("Depois", {dialogInterface, i ->
                                    finish()
                                })
                                .setPositiveButton("Quero fazer parte", {dialogInterface, i ->
                                     val intent = Intent(this, Contact::class.java)
                                    startActivity(intent)
                                    finish()

                                })
                                .show()

                        }

                    }
                }
            }

    }


    //funcoes que abre a galeria
    private fun handleSelect() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, Constants.KEY.REQUEST_CODE)

    }

    private fun handleSelect2() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, Constants.KEY.REQUEST_CODE2)

    }
    //fim

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

    //funcao que salva no db firestore
    private fun handleSaveService() {
        if (validateConection()) {
            if (validate()) {
                mFirestoreService.mDialog.setCancelable(false)
                mFirestoreService.mDialog.show()
                mFirestoreService.mDialog.setContentView(R.layout.progress)
                mFirestoreService.mDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
                //definindo valores para a classe servico
                val serviceId = mUser.uid + UUID.randomUUID().toString()
                //pegando o nome do arquivo que vai ser salvo no firestorage
                val filename = serviceId
                val ref = mStorage.getReference("image/${filename}")
                //fim
                //nome empresa
                mServiceAtributes.nome = editEmpresaAdd.text.toString()
                //uid
                mServiceAtributes.uid = mUser.uid
                //uid profile para atualizações futuras
                mServiceAtributes.uidProfile[mUser.uid.toString()] = true

                //foto
                mServiceAtributes.urlProfile = mUser.url
                //nome da cidade que é encarregada por mostrar para as pessoas (capturada do gps)
                if (mCity.getFilter(Constants.KEY.CITY_NAME) != "") mServiceAtributes.cityName =
                    mCity.getFilter(Constants.KEY.CITY_NAME)
                else mServiceAtributes.cityName = editCidadeService.text.toString()
                //fim

                //desc service
                mServiceAtributes.nomeService = editService.text.toString()
                mServiceAtributes.shortDesc = editDescCurta.text.toString()
                mServiceAtributes.longDesc = editDescDetalhada.text.toString()
                mServiceAtributes.preco = editPreco.text.toString().toFloat()
                mServiceAtributes.taxaEntrega = editTaxaAdd.text.toString().toDouble()

                if (simDelivery.isChecked) {
                    mServiceAtributes.delivery = true
                } else if (NaoDelivery.isChecked) {
                    mServiceAtributes.delivery = false
                }

                mServiceAtributes.tempoResposta = spinnerResposta.selectedItem.toString()
                mServiceAtributes.tempoEntrega = spinnerPreparo.selectedItem.toString()
                mServiceAtributes.categoria = spinnerCategoria.selectedItem.toString()
                if (editObservacao.text.toString() != "") {
                    mServiceAtributes.sabor = editObservacao.text.toString()
                }
                //expediente
                mServiceAtributes.dias = mExp.getFilter(Constants.KEY.EXPEDIENTE)
                if (horario1 == null) {
                    horario1 = "06:00"
                }
                if (horario2 == null) {
                    horario2 = "00:00"
                }
                mServiceAtributes.horario = "${horario1}-${horario2}"
                //expediente
                //endereço
                mServiceAtributes.cep = editCep.text.toString()
                mServiceAtributes.estado = editEstadosAdd.text.toString()
                mServiceAtributes.cidade = editCidadeService.text.toString().trimEnd()
                mServiceAtributes.bairro = EditBairroService.text.toString()
                mServiceAtributes.rua = editRuaService.text.toString()
                mServiceAtributes.numero = editNumService.text.toString()

                //service id, ele tem o mesmo nome da referencia do documento e é usado para acessar produtos mais adiante

                mServiceAtributes.serviceId = serviceId
                //tags para pesquisa
                val tagInput = editTags.text.toString().toLowerCase()
                mServiceAtributes.tagsStr = tagInput
                val tagArray: Array<String> = tagInput.split(",").toTypedArray()
                val tags: MutableMap<String, Boolean> = HashMap()

                for (tag in tagArray) {
                    tags[tag.trimStart().trimEnd()] = true
                }
                if (tags.isEmpty()) tags[""] = false
                mServiceAtributes.tags = tags
                //fim tags

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
                                    mFirestoreService.servicos(mServiceAtributes, serviceId)
                                    updateProfile()
                                    dashBoard()
                                }
                        }

                }
                //savando no db caso não haja uma url
                if (mUri == null) {
                    mFirestoreService.servicos(mServiceAtributes, serviceId)
                    updateProfile()
                    dashBoard()

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

    private fun dashBoard() {
        val tsDoc =
            mFirestore.collection(Constants.DASHBOARD_SERVICE.DASHBOARD_COLLECTION).document(
                Constants.DASHBOARD_SERVICE.DASHBOARD_DOCUMENT
            )
        mFirestore.runTransaction {
            val content = it.get(tsDoc).toObject(DashBoard::class.java)
            content!!.newServices = content.newServices + 1
            it.set(tsDoc, content)
        }
    }

    //função que atualiza nome da empresa no perfil, caso haja algum
    private fun updateProfile() {
        mFirestore.collection(Constants.COLLECTIONS.USER_COLLECTION)
            .document(mAuth.currentUser?.uid.toString())
            .update("nomeEmpresa", editEmpresaAdd.text.toString())

    }


    //função de validacao
    fun validate(): Boolean {
        return editTags.text.toString() != "" &&
                editService.text.toString() != "" &&
                editPreco.text.toString() != "" &&
                editEmpresaAdd.text.toString() != ""

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