package com.kmuaz.alistcloud.data.network.model

data class LoginResponse(

    val code: Int,

    val message: String,

    val data: LoginData?

)

data class LoginData(

    val token: String?,

    val device_key: String? = null

)