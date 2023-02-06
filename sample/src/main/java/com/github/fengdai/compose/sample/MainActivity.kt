package com.github.fengdai.compose.sample

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
                BoxWithConstraints {
                    val scrollState = rememberScrollState()
                    val scrollFlingBehavior = ScrollableDefaults.flingBehavior()
                    val lazyListState = rememberLazyListState()
                    val lazyListFlingBehavior = ScrollableDefaults.flingBehavior()
                    Column(
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState, flingBehavior = remember {
                                object : FlingBehavior {
                                    override suspend fun ScrollScope.performFling(initialVelocity: Float): Float {
                                        var remain = with(scrollFlingBehavior) {
                                            performFling(initialVelocity)
                                        }
                                        Log.d("Dai", "remain: $remain")
                                        if (remain > 0) {
                                            lazyListState.scroll {
                                                with(lazyListFlingBehavior) {
                                                    remain = performFling(remain)
                                                }
                                            }
                                        }
                                        return remain
                                    }
                                }
                            })
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
                                        Brush.verticalGradient(listOf(Color.White, Color.Black))
                                    )
                            )
                            val coroutineScope = rememberCoroutineScope()
                            LazyColumn(
                                modifier = Modifier
                                    .nestedScroll(remember {
                                        object : NestedScrollConnection {
                                            override fun onPreScroll(
                                                available: Offset,
                                                source: NestedScrollSource
                                            ): Offset {
                                                if (available.y < 0 && scrollState.canScrollForward) {
                                                    return Offset(
                                                        0f,
                                                        -scrollState.dispatchRawDelta(-available.y)
                                                    )
                                                }
                                                return super.onPreScroll(available, source)
                                            }
                                        }
                                    })
                                    .height(this@BoxWithConstraints.maxHeight)
                                    .fillMaxWidth(),
                                state = lazyListState,
                                flingBehavior = lazyListFlingBehavior,
                            ) {
                                items(100) {
                                    Text("$it")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
