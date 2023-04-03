package com.github.fengdai.compose.sample

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.node.Ref
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.ViewCompat
import androidx.core.widget.NestedScrollView

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Column(
                        modifier = Modifier
                            .wrapContentHeight()
                            .fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .height(500.dp)
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color.Black,
                                            Color.White
                                        )
                                    )
                                )
                        )
                        val nestedScrollViewRef = remember { Ref<NestedScrollView>() }
                        AndroidView(
                            { context ->
                                object : NestedScrollView(context) {
                                    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
                                        return super.dispatchTouchEvent(ev)
                                    }
                                }.apply {
                                    nestedScrollViewRef.value = this
                                    layoutParams =
                                        ViewGroup.LayoutParams(MATCH_PARENT, 1000)
                                    // Nested scrolling interop is enabled when
                                    // nested scroll is enabled for the root View
                                    ViewCompat.setNestedScrollingEnabled(this, true)
                                    addView(FrameLayout(context).apply {
                                        layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, 1400)
                                        addView(
                                            TextView(context)
                                                .apply {
                                                    text = "text"
                                                },
                                            FrameLayout.LayoutParams(WRAP_CONTENT, 1400)
                                                .also {
                                                    it.gravity = Gravity.BOTTOM
                                                }
                                        )
                                    })
                                }
                            },
                            modifier = Modifier
                                .pointerInput(Unit) {
                                    awaitPointerEventScope {
                                        while (true) {
                                            awaitPointerEvent(PointerEventPass.Initial).also {
                                                Log.d(
                                                    "Main",
                                                    "Initial: ${it.changes[0].isConsumed}"
                                                )
                                            }
                                            awaitPointerEvent(PointerEventPass.Main).also {
                                                Log.d("Main", "Main: ${it.changes[0].isConsumed}")
                                            }
                                            awaitPointerEvent(PointerEventPass.Final).also {
                                                Log.d("Main", "Final: ${it.changes[0].isConsumed}")
                                            }
                                        }
                                    }
                                }
                                .pointerInput(Unit) {
                                    awaitEachGesture {
                                        awaitFirstDown(
                                            requireUnconsumed = false,
                                            pass = PointerEventPass.Initial
                                        )
                                        Log.d("Main", "Initial")
                                        nestedScrollViewRef.value
                                            ?.requestDisallowInterceptTouchEvent(true)
                                    }
                                },
                        )
                    }
                }
            }
        }
    }
}
