package com.github.nayasis.kotlin.javafx.control.basic

import javafx.scene.control.Button
import javafx.scene.control.OverrunStyle
import org.controlsfx.glyphfont.FontAwesome

fun Button(glyph: FontAwesome.Glyph, text: String? = null): Button {
    return Button(text,FontAwesome().create(glyph)).apply { textOverrun = OverrunStyle.CLIP }
}