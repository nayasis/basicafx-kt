package io.github.nayasis.kotlin.javafx.control.glyph

import org.controlsfx.glyphfont.FontAwesome
import org.controlsfx.glyphfont.Glyph

fun FontAwesome.Glyph.toGraphic(): Glyph {
    return FontAwesome().create(this)
}