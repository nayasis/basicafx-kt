package io.github.nayasis.kotlin.javafx.control.basic

import javafx.scene.layout.Region

fun Region.keepPrefHeight() {
    minHeight = Region.USE_PREF_SIZE
    maxHeight = Region.USE_PREF_SIZE
}
