package com.example.upcar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.upcar.ui.theme.Cores
import java.util.Locale

data class Elogio(val titulo: String, val quantidade: Int)

data class Avaliacao(val autor: String, val nota: Int, val comentario: String)

data class DadosPerfil(
    val corridas: Int,
    val elogiosTotal: Int,
    val nota: Double,
    val membroDesde: String,
    val curso: String,
    val elogios: List<Elogio>,
    val avaliacoes: List<Avaliacao>
)

private val TITULOS_ELOGIO = listOf("Pontual", "Direção segura", "Carro limpo", "Boa conversa", "Simpático")
private val MEMBRO_DESDE_OPCOES = listOf("março de 2025", "agosto de 2024", "janeiro de 2026", "novembro de 2025")
private val CURSO_OPCOES = listOf("Engenharia de Software", "Administração", "Design", "Direito", "Arquitetura")
private val AVALIACOES_OPCOES = listOf(
    "Mariana S." to "Chegou no horário, viagem tranquila.",
    "Lucas P." to "Motorista muito cuidadoso.",
    "Ana Beatriz R." to "Carro limpo e conversa agradável."
)

private class PartesIlustrativas(
    val membroDesde: String,
    val curso: String,
    val elogios: List<Elogio>,
    val avaliacoes: List<Avaliacao>
)

// ILUSTRATIVO: não existe backend. Os dados abaixo são gerados a partir do id do
// motorista só para a tela ter conteúdo. Não são avaliações reais.
private fun dadosIlustrativos(m: Motorista): PartesIlustrativas {
    if (m.corridas == 0) return PartesIlustrativas("", "", emptyList(), emptyList())

    // Um elogio a cada 12 corridas, distribuído entre os títulos a partir do id.
    val total = m.corridas / 12
    val contagens = IntArray(TITULOS_ELOGIO.size)
    for (k in 0 until total) contagens[(m.id + k) % TITULOS_ELOGIO.size]++
    val elogios = TITULOS_ELOGIO.indices
        .filter { contagens[it] > 0 }
        .map { Elogio(TITULOS_ELOGIO[it], contagens[it]) }

    val notas = if (m.nota >= 4.5) listOf(5, 5, 4) else listOf(4, 5, 4)
    val avaliacoes = AVALIACOES_OPCOES.mapIndexed { i, (autor, comentario) ->
        Avaliacao(autor, notas[i], comentario)
    }

    return PartesIlustrativas(
        membroDesde = MEMBRO_DESDE_OPCOES[m.id % MEMBRO_DESDE_OPCOES.size],
        curso = CURSO_OPCOES[m.id % CURSO_OPCOES.size],
        elogios = elogios,
        avaliacoes = avaliacoes
    )
}

private fun montarDadosPerfil(state: CaronaViewModel, m: Motorista): DadosPerfil {
    val ilu = dadosIlustrativos(m)

    // Avaliações reais feitas no app nas corridas deste motorista, mais recentes primeiro.
    val reais = state.avaliacoesCorridas.values
        .filter { state.caronaPorId(it.caronaId)?.motoristaId == m.id }
        .sortedByDescending { it.caronaId }
    val autorReal = state.passageiro?.nome?.substringBefore(' ')?.takeIf { it.isNotBlank() } ?: "Você"
    val avaliacoesReais = reais.map { Avaliacao(autorReal, it.nota, it.comentario) }

    // Elogios: ilustrativos somados aos reais, agrupados por título.
    val porTitulo = LinkedHashMap<String, Int>()
    ilu.elogios.forEach { porTitulo[it.titulo] = (porTitulo[it.titulo] ?: 0) + it.quantidade }
    reais.flatMap { it.elogios }.forEach { porTitulo[it] = (porTitulo[it] ?: 0) + 1 }
    val elogios = porTitulo.map { Elogio(it.key, it.value) }.sortedByDescending { it.quantidade }

    // Ilustrativas completam até 3 itens, sem passar do número de corridas sem avaliação real.
    val cabe = minOf(3 - reais.size, m.corridas - reais.size).coerceAtLeast(0)

    return DadosPerfil(
        corridas = m.corridas,
        elogiosTotal = elogios.sumOf { it.quantidade },
        nota = m.nota,
        membroDesde = ilu.membroDesde,
        curso = ilu.curso,
        elogios = elogios,
        avaliacoes = avaliacoesReais + ilu.avaliacoes.take(cabe)
    )
}

private fun notaComVirgula(nota: Double): String =
    String.format(Locale.forLanguageTag("pt-BR"), "%.1f", nota)

