package com.example.upcar

import java.util.Locale

private val LOCALE_BR: Locale = Locale.forLanguageTag("pt-BR")

fun formatarReais(valor: Double): String =
    "R$ " + String.format(LOCALE_BR, "%.2f", valor)

fun formatarNota(nota: Double): String =
    String.format(Locale.US, "%.1f", nota)