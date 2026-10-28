package com.example.upcar

import androidx.room.Entity
import androidx.room.PrimaryKey

const val USER_AGENT_OSM = "UpCar/1.0 (trabalho academico UP; contato: seu-email@exemplo.com)"
const val DESTINO_PADRAO = "Campus Centro • Bloco B"

const val CAMPUS_LAT = -25.4284
const val CAMPUS_LNG = -49.2733

@Entity(tableName = "motoristas")
data class Motorista(
    @PrimaryKey val id: Int,
    val nome: String,
    val nota: Double,
    val horario: String,
    val preco: Double,
    val corridas: Int,
    val carro: String,
    val placa: String,
    val origem: String,
    val destino: String,
    val lat: Double,
    val lng: Double
)

@Entity(tableName = "caronas")
data class Carona(
    @PrimaryKey val id: Int,
    val motoristaId: Int,
    val origem: String,
    val destino: String,
    val horario: String,
    val valor: Double,
    val confirmada: Boolean = false,
    val aguardandoResposta: Boolean = false
)

@Entity(tableName = "mensagens")
data class Mensagem(
    @PrimaryKey val id: Int,
    val caronaId: Int,
    val texto: String,
    val horario: String,
    val minha: Boolean
)

@Entity(tableName = "passageiros")
data class Passageiro(
    val nome: String,
    val rgm: String,
    val email: String,
    val bairroOrigem: String,
    val fotoUri: String? = null,
    @PrimaryKey val id: Int = 1
)

@Entity(tableName = "avaliacoes")
data class AvaliacaoCorrida(
    @PrimaryKey val caronaId: Int,
    val nota: Int,
    val comentario: String,
    val elogios: List<String>
)
