package io.github.nayasis.kotlin.javafx.control.basic

import io.github.nayasis.kotlin.basica.core.string.mask
import io.github.oshai.kotlinlogging.KotlinLogging
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode.*

private val logger = KotlinLogging.logger {}

@Suppress("UNUSED_VARIABLE")
fun TextField.addMask(pattern: String, pass: Char = '#', hide: Char = '*', modifier: ((inputText:String) -> String)? = null): TextField {
    this.addKeyReleased{ e ->
        if( e.code in listOf(HOME,END,LEFT,RIGHT,SHIFT,CONTROL,CAPS,ESCAPE))
            return@addKeyReleased
        val prevCaret = this.caretPosition
        val maskedVal = (modifier?.invoke(text) ?: text).mask(pattern, pass, hide)
        val currCaret = when {
            text.length < maskedVal.length -> prevCaret + (maskedVal.length - text.length) + 1
            e.code == BACK_SPACE -> prevCaret
            e.code == DELETE -> prevCaret
            text.length > maskedVal.length -> {
                // paste
                if(text.length > pattern.length) {
                    prevCaret - (text.length - maskedVal.length)
                // cut
                } else {
                    prevCaret - (text.length - maskedVal.length) + 1
                }
            }
            else -> prevCaret
        }
        logger.trace { "text: $text -> $maskedVal, caret: $prevCaret -> $currCaret, event: ${e.code}" }
        text = maskedVal
        positionCaret(currCaret)
    }
    return this
}