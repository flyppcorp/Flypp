package com.flyppcorp.managerServices

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
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
import com.tapadoo.alerter.Alerter
import kotlinx.android.synthetic.main.activity_andamento.*
import kotlinx.android.synthetic.main.activity_andamento.txtQuantidade
import kotlinx.android.synthetic.main.activity_andamento.txtSabor


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
            if (mConnection.validateConection()) {
                val alertDialog = AlertDialog.Builder(this)
                alertDialog.setMessage("Você tem certeza que deseja finalizar este pedido?")
                alertDialog.setNegativeButton("Não", {dialogInterface, i ->  })
                alertDialog.setPositiveButton("Sim", {dialogInterface, i ->
                    mProgress.setCancelable(false)
                    mProgress.show()
                    mProgress.setContentView(R.layout.progress)
                    mProgress.window?.setBackgroundDrawableResource(android.R.color.transparent)
                    handleFinalizar()
                })
                alertDialog.show()

            }

        }

        floatingActionButton?.setOnClickListener {
            notificationRun()
        }

        floatingActionButton?.setOnLongClickListener {
            Toast.makeText(this, "Avisar que está a caminho", Toast.LENGTH_LONG).show()
            return@setOnLongClickListener true
        }

        floatingActionButtonFinish?.setOnClickListener {
            notificationFinish()
        }
        floatingActionButtonFinish.setOnLongClickListener {
            Toast.makeText(this, "Pedir para cliente finalizar", Toast.LENGTH_LONG).show()
            return@setOnLongClickListener true
        }

        floatingActionButtonRetirada.setOnLongClickListener {
            Toast.makeText(this, "Pedir para cliente retirar pedido no balcão", Toast.LENGTH_LONG).show()
            return@setOnLongClickListener true
        }
        floatingActionButtonRetirada.setOnClickListener {
            notificationRetirada()
        }
        val tb = findViewById<androidx.appcompat.widget.Toolbar>(R.id.tb_andamento)
        tb.title = ""
        setSupportActionBar(tb)

        btnVoltarTbAndamento.setOnClickListener {
            finish()
        }
        txtTitleAndamento.text = "Andamento"


        getEndereco()
        btnText()
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
                if (mAuth.currentUser?.uid == mMyservice?.idContratante) {
                    mFirestore.collection(Constants.COLLECTIONS.USER_COLLECTION)
                        .document(mMyservice?.idContratado.toString())
                        .get()
                        .addOnSuccessListener {
                            val user = it.toObject(User::class.java)
                            val intent = Intent(this, MessageActivity::class.java)
                            intent.putExtra(Constants.KEY.MESSAGE_KEY, user)
                            startActivity(intent)
                            mProgress.hide()
                        }
                } else if (mAuth.currentUser?.uid == mMyservice?.idContratado) {
                    mFirestore.collection(Constants.COLLECTIONS.USER_COLLECTION)
                        .document(mMyservice?.idContratante.toString())
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

                if (uid == mMyservice?.idContratado) {
                    phoneCall(mMyservice?.idContratante.toString())

                } else if (uid == mMyservice?.idContratante) {
                    phoneCall(mMyservice?.idContratado.toString())

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

        }.addOnSuccessListener {
            handleCocluiu(mMyservice?.idContratante)
            val tsServiceDoc = mFirestore.collection(Constants.COLLECTIONS.SERVICE_COLLECTION)
                .document(mMyservice?.serviceId.toString())
            mFirestore.runTransaction {
                val contentService = it.get(tsServiceDoc).toObject(Servicos::class.java)

                contentService?.totalServicos = contentService?.totalServicos!!.toInt() + 1
                it.set(tsServiceDoc, contentService)
            }.addOnSuccessListener {
                if (mMyservice?.idContratante == mAuth.currentUser?.uid) {
                    val intent = Intent(this, AvaliationActivity::class.java)
                    intent.putExtra(Constants.KEY.SERVICE_STATUS, mMyservice)
                    startActivity(intent)
                    finish()
                }else {
                    finish()
                }
                finish()
            }
        }


    }

    private fun handleCocluiu(idContratante: String?) {
        val tsDoc = mFirestore.collection(Constants.COLLECTIONS.USER_COLLECTION)
            .document(idContratante.toString())
        mFirestore.runTransaction {
            val content = it.get(tsDoc).toObject(User::class.java)
            if (!content!!.primeiraCompraConcluida) {
                content.primeiraCompraConcluida = true
            }


            it.set(tsDoc, content)
        }
    }

    private fun servicosFinalizado(uid: String) {
        val tsDoc = mFirestore.collection(Constants.COLLECTIONS.USER_COLLECTION).document(uid)
        mFirestore.runTransaction {
            val content = it.get(tsDoc).toObject(User::class.java)
            content!!.totalServicosFinalizados = content.totalServicosFinalizados + 1
            if (mMyservice?.idContratante == mAuth.currentUser?.uid) {
                //alterei aqui
                notification(mMyservice?.idContratado.toString(), mMyservice?.nomeContratante.toString(), "Concluiu um pedido")

            } else {
                //alterei aqui

                notification(mMyservice?.idContratante.toString(), mMyservice?.nomeContratado.toString(), "Concluiu o pedido. (${mMyservice?.serviceNome}) " +
                        "\nVocê pode deixar sua avaliação indo ao seu histórico de compras em:" +
                        "\nGerenciador de pedidos > Finalizado > Seu pedido.")
            }
            it.set(tsDoc, content)

        }
    }

    private fun notification(uid: String, nome: String, text: String) {
        mFirestore.collection(Constants.COLLECTIONS.USER_COLLECTION)
            .document(uid)
            .get()
            .addOnSuccessListener {
                val user: User? = it.toObject(User::class.java)
                val notification = Notification()
                notification.serviceId = mMyservice?.documentId
                notification.text =
                    //alterei aqui
                    "${nome} ${text}"
                notification.title = "Nova atualização de pedido"

                mFirestore.collection(Constants.COLLECTIONS.NOTIFICATION_SERVICE)
                    .document(user?.token.toString())
                    .set(notification)
            }

    }


    @SuppressLint("RestrictedApi")
    private fun notificationRun() {
        mFirestore.collection(Constants.COLLECTIONS.USER_COLLECTION)
            .document(mMyservice?.idContratante.toString())
            .get()
            .addOnSuccessListener {
                val user: User? = it.toObject(User::class.java)
                val notification = Notification()
                notification.serviceId = mMyservice!!.documentId
                notification.text =
                    "Quase lá, ${mMyservice!!.nomeContratado} está a caminho"
                notification.title = "Boas notícias"

                mFirestore.collection(Constants.COLLECTIONS.NOTIFICATION_SERVICE)
                    .document(user?.token.toString())
                    .set(notification).addOnSuccessListener {

                        val tsDoc = mFirestore.collection(Constants.COLLECTIONS.MY_SERVICE)
                            .document(mMyservice?.documentId.toString())
                        mFirestore.runTransaction {
                            val content = it.get(tsDoc).toObject(Myservice::class.java)
                            if (mMyservice?.caminho == false) {
                                content?.caminho = true
                            }
                            it.set(tsDoc, content!!)
                        }

                        val colorFilter =
                            PorterDuffColorFilter(Color.rgb(22, 160, 133), PorterDuff.Mode.MULTIPLY)
                        floatingActionButton?.background?.colorFilter = colorFilter
                        Alerter.create(this)
                            .setTitle("Quase lá!")
                            .setText("${mMyservice?.nomeContratante} já foi avisado que você está a caminho")
                            .setIcon(R.drawable.ic_run)
                            .setBackgroundColorRes(R.color.colorPrimaryDark)
                            .setDuration(5000)
                            .enableProgress(true)
                            .setProgressColorRes(R.color.textIcons)
                            .enableSwipeToDismiss()
                            .show()
                        if (mMyservice?.idContratado == mAuth.currentUser?.uid){
                            floatingActionButtonFinish?.visibility = View.VISIBLE
                        }
                    }
            }

    }
    @SuppressLint("RestrictedApi")
    private fun notificationRetirada() {
        mFirestore.collection(Constants.COLLECTIONS.USER_COLLECTION)
            .document(mMyservice?.idContratante.toString())
            .get()
            .addOnSuccessListener {
                val user: User? = it.toObject(User::class.java)
                val notification = Notification()
                notification.serviceId = mMyservice!!.documentId
                notification.text =
                    "Opa, ${mMyservice!!.serviceNome} está Pronto e a sua espera, você já pode vim buscar"
                notification.title = "Boas notícias"

                mFirestore.collection(Constants.COLLECTIONS.NOTIFICATION_SERVICE)
                    .document(user?.token.toString())
                    .set(notification).addOnSuccessListener {

                        val tsDoc = mFirestore.collection(Constants.COLLECTIONS.MY_SERVICE)
                            .document(mMyservice?.documentId.toString())
                        mFirestore.runTransaction {
                            val content = it.get(tsDoc).toObject(Myservice::class.java)
                            if (mMyservice?.pronto == false) {
                                content?.pronto = true
                            }
                            it.set(tsDoc, content!!)
                        }

                        val colorFilter =
                            PorterDuffColorFilter(Color.rgb(37, 116, 169), PorterDuff.Mode.MULTIPLY)
                        floatingActionButtonRetirada?.background?.colorFilter = colorFilter
                        Alerter.create(this)
                            .setTitle("Quase lá!")
                            .setText("${mMyservice?.nomeContratante} já foi avisado para vim retirar o pedido")
                            .setIcon(R.drawable.ic_retirada)
                            .setBackgroundColorRes(R.color.colorPrimaryDark)
                            .setDuration(5000)
                            .enableProgress(true)
                            .setProgressColorRes(R.color.textIcons)
                            .enableSwipeToDismiss()
                            .show()
                        if (mMyservice?.idContratado == mAuth.currentUser?.uid){
                            floatingActionButtonFinish?.visibility = View.VISIBLE
                        }
                    }
            }

    }


    private fun notificationFinish() {
        mFirestore.collection(Constants.COLLECTIONS.USER_COLLECTION)
            .document(mMyservice?.idContratante.toString())
            .get()
            .addOnSuccessListener {
                val user: User? = it.toObject(User::class.java)
                val notification = Notification()
                notification.serviceId = mMyservice!!.documentId
                notification.text =
                    "Aí sim, ${mMyservice!!.nomeContratado} entregou seu pedido, por favor o finalize rapidinho." +
                            "\nBasta clicar no ícone de Sacola > Andamento > Selecionar o pedido e finalizar :)"

                notification.title = "Uhul"

                mFirestore.collection(Constants.COLLECTIONS.NOTIFICATION_SERVICE)
                    .document(user?.token.toString())
                    .set(notification).addOnSuccessListener {

                        Alerter.create(this)
                            .setTitle("Pronto!")
                            .setText("${mMyservice?.nomeContratante} já foi avisado que precisa finalizar o pedido")
                            .setIcon(R.drawable.ic_verified)
                            .setBackgroundColorRes(R.color.colorPrimaryDark)
                            .setDuration(5000)
                            .enableProgress(true)
                            .setProgressColorRes(R.color.textIcons)
                            .enableSwipeToDismiss()
                            .show()
                    }
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
                    mProgress.setContentView(R.layout.progress)
                    mProgress.window?.setBackgroundDrawableResource(android.R.color.transparent)
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
                notification.text =
                    "${mMyservice?.nomeContratado} cancelou o pedido (${mMyservice?.serviceNome})"
                notification.title = "Nova atualização de pedido"

                mFirestore.collection(Constants.COLLECTIONS.NOTIFICATION_SERVICE)
                    .document(user?.token.toString())
                    .set(notification)
            }
    }

    @SuppressLint("RestrictedApi")
    private fun btnText() {
        if (mMyservice?.idContratado == mAuth.currentUser?.uid) {
            btnVoltarAndamento.text = "Cancelar"
            //Primeira mexida foi aqui
            //btnFinalizarAndamento.visibility = View.GONE
            textView11.text = "Este pedido está em andamento e aguardando ser entregue e finalizado"
        } else {
            btnVoltarAndamento?.visibility = View.GONE
            textView11.text = "Este pedido está em andamento, finalize somente após receber"
        }

        if (mMyservice?.caminho == true) {
            val colorFilter =
                PorterDuffColorFilter(Color.rgb(22, 160, 133), PorterDuff.Mode.MULTIPLY)
            floatingActionButton?.background?.colorFilter = colorFilter
        }

        if (mMyservice?.pronto == true){
            val colorFilter =
                PorterDuffColorFilter(Color.rgb(37, 116, 169), PorterDuff.Mode.MULTIPLY)
            floatingActionButtonRetirada?.background?.colorFilter = colorFilter
        }
        if (mMyservice?.idContratante == mAuth.currentUser?.uid) {
            floatingActionButton?.visibility = View.GONE
            floatingActionButtonRetirada.visibility = View.GONE
        }
        if (mMyservice?.idContratado == mAuth.currentUser?.uid && mMyservice?.caminho == true ||
            mMyservice?.idContratado == mAuth.currentUser?.uid && mMyservice?.pronto == true){
            floatingActionButtonFinish?.visibility = View.VISIBLE
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
            if (mMyservice?.urlService != null) Picasso.get().load(mMyservice?.urlService)
                .placeholder(R.drawable.ic_working).fit().centerCrop().into(
                    imgAndamentoAcct
                )
            else imgAndamentoAcct.setImageResource(R.drawable.ic_working)
            txtContratadoAndamentoAcct.text = mMyservice?.nomeContratado
            txtContratanteAndamentoAcct.text = mMyservice?.nomeContratante
            txtServiceAndamentoAcct.text = mMyservice?.serviceNome
            if (mMyservice?.observacao != null) {
                txtObservacaoAndamento?.text = mMyservice?.observacao
            } else {
                txtObservacaoAndamento?.visibility = View.GONE
                titileobs?.visibility = View.GONE
            }

            if (mMyservice?.observacaoProfissional != null) {
                txtObsProf?.text = mMyservice?.observacaoProfissional
            } else {
                txtObsProf?.visibility = View.GONE
                titleobsEst?.visibility = View.GONE
            }
            val result = String.format("%.2f", mMyservice?.preco)
            txtPrecoAndamentoAcct.text = "R$ ${result}".replace(".",",")

            if (mMyservice?.delivery == true){
                txtTipoEntregaAndamento.text = "Delivery"
            }else {
                txtTipoEntregaAndamento.text = "Retirada no estabelecimento"
            }

            if (mMyservice?.sabor != null) {
                txtSabor?.text = mMyservice?.sabor
            } else {
                titileSabor?.visibility = View.GONE
                txtSabor?.visibility = View.GONE
            }

            txtEnderecoAndamentoAcct.text = "${it.rua}, ${it.bairro}, ${it.numero} \n" +
                    "${it.cidade}, ${it.estado}, ${it.cep}".replace("null", "-")
            if (it.dateService != null && it.horario != null) {
                txtDate?.text = "${it.dateService} ás ${it.horario}"
            } else {
                txtDate?.visibility = View.GONE
                dataText?.visibility = View.GONE
            }
            txtQuantidade.text = it.quantidate.toString()


        }
    }

    private fun dashBoard() {
        val tsDoc = mFirestore.collection(Constants.DASHBOARD_SERVICE.DASHBOARD_COLLECTION)
            .document(Constants.DASHBOARD_SERVICE.DASHBOARD_DOCUMENT)
        mFirestore.runTransaction {
            val content = it.get(tsDoc).toObject(DashBoard::class.java)
            content!!.finishService = content.finishService + 1
            content.totalGasto = content.totalGasto + mMyservice?.preco!!.toLong()
            content.lucroLiquido =
                content.lucroLiquido + ((mMyservice!!.preco!!.toDouble() / 100) * 10)
            it.set(tsDoc, content)
        }
    }


}
