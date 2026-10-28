package com.example.upcar

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.upcar.ui.theme.Cores

private val ELOGIOS_CORRIDA = listOf("Pontual", "Direção segura", "Carro limpo", "Boa conversa", "Simpático")
private const val LIMITE_COMENTARIO = 200
private const val SEPARADOR_ELOGIOS = "|"

@Composable
fun CorridaFinalizada(state: CaronaViewModel, caronaId: Int, onConcluir: () -> Unit) {
    BackHandler { onConcluir() }

    var nota by rememberSaveable { mutableStateOf(0) }
    var comentario by rememberSaveable { mutableStateOf("") }
    var elogiosTexto by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current

    val carona = state.caronaPorId(caronaId)
    val motorista = carona?.let { state.motoristaPorId(it.motoristaId) }
    if (carona == null || motorista == null) {
        CorridaNaoEncontrada(onConcluir)
        return
    }

    val avaliacaoEnviada = state.avaliacoesCorridas[caronaId]
    val elogiosSelecionados = elogiosTexto.split(SEPARADOR_ELOGIOS).filter { it.isNotEmpty() }
    val elogiosEnviados = if (nota >= 4) elogiosSelecionados else emptyList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cores.FundoClaro)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            CabecalhoConcluida()

            Spacer(modifier = Modifier.height(24.dp))

            CardResumo(carona.origem, carona.destino, carona.horario, formatarReais(carona.valor))

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(nome = motorista.nome, id = motorista.id, tamanho = 48.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = motorista.nome,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Cores.AzulMarca
                    )
                    Text(
                        text = "${motorista.carro} • ${motorista.placa}",
                        fontSize = 12.sp,
                        color = Cores.RotuloCinza
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (avaliacaoEnviada == null) {
                Text(
                    text = "Como foi sua corrida com ${motorista.nome.substringBefore(' ')}?",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Cores.AzulMarca
                )
                Spacer(modifier = Modifier.height(8.dp))
                SeletorEstrelas(nota = nota, onNota = { nota = it })
                Text(
                    text = rotuloNota(nota),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Cores.AzulMarca,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                if (nota >= 4) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "O que foi bom?",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Cores.AzulMarca
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ChipsElogio(
                        selecionados = elogiosSelecionados,
                        onToggle = { titulo ->
                            elogiosTexto = if (titulo in elogiosSelecionados) {
                                (elogiosSelecionados - titulo).joinToString(SEPARADOR_ELOGIOS)
                            } else {
                                (elogiosSelecionados + titulo).joinToString(SEPARADOR_ELOGIOS)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
                OutlinedTextField(
                    value = comentario,
                    onValueChange = { if (it.length <= LIMITE_COMENTARIO) comentario = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(14.dp),
                    placeholder = { Text("Conte como foi (opcional)", color = Cores.RotuloCinza) },
                    supportingText = {
                        Text(
                            text = "${comentario.length}/$LIMITE_COMENTARIO",
                            fontSize = 12.sp,
                            color = Cores.RotuloCinza,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Cores.AzulMarca,
                        unfocusedTextColor = Cores.AzulMarca,
                        focusedContainerColor = Cores.CampoFundo,
                        unfocusedContainerColor = Cores.CampoFundo,
                        focusedBorderColor = Cores.AzulAcao,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = Cores.AzulAcao
                    )
                )
            } else {
                CardAvaliacaoEnviada(avaliacaoEnviada)
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            if (avaliacaoEnviada == null) {
                Button(
                    onClick = {
                        state.avaliarCorrida(caronaId, nota, comentario.trim(), elogiosEnviados)
                        Toast.makeText(context, "Avaliação enviada", Toast.LENGTH_SHORT).show()
                    },
                    enabled = nota > 0,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Cores.AzulAcao,
                        contentColor = Color.White,
                        disabledContainerColor = Cores.Borda,
                        disabledContentColor = Cores.RotuloCinza
                    )
                ) {
                    Text("Enviar avaliação", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                TextButton(
                    onClick = onConcluir,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Pular", color = Cores.RotuloCinza, fontWeight = FontWeight.Medium)
                }
            } else {
                Button(
                    onClick = onConcluir,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Cores.AzulAcao,
                        contentColor = Color.White
                    )
                ) {
                    Text("Voltar ao início", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
private fun CorridaNaoEncontrada(onConcluir: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cores.FundoClaro)
            .statusBarsPadding()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Corrida não encontrada.", fontSize = 15.sp, color = Cores.RotuloCinza)
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onConcluir,
            colors = ButtonDefaults.buttonColors(
                containerColor = Cores.AzulAcao,
                contentColor = Color.White
            )
        ) {
            Text("Voltar ao início")
        }
    }
}

@Composable
private fun CabecalhoConcluida() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(Cores.VerdeClaro, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = Cores.Verde,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Corrida finalizada!",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Cores.AzulMarca
        )
        Text(
            text = "Você chegou ao destino",
            fontSize = 14.sp,
            color = Cores.RotuloCinza
        )
    }
}

@Composable
private fun CardResumo(origem: String, destino: String, horario: String, total: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Cores.Borda)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            RotuloResumo("ORIGEM")
            Text(origem, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Cores.AzulMarca)
            Spacer(modifier = Modifier.height(10.dp))
            RotuloResumo("DESTINO")
            Text(destino, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Cores.AzulMarca)
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Cores.Borda)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    RotuloResumo("HORÁRIO")
                    Text(horario, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Cores.AzulMarca)
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    RotuloResumo("TOTAL")
                    Text(
                        text = total,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Cores.AzulAcao
                    )
                }
            }
        }
    }
}

