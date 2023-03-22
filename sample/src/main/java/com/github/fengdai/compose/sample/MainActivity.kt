package com.github.fengdai.compose.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.node.Ref
import androidx.compose.ui.unit.Constraints.Companion.Infinity
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
            Commodity(
                topContent = {
                    Box(
                        modifier = Modifier
                            .height(500.dp)
                            .fillMaxWidth()
                            .background(Brush.verticalGradient(listOf(Color.White, Color.Black)))
                    )
                }
            )
        }
    }
}

@Composable
fun rememberCommodityState(): CommodityState {
    return remember { CommodityState() }
}

class CommodityState {
    var offsetY by mutableStateOf(0)
        private set
    var minOffsetY: Int
        get() = _minOffsetYState.value
        internal set(newMin) {
            _minOffsetYState.value = newMin
            if (offsetY < newMin) {
                offsetY = newMin
            }
        }
    private var _minOffsetYState = mutableStateOf(Int.MIN_VALUE)
    private var accumulator: Float = 0f

    val reachMinOffsetY by derivedStateOf { offsetY <= minOffsetY }

    val scrollableState = ScrollableState {
        val absolute = (offsetY + it + accumulator)
        val newValue = absolute.coerceIn(minOffsetY.toFloat(), 0f)
        val changed = absolute != newValue
        val consumed = newValue - offsetY
        val consumedInt = consumed.roundToInt()
        offsetY += consumedInt
        accumulator = consumed - consumedInt

        // Avoid floating-point rounding error
        if (changed) consumed else it
    }

    val nestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(
            available: Offset,
            source: NestedScrollSource
        ): Offset {
            return if (available.y < 0 || offsetY > minOffsetY) consume(available)
            else super.onPreScroll(available, source)
        }

        override fun onPostScroll(
            consumed: Offset,
            available: Offset,
            source: NestedScrollSource
        ): Offset {
            return if (available.y > 0) consume(available)
            else super.onPostScroll(consumed, available, source)
        }
    }

    private fun consume(available: Offset): Offset {
        val consumedY = scrollableState.dispatchRawDelta(available.y)
        return available.copy(y = consumedY)
    }
}

@Composable
fun Commodity(
    state: CommodityState = rememberCommodityState(),
    topContent: @Composable () -> Unit
) {
    val webViewRef = remember { Ref<NestedScrollWebView>() }
    val flingBehavior = ScrollableDefaults.flingBehavior()

    val scrollableInteractionSource = remember { MutableInteractionSource() }
    val isDragged by scrollableInteractionSource.collectIsDraggedAsState()
    fun isScrollableEnabled() = !state.reachMinOffsetY || isDragged
    val outerNestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (isScrollableEnabled()) {
                    if (available.y > 0) {
                        webViewRef.value?.run {
                            val consumedY = available.y
                                .coerceIn(0f, scrollY.toFloat())
                            scrollBy(0, -consumedY.roundToInt())
                            return available.copy(y = consumedY)
                        }
                    }
                }
                return super.onPreScroll(available, source)
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (isScrollableEnabled()) {
                    if (available.y < 0) {
                        webViewRef.value?.run {
                            val consumedY = available.y
                                .coerceIn(-(getMaxScrollY() - scrollY).toFloat(), 0f)
                            scrollBy(0, -consumedY.roundToInt())
                            return available.copy(y = consumedY)
                        }
                    }
                }
                return super.onPostScroll(consumed, available, source)
            }
        }
    }
    Layout(
        content = {
            topContent()
            WebView(
                state = rememberWebViewState(url = "https://www.google.com.hk/search?q=nestedscroll+compose"),
                onCreated = {
                    webViewRef.value = it as NestedScrollWebView
                    ViewCompat.setNestedScrollingEnabled(it, true)
                },
                onDispose = {
                    webViewRef.value = null
                },
                factory = ::NestedScrollWebView,
                modifier = Modifier.alpha(0.99f)
            )
        },
        modifier = Modifier
            .fillMaxHeight()
            .nestedScroll(outerNestedScrollConnection)
            .scrollable(
                state.scrollableState,
                Orientation.Vertical,
                enabled = isScrollableEnabled(),
                flingBehavior = object : FlingBehavior {
                    override suspend fun ScrollScope.performFling(initialVelocity: Float): Float {
                        val remain = with(this) {
                            with(flingBehavior) {
                                performFling(initialVelocity)
                            }
                        }
                        if (remain < 0) {
                            webViewRef.value?.fling(-remain.roundToInt())
                            return 0f
                        }
                        return remain
                    }
                },
                interactionSource = scrollableInteractionSource
            )
            .nestedScroll(state.nestedScrollConnection)
    ) { measurables, constraints ->
        check(constraints.hasBoundedHeight)
        val height = constraints.maxHeight
        val topContentPlaceable =
            measurables[0].measure(constraints.copy(minHeight = 0, maxHeight = Infinity))
        val bottomContentPlaceable =
            measurables[1].measure(constraints.copy(minHeight = height, maxHeight = height))
        val width =
            constraints.constrainWidth(max(topContentPlaceable.width, bottomContentPlaceable.width))
        state.minOffsetY = -topContentPlaceable.height
        layout(
            width = width,
            height = height
        ) {
            topContentPlaceable.placeRelativeWithLayer(0, state.offsetY)
            bottomContentPlaceable.placeRelativeWithLayer(
                0,
                topContentPlaceable.height + state.offsetY
            )
        }
    }
}
