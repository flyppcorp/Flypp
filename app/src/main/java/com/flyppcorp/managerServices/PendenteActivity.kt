package com.flyppcorp.managerServices

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.flyppcorp.Helper.Connection
import com.flyppcorp.atributesClass.Myservice
import com.flyppcorp.atributesClass.Notification
import com.flyppcorp.atributesClass.User
import com.flyppcorp.constants.Constants
import com.flyppcorp.flypp.MessageActivity
import com.flyppcorp.flypp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_andamento.*
import kotlinx.android.synthetic.main.activity_pendente.*
import java.util.*

class PendenteActivity : AppCompatActivity() {

    private var mMyService: Myservice? = null
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mFirestore: FirebaseFirestore
    private var mAdress: Myservice? = null
    private lateinit var mConnection: Connection
    private var data : String? = null
    private lateinit var mProgress: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pendente)
        mMyService = intent.extras?.getParcelable(Constants.KEY.SERVICE_STATUS)
        mAuth = FirebaseAuth.getInstance()
        mFirestore = FirebaseFirestore.getInstance()
        mConnection = Connection(this)
        mProgress = ProgressDialog(this)
        btnRecusarDesistirPendente.setOnClickListener {
            if (mConnection.validateConection()) {
                handleDesistirRecusar()
            }

        }
        btnAceitarVoltarPendente.setOnClickListener {
            handleAceitarVoltar()
        }

        btnDate.setOnClickListener {
            handleDate()
        }

        supportActionBar?.title = "Pendente "
        getEndereco()
        handleTextButton()
        handleDateVisibility()


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.message_my_pendente, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.mensagem_my_service -> {
               if (mAuth.currentUser?.uid == mMyService?.idContratante ){
                   mFirestore.collection(Constants.COLLECTIONS.USER_COLLECTION)
                       .document(mMyService?.idContratado.toString())
                       .get()
                       .addOnSuccessListener {
                           val user = it.toObject(User::class.java)
                           val intent = Intent(this, MessageActivity::class.java)
                           intent.putExtra(Constants.KEY.MESSAGE_KEY, user)
                           startActivity(intent)
                       }
               }else if (mAuth.currentUser?.uid == mMyService?.idContratado){
                   mFirestore.collection(Constants.COLLECTIONS.USER_COLLECTION)
                       .document(mMyService?.idContratante.toString())
                       .get()
                       .addOnSuccessListener {
                           val user = it.toObject(User::class.java)
                           val intent = Intent(this, MessageActivity::class.java)
                           intent.putExtra(Constants.KEY.MESSAGE_KEY, user)
                           startActivity(intent)
                       }
               }
               }

        }
        return super.onOptionsItemSelected(item)
    }

    private fun handleDate() : String? {

        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener{
            view, year, month, dayOfMonth ->

            when {
                month <= 8 && dayOfMonth < 10 -> {
                    data = "0$dayOfMonth / 0${month + 1} / $year"
                    btnDate.text = "0$dayOfMonth / 0${month + 1}/ $year"

                }
                month >= 9 && dayOfMonth < 10 -> {
                    data = "0$dayOfMonth / ${month + 1} / $year"
                    btnDate.text = "0$dayOfMonth / ${month + 1}/ $year"
                }
                month <= 8 && dayOfMonth > 10 -> {
                    data = "$dayOfMonth / 0${month + 1} / $year"
                    btnDate.text = "$dayOfMonth / 0${month + 1}/ $year"
                }
                else -> {
                    data = "$dayOfMonth / ${month + 1} / $year"
                    btnDate.text = "$dayOfMonth / ${month + 1}/ $year"
                }
            }



        }, year, month, day)
        dpd.show()
        return data
    }

    private fun handleDateVisibility() {
        if (mAuth.currentUser?.uid == mMyService?.idContratante) {
            btnDate?.visibility = View.GONE
            textInputLayout12?.visibility = View.GONE
            textView43?.visibility = View.GONE
        }

    }

    private fun handleAceitarVoltar() {
        if (mMyService?.idContratante == mAuth.currentUser?.uid) {
            finish()
        } else if (mMyService?.idContratado == mAuth.currentUser?.uid) {
            if (mConnection.validateConection()) {
                mProgress.setCancelable(false)
                mProgress.show()
                val tsDoc = mFirestore.collection(Constants.COLLECTIONS.MY_SERVICE)
                    .document(mMyService?.documentId.toString())
                mFirestore.runTransaction {
                    val content = it.get(tsDoc).toObject(Myservice::class.java)
                    content?.pendente = false
                    content?.andamento = true
                    content?.dateService = data
                    content?.observacaoProfissional = editprofissionalobs?.text.toString()
                    notification()
                    it.set(tsDoc, content!!)
                }
                mProgress.hide()
                finish()
            }
        }
    }

    private fun notification() {
        mFirestore.collection(Constants.COLLECTIONS.USER_COLLECTION)
            .document(mMyService?.idContratante.toString())
            .get()
            .addOnSuccessListener { info ->

                val user: User? = info.toObject(User::class.java)
                val notification = Notification()
                notification.serviceId = mMyService?.serviceId
                notification.text =
                    "${mMyService?.nomeContratado} aceitou sua solicitação de trabalho (${mMyService?.serviceNome})"
                notification.title = "Nova atualização de serviço"

                mFirestore.collection(Constants.COLLECTIONS.NOTIFICATION_SERVICE)
                    .document(user?.token.toString())
                    .set(notification)

            }
    }


    private fun handleDesistirRecusar() {
        val mDialog = AlertDialog.Builder(this)
        mDialog.setTitle("Você tem certeza disso?")
        mDialog.setPositiveButton("Sim") { dialog: DialogInterface?, which: Int ->
            mProgress.setCancelable(false)
            mProgress.show()
            mFirestore.collection(Constants.COLLECTIONS.MY_SERVICE)
                .document(mMyService?.documentId.toString())
                .delete()
                .addOnSuccessListener {
                    if (mMyService?.idContratante == mAuth.currentUser?.uid) {
                        notificationDesistence(mMyService?.idContratado.toString(), "desistiu", mMyService?.nomeContratante.toString())
                    } else {
                        notificationDesistence(mMyService!!.idContratante!!, "rejeitou",mMyService!!.nomeContratado!!)
                    }
                    mProgress.hide()
                    finish()
                }.addOnFailureListener {
                    mProgress.hide()
                    Toast.makeText(this, "Ocorreu um erro. Tente novamente!", Toast.LENGTH_SHORT)
                        .show()
                }
        }
        mDialog.setNegativeButton("Não") { dialog: DialogInterface?, which: Int -> }
        mDialog.show()
    }

    private fun notificationDesistence(uid: String, status: String, nome : String) {
        mFirestore.collection(Constants.COLLECTIONS.USER_COLLECTION)
            .document(uid)
            .get()
            .addOnSuccessListener { info ->

                val user: User? = info.toObject(User::class.java)
                val notification = Notification()
                notification.serviceId = mMyService?.serviceId
                notification.text =
                    "$nome $status sua solicitação de trabalho (${mMyService!!.serviceNome})"
                notification.title = "Nova atualização de serviço"

                mFirestore.collection(Constants.COLLECTIONS.NOTIFICATION_SERVICE)
                    .document(user?.token.toString())
                    .set(notification)

            }
    }


    private fun handleTextButton() {
        if (mMyService?.idContratado == mAuth.currentUser?.uid) {
            btnAceitarVoltarPendente.text = "Aceitar"
            btnRecusarDesistirPendente.text = "Recusar"
        } else {
            btnAceitarVoltarPendente.text = "Voltar"
            btnRecusarDesistirPendente.text = "Desistir"
        }
    }

    private fun getEndereco() {
        mFirestore.collection(Constants.COLLECTIONS.MY_SERVICE)
            .document(mMyService?.documentId.toString())
            .get()
            .addOnSuccessListener {
                mAdress = it.toObject(Myservice::class.java)
                fetchPendente()
            }
    }

    private fun fetchPendente() {
        mAdress?.let {
            if (mMyService?.urlService == null) imgServiceAcct.setImageResource(R.drawable.ic_working)
            else Picasso.get().load(mMyService?.urlService).placeholder(R.drawable.ic_working).fit().centerCrop().into(imgServiceAcct)
            txtContratanteAcct.text = mMyService?.nomeContratante
            txtContratadoAcct.text = mMyService?.nomeContratado
            txtServicoAcct.text = mMyService?.serviceNome
            txtObservacao.text = mMyService?.observacao
            txtPrecoAcct.text = "R$ ${mMyService?.preco.toString().replace(".",",")} por ${mMyService?.tipoCobranca}"
            txtEnderecoAcct.text = "${it.rua}, ${it.bairro}, ${it.numero} \n" +
                    "${it.cidade}, ${it.estado}, ${it.cep}"
        }


    }
}
