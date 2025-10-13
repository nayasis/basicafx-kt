package io.github.nayasis.kotlin.javafx.control.basic

import io.github.nayasis.kotlin.javafx.control.glyph.toGraphic
import javafx.scene.control.Button
import javafx.scene.control.OverrunStyle
import org.controlsfx.glyphfont.FontAwesome

fun Button(text: String? = null, glyph: FontAwesome.Glyph? = null): Button {
    return when (glyph) {
        null -> Button(text)
        else -> Button(text,glyph.toGraphic())
    }.apply { textOverrun = OverrunStyle.CLIP }
}