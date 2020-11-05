package com.flyppcorp.flypp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.flyppcorp.Helper.Connection
import com.flyppcorp.Helper.LifeCyclerApplication
import com.flyppcorp.Helper.SharedFilter
import com.flyppcorp.atributesClass.User
import com.flyppcorp.constants.Constants
import com.flyppcorp.firebase_classes.FirestoreContract
import com.flyppcorp.fragments.*
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.tapadoo.alerter.Alerter
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.util.*


class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    private lateinit var mSharedFilter: SharedFilter
    private lateinit var mFirestoreContract: FirestoreContract
    private lateinit var mConnection: Connection
    private lateinit var client: FusedLocationProviderClient
    private lateinit var mCity: SharedFilter
    private lateinit var mFirestore: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mFirebaseRemoteConfig: FirebaseRemoteConfig
    private lateinit var mAppUpdateManager: AppUpdateManager
    private val UPDATE_REQUEST_CODE = 123



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mSharedFilter = SharedFilter(this)
        mConnection = Connection(this)
        mCity = SharedFilter(this)
        mFirestore = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        mAppUpdateManager = AppUpdateManagerFactory.create(this)
        //acao do bottomNav

        /*mFirestoreContract = FirestoreContract(this)
        mFirestoreContract.mIntertial = InterstitialAd(applicationContext)
        MobileAds.initialize(this)
        mFirestoreContract.mIntertial.adUnitId = getString(R.string.ads_intertitial_id)
        mFirestoreContract.mIntertial.loadAd(AdRequest.Builder().build())*/
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

        if (mAuth.currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        mAppUpdateManager.registerListener {
            if(it.installStatus() == InstallStatus.DOWNLOADED){
                showUpdateDownloadedSnackbar()
            }
        }

        mAppUpdateManager.appUpdateInfo.addOnSuccessListener {
            if (it.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && it.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)){
                mAppUpdateManager.startUpdateFlowForResult(it, AppUpdateType.FLEXIBLE, this, UPDATE_REQUEST_CODE)
                }
        }.addOnFailureListener {
            Log.e("FlexibleUpdateActivity", "Failed to check for update: $it")
        }


        getToken()
        getPermissions()
        //updateLocation()
        //goToLogin()
        messageLocation()
        remoteConfigFunc()
        handleAnonimo()


        //getLocation()


    }

    override fun onResume() {
        super.onResume()
        getLocation()
        mAppUpdateManager.appUpdateInfo.addOnSuccessListener {
            if (it.installStatus() == InstallStatus.DOWNLOADED) {
                showUpdateDownloadedSnackbar()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        mSharedFilter.saveFilter(Constants.FILTERS_VALUES.CATEGORIA, "")
    }

    private fun showUpdateDownloadedSnackbar() {
        Snackbar.make(viewMain, "Atualização baixada", Snackbar.LENGTH_INDEFINITE)
            .setAction("Instalar") { mAppUpdateManager.completeUpdate() }
            .show()
    }

    private fun handleAnonimo() {
        val currentUser = mAuth.currentUser?.isAnonymous
        if (currentUser == true) {
            btnAnonimo?.visibility = View.VISIBLE
            btnAnonimo?.setOnClickListener {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
        }
    }


    private fun remoteConfigFunc() {
        mFirebaseRemoteConfig.fetch(0)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    mFirebaseRemoteConfig.fetchAndActivate()
                    handleRemote(mFirebaseRemoteConfig)
                }
            }
    }

    private fun handleRemote(mFirebaseRemoteConfig: FirebaseRemoteConfig) {
        val showDialog = mFirebaseRemoteConfig.getBoolean(Constants.KEY.NEW_VERSION)
        val version = mFirebaseRemoteConfig.getString(Constants.KEY.VERSION_CODE)

        val pInfo = this.packageManager.getPackageInfo(packageName, 0)
        val versionNumber = pInfo.versionCode
        val dialogView = layoutInflater.inflate(R.layout.dialog, null)

        if (showDialog) {
            if (versionNumber < version.toInt()) {
                AlertDialog.Builder(this)
                    .setView(dialogView)
                    .setPositiveButton("Atualizar", { dialog, which ->
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=com.flyppcorp.flypp")
                        )
                        startActivity(intent)
                    })
                    .setNegativeButton("Depois", { dialog, which -> })
                    .show()


            }
        }

    }

    private fun messageLocation() {
        if (mSharedFilter.getFilter(Constants.KEY.CITY_NAME) == "") {
            Alerter.create(this)
                .setTitle("Estamos atualizando sua localização.")
                .setText(
                    "Para atualizar os estabelecimentos da sua cidade, basta clicar no botão Home. :)"
                )
                .setIcon(R.drawable.ic_location)
                .setBackgroundColorRes(R.color.colorPrimaryDark)
                .setDuration(7000)
                .enableProgress(true)
                .setProgressColorRes(R.color.textIcons)
                .enableSwipeToDismiss()
                .show()
        }
    }


    /*private fun updateLocation() {

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val mFirestoreService = FirebaseFirestore.getInstance()
        mFirestoreService.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
            .whereEqualTo("uid", uid.toString())
            .addSnapshotListener { snapshot, exception ->
                if (snapshot == null) return@addSnapshotListener
                snapshot?.let {
                    for (doc in snapshot) {
                        val serviceLocation = doc.toObject(Servicos::class.java)
                        val tsDoc =
                            mFirestoreService.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
                                .document(serviceLocation.serviceId!!)
                        mFirestoreService.runTransaction {
                            val content = it.get(tsDoc).toObject(Servicos::class.java)
                            if (content?.cityName != mCity.getFilter(Constants.KEY.CITY_NAME) && mCity.getFilter(
                                    Constants.KEY.CITY_NAME
                                ) != ""
                            ) {
                                content?.cityName = mCity.getFilter(Constants.KEY.CITY_NAME)
                            }
                            it.set(tsDoc, content!!)
                        }

                    }
                }
            }
    }*/

    private fun getLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
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
                        mFirestore.collection(Constants.COLLECTIONS.USER_COLLECTION)
                            .document(mAuth.currentUser?.uid.toString())
                            .get()
                            .addOnSuccessListener {
                                val item = it.toObject(User::class.java)
                                mSharedFilter.saveFilter(
                                    Constants.KEY.CITY_NAME,
                                    item?.cidade.toString()
                                )
                            }
                    } else {
                        val geocoder = Geocoder(this@MainActivity, Locale.getDefault())
                        val adress: List<Address>? =
                            geocoder.getFromLocation(it.latitude, it.longitude, 1)
                        if (adress != null) {
                            if (adress.size > 0) {
                                for (adresses: Address in adress) {
                                    if (adresses.subAdminArea != null) {
                                        mCity.saveFilter(
                                            Constants.KEY.CITY_NAME,
                                            adresses.subAdminArea
                                        )


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


    private fun getPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
            && ContextCompat.checkSelfPermission(
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
            getLocation()
            ///////////////////////////////////////////

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
                toolbarMain?.visibility = View.VISIBLE
                return true
            }
            R.id.searchFrag -> {
                val searchFragment = SearchFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_view, searchFragment)
                    .commit()
                toolbarMain?.visibility = View.GONE
                return true
            }
            R.id.addFrag -> {
                if (mAuth.currentUser?.isAnonymous == false) {
                    val intent = Intent(this, AddActivity::class.java)
                    startActivity(intent)
                    toolbarMain?.visibility = View.VISIBLE
                    if (bottom_nav.selectedItemId == R.id.searchFrag) {
                        toolbarMain?.visibility = View.GONE
                    }

                } else {
                    val alert = AlertDialog.Builder(this)
                    alert.setTitle("Ops! uma ação é necessária")
                    alert.setMessage(
                        "Para adicionar um serviço você precisa fazer login ou criar uma conta" +
                                "\nDeseja fazer isso agora ?"
                    )
                    alert.setNegativeButton("Agora não", { dialog, which -> })
                    alert.setPositiveButton("Sim", { dialog, which ->
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    })
                    alert.show()
                }
                return true
            }
            R.id.favFrag -> {
                val favFragment = FavFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_view, favFragment)
                    .commit()
                toolbarMain?.visibility = View.VISIBLE
                return true
            }
            R.id.contaFrag -> {
                val contaFragment = ContaFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_view, contaFragment)
                    .commit()
                toolbarMain?.visibility = View.VISIBLE
                return true
            }
        }
        return false
    }


}
