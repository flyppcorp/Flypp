package com.flyppcorp.managerServices

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.flyppcorp.Helper.SharedFilter
import com.flyppcorp.constants.Constants
import com.flyppcorp.flypp.MainActivity
import com.flyppcorp.flypp.R
import com.flyppcorp.fragments.HomeFragment
import kotlinx.android.synthetic.main.activity_filter.*

class FilterActivity : AppCompatActivity() {
    private lateinit var mSharedFilter: SharedFilter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter)
        mSharedFilter = SharedFilter(this)

        setListeners()

    }

    private fun setListeners() {
        btnMenorPreco.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            mSharedFilter.saveFilter(Constants.KEY.FILTER_KEY, Constants.FILTERS_VALUES.MENOR_PRECO)
            startActivity(intent)
            finish()
        }

        btnMaiorPreco.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            mSharedFilter.saveFilter(Constants.KEY.FILTER_KEY, Constants.FILTERS_VALUES.MAIOR_PRECO)
            startActivity(intent)
            finish()
        }

        btnMenorRelevancia.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            mSharedFilter.saveFilter(Constants.KEY.FILTER_KEY, Constants.FILTERS_VALUES.MENOR_RELEVANCIA)
            startActivity(intent)
            finish()
        }

        btnMaiorRelevancia.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            mSharedFilter.saveFilter(Constants.KEY.FILTER_KEY, Constants.FILTERS_VALUES.MAIOR_RELEVANCIA)
            startActivity(intent)
            finish()
        }

        btnMenorAvaliacao.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            mSharedFilter.saveFilter(Constants.KEY.FILTER_KEY, Constants.FILTERS_VALUES.MENOS_AVALIADO)
            startActivity(intent)
            finish()
        }

        btnMaisAvaliado.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            mSharedFilter.saveFilter(Constants.KEY.FILTER_KEY, Constants.FILTERS_VALUES.MAIS_AVALIADO)
            startActivity(intent)
            finish()
        }
    }


}
