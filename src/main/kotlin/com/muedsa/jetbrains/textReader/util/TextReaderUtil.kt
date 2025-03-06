package com.muedsa.jetbrains.textReader.util

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.io.inputStream
import com.muedsa.jetbrains.textReader.model.SimpleChapterInfo
import com.muedsa.jetbrains.textReader.model.TextFileInfo
import org.mozilla.universalchardet.UniversalDetector
import java.io.*
import java.nio.charset.Charset
import java.nio.file.Path
import java.security.MessageDigest
import java.util.*
import kotlin.io.path.absolutePathString

object TextReaderUtil {

    fun detectCharset(file: File): String = UniversalDetector.detectCharset(file)

    fun parseTextFile(file: VirtualFile, parseChapterTitleRegex: Regex, lineLengthLimit: Int): TextFileInfo {
        val path = file.toNioPath()
        val charset = detectCharset(path.toFile())
        return TextFileInfo().apply {
            this.path = path.absolutePathString()
            this.size = file.length
            this.hash = calculateFileHash(path)
            this.charset = charset
            this.chapters = parseChapters(
                path = path,
                charset = Charset.forName(charset),
                parseChapterTitleRegex = parseChapterTitleRegex,
                lineLengthLimit = lineLengthLimit,
            )
        }
    }

    fun calculateFileHash(path: Path, algorithm: String = "SHA-256"): String {
        // 创建指定算法的 MessageDigest 实例
        val digest = MessageDigest.getInstance(algorithm)
        // 创建文件输入流
        path.inputStream().use { fis ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            // 从文件中读取数据并更新 digest
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        // 完成哈希计算
        val hashBytes = digest.digest()
        // 将字节数组转换为十六进制字符串
        val hexString = StringBuilder()
        for (byte in hashBytes) {
            val hex = Integer.toHexString(0xff and byte.toInt())
            if (hex.length == 1) {
                hexString.append('0')
            }
            hexString.append(hex)
        }
        return hexString.toString()
    }

    fun parseChapters(
        path: Path,
        charset: Charset,
        parseChapterTitleRegex: Regex,
        lineLengthLimit: Int,
    ): Vector<SimpleChapterInfo> {
        val chapters = Vector<SimpleChapterInfo>()
        val reader = TextReaderBufferedReader(InputStreamReader(FileInputStream(path.toFile()), charset))
        reader.use {
            var offset = 0L
            var previousChapter: SimpleChapterInfo? = null
            var line = reader.readLineWithCRLF()
            while (line != null) {
                val trimLine = line.trim()
                if (trimLine.length <= lineLengthLimit) {
                    if (parseChapterTitleRegex.containsMatchIn(trimLine)) {
                        if (previousChapter != null) {
                            previousChapter.length = offset - previousChapter.pos
                        } else if (offset > 0) {
                            chapters.add(
                                SimpleChapterInfo().apply {
                                    title = "章节前部分"
                                    pos = 0L
                                    length = offset
                                }
                            )
                        }
                        previousChapter = SimpleChapterInfo().apply {
                            this.title = trimLine
                            this.pos = offset
                            this.length = -1
                        }
                        chapters.add(previousChapter)
                    }
                }
                offset += line.toByteArray(charset).size
                line = reader.readLineWithCRLF()
            }
            if (previousChapter != null) {
                previousChapter.length = offset - previousChapter.pos
            }
        }
        return chapters
    }

    fun readTextFileContent(
        file: RandomAccessFile,
        charset: Charset,
        pos: Long,
        length: Int,
    ): String {
        val bytes = ByteArray(length)
        file.seek(pos.toLong())
        file.read(bytes)
        return bytes.toString(charset)
    }

    class TextReaderBufferedReader(r: Reader) : BufferedReader(r) {

        @Throws(IOException::class)
        fun readLineWithCRLF(): String? {
            val content = StringBuilder()
            var code = super.read()
            if (code >= 0) {
                while (true) {
                    if (code == '\n'.code) {
                        content.append('\n')
                        break
                    }
                    if (code < 0) {
                        break
                    }
                    content.appendCodePoint(code)
                    code = super.read()
                }
            } else {
                return null
            }
            return content.toString()
        }
    }
}