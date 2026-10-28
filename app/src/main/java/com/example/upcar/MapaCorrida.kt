package com.example.upcar

import android.content.Context
import android.content.ContextWrapper
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.upcar.ui.theme.Cores
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

private val COR_ROTA_CORRIDA = Cores.AzulMotorista.toArgb()
private val COR_DESTINO_CORRIDA = Cores.Verde.toArgb()
private val COR_CARRO_CORRIDA = Cores.AzulAcao.toArgb()

@Composable
fun MapaCorrida(
    origem: GeoPoint,
    destino: GeoPoint,
    posicaoMotorista: GeoPoint,
    rotuloMotorista: String,
    modifier: Modifier = Modifier
) {
    if (LocalInspectionMode.current) {
        Box(modifier.background(Cores.CinzaFundo))
        return
    }

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
            controller.setCenter(origem)
            overlays.add(CopyrightOverlay(context))
            // Enquadra uma unica vez, quando o mapa ja tem tamanho.
            addOnFirstLayoutListener { _, _, _, _, _ ->
                zoomToBoundingBox(BoundingBox.fromGeoPoints(listOf(origem, destino)), false, 120)
            }
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

    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { map ->
            fun icone(texto: String, cor: Int): Drawable =
                cacheIcones.getOrPut("$cor|$texto") { rotulo(map.context, texto, cor) }

            map.overlays.removeAll { it is Marker || it is Polyline }

            val densidade = map.context.resources.displayMetrics.density
            map.overlays.add(
                Polyline().apply {
                    setPoints(listOf(origem, destino))
                    outlinePaint.color = COR_ROTA_CORRIDA
                    outlinePaint.strokeWidth = 5f * densidade
                }
            )

            map.overlays.add(
                criarPino(map, destino, "Campus Centro", icone("Campus Centro", COR_DESTINO_CORRIDA))
            )
            map.overlays.add(
                criarPino(map, posicaoMotorista, rotuloMotorista, icone(rotuloMotorista, COR_CARRO_CORRIDA))
            )

            map.invalidate()
        }
    )
}
