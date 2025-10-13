package io.github.nayasis.kotlin.javafx.geometry

import javafx.geometry.Insets

fun Insets(top: Number, right: Number, bottom: Number, left: Number): Insets =
    Insets(top.toDouble(), right.toDouble(), bottom.toDouble(), left.toDouble())

fun Insets(value: Number): Insets =
    Insets(value.toDouble(), value.toDouble(), value.toDouble(), value.toDouble())