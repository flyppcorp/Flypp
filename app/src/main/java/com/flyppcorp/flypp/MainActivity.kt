package com.flyppcorp.flypp

import android.Manifest
import android.content.ContentProviderClient
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.flyppcorp.Helper.Connection
import com.flyppcorp.Helper.LifeCyclerApplication
import com.flyppcorp.Helper.SharedFilter
import com.flyppcorp.constants.Constants
import com.flyppcorp.firebase_classes.FirestoreContract
import com.flyppcorp.fragments.*
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.util.*


class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    private lateinit var mSharedFilter: SharedFilter
    private lateinit var mFirestoreContract: FirestoreContract
    private lateinit var mConnection: Connection
    private lateinit var client: FusedLocationProviderClient
    //private lateinit var mCity : SharedFilter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mSharedFilter = SharedFilter(this)
        mConnection = Connection(this)
        //acao do bottomNav

        mFirestoreContract = FirestoreContract(this)
        mFirestoreContract.mIntertial = InterstitialAd(applicationContext)
        MobileAds.initialize(this)
        mFirestoreContract.mIntertial.adUnitId = getString(R.string.ads_intertitial_id)
        mFirestoreContract.mIntertial.loadAd(AdRequest.Builder().build())
        client = LocationServices.getFusedLocationProviderClient(this)

        bottom_nav.setOnNavigationItemSelectedListener(this)
        //item que ja esta selecionado
        bottom_nav.selectedItemId = R.id.homeFrag
        //configurações da toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbarMain)
        toolbar.title = ""
        setSupportActionBar(toolbar)

        val application: LifeCyclerApplication = application as LifeCyclerApplication
        getApplication().registerActivityLifecycleCallbacks(application)


        getToken()
        getPermissions()

    }


    private fun getPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                1
            )
        } else {
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
                        val geocoder = Geocoder(this@MainActivity, Locale.getDefault())
                        val adress: List<Address>? =
                            geocoder.getFromLocation(it.latitude, it.longitude, 1)
                        if (adress != null) {
                            if (adress!!.size > 0) {
                                for (adresses: Address in adress) {
                                    Toast.makeText(this, adresses.subAdminArea, Toast.LENGTH_SHORT)
                                        .show()
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
        }
    }

    private fun getToken() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        //val token = FirebaseInstanceId.getInstance().token
        if (uid != null) {
            FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener
                val token = it.result?.token
                FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS.USER_COLLECTION)
                    .document(uid)
                    .update("token", token)
            }
        }
    }


    //metodo que trata as opcoes da bottom navigation
    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.homeFrag -> {
                val homeFragment = HomeFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_view, homeFragment)
                    .commit()
                return true
            }
            R.id.searchFrag -> {
                val searchFragment = SearchFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_view, searchFragment)
                    .commit()
                return true
            }
            R.id.addFrag -> {
                val intent = Intent(this, AddActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.favFrag -> {
                val favFragment = FavFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_view, favFragment)
                    .commit()
                return true
            }
            R.id.contaFrag -> {
                val contaFragment = ContaFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_view, contaFragment)
                    .commit()
                return true
            }
        }
        return false
    }


}
