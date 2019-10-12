package com.flyppcorp.flypp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.widget.ToolbarWidgetWrapper
import androidx.fragment.app.Fragment
import com.flyppcorp.fragments.*
import com.flyppcorp.managerServices.FilterActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.service_items.*

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    var test : String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //acao do bottomNav

        bottom_nav.setOnNavigationItemSelectedListener(this)
        //item que ja esta selecionado
        bottom_nav.selectedItemId = R.id.homeFrag
        //configurações da toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbarMain)
        toolbar.title = ""
        setSupportActionBar(toolbar)
        val extras = intent.extras
        if (extras != null){
            test = extras.getString("teste")
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
