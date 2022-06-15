package com.github.nayasis.kotlin.javafx.stage

import com.github.nayasis.kotlin.basica.core.io.extension
import com.github.nayasis.kotlin.basica.core.string.toPath
import com.github.nayasis.kotlin.basica.core.string.toResource
import com.github.nayasis.kotlin.basica.core.url.inStream
import com.github.nayasis.kotlin.javafx.misc.Images
import javafx.scene.image.Image
import java.io.File
import java.nio.file.Path

class IconContainer {

    val icons = ArrayList<Image>()

    fun add(icon: Image) = icons.add(icon)

    fun add(resourcePath: String) {
        resourcePath.toResource()?.inStream()?.use {
            if( resourcePath.toPath().extension == "ico" ) {
                icons.addAll(Images.toIconImage(it))
            } else {
                icons.add(Image(it))
            }
        }
    }

    fun add(file: File) = icons.addAll(Images.toIconImage(file))

    fun add(path: Path) = add(path.toFile())

    fun clear() = icons.clear()

    fun isEmpty(): Boolean = icons.isEmpty()

}