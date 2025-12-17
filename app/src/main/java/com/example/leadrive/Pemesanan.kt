package com.example.leadrive

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
@Serializable
data class Pemesanan(
    val id_pemesanan: Int,
    val id_paket: Int,
    val tanggal_pemesanan: String,
    val status_pemesanan: String,
    val id_user: Int,
    val latitude: String? = null,
    val longitude: String? = null,
    @SerialName("users")
    val users: Users? = null,
    val jadwal_kursus: List<JadwalKursus>? = null
)

@Serializable
data class Users(
    val name: String? = null
)

@Serializable
data class JadwalKursus(
    val tanggal: String? = null,
    val jam_mulai: String? = null
)