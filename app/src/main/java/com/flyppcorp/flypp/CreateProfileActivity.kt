package com.flyppcorp.flypp

import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ProgressBar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.flyppcorp.Helper.Connection
import com.flyppcorp.atributesClass.User
import com.flyppcorp.constants.Constants
import com.flyppcorp.firebase_classes.FirestoreUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_create_profile.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.jar.Manifest

class CreateProfileActivity : AppCompatActivity() {
    //inicio tardio e declaração dos objetos
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mData: FirebaseFirestore
    private lateinit var mStorage: FirebaseStorage
    private var mUri: Uri? = null
    private lateinit var mFirestoreUser: FirestoreUser
    private lateinit var mUser: User
    private lateinit var mProgress: ProgressDialog
    private lateinit var mConnect: Connection


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_profile)
        //instancia dos objetos
        mAuth = FirebaseAuth.getInstance()
        mData = FirebaseFirestore.getInstance()
        mStorage = FirebaseStorage.getInstance()
        mFirestoreUser = FirestoreUser(this)
        mUser = User()
        mProgress = ProgressDialog(this)
        mConnect = Connection(this)


        //setlistener é uma funcao que tem os botoes de acao da activity
        setListener()

        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            1
        )
    }

    private fun setListener() {
        btnSalvarInfo.setOnClickListener {
            handleProfile()
        }
        selectPhoto.setOnClickListener {
            selectImg()
        }
    }

    private fun selectImg() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, Constants.KEY.REQUEST_CODE)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                1
            )
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.KEY.REQUEST_CODE) {
            mUri = data?.data

            photoSelected.setImageURI(mUri)
            selectPhoto.alpha = 0f
        }
    }

    private fun handleProfile() {
        if (mConnect.validateConection()){
            if (validate()){
                mProgress.setCancelable(false)
                mProgress.show()
                val nome = editNomeUser.text.toString()
                val email = mAuth.currentUser!!.email
                val ddd = editDDD.text.toString()
                val filename = SimpleDateFormat("ddMMaaaa", Locale("PT-BR")).format(Date())
                val ref = mStorage.getReference("image/${filename}")
                mUser.nome = nome
                mUser.ddd = ddd
                mUser.telefone = editPhone.text.toString()
                mUser.cep = editCep.text.toString()
                mUser.estado = spinnerEstado.selectedItem.toString()
                mUser.cidade = editCidade.text.toString().trimEnd()
                mUser.bairro = editBairro.text.toString()
                mUser.rua = editRua.text.toString()
                mUser.numero = editNumero.text.toString()
                mUser.servicosAtivos = 0
                mUser.totalServicosFinalizados = 0


                mUser.uid = mAuth.currentUser!!.uid
                mUser.email = email

                mUri?.let {
                    ref.putFile(it)
                        .addOnSuccessListener {
                            ref.downloadUrl
                                .addOnSuccessListener {

                                    mUser.url = it.toString()

                                    mFirestoreUser.saveUser(mUser)


                                }
                        }
                }

                if (mUri == null) {
                    mFirestoreUser.saveUser(mUser)
                }
            }
        }


    }

    private fun validate(): Boolean {
        val nome = editNomeUser.text.toString()
        if (nome.isEmpty()) {
            editNomeLayout.error = "O campo nome não pode ser vazio."
            return false
        } else {
            editNomeLayout.error = null
            return true
        }
    }


}



