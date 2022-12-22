package com.github.nayasis.kotlin.javafx.animation

import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.animation.Timeline
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.util.Duration

class Timer { companion object {

    fun run(time: Duration, count: Int = 1, action: EventHandler<ActionEvent>): Timeline {
        return timeline(time, count, action).also {
            it.playFromStart()
        }
    }

    fun timeline(time: Duration, count: Int = 0, action: EventHandler<ActionEvent>): Timeline {
        return Timeline( KeyFrame(time,action) ).apply {
            cycleCount = if(count > 0) count else Timeline.INDEFINITE
        }
    }

}}