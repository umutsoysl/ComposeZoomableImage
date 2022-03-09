package com.umut.soysal.composezoomimage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.umut.soysal.composezoomimage.ui.theme.ComposeZoomImageTheme
import com.umut.soysal.zoomableimage.ZoomableImage

@ExperimentalCoilApi
@ExperimentalFoundationApi
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeZoomImageTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = Color.Black) {
                    ImageZoom()
                }
            }
        }
    }
}

@ExperimentalCoilApi
@ExperimentalFoundationApi
@Composable
fun ImageZoom() {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .padding(all = 16.dp)
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        repeat(3) {
            val painter = rememberImagePainter("https://imgrosetta.mynet.com.tr/file/12220872/12220872-1200x824.jpg")
            CreateImageRow(
                painter = painter,
                title = "Zoom Image URL",
                scrollState = scrollState
            )
            val bitmap = remember {
                ResourcesCompat
                    .getDrawable(context.resources, R.drawable.compose, context.theme)!!
                    .toBitmap()
                    .asImageBitmap()
            }
            val painterB = BitmapPainter(bitmap)
            CreateImageRow(
                painter = painterB,
                title = "Zoom Image Bitmap",
                scrollState = scrollState
            )
            val painterR = painterResource(id = R.drawable.compose)
            CreateImageRow(
                painter = painterR,
                title = "Zoom Image Res",
                scrollState = scrollState
            )
        }
    }
}

@ExperimentalFoundationApi
@Composable
fun CreateImageRow(
    painter: Painter,
    title: String,
    scrollState: ScrollState
) {
    Column {
        Text(
            text = title,
            textAlign = TextAlign.Start,
            style = MaterialTheme.typography.h6,
            color = Color.White,
            modifier = Modifier.padding(all = 16.dp)
        )

        ZoomableImage(
            painter = painter,
            isRotation = false,
            isZoomable = true,
            scrollState = scrollState,
            modifier = Modifier
                .fillMaxWidth()
                .size(250.dp)
        )
    }
}


@ExperimentalCoilApi
@ExperimentalFoundationApi
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ComposeZoomImageTheme {
        ImageZoom()
    }
}