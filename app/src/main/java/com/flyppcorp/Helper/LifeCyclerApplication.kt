package com.flyppcorp.Helper

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.multidex.MultiDex
import com.flyppcorp.constants.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LifeCyclerApplication : Application(), Application.ActivityLifecycleCallbacks {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    private fun setOnline(enabled: Boolean) {
        var uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS.USER_COLLECTION)
                .document(uid)
                .update("online", enabled)
        }else{
            return
        }
    }

    override fun onActivityPaused(activity: Activity) {
        //setOnline(false)
    }

    override fun onActivityStarted(activity: Activity) {

    }

    override fun onActivityDestroyed(activity: Activity) {

    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

    }

    override fun onActivityStopped(activity: Activity) {

    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {

    }

    override fun onActivityResumed(activity: Activity) {
        //setOnline(true)
    }
}
