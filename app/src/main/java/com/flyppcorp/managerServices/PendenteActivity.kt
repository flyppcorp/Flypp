package com.flyppcorp.managerServices

import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
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
import kotlinx.android.synthetic.main.activity_pendente.*
import kotlinx.android.synthetic.main.dialog_fr3.view.*


class PendenteActivity : AppCompatActivity() {

    private var mMyService: Myservice? = null
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mFirestore: FirebaseFirestore
    private var mAdress: Myservice? = null
    private lateinit var mConnection: Connection

    //private var data: String? = null
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
        val tb = findViewById<Toolbar>(R.id.tb_pendente)
        tb.title = ""
        btnVoltarTbPendente.setOnClickListener {
            finish()
        }
        txtTitlePendente.text = "Pendente"
        setSupportActionBar(tb)
        /*btnDate.setOnClickListener {
            handleDate()
        }*/

        getEndereco()
        handleTextButton()
        handleDateVisibility()


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.message_my_service, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.mensagem_my_service -> {
                mProgress.setCancelable(false)
                mProgress.show()
                mProgress.setContentView(R.layout.progress)
                mProgress.window?.setBackgroundDrawableResource(android.R.color.transparent)
                if (mAuth.currentUser?.uid == mMyService?.idContratante) {
                    mFirestore.collection(Constants.COLLECTIONS.USER_COLLECTION)
                        .document(mMyService?.idContratado.toString())
                        .get()
                        .addOnSuccessListener {
                            val user = it.toObject(User::class.java)
                            val intent = Intent(this, MessageActivity::class.java)
                            intent.putExtra(Constants.KEY.MESSAGE_KEY, user)
                            startActivity(intent)
                            mProgress.hide()
                        }
                } else if (mAuth.currentUser?.uid == mMyService?.idContratado) {
                    mFirestore.collection(Constants.COLLECTIONS.USER_COLLECTION)
                        .document(mMyService?.idContratante.toString())
                        .get()
                        .addOnSuccessListener {
                            val user = it.toObject(User::class.java)
                            val intent = Intent(this, MessageActivity::class.java)
                            intent.putExtra(Constants.KEY.MESSAGE_KEY, user)
                            startActivity(intent)
                            mProgress.hide()
                        }
                }
            }

