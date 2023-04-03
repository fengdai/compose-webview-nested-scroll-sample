package com.github.fengdai.compose.sample

import android.os.Bundle
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
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
                            .background(Brush.verticalGradient(listOf(Color.Black, Color.White)))
                    )
                    AndroidView(
                        { context ->
                            RecyclerView(context)
                                .apply {
                                    layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, 1200)
                                    layoutManager = LinearLayoutManager(context, VERTICAL, false)
                                    adapter = object : Adapter<ViewHolder>() {
                                        override fun onCreateViewHolder(
                                            parent: ViewGroup,
                                            viewType: Int
                                        ): ViewHolder =
                                            object : ViewHolder(TextView(parent.context)) {}

                                        override fun getItemCount(): Int = 100

                                        override fun onBindViewHolder(
                                            holder: ViewHolder,
                                            position: Int
                                        ) {
                                            (holder.itemView as TextView).apply {
                                                layoutParams =
                                                    ViewGroup.LayoutParams(
                                                        MATCH_PARENT,
                                                        ViewGroup.LayoutParams.WRAP_CONTENT
                                                    )
                                                text = position.toString()
                                            }
                                        }
                                    }
                                    // Nested scrolling interop is enabled when
                                    // nested scroll is enabled for the root View
                                    ViewCompat.setNestedScrollingEnabled(this, true)
                                }
                        }
                    )
                }
            }
        }
    }
}
