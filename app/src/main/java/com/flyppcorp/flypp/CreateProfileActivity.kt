package com.flyppcorp.flypp

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.flyppcorp.Helper.Connection
import com.flyppcorp.Helper.SharedFilter
import com.flyppcorp.atributesClass.User
import com.flyppcorp.constants.Constants
import com.flyppcorp.firebase_classes.FirestoreUser
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_create_profile.*
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

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
    private lateinit var client: FusedLocationProviderClient
    private lateinit var mCity: SharedFilter


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
        mCity = SharedFilter(this)
        client = LocationServices.getFusedLocationProviderClient(this)


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
        getInfo()
        getLocation()
    }

    private fun getLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val errorCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
            when (errorCode) {
                ConnectionResult.SERVICE_MISSING, ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED, ConnectionResult.SERVICE_DISABLED -> {
                    GoogleApiAvailability.getInstance().getErrorDialog(this, errorCode, 0) {
                        finish()
                    }.show()

                }
            }
            client.lastLocation.addOnSuccessListener {
                try {
                    if (it == null) {
                        return@addOnSuccessListener
                    } else {
                        val geocoder = Geocoder(this, Locale.getDefault())
                        val adress: List<Address>? =
                            geocoder.getFromLocation(it.latitude, it.longitude, 1)
                        if (adress != null) {
                            if (adress.size > 0) {
                                for (adresses: Address in adress) {
                                    if (adresses.subAdminArea != null) {
                                        mCity.saveFilter(Constants.KEY.CITY_NAME, adresses.subAdminArea)
                                        editCep.setText(adresses.postalCode.replace("-", ""))
                                        editCidade.setText(adresses.subAdminArea)

                                    }


                                }
                            }

                        } else {
                            return@addOnSuccessListener
                        }
                    }

                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

        } else {
            return
        }
    }

    private fun getInfo() {
        if (mAuth.currentUser?.displayName != null){
            editNomeUser.setText(mAuth.currentUser?.displayName)
        }
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

            if (mUri != null) {
                Picasso.get().load(mUri.toString()).resize(300, 300).centerCrop()
                    .into(photoSelected)
                selectPhoto.alpha = 0f
            }

        }
    }

    private fun handleProfile() {
        if (mConnect.validateConection()) {
            if (validate()) {
                mProgress.setCancelable(false)
                mProgress.show()
                val nome = editNomeUser.text.toString()
                val email = mAuth.currentUser?.email
                val ddd = editDDD.text.toString()
                val filename = mAuth.currentUser?.uid.toString()
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
                    var bitmap : Bitmap? = null
                    try{
                        bitmap = MediaStore.Images.Media.getBitmap(contentResolver, it)
                    }catch (e: Exception){
                        Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
                    }
                    val bytes = ByteArrayOutputStream()
                    bitmap?.compress(Bitmap.CompressFormat.JPEG, 25, bytes)
                    val fileInBytes = bytes.toByteArray()
                    ref.putBytes(fileInBytes)
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



