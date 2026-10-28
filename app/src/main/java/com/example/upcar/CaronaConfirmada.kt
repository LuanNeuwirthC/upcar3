package com.example.upcar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.upcar.ui.theme.Cores
import androidx.compose.runtime.remember

@Composable
fun CaronaConfirmada(
    state: CaronaViewModel,
    caronaId: Int,
    onVoltarInicio: () -> Unit
) {
    val carona = state.caronaPorId(caronaId)
    val motorista = carona?.let { state.motoristaPorId(it.motoristaId) }

    if (carona == null || motorista == null) {
        TelaNaoEncontrada("Carona confirmada", onVoltarInicio)
        return
    }

    Scaffold(topBar = { BarraTopo("Carona confirmada") }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Cores.FundoClaro)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Cores.VerdeClaro),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Confirmada",
                    tint = Cores.Verde,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Carona confirmada!",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Cores.AzulEscuro
            )

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Avatar(nome = motorista.nome, id = motorista.id, tamanho = 48.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "${motorista.nome} ★ ${formatarNota(motorista.nota)}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(
                                text = "${motorista.carro} • ${motorista.placa}",
                                fontSize = 12.sp,
                                color = Cores.TextoSecundario
                            )
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

                    LinhaInfo("Embarque", carona.origem)
                    LinhaInfo("Destino", carona.destino)
                    LinhaInfo("Saída", carona.horario)
                    LinhaInfo("Valor combinado", formatarReais(carona.valor), destaque = true)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onVoltarInicio,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Cores.AzulEscuro),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                Text("Voltar ao início", color = Color.White)
            }
        }
    }
}

@Composable
private fun LinhaInfo(rotulo: String, valor: String, destaque: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = rotulo, fontSize = 13.sp, color = Cores.TextoSecundario)
        Text(
            text = valor,
            fontSize = 13.sp,
            fontWeight = if (destaque) FontWeight.Bold else FontWeight.Medium,
            color = if (destaque) Cores.AzulEscuro else Color.Black
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CaronaConfirmadaPreview() {
    MaterialTheme {
        val state = remember {
            CaronaViewModel().also {
                val carona = it.solicitarCarona(it.motoristas[0])
                it.confirmarCarona(carona.id)
            }
        }
        CaronaConfirmada(state = state, caronaId = state.caronas[0].id, onVoltarInicio = {})
    }
}