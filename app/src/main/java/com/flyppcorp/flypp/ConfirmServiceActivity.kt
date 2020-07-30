package com.flyppcorp.flypp

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.content.Context
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TimePicker
import android.widget.Toast
import com.flyppcorp.Helper.SharedFilter
import com.flyppcorp.atributesClass.Myservice
import com.flyppcorp.atributesClass.Notification
import com.flyppcorp.atributesClass.Servicos
import com.flyppcorp.atributesClass.User
import com.flyppcorp.constants.Constants
import com.flyppcorp.firebase_classes.FirestoreContract
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_confirm_service.*
import java.util.*
import com.google.android.gms.ads.*
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import java.text.SimpleDateFormat
import kotlin.properties.Delegates

class ConfirmServiceActivity : AppCompatActivity() {
    //private var mService: Servicos? = null
    private var mServices: Servicos? = null
    private lateinit var mFirestore: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth
    private var mUser: User? = null
    private lateinit var mFirestoreContract: FirestoreContract
    private lateinit var mMyservice: Myservice
    private var data: String? = null
    private var horario: String? = null
    private var qtd by Delegates.notNull<Int>()
    private lateinit var mRemote: FirebaseRemoteConfig
    private var mCart: Servicos? = null
    private lateinit var alert: ProgressDialog

