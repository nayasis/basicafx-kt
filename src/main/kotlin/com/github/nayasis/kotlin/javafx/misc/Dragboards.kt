package com.github.nayasis.kotlin.javafx.misc

import com.github.nayasis.kotlin.basica.core.string.find
import javafx.scene.input.Dragboard

fun Dragboard.hasRegularFile(): Boolean {
    return this.hasFiles() && this.files.firstOrNull() != null
}

fun Dragboard.hasHtmlImgTag(): Boolean {
    return this.hasHtml() && this.html.find("(?is)^<img\\W".toPattern())
}