package io.github.nayasis.kotlin.javafx.common

import io.github.nayasis.kotlin.basica.core.io.delete
import java.nio.file.Path

object DeleteOnExitHook {

    private val deletePaths = LinkedHashSet<Path>()

    init {
        Runtime.getRuntime().addShutdownHook(Thread {
            deletePaths.forEach { it.delete() }
        })
    }

    fun add(path: Path) {
        deletePaths.add(path)
    }

}