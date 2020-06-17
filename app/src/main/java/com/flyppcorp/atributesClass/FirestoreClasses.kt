package com.flyppcorp.atributesClass

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

abstract class Endereco(
    var cep: String? = "-",
    var estado: String? = "-",
    var cidade: String? = "-",
    var bairro: String? = "-",
    var rua: String? = "-",
    var numero: String? = "-"
)

@Parcelize
data class User(
    var nome: String? = null,
    var nomeEmpresa: String? = null,
    var uid: String? = null,
    var url: String? = null,
    var ddd: String? = null,
    var telefone: String? = null,
    var email: String? = null,
    //var online: Boolean = false,
    var token: String? = null,
    //Sobre servuços
    var servicosAtivos: Int = 0,
    var totalServicosFinalizados: Int = 0,
    var avaliacao: Int = 0,
    var totalAvaliacao: Int = 0,
    var primeiraCompra : Boolean = false,
    var primeiraCompraConcluida: Boolean = false,
    var desconto : Int = 0
) : Endereco(), Parcelable

@Parcelize
data class Servicos(
    var nome: String? = null,
    var uid: String? = null,
    var uidProfile: MutableMap<String, Boolean> = HashMap(),
    var urlProfile: String? = null,
    var urlService: String? = null,
    var serviceId: String? = null,
    var nomeService: String? = null,
    var shortDesc: String? = null,
    var longDesc: String? = null,
    var tempoResposta : String? = "1-5 min",
    var tempoEntrega: String? = "?",
    var preco: Float? = null,
    var avaliacao: Int = 0,
    var totalAvaliacao: Int = 0,
    var totalServicos: Int = 0,
    var favoritos: MutableMap<String, Boolean> = HashMap(),
    var tags: MutableMap<String, Boolean> = HashMap(),
    var tagsStr: String? = null,
    var cityName: String? = null,
    var visible: Boolean = true,
    var comments : Long = 0,
    var sabor : String? = null,
    var categoria : String = "Todos",
    var dias: String = "1,2,3,4,5,6,7",
    var horario: String = "06:00 - 00:00"
) : Endereco(), Parcelable

@Parcelize
data class Message(
    var text: String? = null,
    var toId: String? = null,
    var fromId: String? = null,
    var timestamp: Long = 0,
    var timestampView: Long = 0
) : Parcelable


@Parcelize
data class LastMessage(
    var name: String? = null,
    var url: String? = null,
    var text: String? = null,
    var toId: String? = null,
    var timestamp: Long = 0,
    var unread : Boolean = true
) : Parcelable

@Parcelize
data class Notification(
    var title: String? = null,
    var text: String? = null,
    var serviceId: String? = null
) : Parcelable

@Parcelize
data class NotificationMessage(
    var title: String? = null,
    var text: String? = null,
    var token: String? = null
) : Parcelable

@Parcelize
data class Myservice(
    var idContratante: String? = null,
    var idContratado: String? = null,
    var id: MutableMap<String, Boolean> = HashMap(),
    var timestamp: Long = 0,
    var serviceId: String? = null,
    var serviceNome: String? = null,
    var urlService: String? = null,
    var urlContratante: String? = null,
    var urlContratado: String? = null,
    var preco: Float? = null,
    var quantidate: Int = 1,
    var tipoCobranca: String? = "cada",
    var nomeContratado: String? = null,
    var nomeContratante: String? = null,
    var documentId: String? = null,
    var shortDesc: String? = null,
    var observacao: String? = null,
    var observacaoProfissional: String? = null,
    var idAvaliador: MutableMap<String, Boolean> = HashMap(),
    var pendente: Boolean = false,
    var andamento: Boolean = false,
    var finalizado: Boolean = false,
    var dateService: String? = null,
    var horario: String? = null,
    var caminho: Boolean = false,
    var sabor : String? = null

) : Endereco(), Parcelable

data class DashBoard(
    var newUser: Long = 0,
    var contractService: Long = 0,
    var finishService: Long = 0,
    var newServices: Long = 0,
    var totalGasto: Long = 0,
    var lucroLiquido: Double = 0.0,
    var anonimo : Long = 0
)

data class Comentarios(
    var urlContratante: String? = null,
    var nomeContratante: String? = null,
    var comentario: String? = null,
    var serviceId: String? = null,
    var commentId: String? = null,
    var uid: String? = null
)