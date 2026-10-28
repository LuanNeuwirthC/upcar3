package com.example.upcar

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.upcar.ui.theme.Cores
import org.osmdroid.util.GeoPoint
import kotlin.math.ceil

private const val DURACAO_VIAGEM_MIN = 12f

@Composable
fun CorridaEmAndamento(
    state: CaronaViewModel,
    caronaId: Int,
    onFinalizar: () -> Unit,
    onVerPerfil: (Int) -> Unit = {}
) {
    // Corrida ativa: o botao voltar do sistema nao leva de volta ao chat.
    BackHandler { }

    LaunchedEffect(caronaId) {
        if (state.caronaPorId(caronaId) != null) state.iniciarCorrida(caronaId)
    }

    val carona = state.caronaPorId(caronaId)
    val motorista = carona?.let { state.motoristaPorId(it.motoristaId) }

    if (carona == null || motorista == null) {
        TelaNaoEncontrada("Corrida", onFinalizar)
        return
    }

    var mostrarSos by remember { mutableStateOf(false) }

    val progresso = state.progressoCorridas[caronaId] ?: 0f
    val chegou = progresso >= 1f
    val etaMin = ceil((1f - progresso) * DURACAO_VIAGEM_MIN).toInt()

    val origem = GeoPoint(motorista.lat, motorista.lng)
    val destino = GeoPoint(CAMPUS_LAT, CAMPUS_LNG)
    val posicao = GeoPoint(
        origem.latitude + (destino.latitude - origem.latitude) * progresso,
        origem.longitude + (destino.longitude - origem.longitude) * progresso
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cores.FundoClaro)
    ) {
        Box(
            modifier = Modifier
                .weight(0.55f)
                .fillMaxWidth()
        ) {
            MapaCorrida(
                origem = origem,
                destino = destino,
                posicaoMotorista = posicao,
                rotuloMotorista = motorista.nome.substringBefore(' '),
                modifier = Modifier.fillMaxSize()
            )

            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(16.dp)
                    .background(Color.White, RoundedCornerShape(24.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Cores.Verde, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (chegou) "Você chegou ao destino" else "Corrida em andamento",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Cores.AzulMarca
                )
                if (!chegou) {
                    Text(
                        text = "  •  Chegada em $etaMin min",
                        fontSize = 13.sp,
                        color = Cores.RotuloCinza
                    )
                }
            }
        }

        Surface(
            modifier = Modifier
                .weight(0.45f)
                .fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(start = 20.dp, end = 20.dp, top = 18.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClickLabel = "Ver perfil do motorista") { onVerPerfil(motorista.id) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Avatar(nome = motorista.nome, id = motorista.id, tamanho = 48.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "${motorista.nome} ★ ${formatarNota(motorista.nota)}",
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

                    Spacer(modifier = Modifier.height(14.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Cores.Borda)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                                Column(
                                    modifier = Modifier.fillMaxHeight(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .border(3.dp, Cores.AzulAcao, CircleShape)
                                    )
                                    Canvas(
                                        modifier = Modifier
                                            .weight(1f)
                                            .width(2.dp)
                                            .padding(vertical = 3.dp)
                                    ) {
                                        drawLine(
                                            color = Cores.AzulAcao,
                                            start = Offset(size.width / 2f, 0f),
                                            end = Offset(size.width / 2f, size.height),
                                            strokeWidth = size.width,
                                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .background(Cores.AzulAcao, CircleShape)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                    BlocoRota("EMBARQUE (ORIGEM)", carona.origem)
                                    BlocoRota("DESTINO (CAMPUS)", carona.destino)
                                }
                            }

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
                                    RotuloCaixaAlta("HORÁRIO DE SAÍDA")
                                    Text(
                                        text = carona.horario,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Cores.AzulMarca
                                    )
                                }
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    RotuloCaixaAlta("PREÇO FINAL")
                                    Text(
                                        text = formatarReais(carona.valor),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Cores.AzulAcao,
                                        textAlign = TextAlign.End
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                Column(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Button(
                        onClick = { mostrarSos = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Cores.SosVermelho,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SOS Emergência", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = onFinalizar,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.5.dp, Cores.AzulMarca),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = Cores.AzulMarca
                        )
                    ) {
                        Text("Finalizar corrida", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }

    if (mostrarSos) {
        DialogoSos(
            motorista = motorista,
            carona = carona,
            onDispensar = { mostrarSos = false }
        )
    }
}

@Composable
private fun RotuloCaixaAlta(texto: String) {
    Text(
        text = texto,
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
        color = Cores.RotuloCinza
    )
}

@Composable
private fun BlocoRota(rotulo: String, valor: String) {
    Column {
        RotuloCaixaAlta(rotulo)
        Text(
            text = valor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Cores.AzulMarca
        )
    }
}

@Composable
private fun DialogoSos(motorista: Motorista, carona: Carona, onDispensar: () -> Unit) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDispensar,
        containerColor = Color.White,
        titleContentColor = Cores.AzulMarca,
        textContentColor = Cores.RotuloCinza,
        title = { Text("Emergência", fontWeight = FontWeight.Bold) },
        text = { Text("O que você precisa?") },
        confirmButton = {
            Column(horizontalAlignment = Alignment.End) {
                TextButton(
                    onClick = {
                        abrirIntent(context, Intent(Intent.ACTION_DIAL, Uri.parse("tel:190")))
                        onDispensar()
                    }
                ) {
                    Text("Ligar 190", color = Cores.SosVermelho, fontWeight = FontWeight.Bold)
                }
                TextButton(
                    onClick = {
                        val texto = "Estou em uma carona UpCar. " +
                            "Motorista: ${motorista.nome}, ${motorista.carro}, placa ${motorista.placa}. " +
                            "Trajeto: ${carona.origem} -> ${carona.destino}. " +
                            "Horário: ${carona.horario}."
                        val envio = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, texto)
                        }
                        abrirIntent(context, Intent.createChooser(envio, "Compartilhar viagem"))
                        onDispensar()
                    }
                ) {
                    Text("Compartilhar viagem", color = Cores.AzulAcao)
                }
                TextButton(onClick = onDispensar) {
                    Text("Cancelar", color = Cores.RotuloCinza)
                }
            }
        }
    )
}

private fun abrirIntent(context: Context, intent: Intent) {
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "Não foi possível abrir essa ação neste dispositivo", Toast.LENGTH_SHORT).show()
    }
}

@Preview(showBackground = true)
@Composable
private fun CorridaEmAndamentoPreview() {
    MaterialTheme {
        val state = remember {
            CaronaViewModel().also {
                val carona = it.solicitarCarona(it.motoristas[0])
                it.confirmarCarona(carona.id)
            }
        }
        CorridaEmAndamento(state = state, caronaId = state.caronas[0].id, onFinalizar = {})
    }
}
