package io.github.nayasis.kotlin.javafx.animation

import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.event.ActionEvent
import javafx.event.EventHandler
import kotlin.time.Duration

class Repeat { companion object {

    fun runOnce(delay: Duration, action: EventHandler<ActionEvent>): Timeline {
        return run(delay, 1, action).also {
            it.playFromStart()
        }
    }

    fun run(cycle: Duration, repeatCount: Int = -1, action: EventHandler<ActionEvent>): Timeline {
        return Timeline( KeyFrame(cycle.toFxDuration(),action) ).apply {
            cycleCount = if(repeatCount >= 0) repeatCount else Timeline.INDEFINITE
        }
    }

}}

fun Duration.toFxDuration(): javafx.util.Duration {
    return javafx.util.Duration.millis(this.inWholeMicroseconds / 1000.0)
}