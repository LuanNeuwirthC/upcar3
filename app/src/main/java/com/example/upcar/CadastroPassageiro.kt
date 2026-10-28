package com.example.upcar

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.upcar.ui.theme.Cores
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun CadastroPassageiro(
    state: CaronaViewModel,
    onVoltar: () -> Unit,
    onConcluir: () -> Unit
) {
    val context = LocalContext.current

    var nome by rememberSaveable { mutableStateOf("") }
    var rgm by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var bairro by rememberSaveable { mutableStateOf("") }
    var fotoUri by rememberSaveable { mutableStateOf<String?>(null) }
    var tentouAvancar by rememberSaveable { mutableStateOf(false) }

    val seletorFoto = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) fotoUri = uri.toString() }

    val abrirSeletor = {
        seletorFoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    val foto by produceState<ImageBitmap?>(initialValue = null, fotoUri) {
        value = fotoUri?.let { withContext(Dispatchers.IO) { decodificarFoto(context, Uri.parse(it)) } }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cores.FundoClaro)
            .statusBarsPadding()
            .imePadding()
    ) {
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
            Text(
                text = "Cadastro de Passageiro",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Cores.AzulMarca
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Cores.AzulMarca)
                    .clickable { abrirSeletor() },
                contentAlignment = Alignment.Center
            ) {
                val bitmap = foto
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = "Foto do estudante",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.WorkspacePremium,
                        contentDescription = "Adicionar foto",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Adicionar foto de estudante",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Cores.AzulAcao,
                modifier = Modifier.clickable { abrirSeletor() }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                CampoCadastro(
                    rotulo = "Nome",
                    valor = nome,
                    onChange = { nome = it },
                    placeholder = "Ex: Felipe Silva",
                    erro = if (tentouAvancar && !nomeValido(nome)) "Informe seu nome" else null
                )
                CampoCadastro(
                    rotulo = "RGM",
                    valor = rgm,
                    onChange = { rgm = it },
                    placeholder = "Ex: 293847-5",
                    teclado = KeyboardType.Number,
                    erro = if (tentouAvancar && !rgmValido(rgm)) "RGM inválido" else null
                )
                CampoCadastro(
                    rotulo = "E-mail",
                    valor = email,
                    onChange = { email = it },
                    placeholder = "Ex: felipe.silva@universidade.edu",
                    teclado = KeyboardType.Email,
                    erro = if (tentouAvancar && !emailValido(email)) "E-mail inválido" else null
                )
                CampoCadastro(
                    rotulo = "Bairro de Origem",
                    valor = bairro,
                    onChange = { bairro = it },
                    placeholder = "Ex: Centro",
                    erro = if (tentouAvancar && !bairroValido(bairro)) "Informe o bairro" else null
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        Box(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(20.dp)
        ) {
            Button(
                onClick = {
                    tentouAvancar = true
                    if (nomeValido(nome) && rgmValido(rgm) && emailValido(email) && bairroValido(bairro)) {
                        state.salvarPassageiro(
                            Passageiro(nome.trim(), rgm.trim(), email.trim(), bairro.trim(), fotoUri)
                        )
                        onConcluir()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Cores.AzulAcao,
                    contentColor = Color.White
                )
            ) {
                Text("Avançar", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun CampoCadastro(
    rotulo: String,
    valor: String,
    onChange: (String) -> Unit,
    placeholder: String,
    erro: String?,
    teclado: KeyboardType = KeyboardType.Text
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = rotulo,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Cores.AzulMarca
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = valor,
            onValueChange = onChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = erro != null,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = teclado),
            placeholder = { Text(placeholder, fontSize = 13.sp, color = Cores.RotuloCinza) },
            supportingText = if (erro != null) {
                { Text(erro, fontSize = 11.sp, color = Cores.SosVermelho) }
            } else null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Cores.CampoFundo,
                unfocusedContainerColor = Cores.CampoFundo,
                errorContainerColor = Cores.CampoFundo,
                focusedBorderColor = Cores.AzulAcao,
                unfocusedBorderColor = Color.Transparent,
                errorBorderColor = Cores.SosVermelho,
                focusedTextColor = Cores.AzulMarca,
                unfocusedTextColor = Cores.AzulMarca,
                errorTextColor = Cores.AzulMarca,
                cursorColor = Cores.AzulAcao,
                errorCursorColor = Cores.SosVermelho
            )
        )
    }
}

// Decodifica com downsample (lado maximo ~256px) para nao carregar a foto inteira na memoria.
private fun decodificarFoto(context: Context, uri: Uri, ladoMax: Int = 256): ImageBitmap? = try {
    val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, limites) }

    var amostra = 1
    while (limites.outWidth / (amostra * 2) >= ladoMax && limites.outHeight / (amostra * 2) >= ladoMax) {
        amostra *= 2
    }

    val opcoes = BitmapFactory.Options().apply { inSampleSize = amostra }
    context.contentResolver.openInputStream(uri)
        ?.use { BitmapFactory.decodeStream(it, null, opcoes) }
        ?.asImageBitmap()
} catch (e: Exception) {
    null
}

@Preview(showBackground = true)
@Composable
private fun CadastroPassageiroPreview() {
    MaterialTheme {
        CadastroPassageiro(state = remember { CaronaViewModel() }, onVoltar = {}, onConcluir = {})
    }
}
