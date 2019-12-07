package com.flyppcorp.flypp


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.flyppcorp.firebase_classes.ResetPassword
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_reset_password.*

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var mResetPassword: ResetPassword
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)
        mResetPassword = ResetPassword(this)
        mAuth = FirebaseAuth.getInstance()

        setlisteners()

    }



    private fun setlisteners() {
        btnEnviarEmail.setOnClickListener {
            val email = editResetEmail.text.toString()
            if (validate()) {
                mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener {

                        if (it.isSuccessful) {
                            finish()
                        }


                    }
            }
        }


    }

    private fun validate(): Boolean {
        val email = editResetEmail.text.toString()
        if (email.isEmpty()) {
            editResetEmailLayout.error = "E-mail não pode ser vazio."
            return false
        } else {
            editResetEmailLayout.error = null
            return true
        }
    }
}
