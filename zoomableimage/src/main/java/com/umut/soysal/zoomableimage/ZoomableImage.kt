package com.umut.soysal.zoomableimage

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateZoomBy
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.positionChangeConsumed
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEach
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Creates an Image composable that can be zoomed in and out.
 * @author Arnau Mora, Mr. Pine, umutsoysl
 * @since 20220118
 * @param painter The data to load into the image.
 * @param contentDescription The description of the image for accessibility.
 * @param modifier Modifiers to apply to the image component.
 * @param minScale The minimum scale that can be applied to the image.
 * @param maxScale The maximum scale that can be applied to the image.
 * @param isRotation Whether or not the image can be rotated.
 * @param isZoomable Whether or not the image can be zoomed.
 * @param onSwipeRight Will be called when the user swipes the image to the right.
 * @param onSwipeLeft Will be called when the user swipes the image to the left.
 * @see <a href="https://stackoverflow.com/a/69782530/5717211">StackOverflow</a>
 */
@Composable
fun ZoomableImage(
    painter: Painter,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    minScale: Float = .5f,
    maxScale: Float = 3f,
    isRotation: Boolean = false,
    isZoomable: Boolean = true,
    onSwipeRight: (() -> Unit)? = null,
    onSwipeLeft: (() -> Unit)? = null,
) {
    val scope = rememberCoroutineScope()

    var scale by remember { mutableStateOf(1f) }
    var rotation by remember { mutableStateOf(0f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val state = rememberTransformableState { zoomChange, offsetChange, rotationChange ->
        scale *= zoomChange
        rotation += rotationChange
        offset += offsetChange
    }

    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var imageCenter by remember { mutableStateOf(Offset.Zero) }
    var transformOffset by remember { mutableStateOf(Offset.Zero) }

    fun onTransformGesture(
        centroid: Offset,
        pan: Offset,
        zoom: Float,
        transformRotation: Float
    ) {
        offset += pan
        scale *= zoom

        // Constrain scale
        scale = maxOf(minScale, minOf(maxScale, scale))

        if (isRotation)
            rotation += transformRotation
        else
            rotation = 0f

        val x0 = centroid.x - imageCenter.x
        val y0 = centroid.y - imageCenter.y

        val hyp0 = sqrt(x0 * x0 + y0 * y0)
        val hyp1 = zoom * hyp0 * (if (x0 > 0) 1f else -1f)

        val alpha0 = atan(y0 / x0)

        val alpha1 = alpha0 + (transformRotation * ((2 * PI) / 360))

        val x1 = cos(alpha1) * hyp1
        val y1 = sin(alpha1) * hyp1

        transformOffset =
            centroid - (imageCenter - offset) - Offset(x1.toFloat(), y1.toFloat())
        offset = transformOffset
    }

    Box(
        modifier = modifier
            .clip(RectangleShape)
            .background(MaterialTheme.colorScheme.background)
            .let { mod ->
                return@let if (!isZoomable)
                    mod
                else
                    mod
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = {
                                    if (scale != 1f) {
                                        scope.launch {
                                            state.animateZoomBy(1 / scale)
                                        }
                                        offset = Offset.Zero
                                        rotation = 0f
                                    } else {
                                        scope.launch {
                                            state.animateZoomBy(2f)
                                        }
                                    }
                                }
                            )
                        }
                        .pointerInput(Unit) {
                            val panZoomLock = true
                            forEachGesture {
                                awaitPointerEventScope {
                                    var transformRotation = 0f
                                    var zoom = 1f
                                    var pan = Offset.Zero
                                    var pastTouchSlop = false
                                    val touchSlop = viewConfiguration.touchSlop
                                    var lockedToPanZoom = false
                                    var drag: PointerInputChange?
                                    var overSlop = Offset.Zero

                                    val down = awaitFirstDown(requireUnconsumed = false)

                                    var transformEventCounter = 0
                                    do {
                                        val event = awaitPointerEvent()
                                        val canceled =
                                            event.changes.fastAny { it.positionChangeConsumed() }
                                        var relevant = true
                                        if (event.changes.size > 1) {
                                            if (!canceled) {
                                                val zoomChange = event.calculateZoom()
                                                val rotationChange = event.calculateRotation()
                                                val panChange = event.calculatePan()

                                                if (!pastTouchSlop) {
                                                    zoom *= zoomChange
                                                    transformRotation += rotationChange
                                                    pan += panChange

                                                    val centroidSize =
                                                        event.calculateCentroidSize(useCurrent = false)
                                                    val zoomMotion = abs(1 - zoom) * centroidSize
                                                    val rotationMotion =
                                                        abs(transformRotation * PI.toFloat() * centroidSize / 180f)
                                                    val panMotion = pan.getDistance()

                                                    if (zoomMotion > touchSlop ||
                                                        rotationMotion > touchSlop ||
                                                        panMotion > touchSlop
                                                    ) {
                                                        pastTouchSlop = true
                                                        lockedToPanZoom =
                                                            panZoomLock && rotationMotion < touchSlop
                                                    }
                                                }

                                                if (pastTouchSlop) {
                                                    val eventCentroid =
                                                        event.calculateCentroid(useCurrent = false)
                                                    val effectiveRotation =
                                                        if (lockedToPanZoom) 0f else rotationChange
                                                    if (effectiveRotation != 0f ||
                                                        zoomChange != 1f ||
                                                        panChange != Offset.Zero
                                                    ) {
                                                        onTransformGesture(
                                                            eventCentroid,
                                                            panChange,
                                                            zoomChange,
                                                            effectiveRotation
                                                        )
                                                    }
                                                    event.changes.fastForEach {
                                                        if (it.positionChanged())
                                                            it.consumeAllChanges()
                                                    }
                                                }
                                            }
                                        } else if (transformEventCounter > 3) relevant = false
                                        transformEventCounter++
                                    } while (!canceled && event.changes.fastAny { it.pressed } && relevant)

                                    do {
                                        awaitPointerEvent()
                                        drag =
                                            awaitTouchSlopOrCancellation(down.id) { change, over ->
                                                change.consumePositionChange()
                                                overSlop = over
                                            }
                                    } while (drag != null && !drag.positionChangeConsumed())
                                    if (drag != null) {
                                        dragOffset = Offset.Zero
                                        if (scale !in 0.92f..1.08f)
                                            offset += overSlop
                                        else
                                            dragOffset += overSlop
                                        if (
                                            drag(drag.id) {
                                                if (scale !in 0.92f..1.08f)
                                                    offset += it.positionChange()
                                                else
                                                    dragOffset += it.positionChange()
                                                it.consumePositionChange()
                                            }
                                        ) {
                                            if (scale in 0.92f..1.08f) {
                                                val offsetX = dragOffset.x
                                                if (offsetX > 300)
                                                    onSwipeRight?.invoke()
                                                else if (offsetX < -300)
                                                    onSwipeLeft?.invoke()
                                            }
                                        }
                                    }
                                }
                            }
                        }
            }
    ) {
        Image(
            painter = painter,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = Modifier
                .align(Alignment.Center)
                .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
                .graphicsLayer(
                    scaleX = scale - 0.02f,
                    scaleY = scale - 0.02f,
                    rotationZ = rotation,
                )
                .onGloballyPositioned { coordinates ->
                    val localOffset =
                        Offset(
                            coordinates.size.width.toFloat() / 2,
                            coordinates.size.height.toFloat() / 2,
                        )
                    val windowOffset = coordinates.localToWindow(localOffset)
                    imageCenter = coordinates.parentLayoutCoordinates?.windowToLocal(windowOffset)
                        ?: Offset.Zero
                },
        )
    }
}
