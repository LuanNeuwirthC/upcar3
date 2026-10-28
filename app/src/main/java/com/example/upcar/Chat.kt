package com.example.upcar

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.upcar.ui.theme.Cores
import kotlin.math.abs

@Composable
fun Chat(
    state: CaronaViewModel,
    caronaId: Int,
    onVoltar: () -> Unit,
    onConfirmar: () -> Unit
) {
    val context = LocalContext.current
    var textoMensagem by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val carona = state.caronaPorId(caronaId)
    val motorista = carona?.let { state.motoristaPorId(it.motoristaId) }
    val mensagens = state.mensagensDaCarona(caronaId)

    LaunchedEffect(mensagens.size) {
        if (mensagens.isNotEmpty()) listState.animateScrollToItem(mensagens.lastIndex)
    }

    if (carona == null || motorista == null) {
        TelaNaoEncontrada("Chat", onVoltar)
        return
    }

    val opcoesValor = listOf((motorista.preco - 2.0).coerceAtLeast(1.0), motorista.preco).distinct()
    val bloqueado = carona.confirmada || carona.aguardandoResposta

    Scaffold(topBar = { BarraTopo(motorista.nome, onVoltar) }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .background(Color.White)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Cores.CinzaFundo)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Avatar(nome = motorista.nome, id = motorista.id, tamanho = 42.dp)

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "${motorista.nome} ★ ${formatarNota(motorista.nota)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.Black
                    )
                    Text(
                        text = "${motorista.carro} • ${motorista.placa}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(mensagens, key = { it.id }) { mensagem ->
                    val aoSegurar: (() -> Unit)? = if (mensagem.minha) {
                        {
                            state.removerMensagem(mensagem)
                            Toast.makeText(context, "Mensagem removida", Toast.LENGTH_SHORT).show()
                        }
                    } else null

                    BalaoMensagem(
                        texto = mensagem.texto,
                        horario = mensagem.horario,
                        esquerda = !mensagem.minha,
                        corFundo = if (mensagem.minha) Cores.AzulEscuro else Cores.CinzaBalao,
                        corTexto = if (mensagem.minha) Color.White else Color.Black,
                        onLongClick = aoSegurar
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Cores.CinzaFundo)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Valor atual: ${formatarReais(carona.valor)}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Cores.AzulEscuro
                )

                if (carona.aguardandoResposta) {
                    Text(
                        text = "Aguardando resposta do motorista...",
                        fontSize = 12.sp,
                        color = Cores.TextoSecundario
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Sugerir contraproposta rápida:",
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row {
                    opcoesValor.forEachIndexed { indice, valor ->
                        if (indice > 0) Spacer(modifier = Modifier.width(10.dp))

                        val selecionado = abs(carona.valor - valor) < 0.001
                        BotaoContraproposta(
                            texto = formatarReais(valor),
                            selecionado = selecionado,
                            habilitado = !bloqueado,
                            onClick = {
                                if (!bloqueado && !selecionado) {
                                    state.enviarContraproposta(caronaId, valor)
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = textoMensagem,
                        onValueChange = { textoMensagem = it },
                        placeholder = { Text("Escreva uma mensagem...", fontSize = 13.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .background(Color.White),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (textoMensagem.isNotBlank()) {
                                state.enviarMensagem(caronaId, textoMensagem.trim())
                                textoMensagem = ""
                            } else {
                                Toast.makeText(context, "Digite uma mensagem antes de enviar", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Cores.AzulEscuro),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Text("Enviar", color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (!carona.confirmada) {
                            state.confirmarCarona(caronaId)
                            Toast.makeText(context, "Carona confirmada", Toast.LENGTH_SHORT).show()
                        }
                        onConfirmar()
                    },
                    enabled = !carona.aguardandoResposta,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Cores.AzulEscuro,
                        disabledContainerColor = Cores.AzulEscuro.copy(alpha = 0.4f)
                    ),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Text(
                        text = if (carona.confirmada) "Ver carona confirmada" else "Aceitar valor e confirmar",
                        color = Color.White
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BalaoMensagem(
    texto: String,
    horario: String,
    esquerda: Boolean,
    corFundo: Color,
    corTexto: Color,
    onLongClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        contentAlignment = if (esquerda) Alignment.CenterStart else Alignment.CenterEnd
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(corFundo)
                .then(
                    if (onLongClick != null) {
                        Modifier.combinedClickable(onClick = {}, onLongClick = onLongClick)
                    } else {
                        Modifier
                    }
                )
                .padding(10.dp)
        ) {
            Text(text = texto, fontSize = 13.sp, color = corTexto)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = horario,
                fontSize = 9.sp,
                color = if (esquerda) Color.Gray else Color(0xFFD0D0D0),
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
fun BotaoContraproposta(
    texto: String,
    selecionado: Boolean,
    habilitado: Boolean = true,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .alpha(if (habilitado) 1f else 0.5f)
            .background(if (selecionado) Cores.AzulEscuro else Color.White)
            .border(1.dp, Cores.AzulEscuro)
            .clickable(enabled = habilitado) { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = texto,
            fontSize = 12.sp,
            color = if (selecionado) Color.White else Cores.AzulEscuro,
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    MaterialTheme {
        val state = remember {
            CaronaViewModel().also { it.solicitarCarona(it.motoristas[0]) }
        }
        Chat(
            state = state,
            caronaId = state.caronas[0].id,
            onVoltar = {},
            onConfirmar = {}
        )
    }
}