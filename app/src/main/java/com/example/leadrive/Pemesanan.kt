package com.example.leadrive

import kotlinx.serialization.Serializable

@Serializable
data class Pemesanan(
    val id_pemesanan: Int,
    val id_paket: Int,
    val tanggal_pemesanan: String,
    val status_pemesanan: String,
    val id_user: Int,
    val latitude: String? = null,
    val longitude: String? = null,
    val users: Users? = null
)

@Serializable
data class Users(
    val name: String? = null
)