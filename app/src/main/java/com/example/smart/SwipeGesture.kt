package com.example.smart

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

interface SwipeGesture {
    interface SwipeListener {
        fun onSwipeLeft()
        fun onSwipeRight()
    }

    class SwipeGestureDetector(
        private val context: Context,
        private val swipeListener: SwipeListener
    ) : GestureDetector.SimpleOnGestureListener() {

        private val gestureDetector = GestureDetector(context, this)

        fun setOnTouchListener(view: View) {
            view.setOnTouchListener { _, event ->
                gestureDetector.onTouchEvent(event)
                view.performClick()
                true
            }
        }

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            try {
                if (e1 == null) return false

                val diffAbs = abs(e1.y - e2.y)
                val diff = e1.x - e2.x

                if (diffAbs > SWIPE_MAX_OFF_PATH) return false

                if (diff > SWIPE_MIN_DISTANCE && abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    swipeListener.onSwipeLeft()
                    return true
                } else if (-diff > SWIPE_MIN_DISTANCE && abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    swipeListener.onSwipeRight()
                    return true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return false
        }

        companion object {
            private const val SWIPE_MIN_DISTANCE = 50
            private const val SWIPE_MAX_OFF_PATH = 200
            private const val SWIPE_THRESHOLD_VELOCITY = 200
        }
    }
}