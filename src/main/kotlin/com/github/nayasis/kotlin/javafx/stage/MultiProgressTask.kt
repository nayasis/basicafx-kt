package com.github.nayasis.kotlin.javafx.stage

import javafx.beans.property.SimpleMapProperty
import javafx.concurrent.Task

abstract class MultiProgressTask<T>: Task<T>() {

    var dialog: MultiProgressDialogCore? = null

    override fun run() {
        super.run()
    }

    abstract fun run(dialog: MultiProgressDialogCore)

}