package com.github.nayasis.kotlin.javafx.control.basic

import com.github.nayasis.kotlin.basica.core.string.mask
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode.*
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

@Suppress("UNUSED_VARIABLE")
fun TextField.addMask(pattern: String,pass: Char = '#',modifier: ((inputText:String) -> String)? = null): TextField {
    val maskChars = pattern.toCharArray().toSet().filter { it != pass }.toSet()
    this.addKeyReleased{ e ->
        if( e.code in listOf(HOME,END,LEFT,RIGHT,SHIFT,CONTROL,CAPS,ESCAPE))
            return@addKeyReleased
        val prevCaret = this.caretPosition
        val maskedVal = (modifier?.invoke(text) ?: text).mask(pattern)
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