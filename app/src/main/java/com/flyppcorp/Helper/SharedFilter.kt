package com.flyppcorp.Helper

import android.content.Context
import android.content.SharedPreferences

class SharedFilter (val context: Context) {
    private val mSharedPreferences : SharedPreferences = context.getSharedPreferences("filter", Context.MODE_PRIVATE)

    fun saveFilter(key : String, value: String){
        mSharedPreferences.edit().putString(key, value).apply()
    }

    fun getFilter(key: String): String {
        return mSharedPreferences.getString(key, "").toString()

    }

}