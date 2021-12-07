package com.github.nayasis.kotlin.tornadofx.extension

import tornadofx.FXTask

fun FXTask<*>.updateProgress(workDone: Number, max: Number) {
    this.updateProgress(workDone.toDouble(),max.toDouble())
}