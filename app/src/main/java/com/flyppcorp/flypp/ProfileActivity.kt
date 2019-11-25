package com.flyppcorp.flypp

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.flyppcorp.atributesClass.Servicos
import com.flyppcorp.atributesClass.User
import com.flyppcorp.constants.Constants
import com.flyppcorp.firebase_classes.FirestoreUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_profile.*
import java.text.SimpleDateFormat
import java.util.*


class ProfileActivity : AppCompatActivity() {
    private var mUser: User? = null
    private var mUri: Uri? = null
    private lateinit var mFirestore: FirebaseFirestore
    private lateinit var mUserInfo: User
    private lateinit var mFirestoreUser: FirestoreUser
    private lateinit var mStorage: FirebaseStorage
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mProgress: ProgressDialog
    private var servicosAtivos : Int? = null
    private var servicosFinalizados : Int? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        mUser = intent.extras?.getParcelable(Constants.KEY.PROFILE_KEY)
        mFirestore = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()
        mUserInfo = User()
        mFirestoreUser = FirestoreUser(this)
        mStorage = FirebaseStorage.getInstance()
        mProgress = ProgressDialog(this)

        selectPhotoProfile.setOnClickListener {
            handleSelectPhoto()
        }
        btnSalvarInfoProfile.setOnClickListener {
            handleUpdate()
        }
        btnCancel.setOnClickListener {
            finish()
        }
        fetchData()



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
            photoSelectedProfile.setImageURI(mUri)
            selectPhotoProfile.alpha = 0f

        }
    }

    private fun fetchData() {
        mFirestore.collection(Constants.COLLECTIONS.USER_COLLECTION)
            .whereEqualTo("uid", mUser!!.uid)
            .addSnapshotListener { snapshot, firestoreException ->
                for (doc in snapshot!!.documents) {
                    val userItem = doc.toObject(User::class.java)
                    if (userItem?.url != null && mUri == null) {
                        Picasso.get().load(userItem.url).into(photoSelectedProfile)
                        selectPhotoProfile.alpha = 0f
                    } else if (mUser!!.url == null) {
                        photoSelectedProfile.setImageResource(R.drawable.btn_select_photo_profile)
                    } else if (mUri != null) {
                        photoSelectedProfile.setImageURI(mUri)
                        selectPhotoProfile.alpha = 0f
                    }
                    editNomeUserProfile.setText(userItem!!.nome)
                    editDDDProfile.setText(userItem.ddd)
                    editPhoneProfile.setText(userItem.telefone)
                    editCepProfile.setText(userItem.cep)
                    editEstado.setText(userItem.estado)
                    editCidadeProfile.setText(userItem.cidade)
                    editBairroProfile.setText(userItem.bairro)
                    editRuaProfile.setText(userItem.rua)
                    editNumeroProfile.setText(userItem.numero)
                    servicosAtivos = userItem.servicosAtivos
                    servicosFinalizados = userItem.totalServicosFinalizados




                }

            }
    }

    private fun handleUpdate() {
        if (!validate()) {
            return
        } else {
            mProgress.show()
            val nome = editNomeUserProfile.text.toString()
            val ddd = editDDDProfile.text.toString()
            val phoneNumber = editPhoneProfile.text.toString()
            val filename = SimpleDateFormat("ddMMaaaa", Locale("PT-BR")).format(Date())
            val ref = mStorage.getReference("image/${filename}")
            mUserInfo.nome = nome
            mUserInfo.ddd = ddd
            mUserInfo.telefone = phoneNumber
            mUserInfo.cep = editCepProfile.text.toString()
            mUserInfo.estado = editEstado.text.toString()
            mUserInfo.cidade = editCidadeProfile.text.toString()
            mUserInfo.bairro = editBairroProfile.text.toString()
            mUserInfo.rua = editRuaProfile.text.toString()
            mUserInfo.numero = editNumeroProfile.text.toString()
            mUserInfo.email = mAuth.currentUser!!.email
            mUserInfo.uid = mAuth.currentUser!!.uid
            mUserInfo.servicosAtivos = servicosAtivos!!
            mUserInfo.totalServicosFinalizados = servicosFinalizados!!



            mUri?.let {
                ref.putFile(it)
                    .addOnSuccessListener {
                        ref.downloadUrl
                            .addOnSuccessListener {
                                mUserInfo.url = it.toString()
                                mFirestoreUser.saveUser(mUserInfo)

                            }
                    }
            }
        }
        if (mUri == null) {
            if (!validate()) {
                return
            } else {
                val nome = editNomeUserProfile.text.toString()
                if (mUser?.url != null) mUserInfo.url = mUser?.url
                mFirestoreUser.saveUser(mUserInfo)

            }

        }

    }





    private fun validate(): Boolean {
        val nome = editNomeUserProfile.text.toString()
        if (nome.isEmpty()) {
            editNomeUserProfile.error = "O campo nome não pode ser vazio."
            return false
        } else {
            editNomeUserProfile.error = null
            return true
        }
    }


}
