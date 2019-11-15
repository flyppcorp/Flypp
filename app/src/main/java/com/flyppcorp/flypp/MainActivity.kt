package com.flyppcorp.flypp

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.widget.ToolbarWidgetWrapper
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.flyppcorp.Helper.LifeCyclerApplication
import com.flyppcorp.Helper.SharedFilter
import com.flyppcorp.constants.Constants
import com.flyppcorp.firebase_classes.FirestoreContract
import com.flyppcorp.fragments.*
import com.flyppcorp.managerServices.FilterActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.service_items.*

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    private lateinit var mSharedFilter: SharedFilter
    private lateinit var mFirestoreContract: FirestoreContract
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mSharedFilter = SharedFilter(this)
        //acao do bottomNav

        mFirestoreContract = FirestoreContract(this)
        mFirestoreContract.mIntertial = InterstitialAd(applicationContext)
        MobileAds.initialize(this)
        mFirestoreContract.mIntertial.adUnitId = getString(R.string.ads_intertitial_id)
        mFirestoreContract.mIntertial.loadAd(AdRequest.Builder().build())

        bottom_nav.setOnNavigationItemSelectedListener(this)
        //item que ja esta selecionado
        bottom_nav.selectedItemId = R.id.homeFrag
        //configurações da toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbarMain)
        toolbar.title = ""
        setSupportActionBar(toolbar)

        val application : LifeCyclerApplication = application as LifeCyclerApplication
        getApplication().registerActivityLifecycleCallbacks(application)


        getToken()
        getPermissions()

    }

    private fun getPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED  ){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        }
    }

    private fun getToken() {
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        //val token = FirebaseInstanceId.getInstance().token
        if (uid != null){
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
                val addFragment = AddFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_view, addFragment)
                    .commit()
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