    //fim
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm_service)
        //iniciação
        qtd = 1
        mServices = intent.extras?.getParcelable(Constants.KEY.SERVICE_KEY)
        mFirestore = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()
        mFirestoreContract = FirestoreContract(this)
        mMyservice = Myservice()
        mRemote = FirebaseRemoteConfig.getInstance()
        //fim das instancias das classes

        //alert dialog para que abre ao iniciar
        alert = ProgressDialog(this)
        alert.setCancelable(false)
        alert.show()
        alert.setContentView(R.layout.progress)
        alert.window?.setBackgroundDrawableResource(android.R.color.transparent)
        //alert é mostrado

        //função que captura os dados do servico para qtd
        getDataService()

        //ações de click
        btnConfirmContract.setOnClickListener {
            if (validateConection()) {
                mFirestoreContract.progress.setCancelable(false)
                mFirestoreContract.progress.show()
                mFirestoreContract.progress.setContentView(R.layout.progress)
                mFirestoreContract.progress.window?.setBackgroundDrawableResource(android.R.color.transparent)
                //o getTosave é carregado
                getToSave()
            }
        }

        btn_tb_voltar.setOnClickListener {
            finish()
        }

        //Botões de fata data e horario
        btnData.setOnClickListener {
            handleDate()
        }

        btnHorario.setOnClickListener {
            handleHorario()
        }
        //fim dos botões

        //botões de mais e menos, lembrando que a função de recuperar dados é chamada nele a cada alteração
        mais.setOnClickListener {
            mMyservice.quantidate = mMyservice.quantidate + 1
            qtd = mMyservice.quantidate
            txtQtd.text = qtd.toString()
            getDataService()
            //handlePreco()

        }

        menos.setOnClickListener {

            if (qtd > 1) {
                mMyservice.quantidate = mMyservice.quantidate - 1
                qtd = mMyservice.quantidate
                txtQtd.text = qtd.toString()
                getDataService()
            }
        }
        //fim dos btns de quantidade

        //função que pega os dados do servico
        mFirestore.collection(Constants.COLLECTIONS.USER_COLLECTION)
            .document(mAuth.currentUser?.uid.toString())
            .get()
            .addOnSuccessListener {
                mUser = it.toObject(User::class.java)
                //a função abaixo permite a recuperação dos dados na função
                getDataService()

            }

    }

    /*private fun handlePreco() {
        var precoQuantidade = mServices?.preco!! * qtd.toFloat()
        mRemote.fetch(0).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                mRemote.fetchAndActivate()
                val off = mRemote.getString("off")
                val desconto: Double = precoQuantidade * (off.toInt() / 100.0)
                mMyservice.preco = (precoQuantidade - desconto).toFloat()
                mMyservice.quantidate = qtd
            } else if (!task.isSuccessful) {
                val desconto: Double = 0.0
                mMyservice.preco = (precoQuantidade - desconto).toFloat()
                mMyservice.quantidate = qtd
            }

        }
    }*/


    //função que captura o horario
    private fun handleHorario(): String? {
        val calendar = Calendar.getInstance()
        val timeSetListener =
            TimePickerDialog.OnTimeSetListener { timepicker: TimePicker?, hourOfDay: Int, minute: Int ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                horario = SimpleDateFormat("HH:mm").format(calendar.time)
                btnHorario.text = horario


            }
        TimePickerDialog(
            this,
            timeSetListener,
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
        return horario
    }

    //função que captura os dados para salvar, há outra função dentro dela
    private fun getToSave() {
        mFirestore.collection(Constants.COLLECTIONS.USER_COLLECTION)
            .document(mAuth.currentUser?.uid.toString())
            .get()
            .addOnSuccessListener {
                mUser = it.toObject(User::class.java)
                //fun que realmente salva
                handleConfirm()
            }
    }

    //manipular datas
    private fun handleDate(): String? {

        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->

                when {
                    month <= 8 && dayOfMonth < 10 -> {
                        data = "0$dayOfMonth / 0${month + 1} / $year"
                        btnData.text = "0$dayOfMonth / 0${month + 1}/ $year"

                    }
                    month >= 9 && dayOfMonth < 10 -> {
                        data = "0$dayOfMonth / ${month + 1} / $year"
                        btnData.text = "0$dayOfMonth / ${month + 1}/ $year"
                    }
                    month <= 8 && dayOfMonth > 10 -> {
                        data = "$dayOfMonth / 0${month + 1} / $year"
                        btnData.text = "$dayOfMonth / 0${month + 1}/ $year"
                    }
                    else -> {
                        data = "$dayOfMonth / ${month + 1} / $year"
                        btnData.text = "$dayOfMonth / ${month + 1}/ $year"
                    }
                }


            },
            year,
            month,
            day
        )
        dpd.show()
        return data
    }

    //função que aponta dados e salva
    private fun handleConfirm() {
        mUser?.let {
            //uid
            mMyservice.idContratante = it.uid
            //id contratado
            mMyservice.idContratado = mServices?.uid.toString()
            //id para consulta no manager
            mMyservice.id[it.uid.toString()] = true
            mMyservice.id[mServices?.uid.toString()] = true
            //hora da compra
            mMyservice.timestamp = System.currentTimeMillis()
            //opcoes de data e hora para pedido
            mMyservice.dateService = data
            mMyservice.horario = horario
            //service id
            mMyservice.serviceId = mServices?.serviceId.toString()
            //url
            mMyservice.urlService = mServices?.urlService
            //nome service
            mMyservice.serviceNome = mServices?.nomeService
            //url contratante
            mMyservice.urlContratante = it.url
            mMyservice.urlContratado = mServices?.urlProfile

            mMyservice.observacao = editObservacao.text.toString()
            //salva a quantidade
            mMyservice.quantidate = qtd
            // fim Quantidade

            //sabor
            if (mServices?.sabor != null) {
                mMyservice.sabor = spinnerSabor?.selectedItem.toString()
            } else {
                mMyservice.sabor = null
            }
            //fim sabor

            //entrega
            if (delivery.isChecked){
                mMyservice.delivery = true
            }else if (retirada.isChecked){
                mMyservice.delivery = false
            }

            //fim contratado e contratante
            mMyservice.nomeContratado = mServices?.nome
            mMyservice.nomeContratante = it.nome
            //desc
            mMyservice.shortDesc = mServices?.shortDesc
            //obs
            //enderecos
            mMyservice.cep = edtCepConfirm.text.toString()
            mMyservice.estado = edtEstadoConfirm.text.toString()
            mMyservice.cidade = edtCidadeConfirm.text.toString()
            mMyservice.bairro = edtBairroConfirm.text.toString()
            mMyservice.rua = edtRuaConfirm.text.toString()
            mMyservice.numero = edtNumConfirm.text.toString()
            //status
            mMyservice.pendente = true
            mMyservice.finalizado = false
            mMyservice.andamento = false
            //obs
            mMyservice.observacao = editObservacao.text.toString()
            //docID é o nome que o documento vai receber para ser manipulado posteriormente
            val documentId = it.uid + UUID.randomUUID().toString()
            mMyservice.documentId = documentId

            //notificação
            mFirestore.collection(Constants.COLLECTIONS.USER_COLLECTION)
                .document(mMyservice.idContratado.toString())
                .get()
                .addOnSuccessListener { info ->

                    val user: User? = info.toObject(User::class.java)
                    val notification = Notification()
                    notification.serviceId = documentId
                    notification.text =
                        "${mMyservice.nomeContratante} está fazendo um pedido (${mMyservice.serviceNome})"
                    notification.title = "Novo pedido"

                    mFirestoreContract.confirmServiceContract(
                        mMyservice,
                        documentId,
                        user?.token.toString(),
                        notification
                    )
                }
            //se for a primeira compra ele faz um transaction e altera a variável
            if (!it.primeiraCompra) {
                mFirestoreContract.tsFirstCompra(it.uid.toString())
            }


        }
    }

    //show data
    private fun getDataService() {
        mUser?.let {
            //nome
            nomeContratante.text = it.nome
            //nome produto
            txtServicoContratar.text = mServices?.nomeService
            //sabor
            if (mServices?.sabor != null) {
                textView14?.visibility = View.VISIBLE
                spinnerSabor?.visibility = View.VISIBLE
                val sabores = mServices?.sabor.toString().split(",")
                spinnerSabor?.adapter =
                    ArrayAdapter(this, android.R.layout.simple_list_item_1, sabores)


            }

            //fim sabor

            //entrega
            if (mServices?.delivery == false){
                delivery.isEnabled = false
                retirada.isChecked = true
            }
            //preco x quantidade e desconto
            //mascara de preco
            var precoQtd = mServices?.preco!! * qtd.toFloat()
            if (!it.primeiraCompra) {
                alert.hide()
                val desconto: Double = precoQtd * (10 / 100.0)
                precoQtd = (precoQtd - desconto).toFloat()
                val result = String.format("%.2f", precoQtd)
                txtPrecoContratante.text = "R$ ${result}".replace(".", ",")
                //detalhe importante, o preco é a unica variavel da classe que não salva no botão salvar e sim no load desta função
                //que ocorre na abertura da activity
                mMyservice.preco = precoQtd


            } else {
                mRemote.fetch(0).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        mRemote.fetchAndActivate()
                        val off = mRemote.getString("off")
                        val desconto: Double = precoQtd * (off.toInt() / 100.0)
                        precoQtd = (precoQtd - desconto).toFloat()
                        val result = String.format("%.2f", precoQtd)
                        txtPrecoContratante.text = "R$ ${result}".replace(".", ",")
                        //detalhe importante, o preco é a unica variavel da classe que não salva no botão salvar e sim no load desta função
                        //que ocorre na abertura da activity
                        mMyservice.preco = precoQtd

                    } else if (!task.isSuccessful) {
                        val desconto: Double = 0.0
                        precoQtd = (precoQtd - desconto).toFloat()

                        val result = String.format("%.2f", precoQtd)
                        txtPrecoContratante.text = "R$ ${result}".replace(".", ",")
                        //detalhe importante, o preco é a unica variavel da classe que não salva no botão salvar e sim no load desta função
                        //que ocorre na abertura da activity
                        mMyservice.preco = precoQtd
                    }
                    //o alert do onCreate, só fecha depois que o Remote Config carrega
                    alert.hide()

                }

            }
            //fim

            //endereco
            if (it.rua != null) edtRuaConfirm.setText(it.rua)
            if (it.bairro != null) edtBairroConfirm.setText(it.bairro)
            if (it.cidade != null) edtCidadeConfirm.setText(it.cidade)
            if (it.estado != null) edtEstadoConfirm.setText(it.estado)
            if (it.numero != null) edtNumConfirm.setText(it.numero)
            if (it.cep != null) edtCepConfirm.setText(it.cep)


        }
    }

    private fun validateConection(): Boolean {
        val cm = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = cm.activeNetworkInfo
        if (networkInfo != null && networkInfo.isConnected) {
            return true
        } else {
            //progressBar.visibility = View.GONE
            Toast.makeText(this, "Você não possui conexão com a internet", Toast.LENGTH_SHORT)
                .show()
            return false
        }

    }
}
