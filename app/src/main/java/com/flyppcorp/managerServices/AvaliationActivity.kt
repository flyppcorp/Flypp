package com.flyppcorp.managerServices

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.flyppcorp.Helper.Connection
import com.flyppcorp.atributesClass.Comentarios
import com.flyppcorp.atributesClass.Myservice
import com.flyppcorp.atributesClass.Servicos
import com.flyppcorp.atributesClass.User
import com.flyppcorp.constants.Constants
import com.flyppcorp.flypp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_avaliation.*
import java.util.*

class AvaliationActivity : AppCompatActivity() {

    private var mMyservice: Myservice? = null
    private lateinit var mfirestore: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mConnection: Connection
    private lateinit var mProgress: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_avaliation)
        mMyservice = intent.extras?.getParcelable(Constants.KEY.SERVICE_STATUS)
        mfirestore = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()
        mConnection = Connection(this)
        mProgress = ProgressDialog(this)
        btnNota.setOnClickListener {
            if (mConnection.validateConection()) {
                handleAvalatiation()
            }

        }
        focus()
    }

    private fun focus() {
        editNota.requestFocus()
    }

    private fun handleAvalatiation() {
        if (editNota.text.toString().isEmpty()) {
            editNotaLayout.error = "Este campo não pode ser vazio"
        } else if (editNota.text.toString().toInt() > 5) {
            editNotaLayout.error = "Sua nota não pode ser maior que 5"
        } else {
            avaliationUser()
            mProgress.setCancelable(false)
            mProgress.show()
            val tsDoc = mfirestore.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
                .document(mMyservice?.serviceId.toString())
            mfirestore.runTransaction {
                val content = it.get(tsDoc).toObject(Servicos::class.java)
                content!!.avaliacao = content.avaliacao + editNota.text.toString().toInt()
                content.totalAvaliacao = content.totalAvaliacao + 1
                if (editComentario?.text.toString() != "") {
                    content.comments = content.comments + 1
                }
                comment()
                //mProgress.hide()
                it.set(tsDoc, content)
            }.addOnSuccessListener {
                val tsDocId = mfirestore.collection(Constants.COLLECTIONS.MY_SERVICE)
                    .document(mMyservice?.documentId.toString())
                mfirestore.runTransaction {
                    val content = it.get(tsDocId).toObject(Myservice::class.java)
                    content!!.idAvaliador[mMyservice!!.idContratante.toString()] = true
                    it.set(tsDocId, content)
                }.addOnSuccessListener {
                    finish()
                }
            }


        }
    }

    private fun avaliationUser() {
        val tsDoc = mfirestore.collection(Constants.COLLECTIONS.USER_COLLECTION)
            .document(mMyservice?.idContratado.toString())
        mfirestore.runTransaction {
            val content = it.get(tsDoc).toObject(User::class.java)
            content!!.totalAvaliacao = content.totalAvaliacao + 1
            content.avaliacao = content.avaliacao + editNota.text.toString().toInt()
            it.set(tsDoc, content)
        }


    }

    private fun comment() {
        val mComments = Comentarios()
        mComments.comentario = editComentario.text.toString()
        mComments.nomeContratante = mMyservice?.nomeContratante
        mComments.serviceId = mMyservice?.serviceId.toString()
        mComments.urlContratante = mMyservice?.urlContratante
        mComments.uid = FirebaseAuth.getInstance().currentUser?.uid
        val docId = UUID.randomUUID().toString()
        mComments.commentId = docId
        if (editComentario?.text.toString() != "") {
            mfirestore.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
                .document(mMyservice?.serviceId.toString())
                .collection(Constants.COLLECTIONS.COMMENTS)
                .document(docId)
                .set(mComments)
        } else {
            return
        }

    }


}
