package com.example.upcar

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun horaAgora(): String =
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

private const val DURACAO_SIMULACAO_MS = 60_000L
private const val TICK_SIMULACAO_MS = 500L

class CaronaViewModel(application: Application = Application()) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val motoristaDao = db.motoristaDao()
    private val caronaDao = db.caronaDao()
    private val mensagemDao = db.mensagemDao()
    private val passageiroDao = db.passageiroDao()
    private val avaliacaoDao = db.avaliacaoDao()

    val motoristas = mutableStateListOf<Motorista>()
    val caronas = mutableStateListOf<Carona>()
    val mensagens = mutableStateListOf<Mensagem>()

    var passageiro by mutableStateOf<Passageiro?>(null)
        private set

    // caronaId -> progresso da corrida simulada (0f..1f)
    val progressoCorridas = mutableStateMapOf<Int, Float>()

    // Caronas cuja corrida já foi finalizada (cada uma soma 1 corrida ao motorista uma vez).
    val corridasFinalizadas = mutableStateListOf<Int>()

    // caronaId -> avaliação do passageiro (uma por corrida).
    val avaliacoesCorridas = mutableStateMapOf<Int, AvaliacaoCorrida>()

    private var proximoMotoristaId = 1
    private var proximaCaronaId = 1
    private var proximaMensagemId = 1

    init {
        viewModelScope.launch {
            // Carrega Passageiro salvo
            val p = passageiroDao.getPassageiro()
            if (p != null) {
                passageiro = p
            }

            // Carrega Motoristas do banco de dados (ou popula os iniciais)
            val motoristasSalvos = motoristaDao.getTodos()
            if (motoristasSalvos.isEmpty()) {
                val iniciais = motoristasIniciais()
                motoristaDao.salvarTodos(iniciais)
                motoristas.clear()
                motoristas.addAll(iniciais)
            } else {
                motoristas.clear()
                motoristas.addAll(motoristasSalvos)
            }
            proximoMotoristaId = (motoristas.maxOfOrNull { it.id } ?: 0) + 1

            // Carrega Caronas salvas
            val caronasSalvas = caronaDao.getTodas()
            caronas.clear()
            caronas.addAll(caronasSalvas)
            proximaCaronaId = (caronas.maxOfOrNull { it.id } ?: 0) + 1

            // Carrega Mensagens salvas
            val mensagensSalvas = mensagemDao.getTodas()
            mensagens.clear()
            mensagens.addAll(mensagensSalvas)
            proximaMensagemId = (mensagens.maxOfOrNull { it.id } ?: 0) + 1

            // Carrega Avaliações salvas
            val avaliacoesSalvas = avaliacaoDao.getTodas()
            avaliacoesSalvas.forEach { avaliacao ->
                avaliacoesCorridas[avaliacao.caronaId] = avaliacao
                if (avaliacao.caronaId !in corridasFinalizadas) {
                    corridasFinalizadas.add(avaliacao.caronaId)
                }
            }
        }
    }

    fun motoristaPorId(id: Int): Motorista? = motoristas.firstOrNull { it.id == id }

    fun caronaPorId(id: Int): Carona? = caronas.firstOrNull { it.id == id }

    fun mensagensDaCarona(caronaId: Int): List<Mensagem> =
        mensagens.filter { it.caronaId == caronaId }

    fun adicionarMotorista(
        nome: String,
        carro: String,
        placa: String,
        horario: String,
        preco: Double,
        origem: String,
        lat: Double? = null,
        lng: Double? = null
    ) {
        val id = proximoMotoristaId++
        val latFinal = lat ?: (CAMPUS_LAT + ((id * 37) % 21 - 10) * 0.0007)
        val lngFinal = lng ?: (CAMPUS_LNG + ((id * 53) % 21 - 10) * 0.0007)
        val novo = Motorista(id, nome, 5.0, horario, preco, 0, carro, placa, origem, DESTINO_PADRAO, latFinal, lngFinal)
        motoristas.add(novo)

        viewModelScope.launch {
            motoristaDao.salvar(novo)
        }
    }

    fun removerMotorista(motorista: Motorista) {
        val idsCaronas = caronas.filter { it.motoristaId == motorista.id }.map { it.id }
        mensagens.removeAll { it.caronaId in idsCaronas }
        caronas.removeAll { it.motoristaId == motorista.id }
        motoristas.remove(motorista)

        viewModelScope.launch {
            if (idsCaronas.isNotEmpty()) {
                mensagemDao.removerPorCaronas(idsCaronas)
            }
            caronas.filter { it.motoristaId == motorista.id }.forEach { caronaDao.remover(it) }
            motoristaDao.remover(motorista)
        }
    }

    fun solicitarCarona(motorista: Motorista): Carona {
        val pendente = caronas.firstOrNull { it.motoristaId == motorista.id && !it.confirmada }
        if (pendente != null) return pendente

        val carona = Carona(
            id = proximaCaronaId++,
            motoristaId = motorista.id,
            origem = motorista.origem,
            destino = motorista.destino,
            horario = motorista.horario,
            valor = motorista.preco
        )
        caronas.add(carona)

        val msgInicial = Mensagem(
            id = proximaMensagemId++,
            caronaId = carona.id,
            texto = "Olá! Tudo bem? Vi que você pediu a carona para o ${motorista.destino}.",
            horario = horaAgora(),
            minha = false
        )
        mensagens.add(msgInicial)

        viewModelScope.launch {
            caronaDao.salvar(carona)
            mensagemDao.salvar(msgInicial)
        }

        return carona
    }

    fun enviarMensagem(caronaId: Int, texto: String, minha: Boolean = true) {
        val mensagem = Mensagem(proximaMensagemId++, caronaId, texto, horaAgora(), minha)
        mensagens.add(mensagem)

        viewModelScope.launch {
            mensagemDao.salvar(mensagem)
        }
    }

    fun removerMensagem(mensagem: Mensagem) {
        mensagens.remove(mensagem)

        viewModelScope.launch {
            mensagemDao.remover(mensagem)
        }
    }

    fun enviarContraproposta(caronaId: Int, valor: Double) {
        val i = caronas.indexOfFirst { it.id == caronaId }
        if (i < 0) return
        val carona = caronas[i]
        if (carona.confirmada || carona.aguardandoResposta) return

        val caronaAtualizada = carona.copy(valor = valor, aguardandoResposta = true)
        caronas[i] = caronaAtualizada
        enviarMensagem(caronaId, "Que tal ${formatarReais(valor)}?")

        viewModelScope.launch {
            caronaDao.salvar(caronaAtualizada)

            delay(1500)
            val j = caronas.indexOfFirst { it.id == caronaId }
            if (j >= 0) {
                val caronaAceita = caronas[j].copy(aguardandoResposta = false)
                caronas[j] = caronaAceita
                caronaDao.salvar(caronaAceita)
                enviarMensagem(caronaId, "Combinado! Pode ser ${formatarReais(valor)}.", minha = false)
            }
        }
    }

    fun salvarPassageiro(p: Passageiro) {
        passageiro = p

        viewModelScope.launch {
            passageiroDao.salvar(p)
        }
    }

    fun iniciarCorrida(caronaId: Int) {
        if (progressoCorridas.containsKey(caronaId)) return
        progressoCorridas[caronaId] = 0f

        viewModelScope.launch {
            val passos = (DURACAO_SIMULACAO_MS / TICK_SIMULACAO_MS).toInt()
            for (i in 1..passos) {
                delay(TICK_SIMULACAO_MS)
                progressoCorridas[caronaId] = i / passos.toFloat()
            }
        }
    }

    fun confirmarCarona(caronaId: Int) {
        val i = caronas.indexOfFirst { it.id == caronaId }
        if (i < 0) return
        val carona = caronas[i]
        if (carona.confirmada || carona.aguardandoResposta) return

        val caronaConfirmada = carona.copy(confirmada = true)
        caronas[i] = caronaConfirmada
        enviarMensagem(caronaId, "Fechado! Já aceito o valor de ${formatarReais(carona.valor)}.")

        viewModelScope.launch {
            caronaDao.salvar(caronaConfirmada)
        }
    }

    // Idempotente: finalizar a mesma corrida de novo não soma corridas em dobro.
    fun finalizarCorrida(caronaId: Int) {
        if (caronaId in corridasFinalizadas) return
        val carona = caronaPorId(caronaId) ?: return
        val i = motoristas.indexOfFirst { it.id == carona.motoristaId }
        if (i < 0) return
        corridasFinalizadas.add(caronaId)
        val motoristaAtualizado = motoristas[i].copy(corridas = motoristas[i].corridas + 1)
        motoristas[i] = motoristaAtualizado

        viewModelScope.launch {
            motoristaDao.atualizar(motoristaAtualizado)
        }
    }

    // Idempotente: uma avaliação por corrida.
    fun avaliarCorrida(caronaId: Int, nota: Int, comentario: String, elogios: List<String>) {
        if (caronaId !in corridasFinalizadas) finalizarCorrida(caronaId)
        if (avaliacoesCorridas.containsKey(caronaId)) return
        val carona = caronaPorId(caronaId) ?: return
        val i = motoristas.indexOfFirst { it.id == carona.motoristaId }
        if (i < 0) return

        val notaLimpa = nota.coerceIn(1, 5)
        val avaliacao = AvaliacaoCorrida(caronaId, notaLimpa, comentario, elogios)
        avaliacoesCorridas[caronaId] = avaliacao

        val motorista = motoristas[i]
        val base = motorista.corridas - 1
        val novaNota = ((motorista.nota * base + notaLimpa) / (base + 1)).coerceIn(1.0, 5.0)
        val motoristaAtualizado = motorista.copy(nota = novaNota)
        motoristas[i] = motoristaAtualizado

        viewModelScope.launch {
            avaliacaoDao.salvar(avaliacao)
            motoristaDao.atualizar(motoristaAtualizado)
        }
    }
}
