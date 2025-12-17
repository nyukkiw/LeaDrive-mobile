package com.example.leadrive

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
@Serializable
data class Jadwal(
    val id_jadwal: Int,
    val id_pemesanan: Int,
    val tanggal: String,
    val jam_mulai: String?,
    val id_instruktur: Int?,
    @SerialName("pemesanan")
    val pemesanan: Pemesanan? = null
)


