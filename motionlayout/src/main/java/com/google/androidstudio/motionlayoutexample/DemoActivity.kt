/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.androidstudio.motionlayoutexample

import android.os.Build
import android.os.Bundle
import android.support.annotation.IntRange
import android.support.annotation.LayoutRes
import android.support.annotation.RequiresApi
import android.support.constraint.motion.MotionLayout
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.ImageView

@RequiresApi(Build.VERSION_CODES.LOLLIPOP) // for View#clipToOutline
class DemoActivity : AppCompatActivity() {
    private lateinit var container: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = intent.getIntExtra("layout_file_id", R.layout.motion_01_basic)
        setContentView(layout)
        container = findViewById(R.id.motionLayout)

        if (layout == R.layout.motion_11_coordinatorlayout) {
            val icon = findViewById<ImageView>(R.id.icon)
            icon?.clipToOutline = true
        }

        val doShowPaths = intent.getBooleanExtra("showPaths", false)
        val motionLayout = (container as MotionLayout)
        motionLayout.setShowPaths(doShowPaths)

        scene {
            duration = 1000
            layouts {
                layout {
                    layoutRes = R.layout.motion_01_cl_start
                    percent = 0
                }
                layout {
                    layoutRes = R.layout.motion_01_cl_middle
                    percent = 50
                }
                layout {
                    layoutRes = R.layout.motion_01_cl_end
                    percent = 100
                }
            }
        }.startOn(motionLayout)
    }

    @DslMarker
    annotation class SceneDsl

    data class Scene(
            val duration: Int,
            val layouts: List<Layout>
    )

    data class Layout(
            @LayoutRes
            val layoutRes: Int,
            @IntRange(from = 0, to = 100)
            val percent: Int
    )

    fun scene(block: SceneBuilder.() -> Unit): Scene =
            SceneBuilder().apply(block).build()

    fun Scene.startOn(motionLayout : MotionLayout){
        Log.d("MotionLayout", "Duration: $duration")
        Log.d("MotionLayout", "Layouts: ${layouts.size}")
        layouts.forEachIndexed{index, layout ->
            Log.d("MotionLayout", "    Layout[$index]: ${layout.layoutRes} in ${layout.percent}%")
        }
        addListener(motionLayout, layouts)
    }

    private fun addListener(motionLayout: MotionLayout, layouts: List<Layout>) {
        motionLayout.setTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, progress: Float) {
                Log.d("MotionLayout", "progress: $progress")
            }

            override fun onTransitionCompleted(motionLayout: MotionLayout, currentLayout: Int) {
                Log.d("MotionLayout", "transition changed to layout: $currentLayout")

                //TODO change transition layouts and start animation
                //motionLayout.setTransition(layouts[0].layoutRes, layouts[1].layoutRes)
            }

        })

    }

    @SceneDsl
    class SceneBuilder {
        var duration: Int = 1000
        private var layouts = mutableListOf<Layout>()

        fun layouts(block: LAYOUTS.() -> Unit) {
            layouts.addAll(LAYOUTS().apply(block))
        }

        fun build(): Scene = Scene(duration, layouts)
    }

    @SceneDsl
    class LAYOUTS : ArrayList<Layout>() {
        fun layout(block: LayoutBuilder.() -> Unit) {
            add(LayoutBuilder().apply(block).build())
        }
    }

    @SceneDsl
    class LayoutBuilder {
        var layoutRes: Int = -1
        @IntRange(from = 0, to = 100)
        var percent: Int = 0

        fun build(): Layout = Layout(layoutRes, percent)
    }


    fun changeState(v: View?) {
        val motionLayout = container as? MotionLayout ?: return
        if (motionLayout.progress > 0.5f) {
            motionLayout.transitionToStart()
        } else {
            motionLayout.transitionToEnd()
        }
    }
}