@Composable
fun PerfilMotorista(state: CaronaViewModel, motoristaId: Int, onVoltar: () -> Unit) {
    val motorista = state.motoristaPorId(motoristaId)
    if (motorista == null) {
        PerfilNaoEncontrado(onVoltar)
        return
    }

    val dados = montarDadosPerfil(state, motorista)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cores.FundoClaro)
            .statusBarsPadding()
    ) {
        CabecalhoPerfil(onVoltar)

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Avatar(nome = motorista.nome, id = motorista.id, tamanho = 96.dp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = motorista.nome,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Cores.AzulMarca
                )
                Text(
                    text = "${motorista.carro} • ${motorista.placa}",
                    fontSize = 13.sp,
                    color = Cores.RotuloCinza
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            CardEstatisticas(dados)

            Spacer(modifier = Modifier.height(20.dp))
            TituloSecao("Elogios recebidos")
            Spacer(modifier = Modifier.height(8.dp))
            if (dados.elogios.isEmpty()) {
                Text("Ainda sem elogios", fontSize = 13.sp, color = Cores.RotuloCinza)
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    dados.elogios.forEach { ChipElogio(it) }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            TituloSecao("Sobre")
            Spacer(modifier = Modifier.height(8.dp))
            CardSobre(
                membroDesde = dados.membroDesde.ifBlank { "—" },
                curso = dados.curso.ifBlank { "—" },
                origem = motorista.origem,
                destino = motorista.destino
            )

            Spacer(modifier = Modifier.height(20.dp))
            TituloSecao("Avaliações recentes")
            Spacer(modifier = Modifier.height(8.dp))
            if (dados.avaliacoes.isEmpty()) {
                Text("Ainda sem avaliações", fontSize = 13.sp, color = Cores.RotuloCinza)
            } else {
                dados.avaliacoes.forEach { CardAvaliacao(it) }
            }

            Spacer(modifier = Modifier.height(24.dp).navigationBarsPadding())
        }
    }
}

@Composable
private fun CabecalhoPerfil(onVoltar: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onVoltar) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Voltar",
                tint = Cores.AzulMarca
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "Perfil do motorista",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Cores.AzulMarca
        )
    }
}

@Composable
private fun CardEstatisticas(dados: DadosPerfil) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Cores.Borda)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val corridasTexto = when {
                dados.corridas == 0 -> "Novo"
                dados.corridas >= 100 -> "100+"
                else -> dados.corridas.toString()
            }
            Estatistica("CORRIDAS", corridasTexto, Modifier.weight(1f))
            DivisorVertical()
            Estatistica("ELOGIOS", dados.elogiosTotal.toString(), Modifier.weight(1f))
            DivisorVertical()

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                RotuloPerfil("NOTA")
                Spacer(modifier = Modifier.height(4.dp))
                if (dados.corridas == 0) {
                    Text(
                        text = "Novo",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Cores.AzulMarca
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = Cores.Estrela,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = notaComVirgula(dados.nota),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Cores.AzulMarca
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Estatistica(rotulo: String, valor: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        RotuloPerfil(rotulo)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = valor,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Cores.AzulMarca
        )
    }
}

@Composable
private fun DivisorVertical() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .fillMaxHeight()
            .background(Cores.Borda)
    )
}

@Composable
private fun RotuloPerfil(texto: String) {
    Text(
        text = texto,
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
        color = Cores.RotuloCinza
    )
}

@Composable
private fun TituloSecao(texto: String) {
    Text(
        text = texto,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = Cores.AzulMarca
    )
}

@Composable
private fun ChipElogio(elogio: Elogio) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Cores.VerdeClaro)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = "${elogio.titulo}  ${elogio.quantidade}",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Cores.Verde
        )
    }
}

@Composable
private fun CardSobre(membroDesde: String, curso: String, origem: String, destino: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Cores.Borda)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            LinhaSobre("Motorista desde", membroDesde)
            LinhaSobre("Curso", curso)
            LinhaSobre("Saída de", origem)
            LinhaSobre("Destino", destino)
        }
    }
}

@Composable
private fun LinhaSobre(rotulo: String, valor: String) {
    Column {
        Text(text = rotulo, fontSize = 12.sp, color = Cores.RotuloCinza)
        Text(
            text = valor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Cores.AzulMarca
        )
    }
}

@Composable
private fun CardAvaliacao(avaliacao: Avaliacao) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Cores.Borda)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = avaliacao.autor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Cores.AzulMarca
                )
                Spacer(modifier = Modifier.weight(1f))
                Row {
                    repeat(avaliacao.nota.coerceIn(0, 5)) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = Cores.Estrela,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
            if (avaliacao.comentario.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = avaliacao.comentario,
                    fontSize = 13.sp,
                    color = Cores.RotuloCinza
                )
            }
        }
    }
}

@Composable
private fun PerfilNaoEncontrado(onVoltar: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cores.FundoClaro)
            .statusBarsPadding()
    ) {
        CabecalhoPerfil(onVoltar)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Motorista não encontrado.", fontSize = 15.sp, color = Cores.RotuloCinza)
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onVoltar,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Cores.AzulAcao,
                    contentColor = Color.White
                )
            ) {
                Text("Voltar")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PerfilMotoristaPreview() {
    MaterialTheme {
        val state = remember { CaronaViewModel() }
        PerfilMotorista(state = state, motoristaId = 1, onVoltar = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun PerfilMotoristaNovoPreview() {
    MaterialTheme {
        val state = remember {
            CaronaViewModel().also {
                it.adicionarMotorista(
                    nome = "Carlos Lima",
                    carro = "Fiat Argo • Prata",
                    placa = "BRA2E19",
                    horario = "07:30",
                    preco = 12.0,
                    origem = "Bairro Novo"
                )
            }
        }
        PerfilMotorista(state = state, motoristaId = state.motoristas.last().id, onVoltar = {})
    }
}
