package io.github.nayasis.kotlin.javafx.common

import io.github.nayasis.kotlin.basica.core.io.invariantPath
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi

private val logger = KotlinLogging.logger {}

fun createTempFile(prefix: String, suffix: String, deleteOnExist: Boolean = true): Path {
    return File.createTempFile(prefix, suffix).toPath().apply {
        logger.debug{"- temp file created: ${this.invariantPath}"}
        if(deleteOnExist) {
            DeleteOnExitHook.add(this)
        }
    }
}

@OptIn(ExperimentalPathApi::class)
fun createTempDir(prefix: String, deleteOnExist: Boolean = true): Path {
    return Files.createTempDirectory(prefix).apply {
        logger.debug{"- temp directory created: ${this.invariantPath}"}
        if(deleteOnExist) {
            DeleteOnExitHook.add(this)
        }
    }
}