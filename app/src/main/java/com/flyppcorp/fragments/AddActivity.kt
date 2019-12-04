package com.flyppcorp.fragments

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.widget.Toast
import com.flyppcorp.atributesClass.Servicos
import com.flyppcorp.atributesClass.User
import com.flyppcorp.constants.Constants
import com.flyppcorp.firebase_classes.FirestoreService
import com.flyppcorp.flypp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_add.*
import kotlinx.android.synthetic.main.activity_add.editCep
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class AddActivity : AppCompatActivity() {
    //declaração das variaveis e objetos
    private var mUri: Uri? = null
    private lateinit var mFirestoreService: FirestoreService
    private lateinit var mStorage: FirebaseStorage
    private lateinit var mServiceAtributes: Servicos
    private lateinit var mFirestore: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mUser: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)

        mFirestoreService = FirestoreService(this)
        mStorage = FirebaseStorage.getInstance()
        mFirestore = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()
        mServiceAtributes = Servicos()
        mUser = User()
        supportActionBar!!.title = "Adicionar serviço"

        btnSelectPhotoService.setOnClickListener { handleSelect() }
        btnSaveService.setOnClickListener {
            handleSaveService()
        }
        //chamando funcao que preenche dadas existentes da colecao user na service
        getInfo()
    }

    fun getInfo() {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.KEY.REQUEST_CODE) {
            mUri = data?.data

            imgService.setImageURI(mUri)
            btnSelectPhotoService.alpha = 0f

        }
    }

    //funcao que salva no db firestore
    private fun handleSaveService() {
        if (validateConection()) {
            if (validate()) {

                //definindo valores para a classe servico
                val filename = SimpleDateFormat("yMdMs", Locale.getDefault()).format(Date())
                val ref = mStorage.getReference("image/${filename}")
                mServiceAtributes.nome = mUser.nome
                mServiceAtributes.uid = mUser.uid
                mServiceAtributes.uidProfile[mUser.uid.toString()] = true

                mServiceAtributes.urlProfile = mUser.url
                mServiceAtributes.ddd = mUser.ddd
                mServiceAtributes.telefone = mUser.telefone
                mServiceAtributes.nomeService = editService.text.toString()
                mServiceAtributes.shortDesc = editDescCurta.text.toString()
                mServiceAtributes.longDesc = editDescDetalhada.text.toString()
                mServiceAtributes.preco = editPreco.text.toString()
                mServiceAtributes.tipoCobranca = spinnerDuracaoService.selectedItem.toString()
                mServiceAtributes.qualidadesDiferenciais =
                    editQualidadesDiferenciais.text.toString()
                mServiceAtributes.cep = editCep.text.toString()
                mServiceAtributes.estado = editEstadosAdd.text.toString()
                mServiceAtributes.cidade = editCidadeService.text.toString()
                mServiceAtributes.bairro = EditBairroService.text.toString()
                mServiceAtributes.rua = editRuaService.text.toString()
                mServiceAtributes.numero = editNumService.text.toString()
                mServiceAtributes.email = mAuth.currentUser!!.email
                val serviceId = UUID.randomUUID().toString() + mUser.uid
                mServiceAtributes.serviceId = serviceId
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
                                    mFirestoreService.servicos(mServiceAtributes, serviceId)


                                }
                        }

                }
                //savando no db caso não haja uma url
                if (mUri == null) {
                    mFirestoreService.servicos(mServiceAtributes, serviceId)
                }

            } else {
                Toast.makeText(
                    this,
                    "Verifique se os campos nome serviço, preço e tags estão preenchidos",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }


    }


    //função de validacao
    fun validate(): Boolean {
        return editTags.text.toString() != "" &&
                editService.text.toString() != "" &&
                editPreco.text.toString() != ""

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
