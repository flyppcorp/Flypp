package com.flyppcorp.profile_settings

import com.flyppcorp.flypp.R

data class Profile (val logo: Int,
                    val nomeOption: String)

class ProfileBuilder{
    var logo: Int = 0
    var nomeOption: String = ""
    fun build (): Profile = Profile(logo, nomeOption)
}
fun profile(block : ProfileBuilder.() -> Unit) : Profile = ProfileBuilder().apply(block).build()

fun menuOptions()= mutableListOf(
    profile {
        logo = R.drawable.ic_my_services
        nomeOption = "Meus Serviços"
    },
    profile  {
        logo = R.drawable.ic_help
        nomeOption = "Ajuda"
    },
    profile  {
        logo = R.drawable.ic_share2
        nomeOption = "Convidar amigos"
    }
)