@Composable
private fun RotuloResumo(texto: String) {
    Text(
        text = texto,
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
        color = Cores.RotuloCinza
    )
}

@Composable
private fun SeletorEstrelas(nota: Int, onNota: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (n in 1..5) {
            IconButton(
                onClick = { onNota(n) },
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "$n estrelas",
                    tint = if (n <= nota) Cores.Estrela else Cores.Borda,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}

private fun rotuloNota(nota: Int): String = when (nota) {
    1 -> "Muito ruim"
    2 -> "Ruim"
    3 -> "Regular"
    4 -> "Bom"
    5 -> "Excelente"
    else -> ""
}

@Composable
private fun ChipsElogio(selecionados: List<String>, onToggle: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ELOGIOS_CORRIDA.forEach { titulo ->
            val marcado = titulo in selecionados
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (marcado) Cores.VerdeClaro else Cores.CampoFundo)
                    .clickable { onToggle(titulo) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = titulo,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (marcado) Cores.Verde else Cores.RotuloCinza
                )
            }
        }
    }
}

@Composable
private fun CardAvaliacaoEnviada(avaliacao: AvaliacaoCorrida) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Cores.Borda)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Obrigado pela avaliação!",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Cores.AzulMarca
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row {
                for (n in 1..5) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = if (n <= avaliacao.nota) Cores.Estrela else Cores.Borda,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            if (avaliacao.elogios.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    avaliacao.elogios.forEach { titulo ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Cores.VerdeClaro)
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = titulo,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Cores.Verde
                            )
                        }
                    }
                }
            }
            if (avaliacao.comentario.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = avaliacao.comentario,
                    fontSize = 14.sp,
                    color = Cores.AzulMarca,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CorridaFinalizadaPreview() {
    MaterialTheme {
        val state = remember {
            CaronaViewModel().also {
                val carona = it.solicitarCarona(it.motoristas[0])
                it.confirmarCarona(carona.id)
                it.finalizarCorrida(carona.id)
            }
        }
        CorridaFinalizada(state = state, caronaId = state.caronas[0].id, onConcluir = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun CorridaFinalizadaAvaliadaPreview() {
    MaterialTheme {
        val state = remember {
            CaronaViewModel().also {
                val carona = it.solicitarCarona(it.motoristas[0])
                it.confirmarCarona(carona.id)
                it.finalizarCorrida(carona.id)
                it.avaliarCorrida(carona.id, 5, "Motorista muito atencioso.", listOf("Pontual", "Carro limpo"))
            }
        }
        CorridaFinalizada(state = state, caronaId = state.caronas[0].id, onConcluir = {})
    }
}
