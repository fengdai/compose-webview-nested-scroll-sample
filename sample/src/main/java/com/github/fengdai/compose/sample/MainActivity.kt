package com.github.fengdai.compose.sample

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.NestedScrollSource.Companion.Fling
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.node.Ref
import androidx.compose.ui.unit.*
import androidx.compose.ui.unit.Constraints.Companion.Infinity
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
                            .height(1000.dp)
                            .fillMaxWidth()
                            .background(Brush.verticalGradient(listOf(Color.Black, Color.White)))
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

@Stable
class CommodityState {
    var scrollY by mutableStateOf(0)
        private set
    var maxScrollY: Int
        get() = _maxScrollYState.value
        internal set(newMax) {
            _maxScrollYState.value = newMax
            if (scrollY > newMax) {
                scrollY = newMax
            }
        }
    private var _maxScrollYState = mutableStateOf(Int.MAX_VALUE)
    private var accumulator: Float = 0f

    internal val scrollableState = ScrollableState {
        val absolute = (scrollY + it + accumulator)
        val newValue = absolute.coerceIn(0f, maxScrollY.toFloat())
        val changed = absolute != newValue
        val consumed = newValue - scrollY
        val consumedInt = consumed.roundToInt()
        scrollY += consumedInt
        accumulator = consumed - consumedInt

        // Avoid floating-point rounding error
        if (changed) consumed else it
    }

    internal lateinit var flingBehavior: FlingBehavior

    internal val nestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(
            available: Offset,
            source: NestedScrollSource
        ): Offset {
            return if (source != Fling && (available.y < 0 || scrollY < maxScrollY)) {
                consume(available)
            } else super.onPreScroll(available, source)
        }

        override fun onPostScroll(
            consumed: Offset,
            available: Offset,
            source: NestedScrollSource
        ): Offset {
            return if (source != Fling && available.y > 0) consume(available)
            else super.onPostScroll(consumed, available, source)
        }

        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
            if (available.y > 0) {
                var remain = available.y
                scrollableState.scroll {
                    with(this) {
                        with(flingBehavior) {
                            remain = -performFling(-available.y)
                        }
                    }
                }
                return Velocity.Zero.copy(y = available.y - remain)
            }
            return Velocity.Zero
        }
    }

    private fun consume(available: Offset): Offset {
        val consumedY = -scrollableState.dispatchRawDelta(-available.y)
        return available.copy(y = consumedY)
    }

    val canScrollForward by derivedStateOf { scrollY < maxScrollY }

    suspend fun animateScrollTo(
        scrollY: Float,
        animationSpec: AnimationSpec<Float> = SpringSpec()
    ) {
        scrollableState.animateScrollBy(scrollY - this.scrollY, animationSpec)
    }

    suspend fun scrollTo(scrollY: Float): Float = scrollableState.scrollBy(scrollY - this.scrollY)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Commodity(
    state: CommodityState = rememberCommodityState(),
    topContent: @Composable () -> Unit
) {
    val webViewRef = remember { Ref<NestedScrollWebView>() }
    val flingBehavior = ScrollableDefaults.flingBehavior()
    SideEffect { state.flingBehavior = flingBehavior }

    val scrollableInteractionSource = remember { MutableInteractionSource() }
    val isDragged by scrollableInteractionSource.collectIsDraggedAsState()
    fun isScrollableEnabled() = state.canScrollForward || isDragged
    val scope = rememberCoroutineScope()
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
                reverseDirection = true,
                flingBehavior = remember {
                    object : FlingBehavior {
                        override suspend fun ScrollScope.performFling(initialVelocity: Float): Float {
                            val remain = with(this) {
                                with(flingBehavior) {
                                    performFling(initialVelocity)
                                }
                            }
                            if (remain > 0) {
                                webViewRef.value?.fling(remain.roundToInt())
                                return 0f
                            }
                            Log.d("Dai", "performFling: $initialVelocity -> $remain")
                            return remain
                        }
                    }
                },
                interactionSource = scrollableInteractionSource,
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
        state.maxScrollY = topContentPlaceable.height
        layout(
            width = width,
            height = height
        ) {
            topContentPlaceable.placeRelativeWithLayer(0, -state.scrollY)
            bottomContentPlaceable.placeRelativeWithLayer(
                0,
                topContentPlaceable.height - state.scrollY
            )
        }
    }
}
