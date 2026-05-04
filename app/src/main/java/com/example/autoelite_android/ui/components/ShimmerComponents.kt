package com.example.autoelite_android.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Shimmer Brush animado
@Composable
fun shimmerBrush(): Brush {
    val baseColor = MaterialTheme.colorScheme.surfaceVariant
    val highlightColor = MaterialTheme.colorScheme.surfaceContainerHighest

    val shimmerColors = listOf(
        baseColor,
        highlightColor,
        baseColor
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim.value - 400f, 0f),
        end = Offset(translateAnim.value, 0f)
    )
}

// Bloques shimmer básicos
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    height: Dp = 16.dp,
    shape: RoundedCornerShape = RoundedCornerShape(4.dp)
) {
    val brush = shimmerBrush()
    Box(
        modifier = modifier
            .height(height)
            .clip(shape)
            .background(brush)
    )
}

@Composable
fun ShimmerCircle(
    size: Dp = 40.dp
) {
    val brush = shimmerBrush()
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(brush)
    )
}

// Skeleton Cards por pantalla
@Composable
fun CitaCardSkeleton() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Fecha placeholder
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(52.dp)
            ) {
                ShimmerBox(modifier = Modifier.width(32.dp), height = 24.dp)
                Spacer(Modifier.height(4.dp))
                ShimmerBox(modifier = Modifier.width(28.dp), height = 12.dp)
            }

            Spacer(Modifier.width(16.dp))

            // Contenido
            Column(modifier = Modifier.weight(1f)) {
                ShimmerBox(modifier = Modifier.fillMaxWidth(0.6f), height = 16.dp)
                Spacer(Modifier.height(8.dp))
                ShimmerBox(modifier = Modifier.fillMaxWidth(0.4f), height = 12.dp)
                Spacer(Modifier.height(8.dp))
                ShimmerBox(
                    modifier = Modifier.width(80.dp),
                    height = 24.dp,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(Modifier.width(8.dp))
            ShimmerCircle(size = 32.dp)
        }
    }
}

@Composable
fun ReparacionCardSkeleton() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Cabecera
            Row(verticalAlignment = Alignment.CenterVertically) {
                ShimmerCircle(size = 24.dp)
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    ShimmerBox(modifier = Modifier.fillMaxWidth(0.7f), height = 16.dp)
                    Spacer(Modifier.height(6.dp))
                    ShimmerBox(modifier = Modifier.fillMaxWidth(0.5f), height = 12.dp)
                }
                ShimmerBox(modifier = Modifier.width(60.dp), height = 12.dp)
            }

            Spacer(Modifier.height(12.dp))

            // Coste
            ShimmerBox(modifier = Modifier.width(120.dp), height = 12.dp)

            Spacer(Modifier.height(16.dp))

            // Barra de progreso
            ShimmerBox(modifier = Modifier.fillMaxWidth(0.4f), height = 12.dp)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                repeat(4) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        ShimmerCircle(size = 22.dp)
                        Spacer(Modifier.height(4.dp))
                        ShimmerBox(modifier = Modifier.width(40.dp), height = 8.dp)
                    }
                }
            }
        }
    }
}

@Composable
fun FacturaCardSkeleton() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShimmerCircle(size = 40.dp)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    ShimmerBox(modifier = Modifier.fillMaxWidth(0.5f), height = 16.dp)
                    Spacer(Modifier.height(6.dp))
                    ShimmerBox(modifier = Modifier.fillMaxWidth(0.4f), height = 12.dp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    ShimmerBox(modifier = Modifier.width(60.dp), height = 18.dp)
                    Spacer(Modifier.height(4.dp))
                    ShimmerBox(modifier = Modifier.width(50.dp), height = 12.dp)
                }
            }

            // Botones
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ShimmerBox(
                    modifier = Modifier.weight(1f),
                    height = 36.dp,
                    shape = RoundedCornerShape(20.dp)
                )
                ShimmerBox(
                    modifier = Modifier.weight(1f),
                    height = 36.dp,
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }
    }
}

@Composable
fun VehiculoCardSkeleton() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ShimmerCircle(size = 48.dp)
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                ShimmerBox(modifier = Modifier.fillMaxWidth(0.6f), height = 16.dp)
                Spacer(Modifier.height(6.dp))
                ShimmerBox(modifier = Modifier.fillMaxWidth(0.5f), height = 12.dp)
                Spacer(Modifier.height(4.dp))
                ShimmerBox(modifier = Modifier.fillMaxWidth(0.3f), height = 12.dp)
            }
        }
    }
}

// Contenedor genérico que muestra N skeletons
@Composable
fun ShimmerList(
    count: Int = 5,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
    itemSpacing: Dp = 10.dp,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(itemSpacing)
    ) {
        repeat(count) {
            content()
        }
    }
}