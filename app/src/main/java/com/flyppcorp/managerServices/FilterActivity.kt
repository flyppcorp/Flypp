package com.flyppcorp.managerServices

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.flyppcorp.Helper.SharedFilter
import com.flyppcorp.constants.Constants
import com.flyppcorp.flypp.MainActivity
import com.flyppcorp.flypp.R
import kotlinx.android.synthetic.main.activity_filter.*

class FilterActivity : AppCompatActivity() {
    private lateinit var mSharedFilter: SharedFilter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter)
        mSharedFilter = SharedFilter(this)

        btnFilter.setOnClickListener {
            setListeners()
            handleCategoria()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }


    }

    private fun handleCategoria() {
        if (spinnerCategoriaFilter.selectedItem.toString() != "Todos"){
            mSharedFilter.saveFilter(Constants.FILTERS_VALUES.CATEGORIA, spinnerCategoriaFilter.selectedItem.toString())
        }else{
            mSharedFilter.saveFilter(Constants.FILTERS_VALUES.CATEGORIA, "")
        }
    }

    private fun setListeners() {
        if(radioGraoup.checkedRadioButtonId != -1){
            when{
                menor_preco.isChecked -> mSharedFilter.saveFilter(Constants.KEY.FILTER_KEY, Constants.FILTERS_VALUES.MENOR_PRECO)
                maior_preco.isChecked -> mSharedFilter.saveFilter(Constants.KEY.FILTER_KEY, Constants.FILTERS_VALUES.MAIOR_PRECO)
                menor_relevancia.isChecked -> mSharedFilter.saveFilter(Constants.KEY.FILTER_KEY, Constants.FILTERS_VALUES.MENOR_RELEVANCIA)
                maior_relevancia.isChecked -> mSharedFilter.saveFilter(Constants.KEY.FILTER_KEY, Constants.FILTERS_VALUES.MAIOR_RELEVANCIA)
                menor_avaliado.isChecked -> mSharedFilter.saveFilter(Constants.KEY.FILTER_KEY, Constants.FILTERS_VALUES.MENOS_AVALIADO)
                maior_avaliado.isChecked -> mSharedFilter.saveFilter(Constants.KEY.FILTER_KEY, Constants.FILTERS_VALUES.MAIS_AVALIADO)
            }

        }

    }


}
