package com.example.leadrive

import kotlinx.serialization.Serializable

@Serializable
data class Jadwal(
    val id_jadwal: Int,
    val id_pemesanan: Int,
    val tanggal: String,
    val jam_mulai: String?,
    val id_instruktur: Int?
)
