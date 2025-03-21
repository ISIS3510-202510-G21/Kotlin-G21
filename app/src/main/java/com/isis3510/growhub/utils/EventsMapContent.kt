package com.isis3510.growhub.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.model.AdvancedMarkerOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.PinConfig
import com.google.maps.android.compose.AdvancedMarker
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.rememberMarkerState
import com.isis3510.growhub.model.objects.Event
import com.isis3510.growhub.R

@Composable
@GoogleMapComposable
fun AdvancedMarkersMapContent(
    events: List<Event>,
    coordinates: List<LatLng>,
    onEventClick: (Marker) -> Boolean = { false },
) {
    // Genera el icono personalizado para los markers
    val eventIcon = vectorToBitmap(
        LocalContext.current,
        BitmapParameters(
            id = R.drawable.flag,
            iconColor = MaterialTheme.colorScheme.onPrimary.toArgb(),
        )
    )

    // Configura el PinConfig con el icono y colores personalizados
    val eventPin = with(PinConfig.builder()) {
        setGlyph(PinConfig.Glyph(eventIcon))
        setBackgroundColor(MaterialTheme.colorScheme.primary.toArgb())
        setBorderColor(MaterialTheme.colorScheme.onPrimary.toArgb())
        build()
    }

    // Itera sobre la lista de eventos, asociando cada evento con su coordenada (si existe)
    coordinates.forEachIndexed { index, coordinate ->
        AdvancedMarker(
            state = rememberMarkerState(position = coordinate),
            title = events[index].title,
            snippet = events[index].date,
            collisionBehavior = AdvancedMarkerOptions.CollisionBehavior.REQUIRED_AND_HIDES_OPTIONAL,
            pinConfig = eventPin,
            onClick = { marker ->
                onEventClick(marker)
                false
            }
        )
    }
}
