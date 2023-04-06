package com.github.fengdai.compose.sample

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
                BoxWithConstraints {
                    val scrollState = rememberScrollState()
                    val lazyListState = rememberLazyListState()
                    val lazyListFlingBehavior = ScrollableDefaults.flingBehavior()
                    Column(
                        Modifier
                            .fillMaxSize()
                            .nestedScroll(remember {
                                object : NestedScrollConnection {
                                    override fun onPreScroll(
                                        available: Offset,
                                        source: NestedScrollSource
                                    ): Offset {
                                        Log.d("Dai", "onPreScroll: $available")
                                        if (available.y < 0 && scrollState.canScrollForward) {
                                            val consumed =
                                                -scrollState.dispatchRawDelta(-available.y)
                                            return if (consumed == 0f) Offset.Zero
                                            else Offset(0f, consumed)
                                        }
                                        return super.onPreScroll(available, source)
                                    }

                                    override suspend fun onPostFling(
                                        consumed: Velocity,
                                        available: Velocity
                                    ): Velocity {
                                        if (available.y < 0 && lazyListState.canScrollForward) {
                                            var remain = available.y
                                            lazyListState.scroll {
                                                with(lazyListFlingBehavior) {
                                                    remain = -performFling(-available.y)
                                                }
                                            }
                                            return if (remain == available.y) Velocity.Zero
                                            else Velocity(0f, available.y - remain)
                                        }
                                        return super.onPostFling(consumed, available)
                                    }
                                }
                            })
                            .verticalScroll(scrollState)
                    ) {
                        Column(
                            modifier = Modifier
                                .wrapContentHeight()
                                .fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .height(1000.dp)
                                    .fillMaxWidth()
                                    .background(
                                        Brush.verticalGradient(listOf(Color.White, Color.Black))
                                    )
                            )
                            LazyColumn(
                                modifier = Modifier
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
