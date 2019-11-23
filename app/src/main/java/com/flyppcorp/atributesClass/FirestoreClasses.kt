package com.flyppcorp.atributesClass

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

abstract class Endereco(
    var cep: String? = null,
    var estado: String? = null,
    var cidade: String? = null,
    var bairro: String? = null,
    var rua: String? = null,
    var numero: String? = null
)

@Parcelize
data class User(
    var nome: String? = null,
    var uid: String? = null,
    var url: String? = null,
    var ddd: String? = null,
    var telefone: String? = null,
    var email: String? = null,
    var online: Boolean = false,
    var token: String? = null,
    //Informacoes de servicos
    var totalServicosAtivos: Int = 0,
    var totalServicosFinalizados: Int = 0,
    var avaliacao: Int = 0,
    var totalAvaliacao: Int= 0
) : Endereco(), Parcelable

@Parcelize
data class Servicos(
    var nome: String? = null,
    var uid: String? = null,
    var uidProfile: MutableMap<String, Boolean> = HashMap(),
    var urlProfile: String? = null,
    var urlService: String? = null,
    var ddd: String? = null,
    var telefone: String? = null,
    var email: String? = null,
    var serviceId: String? = null,
    var nomeService: String? = null,
    var shortDesc: String? = null,
    var longDesc: String? = null,
    var preco: String? = null,
    var tipoCobranca: String? = null,
    var qualidadesDiferenciais: String? = null,
    var avaliacao: Int = 0,
    var totalAvaliacao: Int = 0,
    var totalServicos: Int = 0,
    var favoritos: MutableMap<String, Boolean> = HashMap(),
    var tags: MutableMap<String, Boolean> = HashMap(),
    var tagsStr: String? = null

) : Endereco(), Parcelable

@Parcelize
data class Message(
    var text: String? = null,
    var toId: String? = null,
    var fromId: String? = null,
    var timestamp: Long = 0
) : Parcelable


@Parcelize
data class LastMessage(
    var name: String? = null,
    var url: String? = null,
    var lastMessage: String? = null,
    var toId: String? = null,
    var timestamp: Long = 0
) : Parcelable

@Parcelize
data class NotificationMessage(
    var fromName: String? = null,
    var toId: String? = null,
    var text: String? = null,
    var fromId: String? = null,
    var timestamp: Long = 0
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
    var preco: String? = null,
    var tipoCobranca: String? = null,
    var nomeContratado: String? = null,
    var nomeContratante: String? = null,
    var documentId: String? = null,
    var shortDesc: String? = null,
    var observacao: String? = null,
    var idAvaliador: MutableMap<String, Boolean> = HashMap(),
    var pendente: Boolean = false,
    var andamento: Boolean = false,
    var finalizado: Boolean = false
) : Endereco(), Parcelable