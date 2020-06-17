package com.flyppcorp.flypp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.flyppcorp.Helper.RedimensionImage
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mSize : RedimensionImage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        mAuth = FirebaseAuth.getInstance()
        mSize = RedimensionImage()

        Handler().postDelayed({
            Handler().postDelayed({
                when {
                    mAuth.currentUser?.uid != null -> {
                        val intent = Intent(baseContext, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }

                    else -> {
                        val intent = Intent(baseContext, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            }, 500)
        }, 2500)

        //loadImage()
    }

    private fun loadImage() {
       // Picasso.get().load(R.drawable.logo).resize(500,500).centerInside().into(imageView8)
        imageView8.setImageBitmap(mSize.redimensionarResource(resources,R.drawable.ic_logo_splash, 300, 130))
    }


}
