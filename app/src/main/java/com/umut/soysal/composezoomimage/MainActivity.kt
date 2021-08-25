package com.umut.soysal.composezoomimage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.umut.soysal.composezoomimage.ui.theme.ComposeZoomImageTheme
import com.umut.soysal.zoomableimage.ZoomableImage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeZoomImageTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    ImageZoom()
                }
            }
        }
    }
}

@Composable
fun ImageZoom() {
    Column(modifier = Modifier
        .padding(all = 16.dp)
        .fillMaxSize()) {

        val painter = rememberImagePainter("https://imgrosetta.mynet.com.tr/file/12220872/12220872-1200x824.jpg")

        createImageRow(painter, "Zoom Image URL")

        val painterR = painterResource(id = R.drawable.compose)

        createImageRow(painterR, "Zoom Image Res")
    }

}

@Composable
fun createImageRow(painter: Painter, title: String) {
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
            modifier = Modifier.fillMaxWidth().size(250.dp)
        )
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ComposeZoomImageTheme {
        ImageZoom()
    }
}