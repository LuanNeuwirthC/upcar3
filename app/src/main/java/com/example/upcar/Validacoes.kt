package com.example.upcar

import android.util.Patterns

private val REGEX_HORARIO = Regex("^([01]?\\d|2[0-3]):[0-5]\\d$")

private val REGEX_PLACA = Regex("^[A-Z]{3}[0-9][A-Z0-9][0-9]{2}$")

fun horarioValido(texto: String): Boolean = REGEX_HORARIO.matches(texto.trim())

fun normalizarHorario(texto: String): String {
    val (hora, minuto) = texto.trim().split(":")
    return hora.padStart(2, '0') + ":" + minuto
}

fun normalizarPlaca(texto: String): String = texto.trim().uppercase().replace("-", "")

fun placaValida(texto: String): Boolean = REGEX_PLACA.matches(normalizarPlaca(texto))


private val REGEX_RGM = Regex("^\\d{5,}-?\\d$")

fun nomeValido(texto: String): Boolean = texto.trim().length >= 3

fun rgmValido(texto: String): Boolean = REGEX_RGM.matches(texto.trim())

fun emailValido(texto: String): Boolean = Patterns.EMAIL_ADDRESS.matcher(texto.trim()).matches()

fun bairroValido(texto: String): Boolean = texto.isNotBlank()