            /*R.id.ligar_my_service -> {
                val uid = mAuth.currentUser?.uid.toString()

                if (uid == mMyService?.idContratado) {
                    phoneCall(mMyService?.idContratante.toString())

                } else if (uid == mMyService?.idContratante) {
                    phoneCall(mMyService?.idContratado.toString())

                }
            }*/

        }
        return super.onOptionsItemSelected(item)
    }

    private fun phoneCall(uid: String) {
        mFirestore.collection(Constants.COLLECTIONS.USER_COLLECTION)
            .document(uid)
            .get()
            .addOnSuccessListener {

                val items = it.toObject(User::class.java)
                if (items?.ddd != null && items.telefone != null) {
                    val phone = items.ddd + items.telefone
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                } else {
                    val alert = AlertDialog.Builder(this)
                    alert.setMessage("Este usuário não informou o telefone")
                    alert.setPositiveButton("Ok", { dialog, which -> })
                    alert.show()
                }

            }
    }


    private fun handleDateVisibility() {
        if (mAuth.currentUser?.uid == mMyService?.idContratante) {
            //btnDate?.visibility = View.GONE
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
                mProgress.setContentView(R.layout.progress)
                mProgress.window?.setBackgroundDrawableResource(android.R.color.transparent)
                val tsDoc = mFirestore.collection(Constants.COLLECTIONS.MY_SERVICE)
                    .document(mMyService?.documentId.toString())
                mFirestore.runTransaction {
                    val content = it.get(tsDoc).toObject(Myservice::class.java)
                    content?.pendente = false
                    content?.andamento = true
                    //content?.dateService = data
                    content?.observacaoProfissional = editprofissionalobs?.text.toString()
                    notification()
                    it.set(tsDoc, content!!)
                }.addOnSuccessListener {
                    mProgress.hide()
                    finish()
                }

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
                    "${mMyService?.nomeContratado} aceitou seu pedido (${mMyService?.serviceNome})"
                notification.title = "Nova atualização de pedido"

                mFirestore.collection(Constants.COLLECTIONS.NOTIFICATION_SERVICE)
                    .document(user?.token.toString())
                    .set(notification)

            }
    }


    private fun handleDesistirRecusar() {
        val vd = layoutInflater.inflate(R.layout.dialog_fr3, null)
        val mDialog = AlertDialog.Builder(this)
            .setView(vd)
        mDialog.setTitle("Você tem certeza disso?")
        mDialog.setMessage("Se quiser, pode dizer o motivo da sua desistência")
        mDialog.setPositiveButton("Sim") { dialog: DialogInterface?, which: Int ->
            mProgress.setCancelable(false)
            mProgress.show()
            mProgress.setContentView(R.layout.progress)
            mProgress.window?.setBackgroundDrawableResource(android.R.color.transparent)
            mFirestore.collection(Constants.COLLECTIONS.MY_SERVICE)
                .document(mMyService?.documentId.toString())
                .delete()
                .addOnSuccessListener {
                    if (mMyService?.idContratante == mAuth.currentUser?.uid) {
                        notificationDesistence(
                            mMyService?.idContratado.toString(),
                            "desistiu do pedido",
                            mMyService?.nomeContratante.toString(), vd.edtMotivo.text.toString()
                        )
                    } else {
                        notificationDesistence(
                            mMyService?.idContratante.toString(),
                            "teve que recusar o seu pedido, não fique triste, temos vários outros estabelecimentos prontos para te receber",
                            mMyService?.nomeContratado.toString(), vd.edtMotivo.text.toString()
                        )
                    }
                    handleTs(mMyService?.idContratante)
                    mProgress.hide()
                    finish()
                }.addOnFailureListener {
                    mProgress.hide()
                    Toast.makeText(this, "Ocorreu um erro. Tente novamente!", Toast.LENGTH_SHORT)
                        .show()
                }
        }
        mDialog.setNegativeButton("Não") { dialog: DialogInterface?, which: Int -> }
        val alert = mDialog.create()
        alert.show()
    }

    private fun handleTs(idContratante: String?) {
        val tsDoc = mFirestore.collection(Constants.COLLECTIONS.USER_COLLECTION)
            .document(idContratante.toString())
        mFirestore.runTransaction {
            val content = it.get(tsDoc).toObject(User::class.java)
            if (content!!.primeiraCompra && !content.primeiraCompraConcluida) {
                content.primeiraCompra = false
            }


            it.set(tsDoc, content)
        }
    }

    private fun notificationDesistence(
        uid: String,
        status: String,
        nome: String,
        motivos: String?
    ) {
        mFirestore.collection(Constants.COLLECTIONS.USER_COLLECTION)
            .document(uid)
            .get()
            .addOnSuccessListener { info ->

                val user: User? = info.toObject(User::class.java)
                val notification = Notification()
                notification.serviceId = mMyService?.serviceId
                if (motivos != "") {
                    notification.text =
                        "$nome $status (${mMyService!!.serviceNome})" +
                                "\nMotivo: ${motivos}"
                } else {
                    notification.text =
                        "$nome $status (${mMyService!!.serviceNome})"
                }
                notification.title = "Nova atualização de pedido"

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
            btnAceitarVoltarPendente?.visibility = View.GONE
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
            else Picasso.get().load(mMyService?.urlService).placeholder(R.drawable.ic_working).fit()
                .centerCrop().into(
                    imgServiceAcct
                )
            txtContratanteAcct.text = mMyService?.nomeContratante
            if (mMyService?.dateService != null || mMyService?.horario != null) {
                txtData?.text =
                    "${mMyService?.dateService} ás ${mMyService?.horario}".replace("null", "-")
            } else {
                txtTittleData?.visibility = View.GONE
                txtData?.visibility = View.GONE
            }
            txtQuantidade.text = mMyService?.quantidate.toString()
            txtServicoAcct.text = mMyService?.serviceNome
            if (mMyService?.observacao != null) {
                txtObservacao?.text = mMyService?.observacao
            } else {
                titleObservacao?.visibility = View.GONE
                txtObservacao?.visibility = View.GONE
            }

            if (mMyService?.sabor != null) {
                txtSabor?.text = mMyService?.sabor
            } else {
                titleSabor?.visibility = View.GONE
                txtSabor?.visibility = View.GONE
            }
            txtEnderecoAcct.text = "${it.rua}, ${it.bairro}, ${it.numero} \n" +
                    "${it.cidade}, ${it.estado}, ${it.cep}".replace("null", "-")
        }

        val result = String.format("%.2f", mMyService?.preco)
        txtPrecoAcct.text = "R$ ${result}".replace(".",",")


    }
}
