package com.zhukovartemvl.sudokusolver.ui.service_views

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import coil.size.Size
import com.zhukovartemvl.sudokusolver.R

@Composable
fun HappyCatView(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val imageLoader = ImageLoader.Builder(context)
        .components {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }.build()

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Image(
            painter = rememberAsyncImagePainter(
                imageLoader = imageLoader,
                model = ImageRequest.Builder(context)
                    .data(data = R.drawable.happy_cat)
                    .apply { size(Size.ORIGINAL) }
                    .build()
            ),
            contentDescription = null,
        )
    }
}
