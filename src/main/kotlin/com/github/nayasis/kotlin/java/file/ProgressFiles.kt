package com.github.nayasis.kotlin.java.file

import com.github.nayasis.kotlin.basica.core.path.delete
import com.github.nayasis.kotlin.basica.core.path.exists
import com.github.nayasis.kotlin.basica.core.path.fileSize
import com.github.nayasis.kotlin.basica.core.path.getAttributes
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
import java.nio.file.attribute.BasicFileAttributeView
import java.nio.file.attribute.BasicFileAttributes

class ProgressFiles { companion object {

    fun copyFile(src: Path, trg: Path, overwrite: Boolean = true, callback: ((readSize: Long,totalSize: Long) -> Unit)? = null) {
        if(!src.isFile())
            throw IllegalArgumentException("No file found (${src})")
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

    fun moveFile(src: Path, trg: Path, overwrite: Boolean = true, callback: ((readSize: Long,totalSize: Long) -> Unit)? = null) {
        if(!src.isFile())
            throw IllegalArgumentException("No file found (${src})")
        var trgProvider = trg.fileSystem.provider()
        if(src.fileSystem.provider() == trgProvider ) {
            val option = toCopyOptions(overwrite)
            Files.move(src,trg,*option)
            src.fileSize.let { callback?.invoke(it,it) }
        } else {
            copyFile(src,trg,overwrite,callback)
            copyAttribute(src,trg)
            src.delete()
        }
    }

    private fun copyAttribute(source: Path,target: Path) {
        val srcAttr = source.getAttributes<BasicFileAttributes>()
        val trgAttr = Files.getFileAttributeView(target,BasicFileAttributeView::class.java)
        trgAttr.setTimes(
            srcAttr.lastModifiedTime(),
            srcAttr.lastAccessTime(),
            srcAttr.creationTime(),
        )
    }

    fun copyDirectory(source: Path,target: Path,overwrite: Boolean = true,callback:((index: Int,file: Path,readSize: Long,fileSize: Long) -> Unit)?) {
        if(!source.isDirectory())
            throw IllegalArgumentException("No directory found (${source})")
        var index = 0
        Files.walkFileTree(source, object: SimpleFileVisitor<Path>() {
            override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                target.resolve(source.relativize(dir)).makeDir()
                return FileVisitResult.CONTINUE
            }
            override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                index++
                copyFile(file,target.resolve(source.relativize(file)),overwrite) { read,total ->
                    callback?.invoke(index,file,read,total)
                }
                return FileVisitResult.CONTINUE
            }
        })
    }

    fun moveDirectory(source: Path,target: Path,overwrite: Boolean,callback:((index: Int,file: Path,readSize: Long,fileSize: Long) -> Unit)?) {
        if(!source.isDirectory())
            throw IllegalArgumentException("No directory found (${source})")
        var index = 0
        Files.walkFileTree(source, object: SimpleFileVisitor<Path>() {
            override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                target.resolve(source.relativize(dir)).let {
                    copyAttribute(dir,it)
                    it.makeDir()
                }
                return FileVisitResult.CONTINUE
            }
            override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                index++
                moveFile(file,target.resolve(source.relativize(file)),overwrite){ read,total ->
                    callback?.invoke(index,file,read,total)
                }
                return FileVisitResult.CONTINUE
            }
        })
    }

    private fun toCopyOptions(overwrite: Boolean) =
        if (overwrite) arrayOf(StandardCopyOption.REPLACE_EXISTING) else emptyArray()

}}