package com.flyppcorp.flypp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TimePicker
import android.widget.Toast
import com.flyppcorp.atributesClass.Myservice
import com.flyppcorp.atributesClass.Notification
import com.flyppcorp.atributesClass.Servicos
import com.flyppcorp.atributesClass.User
import com.flyppcorp.constants.Constants
import com.flyppcorp.firebase_classes.FirestoreContract
import com.google.android.gms.ads.InterstitialAd
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_confirm_service.*
import java.util.*
import com.google.android.gms.ads.*
import kotlinx.android.synthetic.main.activity_pendente.*
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
    private var user: User? = null
    private var data: String? = null
    private var horario: String? = null
    private var qtd by Delegates.notNull<Int>()

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
        mFirestoreContract.mIntertial = InterstitialAd(applicationContext)
        MobileAds.initialize(this)
        mFirestoreContract.mIntertial.adUnitId = getString(R.string.ads_intertitial_id)
        mFirestoreContract.mIntertial.loadAd(AdRequest.Builder().build())
        mMyservice = Myservice()
        //fim

        //função que captura os dados do servico para qtd
        getDataService()

        //ações de click
        btnConfirmContract.setOnClickListener {
            if (validateConection()) {
                getToSave()
            }
        }

        btn_tb_voltar.setOnClickListener {
            finish()
        }

        btnData.setOnClickListener {
            handleDate()
        }

        btnHorario.setOnClickListener {
            handleHorario()
        }

        mais.setOnClickListener {
            mMyservice.quantidate = mMyservice.quantidate + 1
            qtd = mMyservice.quantidate
            txtQtd.text = qtd.toString()
            getDataService()

        }

        menos.setOnClickListener {

            if (qtd > 1) {
                mMyservice.quantidate = mMyservice.quantidate - 1
                qtd = mMyservice.quantidate
                txtQtd.text = qtd.toString()
                getDataService()
            }
        }

        //fim

        //função que pega os dados do servico
        mFirestore.collection(Constants.COLLECTIONS.USER_COLLECTION)
            .document(mAuth.currentUser?.uid.toString())
            .get()
            .addOnSuccessListener {
                mUser = it.toObject(User::class.java)
                //função que usa os dados
                getDataService()

            }

    }

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
            //preco de acordo com a quantidade
            mMyservice.preco = mServices?.preco!! * qtd.toFloat()
            mMyservice.quantidate = qtd
            // fim Quantidade

            //fim contratado e contratante
            mMyservice.nomeContratado = mServices?.nome
            mMyservice.nomeContratante = it.nome
            //desc
            mMyservice.shortDesc = mServices?.shortDesc
            //enderecos
            mMyservice.cep = it.cep
            mMyservice.estado = it.estado
            mMyservice.cidade = it.cidade
            mMyservice.bairro = it.bairro
            mMyservice.rua = it.rua
            mMyservice.numero = it.numero
            //status
            mMyservice.pendente = true
            mMyservice.finalizado = false
            mMyservice.andamento = false
            //obs
            mMyservice.observacao = editObservacao.text.toString()
            //docID é o nome que o documento vai receber para ser manipulado posteriormente
            val documentId = UUID.randomUUID().toString() + it.uid
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
                        "${mMyservice.nomeContratante} está solicitando um pedido (${mMyservice.serviceNome})"
                    notification.title = "Novo pedido"

                    mFirestoreContract.confirmServiceContract(
                        mMyservice, documentId, user?.token.toString(), notification
                    )
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
            //preco x quantidade
            val precoQtd = mServices?.preco!! * qtd.toFloat()
            //mascara de preco
            if (precoQtd.toString().substringAfter(".").length == 1) {
                txtPrecoContratante.text =
                    "R$ ${precoQtd.toString().replace(
                        ".",
                        ","
                    )}${"0"}"
            } else {
                txtPrecoContratante.text =
                    "R$ ${precoQtd.toString().replace(
                        ".",
                        ","
                    )}"
            }
            //fim

            //endereco
            txtEnderecoContratante.text =
                "${it.rua}, ${it.bairro}, ${it.numero} \n ${it.cidade}, ${it.estado}, ${it.cep}"

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
