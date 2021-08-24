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
                    Greeting("Android")
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {

    Column(modifier = Modifier.padding(all=16.dp)) {
        Text(
            text = "Zoom Compose Image",
            textAlign = TextAlign.Start,
            style = MaterialTheme.typography.h5,
            color = Color.White,
            modifier = Modifier.padding(all=16.dp)
        )

        val painter = rememberImagePainter("https://maximum.com.tr/content-management/PublishingImages/campaigns/2020-kampanyalar/Temmuz/Boyner_TemmuzKampanyasi_Maximum_280x280.jpg")

        ZoomableImage(painter = painter,
            isRotation = false,
            modifier = Modifier.fillMaxSize())
    }

}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ComposeZoomImageTheme {
        Greeting("Android")
    }
}