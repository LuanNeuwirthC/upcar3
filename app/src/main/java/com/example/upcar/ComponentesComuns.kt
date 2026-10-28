package com.example.upcar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.upcar.ui.theme.Cores

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraTopo(titulo: String, onVoltar: (() -> Unit)? = null) {
    TopAppBar(
        title = { Text(titulo, fontWeight = FontWeight.Bold) },
        navigationIcon = {
            if (onVoltar != null) {
                IconButton(onClick = onVoltar) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar"
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Cores.AzulEscuro,
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White
        )
    )
}


@Composable
fun Avatar(nome: String, id: Int, tamanho: Dp = 48.dp) {
    val paleta = listOf(0xFF1A3C6E, 0xFF2E9E5B, 0xFFB5651D, 0xFF7B3FA0, 0xFFC0392B)
    Box(
        modifier = Modifier
            .size(tamanho)
            .clip(CircleShape)
            .background(Color(paleta[id % paleta.size])),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = nome.firstOrNull()?.uppercase() ?: "?",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = (tamanho.value * 0.4f).sp
        )
    }
}


@Composable
fun TelaNaoEncontrada(titulo: String, onVoltar: () -> Unit) {
    Scaffold(topBar = { BarraTopo(titulo, onVoltar) }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Carona não encontrada.", fontSize = 15.sp, color = Color.DarkGray)
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onVoltar,
                colors = ButtonDefaults.buttonColors(containerColor = Cores.AzulEscuro)
            ) {
                Text("Voltar", color = Color.White)
            }
        }
    }
}