package com.example.progettoeafrontend.model

import kotlinx.serialization.Serializable


@Serializable
data class Utente(
    val id: Int,
    val nome: String,
    val cognome: String ) {
}