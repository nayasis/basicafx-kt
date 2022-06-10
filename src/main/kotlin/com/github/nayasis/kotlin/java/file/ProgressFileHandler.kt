package com.github.nayasis.kotlin.java.file

import com.github.nayasis.kotlin.basica.core.path.delete
import com.github.nayasis.kotlin.basica.core.path.exists
import com.github.nayasis.kotlin.basica.core.path.fileSize
import com.github.nayasis.kotlin.basica.core.path.isDirectory
import com.github.nayasis.kotlin.basica.core.path.isFile
import com.github.nayasis.kotlin.basica.core.path.makeDir
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.BasicFileAttributes

fun copyFile(src: Path, trg: Path, overwrite: Boolean = true, callback: ((read: Long, size: Long) -> Unit)? = null) {
    if(trg.exists()) {
        if(trg.isFile() && overwrite) {
            trg.delete()
        } else {
            src.fileSize.let { callback?.invoke(it,it) }
            return
        }
    }
    FileInputStream(src.toFile()).channel.use { srcChannel ->
        val rbc = CallbackByteChannel(srcChannel,src.fileSize,callback)
        FileOutputStream(trg.toFile()).channel.use { trgChannel ->
            trgChannel.transferFrom(rbc,0,Long.MAX_VALUE)
        }
    }
}

fun copyTree(src: Path, target: Path, overwrite: Boolean = true, callback:((index: Int, file: Path, fileRead: Long, fileSize: Long) -> Unit)?) {
    if(!src.isDirectory()) return
    var index = 0
    Files.walkFileTree(src, object: SimpleFileVisitor<Path>() {
        override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
            target.resolve(src.relativize(dir)).makeDir()
            return FileVisitResult.CONTINUE
        }
        override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
            index++
            copyFile(file,target.resolve(src.relativize(file)),overwrite) { read, size ->
                callback?.invoke(index,file,read,size)
            }
            return FileVisitResult.CONTINUE
        }
    })
}

fun moveTree(src: Path, trg: Path, overwrite: Boolean, callback:((index: Int,file: Path,fileRead: Long,fileSize: Long) -> Unit)?) {
    if(!src.isDirectory()) return
    val option = if(overwrite) arrayOf(StandardCopyOption.REPLACE_EXISTING) else emptyArray()
    var trgProvider = trg.fileSystem.provider()
    var index = 0
    Files.walkFileTree(src, object: SimpleFileVisitor<Path>() {
        override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
            trg.resolve(src.relativize(dir)).makeDir()
            return FileVisitResult.CONTINUE
        }
        override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
            index++
            val desc = trg.resolve(src.relativize(file))
            if(file.fileSystem.provider() == trgProvider ) {
                Files.move(file,desc,*option)
                file.fileSize.let { callback?.invoke(index,file,it,it) }
            } else {
                copyFile(file,desc,overwrite) { read, size ->
                    callback?.invoke(index,file,read,size)
                }
            }
            return FileVisitResult.CONTINUE
        }
    })
}