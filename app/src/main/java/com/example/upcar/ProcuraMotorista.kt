package com.example.upcar

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.upcar.ui.theme.Cores


@Composable
fun MotoristasScreen(state: CaronaViewModel, onSolicitar: (Motorista) -> Unit) {
    val context = LocalContext.current
    val listState = rememberLazyListState()

    var destinoBusca by remember { mutableStateOf("Campus Centro") }
    var selecionadoId by remember { mutableStateOf<Int?>(null) }
    var mostrarFormulario by remember { mutableStateOf(false) }
    var motoristaParaRemover by remember { mutableStateOf<Motorista?>(null) }

    val lista = state.motoristas.filter {
        destinoBusca.isBlank() || it.destino.contains(destinoBusca.trim(), ignoreCase = true)
    }

    LaunchedEffect(selecionadoId) {
        val indice = lista.indexOfFirst { it.id == selecionadoId }
        if (indice >= 0) listState.animateScrollToItem(indice)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cores.FundoClaro)
    ) {
        Box(
            modifier = Modifier
                .weight(0.45f)
                .fillMaxWidth()
        ) {
            MapaMotoristas(
                motoristas = lista,
                selecionadoId = selecionadoId,
                onSelecionar = { selecionadoId = it },
                modifier = Modifier.fillMaxSize()
            )

            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(16.dp)
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(28.dp))
                    .padding(start = 16.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Buscar",
                    tint = Cores.AzulEscuro,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "Para:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Cores.AzulEscuro
                )
                OutlinedTextField(
                    value = destinoBusca,
                    onValueChange = { destinoBusca = it },
                    placeholder = { Text("Destino", fontSize = 14.sp) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    )
                )
            }
        }

        Surface(
            modifier = Modifier
                .weight(0.55f)
                .fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Motoristas Disponíveis",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Cores.AzulEscuro,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = if (lista.size == 1) "1 encontrado" else "${lista.size} encontrados",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Cores.AzulMotorista
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { mostrarFormulario = true }) {
                        Text("+ Oferecer carona", fontSize = 12.sp, color = Cores.AzulEscuro)
                    }
                    Text(
                        text = "Segure um card para remover",
                        fontSize = 10.sp,
                        color = Cores.TextoSecundario
                    )
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (lista.isEmpty()) {
                        item {
                            Text(
                                text = "Nenhum motorista encontrado para esse destino.",
                                fontSize = 13.sp,
                                color = Cores.TextoSecundario
                            )
                        }
                    } else {
                        items(lista, key = { it.id }) { motorista ->
                            CardMotorista(
                                motorista = motorista,
                                selecionado = motorista.id == selecionadoId,
                                onClick = { selecionadoId = motorista.id },
                                onLongClick = { motoristaParaRemover = motorista },
                                onPedirClick = {
                                    Toast.makeText(
                                        context,
                                        "Carona solicitada com ${motorista.nome}!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    onSolicitar(motorista)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (mostrarFormulario) {
        FormularioMotorista(
            onDismiss = { mostrarFormulario = false },
            onSalvar = { nome, carro, placa, horario, preco, origem, lat, lng ->
                state.adicionarMotorista(nome, carro, placa, horario, preco, origem, lat, lng)
                mostrarFormulario = false
                val aviso = if (lat == null) "Motorista adicionado (pino aproximado)" else "Motorista adicionado"
                Toast.makeText(context, aviso, Toast.LENGTH_SHORT).show()
            }
        )
    }

    motoristaParaRemover?.let { alvo ->
        AlertDialog(
            onDismissRequest = { motoristaParaRemover = null },
            title = { Text("Remover motorista") },
            text = { Text("Remover ${alvo.nome} da lista?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.removerMotorista(alvo)
                        if (selecionadoId == alvo.id) selecionadoId = null
                        motoristaParaRemover = null
                        Toast.makeText(context, "${alvo.nome} removido", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Remover")
                }
            },
            dismissButton = {
                TextButton(onClick = { motoristaParaRemover = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CardMotorista(
    motorista: Motorista,
    selecionado: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onPedirClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(
            width = if (selecionado) 2.dp else 1.dp,
            color = if (selecionado) Cores.AzulMotorista else Cores.Borda
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Avatar(nome = motorista.nome, id = motorista.id, tamanho = 48.dp)

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = motorista.nome,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "★ ${formatarNota(motorista.nota)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Cores.Estrela
                    )
                }
                Text(
                    text = motorista.carro,
                    fontSize = 12.sp,
                    color = Cores.TextoSecundario
                )
                Text(
                    text = "Saída ${motorista.horario}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatarReais(motorista.preco),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Cores.AzulEscuro
                )
                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = onPedirClick,
                    modifier = Modifier.height(30.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Cores.AzulMotorista),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                ) {
                    Text("Pedir", color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun FormularioMotorista(
    onDismiss: () -> Unit,
    onSalvar: (
        nome: String, carro: String, placa: String, horario: String,
        preco: Double, origem: String, lat: Double?, lng: Double?
    ) -> Unit
) {
    val context = LocalContext.current

    var nome by remember { mutableStateOf("") }
    var carro by remember { mutableStateOf("") }
    var placa by remember { mutableStateOf("") }
    var horario by remember { mutableStateOf("") }
    var preco by remember { mutableStateOf("") }
    var origem by remember { mutableStateOf("") }
    var latTexto by remember { mutableStateOf("") }
    var lngTexto by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Oferecer carona") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome do motorista") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = carro,
                        onValueChange = { carro = it },
                        label = { Text("Carro") },
                        placeholder = { Text("Honda Civic • Cinza", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = placa,
                        onValueChange = { placa = it.uppercase().take(8) },
                        label = { Text("Placa") },
                        placeholder = { Text("ABC1D23", fontSize = 12.sp) },
                        modifier = Modifier.weight(0.7f),
                        singleLine = true
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = horario,
                        onValueChange = { horario = it },
                        label = { Text("Saída") },
                        placeholder = { Text("18:30", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = preco,
                        onValueChange = { preco = it },
                        label = { Text("Preço (R$)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
                OutlinedTextField(
                    value = origem,
                    onValueChange = { origem = it },
                    label = { Text("Ponto de embarque") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = latTexto,
                        onValueChange = { latTexto = it },
                        label = { Text("Latitude") },
                        placeholder = { Text("-25.4284", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii)
                    )
                    OutlinedTextField(
                        value = lngTexto,
                        onValueChange = { lngTexto = it },
                        label = { Text("Longitude") },
                        placeholder = { Text("-49.2733", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii)
                    )
                }
                Text(
                    text = "Latitude e longitude são opcionais. Sem elas, o pino fica em posição aproximada perto do campus.",
                    fontSize = 11.sp,
                    color = Cores.TextoSecundario
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val valor = preco.replace(',', '.').toDoubleOrNull()
                    val lat = latTexto.trim().replace(',', '.').toDoubleOrNull()
                    val lng = lngTexto.trim().replace(',', '.').toDoubleOrNull()
                    val informouCoordenadas = latTexto.isNotBlank() || lngTexto.isNotBlank()

                    val erro: String? = when {
                        nome.isBlank() -> "Informe o nome do motorista."
                        carro.isBlank() -> "Informe o carro."
                        !placaValida(placa) -> "Placa inválida. Exemplo: ABC1D23."
                        !horarioValido(horario) -> "Saída inválida. Use o formato 18:30."
                        valor == null || valor <= 0.0 -> "Informe um preço válido."
                        origem.isBlank() -> "Informe o ponto de embarque."
                        informouCoordenadas && (
                                lat == null || lng == null ||
                                        lat !in -90.0..90.0 || lng !in -180.0..180.0
                                ) -> "Coordenadas inválidas. Informe latitude e longitude, ou deixe ambas em branco."
                        else -> null
                    }

                    if (erro != null || valor == null) {
                        Toast.makeText(context, erro ?: "Informe um preço válido.", Toast.LENGTH_SHORT).show()
                    } else {
                        onSalvar(
                            nome.trim(),
                            carro.trim(),
                            normalizarPlaca(placa),
                            normalizarHorario(horario),
                            valor,
                            origem.trim(),
                            if (informouCoordenadas) lat else null,
                            if (informouCoordenadas) lng else null
                        )
                    }
                }
            ) {
                Text("Adicionar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}