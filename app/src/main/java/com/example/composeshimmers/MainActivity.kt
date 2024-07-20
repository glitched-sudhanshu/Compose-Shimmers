package com.example.composeshimmers

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.composeshimmers.ui.theme.ComposeShimmersTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComposeShimmersTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(Color(0xFF535454)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(30.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val showShimmer = remember { mutableStateOf(true) }
                            val imageUrl =
                                "https://banner2.cleanpng.com/20240204/wic/transparent-spider-man-intense-spider-man-in-red-hoodie-with-1710887687018.webp"
                            AsyncImage(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        shimmerBrush(
                                            targetValue = 1300f,
                                            showShimmer = showShimmer.value
                                        )
                                    )
                                    .padding(start = 0.dp, bottom = 2.dp)
                                    .size(300.dp),
                                onSuccess = { showShimmer.value = false },
                                model = imageUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                            )

                            ImageShimmer(imageUrl, modifier = Modifier.size(300.dp))
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun shimmerBrush(showShimmer: Boolean = true, targetValue: Float = 1000f): Brush {
        val color = Color(0xFF535454)
        return if (showShimmer) {
            val shimmerColors = listOf(
                color.copy(alpha = 0.6f),
                color.copy(alpha = 0.2f),
                color.copy(alpha = 0.6f),
            )
            val transition = rememberInfiniteTransition(label = "")
            val translateAnimation = transition.animateFloat(
                initialValue = 0f,
                targetValue = targetValue,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = LinearEasing), repeatMode = RepeatMode.Restart
                ),
                label = ""
            )
            Brush.linearGradient(
                colors = shimmerColors,
                start = Offset.Zero,
                end = Offset(x = translateAnimation.value, y = translateAnimation.value)
            )
        } else {
            Brush.linearGradient(
                colors = listOf(Color.Transparent, Color.Transparent),
                start = Offset.Zero,
                end = Offset.Zero
            )
        }
    }


    @Composable
    fun ImageShimmer(imageUrl: String, modifier: Modifier = Modifier) {
        val context = LocalContext.current
        val shimmerColors = listOf(
            Color.White.copy(.0f),
            Color.White.copy(0.5f),
            Color.White.copy(.0f)
        )
        val widthOfShadowBrush = 200
        val transition = rememberInfiniteTransition(label = "")
        val translateAnimation = transition.animateFloat(
            initialValue = 0f,
            targetValue = 1000f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 1000 + widthOfShadowBrush,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            ),
            label = ""
        )
        val brush = Brush.horizontalGradient(
            colors = shimmerColors,
            startX = translateAnimation.value - widthOfShadowBrush,
            endX = translateAnimation.value + widthOfShadowBrush
        )
        val bitmapState = remember { mutableStateOf<Bitmap?>(null) }
        LaunchedEffect(imageUrl) {
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                //pass image url in data if required
                .data(R.drawable.spiderman)
                .allowHardware(false)
                .build()
            val result = withContext(Dispatchers.IO) {
                loader.execute(request)
            }

            if (result is SuccessResult) {
                bitmapState.value = (result.drawable as BitmapDrawable).bitmap
            }
        }
        bitmapState.value?.let { bitmap ->
            Canvas(
                modifier = modifier
                    .fillMaxWidth()
                    .graphicsLayer(alpha = 0.99f)
            ) {
                drawImage(
                    image = bitmap.asImageBitmap(),
                    dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt())
                )
                drawRect(brush, blendMode = BlendMode.SrcAtop)
            }
        }
    }

}