package com.example.upcar

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.upcar.ui.theme.Cores
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.Marker

private val COR_MOTORISTA = Cores.AzulMotorista.toArgb()
private val COR_SELECIONADO = Cores.Vermelho.toArgb()
private val COR_CAMPUS = Cores.Verde.toArgb()


@Composable
fun MapaMotoristas(
    motoristas: List<Motorista>,
    selecionadoId: Int?,
    onSelecionar: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val mapView = remember {

        Configuration.getInstance().load(
            context,
            context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = context.packageName

        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(13.0)
            controller.setCenter(GeoPoint(CAMPUS_LAT, CAMPUS_LNG))
            overlays.add(CopyrightOverlay(context))
        }
    }


    val cacheIcones = remember { HashMap<String, BitmapDrawable>() }


    DisposableEffect(mapView) {
        val dono = generateSequence(context) { (it as? ContextWrapper)?.baseContext }
            .filterIsInstance<LifecycleOwner>()
            .firstOrNull()

        val observador = LifecycleEventObserver { _, evento ->
            when (evento) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> {}
            }
        }
        dono?.lifecycle?.addObserver(observador)

        onDispose {
            dono?.lifecycle?.removeObserver(observador)
            mapView.onDetach()
        }
    }


    LaunchedEffect(selecionadoId) {
        val alvo = motoristas.firstOrNull { it.id == selecionadoId }
        if (alvo != null) {
            mapView.controller.animateTo(GeoPoint(alvo.lat, alvo.lng))
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { map ->
            fun icone(texto: String, cor: Int): Drawable =
                cacheIcones.getOrPut("$cor|$texto") { rotulo(map.context, texto, cor) }

            map.overlays.removeAll { it is Marker }

            map.overlays.add(
                criarPino(
                    map = map,
                    posicao = GeoPoint(CAMPUS_LAT, CAMPUS_LNG),
                    texto = "Campus Centro",
                    icone = icone("Campus Centro", COR_CAMPUS)
                )
            )


            for (motorista in motoristas.sortedBy { it.id == selecionadoId }) {
                val selecionado = motorista.id == selecionadoId
                val texto = "${motorista.nome.substringBefore(' ')} (${motorista.horario.substringBefore(':')}h)"
                map.overlays.add(
                    criarPino(
                        map = map,
                        posicao = GeoPoint(motorista.lat, motorista.lng),
                        texto = texto,
                        icone = icone(texto, if (selecionado) COR_SELECIONADO else COR_MOTORISTA),
                        aoClicar = { onSelecionar(motorista.id) }
                    )
                )
            }

            map.invalidate()
        }
    )
}

internal fun criarPino(
    map: MapView,
    posicao: GeoPoint,
    texto: String,
    icone: Drawable,
    aoClicar: () -> Unit = {}
): Marker =
    Marker(map).apply {
        position = posicao
        title = texto
        this.icon = icone
        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        setOnMarkerClickListener { _, _ ->
            aoClicar()
            true
        }
    }


internal fun rotulo(context: Context, texto: String, corFundo: Int): BitmapDrawable {
    val densidade = context.resources.displayMetrics.density

    val tinta = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        textSize = 13f * densidade
        typeface = Typeface.DEFAULT_BOLD
    }
    val margemH = 10f * densidade
    val margemV = 6f * densidade
    val metrica = tinta.fontMetrics

    val largura = (tinta.measureText(texto) + 2 * margemH).toInt()
    val altura = (metrica.descent - metrica.ascent + 2 * margemV).toInt()

    val bitmap = Bitmap.createBitmap(largura, altura, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val fundo = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = corFundo }
    canvas.drawRoundRect(
        RectF(0f, 0f, largura.toFloat(), altura.toFloat()),
        altura / 2f,
        altura / 2f,
        fundo
    )
    canvas.drawText(texto, margemH, margemV - metrica.ascent, tinta)

    return BitmapDrawable(context.resources, bitmap)
}