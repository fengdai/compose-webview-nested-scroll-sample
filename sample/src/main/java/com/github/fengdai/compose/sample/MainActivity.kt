package com.github.fengdai.compose.sample

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewState
import com.telefonica.nestedscrollwebview.NestedScrollWebView
import java.lang.Integer.max
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Solution1()
        }
    }
}

@Composable
fun Solution1() {
    val coroutineScope = rememberCoroutineScope()
    var minOffsetY by remember { mutableStateOf(0) }
    var offsetY by remember { mutableStateOf(0) }
    var accumulator by remember { mutableStateOf(0f) }
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                Log.d("NestedScroll", "onPreScroll: $available")
                return if (available.y < 0) {
                    val absoluteY = offsetY + available.y + accumulator
                    val newOffsetY = absoluteY.coerceIn(minOffsetY.toFloat(), 0f)
                    val changed = absoluteY != newOffsetY
                    val consumedY = newOffsetY - offsetY
                    val consumedYInt = consumedY.roundToInt()
                    offsetY += consumedYInt
                    accumulator = consumedY - consumedYInt
                    if (changed) available.copy(y = consumedY) else available
                } else super.onPreScroll(available, source)
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                Log.d("NestedScroll", "onPostScroll: consumed: $consumed; available: $available")
                return if (available.y > 0) {
                    val absoluteY = offsetY + available.y + accumulator
                    val newOffsetY = absoluteY.coerceIn(minOffsetY.toFloat(), 0f)
                    val changed = absoluteY != newOffsetY
                    val consumedY = newOffsetY - offsetY
                    val consumedYInt = consumedY.roundToInt()
                    offsetY += consumedYInt
                    accumulator = consumedY - consumedYInt
                    if (changed) available.copy(y = consumedY) else available
                } else super.onPostScroll(consumed, available, source)
            }
        }
    }
    Layout(
        content = {
            Box(
                modifier = Modifier
                    .height(500.dp)
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color.White, Color.Black)))
                    .verticalScroll(rememberScrollState())
            )
            WebView(
                state = rememberWebViewState(url = "https://www.google.com.hk/search?q=nestedscroll+compose"),
                onCreated = {
                    ViewCompat.setNestedScrollingEnabled(it, true)
                },
                factory = ::NestedScrollWebView
            )
//            AndroidView(
//                factory = {
//                    NestedScrollView(it).apply {
//                        addView(
//                            View(it).apply {
//                                setBackgroundColor(Color.Red.toArgb())
//                            },
//                            ViewGroup.LayoutParams(
//                                ViewGroup.LayoutParams.MATCH_PARENT,
//                                20000
//                            )
//                        )
//                    }
//                },
//                modifier = Modifier
//                    .fillMaxWidth()
//            )
        },
        modifier = Modifier
            .nestedScroll(nestedScrollConnection)
    ) { measurables, constraints ->
        val height = constraints.maxHeight
        val topContent =
            measurables[0].measure(constraints.copy(maxHeight = Constraints.Infinity))
        val bottomContent =
            measurables[1].measure(constraints.copy(minHeight = height, maxHeight = height))
        val width = constraints.constrainWidth(max(topContent.width, bottomContent.width))
        minOffsetY = -topContent.height
        layout(
            width = width,
            height = height
        ) {
            topContent.placeRelativeWithLayer(0, offsetY)
            bottomContent.placeRelativeWithLayer(0, topContent.height + offsetY)
        }
    }
}
