package com.github.nayasis.kotlin.javafx.misc

import com.sun.javafx.application.PlatformImpl

/**
 * run the specified function on JavaFx thread in future and wait for termination.
 * @param fn function
 */
fun runAndWait(fn: () -> Unit) = PlatformImpl.runAndWait(fn)