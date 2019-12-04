package com.flyppcorp.Helper

import android.content.Context
import android.net.ConnectivityManager
import android.widget.Toast

class Connection (val context: Context) {
     fun validateConection(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = cm.activeNetworkInfo
        if (networkInfo != null && networkInfo.isConnected) {
            return true
        } else {
            //progressBar.visibility = View.GONE
            Toast.makeText(context, "Você não possui conexão com a internet", Toast.LENGTH_SHORT)
                .show()
            return false
        }

    }
}