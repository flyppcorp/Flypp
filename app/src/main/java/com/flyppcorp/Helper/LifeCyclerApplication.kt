package com.flyppcorp.Helper

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.flyppcorp.constants.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LifeCyclerApplication : Application(), Application.ActivityLifecycleCallbacks {

    private fun setOnline(enabled: Boolean) {
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        if (uid != null) {
            FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS.USER_COLLECTION)
                .document(uid)
                .update("online", enabled)
        }

    }

    override fun onActivityPaused(activity: Activity) {
        setOnline(false)
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
        setOnline(true)
    }
}