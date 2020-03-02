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
import com.flyppcorp.Helper.Connection
import com.flyppcorp.atributesClass.*
import com.flyppcorp.constants.Constants
import com.flyppcorp.flypp.MessageActivity
import com.flyppcorp.flypp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_andamento.*

class AndamentoActivity : AppCompatActivity() {
    private lateinit var mFirestore: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth
    private var mMyservice: Myservice? = null
    private var mAdress: Myservice? = null
    private lateinit var mConnection: Connection
    private lateinit var mProgress: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_andamento)
        mFirestore = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()
        mConnection = Connection(this)
        mProgress = ProgressDialog(this)
        mMyservice = intent.extras?.getParcelable(Constants.KEY.SERVICE_STATUS)
        btnVoltarAndamento.setOnClickListener {
            handleCancel()
        }
        btnFinalizarAndamento.setOnClickListener {
            if (mConnection.validateConection()){
                mProgress.setCancelable(false)
                mProgress.show()
                handleFinalizar()
            }

        }
        supportActionBar?.title = "Em andamento"
        getEndereco()
        btnText()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.message_my_service, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.mensagem_my_service -> {
                if (mAuth.currentUser?.uid == mMyservice?.idContratante ){
                    mFirestore.collection(Constants.COLLECTIONS.USER_COLLECTION)
                        .document(mMyservice?.idContratado.toString())
                        .get()
                        .addOnSuccessListener {
                            val user = it.toObject(User::class.java)
                            val intent = Intent(this, MessageActivity::class.java)
                            intent.putExtra(Constants.KEY.MESSAGE_KEY, user)
                            startActivity(intent)
                        }
                }else if (mAuth.currentUser?.uid == mMyservice?.idContratado){
                    mFirestore.collection(Constants.COLLECTIONS.USER_COLLECTION)
                        .document(mMyservice?.idContratante.toString())
                        .get()
                        .addOnSuccessListener {
                            val user = it.toObject(User::class.java)
                            val intent = Intent(this, MessageActivity::class.java)
                            intent.putExtra(Constants.KEY.MESSAGE_KEY, user)
                            startActivity(intent)
                        }
                }
            }
            R.id.ligar_my_service -> {
                val uid = mAuth.currentUser?.uid.toString()

                if (uid == mMyservice?.idContratado) {
                    phoneCall(mMyservice?.idContratante.toString())

                } else if (uid == mMyservice?.idContratante) {
                    phoneCall(mMyservice?.idContratado.toString())

                }
            }
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

    private fun handleFinalizar() {
        val tsDoc = mFirestore.collection(Constants.COLLECTIONS.MY_SERVICE)
            .document(mMyservice?.documentId.toString())
        mFirestore.runTransaction {
            val content = it.get(tsDoc).toObject(Myservice::class.java)
            content?.andamento = false
            content?.finalizado = true
            content?.timestamp = System.currentTimeMillis()
            //Aqui será a chamada para a tela de avaliacao caso seja finalizado pelo contratante
            it.set(tsDoc, content!!)
            servicosFinalizado(mMyservice?.idContratado.toString())
            dashBoard()

        }
        val tsServiceDoc = mFirestore.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
            .document(mMyservice?.serviceId.toString())
        mFirestore.runTransaction {
            val contentService = it.get(tsServiceDoc).toObject(Servicos::class.java)

            contentService?.totalServicos = contentService?.totalServicos!!.toInt() + 1
            it.set(tsServiceDoc, contentService)
        }

        if (mMyservice?.idContratante == mAuth.currentUser?.uid) {
            val intent = Intent(this, AvaliationActivity::class.java)
            intent.putExtra(Constants.KEY.SERVICE_STATUS, mMyservice)
            startActivity(intent)
            finish()
        }
        finish()

    }

    private fun servicosFinalizado(uid: String) {
        val tsDoc = mFirestore.collection(Constants.COLLECTIONS.USER_COLLECTION).document(uid)
        mFirestore.runTransaction {
            val content = it.get(tsDoc).toObject(User::class.java)
            content!!.totalServicosFinalizados = content.totalServicosFinalizados + 1
            if (mMyservice?.idContratante == mAuth.currentUser?.uid) {
                notification(mMyservice?.idContratado.toString())
            } else {
                notification(mMyservice?.idContratante.toString())
            }
            it.set(tsDoc, content)

        }
    }

    private fun notification(uid: String) {
        mFirestore.collection(Constants.COLLECTIONS.USER_COLLECTION)
            .document(uid)
            .get()
            .addOnSuccessListener {
                val user: User? = it.toObject(User::class.java)
                val notification = Notification()
                notification.serviceId = mMyservice?.documentId
                notification.text =
                    "${mMyservice?.nomeContratante} finalizou um serviço (${mMyservice?.serviceNome})"
                notification.title = "Nova atualização de serviço"

                mFirestore.collection(Constants.COLLECTIONS.NOTIFICATION_SERVICE)
                    .document(user?.token.toString())
                    .set(notification)
            }

    }

    private fun handleCancel() {
        val mAlert = AlertDialog.Builder(this)
        mAlert.setTitle("Você tem certeza disso?")
        if (mMyservice?.idContratado == mAuth.currentUser?.uid) {
            if (mConnection.validateConection()) {
                mAlert.setPositiveButton("Sim") { dialog: DialogInterface?, which: Int ->
                    mProgress.setCancelable(false)
                    mProgress.show()
                    mFirestore.collection(Constants.COLLECTIONS.MY_SERVICE)
                        .document(mMyservice?.documentId.toString())
                        .delete()
                        .addOnSuccessListener {
                            notificationCancel()
                            mProgress.hide()
                            finish()
                        }.addOnFailureListener {
                            mProgress.hide()
                            Toast.makeText(
                                this,
                                "Ocorreu um erro. Tente novamente!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
                mAlert.setNegativeButton("Não") { dialog: DialogInterface?, which: Int -> }
                mAlert.show()
            }

        } else {
            finish()
        }

    }

    private fun notificationCancel() {
        mFirestore.collection(Constants.COLLECTIONS.USER_COLLECTION)
            .document(mMyservice?.idContratante.toString())
            .get()
            .addOnSuccessListener {
                val user = it.toObject(User::class.java)
                val notification = Notification()
                notification.serviceId = mMyservice?.serviceId
                notification.text = "${mMyservice?.nomeContratado} cancelou o serviço (${mMyservice?.serviceNome})"
                notification.title ="Nova atualização de serviço"

                mFirestore.collection(Constants.COLLECTIONS.NOTIFICATION_SERVICE)
                    .document(user?.token.toString())
                    .set(notification)
            }
    }

    private fun btnText() {
        if (mMyservice?.idContratado == mAuth.currentUser?.uid) {
            btnVoltarAndamento.text = "Cancelar"
            btnFinalizarAndamento.visibility = View.GONE
        }else{
            btnVoltarAndamento?.visibility= View.GONE
        }
    }

    private fun getEndereco() {
        mFirestore.collection(Constants.COLLECTIONS.MY_SERVICE)
            .document(mMyservice?.documentId.toString())
            .get()
            .addOnSuccessListener {
                mAdress = it.toObject(Myservice::class.java)
                fetchService()
            }
    }

    private fun fetchService() {
        mAdress?.let {
            if (mMyservice?.urlService != null) Picasso.get().load(mMyservice?.urlService).placeholder(R.drawable.ic_working).fit().centerCrop().into(
                imgAndamentoAcct
            )
            else imgAndamentoAcct.setImageResource(R.drawable.ic_working)
            txtContratadoAndamentoAcct.text = mMyservice?.nomeContratado
            txtContratanteAndamentoAcct.text = mMyservice?.nomeContratante
            txtServiceAndamentoAcct.text = mMyservice?.serviceNome
            txtObservacaoAndamento.text = mMyservice?.observacao
            txtObsProf.text = mMyservice?.observacaoProfissional
            txtPrecoAndamentoAcct.text = "R$ ${mMyservice?.preco.toString().replace(".",",")} por ${mMyservice?.tipoCobranca}"
            txtEnderecoAndamentoAcct.text = "${it.rua}, ${it.bairro}, ${it.numero} \n" +
                    "${it.cidade}, ${it.estado}, ${it.cep}"
            if (it.dateService != null){
                txtDate.text = it.dateService
            }else{
                txtDate.visibility = View.GONE
                dataText.visibility = View.GONE
            }

        }
    }

    private fun dashBoard() {
        val tsDoc = mFirestore.collection(Constants.DASHBOARD_SERVICE.DASHBOARD_COLLECTION)
            .document(Constants.DASHBOARD_SERVICE.DASHBOARD_DOCUMENT)
        mFirestore.runTransaction {
            val content = it.get(tsDoc).toObject(DashBoard::class.java)
            content!!.finishService = content.finishService + 1
            content.totalGasto = content.totalGasto + mMyservice?.preco!!.toLong()
            content.lucroLiquido = content.lucroLiquido + ((mMyservice!!.preco!!.toDouble() / 100) * 10)
            it.set(tsDoc, content)
        }
    }


}
