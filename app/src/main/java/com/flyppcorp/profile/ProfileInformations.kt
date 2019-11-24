package com.flyppcorp.profile

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.flyppcorp.atributesClass.User
import com.flyppcorp.constants.Constants
import com.flyppcorp.flypp.LoginActivity
import com.flyppcorp.flypp.ProfileActivity
import com.flyppcorp.flypp.R
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_profile_informations.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.nav_header.view.*

class ProfileInformations : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var header: View
    private lateinit var profileName: TextView
    private lateinit var imageProfile: ImageView
    private lateinit var drawer: DrawerLayout
    private var mUser: User? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_informations)
        setSupportActionBar(toolbarprofile)
        val navigationView: NavigationView = findViewById(R.id.nav_view)
        toolbarprofile.setTitleTextColor(Color.WHITE)
        header = navigationView.getHeaderView(0)
        profileName = header.findViewById(R.id.txt_nav_profile)
        imageProfile = header.findViewById(R.id.img_nav_profile)
        drawer = findViewById(R.id.drawerLayout)
        navigationView.setNavigationItemSelectedListener(this)
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbarprofile,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawer.addDrawerListener(toggle)
        toggle.syncState()


        if (savedInstanceState == null) {
            val profileFragment = ProfileFragment()
            supportFragmentManager.beginTransaction().replace(R.id.main_conteiner, profileFragment)
                .commit()
            navigationView.setCheckedItem(R.id.perfil)
        }


        fetch()


    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.perfil -> {
                val profileFragment = ProfileFragment()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.main_conteiner, profileFragment).commit()
                return true
            }

            R.id.sair -> {
                val user = FirebaseAuth.getInstance().currentUser
                val alert = AlertDialog.Builder(this)
                alert.setMessage("Você tem certeza que deseja sair?")
                alert.setNegativeButton("Não", {dialog, which ->  })
                alert.setPositiveButton("Sim", {dialog, which ->
                    if (user != null){
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
                })
                alert.show()

            }
            R.id.conta_bancaria -> Toast.makeText(this, "CONTA", Toast.LENGTH_SHORT).show()
        }

        return false
    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }

    }

    private fun fetch() {
        FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS.USER_COLLECTION)
            .document(FirebaseAuth.getInstance().currentUser!!.uid)
            .get()
            .addOnSuccessListener {
                mUser = it.toObject(User::class.java)
                profileName.text = mUser!!.nome
                Picasso.get().load(mUser?.url).into(imageProfile)


                profileGo()
            }
    }

    private fun profileGo() {
        mUser?.let { information ->
            header.setOnClickListener {
                val intent = Intent(this, ProfileActivity::class.java)
                intent.putExtra(Constants.KEY.PROFILE_KEY, mUser)
                startActivity(intent)
            }

        }
    }


}
