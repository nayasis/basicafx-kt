package com.github.nayasis.kotlin.javafx.stage

import javafx.beans.property.SimpleMapProperty
import javafx.concurrent.Task

abstract class MultiProgressTask<T>: Task<T>() {

    val subProgress = SimpleMapProperty<Int,Double>()

    fun getProgress(index: Int): Double {
        return when(index) {
            0 -> progress
            else -> subProgress[index] ?: 0.0
        }
    }

}