@file:Suppress("unused")

package io.github.nayasis.kotlin.javafx.misc

import com.github.nayasis.kotlin.basica.core.string.find
import io.github.oshai.kotlinlogging.KotlinLogging
import javafx.scene.image.Image
import javafx.scene.input.Clipboard
import javafx.scene.input.DragEvent
import javafx.scene.input.Dragboard

private val logger = KotlinLogging.logger {}

fun DragEvent?.isAcceptable(): Boolean {
    return when {
        this == null -> false
        dragboard.hasHtmlImgTag() -> true
        dragboard.hasImage() -> true
        dragboard.hasUrl() -> true
        dragboard.hasRegularFile() -> true
        dragboard.hasString() -> true
        else -> false
    }
}

fun DragEvent?.toImage(): Image? {
    return this?.dragboard?.let { when {
        it.hasHtmlImgTag() -> getSrcFromImageTag(it.html)?.toImage()
        it.hasRegularFile() -> it.files.firstOrNull()?.toImage()
        it.hasUrl() -> it.url?.toImage()
        it.hasString() -> it.string?.toImage()
        it.hasImage() -> it.image
        else -> null
    }}
}

fun Clipboard?.toImage(): Image? {
    return this?.let { when {
        hasImage()  -> it.image.toJpgBinary().toImage()
        hasFiles()  -> it.files.firstOrNull()?.toImage()
        hasUrl()    -> it.url.toImage()
        hasString() -> it.string.toImage()
        else -> null
    }}
}

private fun getSrcFromImageTag(imgTag: String): String? {

    val regex = "(?is)^<img\\W.*?(src|srcset)=[\"|'](.*?)[\"|'].*?>".toRegex()
    val info = imgTag.replaceFirst(regex, "$1 :: $2").split(" :: ")
    val type = info[0].lowercase()
    val url  = info[1].replace("(?is)&amp;".toRegex(), "&")

    logger.trace { "html : ${imgTag}\ntype : ${type}\nurl  : ${url}" }

    return when(type) {
        "src" -> url
        "srcset" -> {
            val urls = HashMap<String,String>()
            var tmpKey = 0
            for (line in url.split(",")) {
                val values = line.split(" ")
                if (values.size == 2) {
                    urls[values[1]] = values[0]
                } else if (values.size == 1) {
                    urls[tmpKey++.toString()] = values[0]
                }
            }
            if(urls.isNotEmpty()) urls[ ArrayList(urls.keys).apply{reverse()}[0] ] else null
        }
        else -> null
    }

}

private fun Dragboard.hasRegularFile(): Boolean {
    return this.hasFiles() && this.files.firstOrNull() != null
}

private fun Dragboard.hasHtmlImgTag(): Boolean {
    return this.hasHtml() && this.html.find("(?is)^<img\\W".toPattern())
}
