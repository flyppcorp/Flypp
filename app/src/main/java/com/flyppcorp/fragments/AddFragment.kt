package com.flyppcorp.fragments

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.flyppcorp.atributesClass.Servicos
import com.flyppcorp.atributesClass.User
import com.flyppcorp.constants.Constants
import com.flyppcorp.firebase_classes.FirestoreService
import com.flyppcorp.flypp.MainActivity
import com.flyppcorp.flypp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_create_profile.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_add.*
import kotlinx.android.synthetic.main.fragment_add.editCep
import kotlinx.android.synthetic.main.fragment_add.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class AddFragment : Fragment() {
    //declaração das variaveis e objetos
    private var mUri: Uri? = null
    private lateinit var mFirestoreService: FirestoreService
    private lateinit var mStorage: FirebaseStorage
    private lateinit var mServiceAtributes: Servicos
    private lateinit var mFirestore: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mUser: User

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //instancia de objetos
        mFirestoreService = FirestoreService(context!!)
        mStorage = FirebaseStorage.getInstance()
        mFirestore = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()
        mServiceAtributes = Servicos()
        mUser = User()

        //inflando a fragment
        val view = LayoutInflater.from(activity).inflate(R.layout.fragment_add, container, false)
        //usar a view para colocar acoes nos botoes
        view.btnSelectPhotoService.setOnClickListener { handleSelect() }
        view.btnSaveService.setOnClickListener {
            handleSaveService()
        if(validate()){
            //progressBar5.visibility = View.VISIBLE
            mFirestoreService.mDialog.show()
        }
            if (!validateConection()){
                return@setOnClickListener
            }
        }
        //chamando funcao que preenche dadas existentes da colecao user na service
        getInfo()
        return view

    }

    //funcao que preenche dadas existentes da colecao user na service
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
        //definindo valores para a classe servico
        val timestamp = SimpleDateFormat("aaaaMMdd", Locale("EUA")).format(Date())
        val ref = mStorage.getReference("/ServicesImages/${timestamp}")
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
        mServiceAtributes.qualidadesDiferenciais = editQualidadesDiferenciais.text.toString()
        mServiceAtributes.cep = editCep.text.toString()
        mServiceAtributes.estado = spinnerEstadoService.selectedItem.toString()
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
                            if (validate()) {
                                mFirestoreService.servicos(mServiceAtributes, serviceId)
                                totalServicosAtivos()
                                val frag = HomeFragment()
                                val ft = fragmentManager!!.beginTransaction()
                                ft.replace(R.id.main_view, frag, "HomeFragment")
                                ft.addToBackStack(null)
                                ft.commit()
                            } else {
                                Toast.makeText(
                                    context,
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
                mFirestoreService.servicos(mServiceAtributes, serviceId)
                totalServicosAtivos()
                val frag = HomeFragment()
                val ft = fragmentManager!!.beginTransaction()
                ft.replace(R.id.main_view, frag, "HomeFragment")
                ft.addToBackStack(null)
                ft.commit()

            } else {
                Toast.makeText(
                    context,
                    "Por favor, preencha o nome do serviço e/ou as TAGS",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


    }
    private fun totalServicosAtivos(){
        val tsDoc = mFirestore.collection(Constants.COLLECTIONS.USER_COLLECTION).document(mAuth.currentUser!!.uid)
        mFirestore.runTransaction {
            val content = it.get(tsDoc).toObject(User::class.java)
            content!!.totalServicosAtivos = content.totalServicosAtivos + 1
            it.set(tsDoc, content)
        }
    }

    //função de validacao
    fun validate(): Boolean {
        return editTags.text.toString() != "" &&
                editService.text.toString() != ""

    }
    fun validateConection(): Boolean{
        val cm = context!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = cm.activeNetworkInfo
        if (networkInfo != null && networkInfo.isConnected){
            return true
        }else{
            progressBar5.visibility = View.GONE
            Toast.makeText(context, "Você não possui conexão com a internet", Toast.LENGTH_SHORT).show()
            return false
        }

    }

}






