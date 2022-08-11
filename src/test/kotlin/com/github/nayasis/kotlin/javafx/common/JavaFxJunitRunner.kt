package com.github.nayasis.kotlin.javafx.common

import com.sun.javafx.application.PlatformImpl
import java.util.concurrent.CountDownLatch

fun JavaFxJunitRunner(module: () -> Unit) {
    val latch = CountDownLatch(1)
    PlatformImpl.startup{
        try {
            module.invoke()
        } finally {
            latch.countDown()
        }
    }
    latch.await()
}