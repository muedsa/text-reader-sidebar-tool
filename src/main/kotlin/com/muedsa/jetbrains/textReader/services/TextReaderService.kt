package com.muedsa.jetbrains.textReader.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.muedsa.jetbrains.textReader.TextReaderSidebarToolIcons
import com.muedsa.jetbrains.textReader.bus.TextReaderEvents
import com.muedsa.jetbrains.textReader.editor.EditorBorderTextInfo
import com.muedsa.jetbrains.textReader.model.Chapter
import com.muedsa.jetbrains.textReader.notification.TextReaderNotifications
import com.muedsa.jetbrains.textReader.setting.TextReaderSettings
import com.muedsa.jetbrains.textReader.util.TextReaderUtil
import java.io.RandomAccessFile
import kotlin.math.max
import kotlin.math.min

@Service(Service.Level.APP)
class TextReaderService {

    private val settings by lazy {
        TextReaderSettings.getInstance()
    }

    private val selectedFileInfoStore by lazy {
        TextReaderFileInfoStore.getInstance()
    }

    fun loadFile(file: VirtualFile, project: Project? = null) {
        val textFileInfo = TextReaderUtil.parseTextFile(
            file = file,
            parseChapterTitleRegex = settings.state.parseChapterTitleRegex,
            lineLengthLimit = settings.state.chapterTitleLineLengthLimit,
        )
        selectedFileInfoStore.loadState(textFileInfo)
        TextReaderNotifications.info(
            title = "加载成功",
            content = "${textFileInfo.path}<br><em>${textFileInfo.charset}</em> 共${textFileInfo.chapters.size}章",
            icon = TextReaderSidebarToolIcons.BOOK_SKULL,
            project = project
        )
        TextReaderEvents.syncPublisher().onEvent(TextReaderEvents.ChangeChapterListEvent)
        this.file = RandomAccessFile(textFileInfo.path, "r")
        changeChapter(textFileInfo.chapterIndex)
    }

    private var _file: RandomAccessFile? = null
    private var file: RandomAccessFile?
        get() {
            if (_file == null) {
                _file = selectedFileInfoStore.state?.path?.let {
                    RandomAccessFile(it, "r")
                }
            }
            return _file
        }
        set(file) {
            _file?.close()
            _file = file
        }

    private var _chapter: Chapter? = null
    val chapter: Chapter?
        get() {
            if (_chapter == null) {
                selectedFileInfoStore.state?.chapterIndex?.let {
                    changeChapter(it)
                }
            }
            return _chapter
        }

    private var rangeOfChapterContent: IntRange = 0 until 0

    val editorBorderTextInfo by lazy {
        EditorBorderTextInfo(
            font = settings.state.toFont(),
            offsetType = settings.state.editorBackgroundOffsetType,
            offsetX = settings.state.editorBackgroundOffsetX,
            offsetY = settings.state.editorBackgroundOffsetY,
        )
    }

    fun nextChapter() {
        val index = selectedFileInfoStore.state?.chapterIndex ?: return
        changeChapter(index + 1)
    }

    fun previousChapter() {
        val index = selectedFileInfoStore.state?.chapterIndex ?: return
        changeChapter(index - 1, true)
    }

    fun nextContent(length: Int): String? {
        var content: String? = null
        var chapterContent =  chapter?.contentWithoutLF
        if (chapterContent != null) {
            if (rangeOfChapterContent.endExclusive == chapterContent.length) {
                nextChapter()
                chapterContent = chapter?.contentWithoutLF
            }
        }
        if (chapterContent != null) {
            val end = min(rangeOfChapterContent.endExclusive + length, chapterContent.length)
            rangeOfChapterContent = rangeOfChapterContent.endExclusive until end
            content = chapterContent.substring(rangeOfChapterContent)
            editorBorderTextInfo.text = content
        }
        return content
    }

    fun previousContent(length: Int): String? {
        var content: String? = null
        var chapterContent = chapter?.contentWithoutLF
        if (rangeOfChapterContent.start == 0) {
            previousChapter()
            chapterContent = chapter?.contentWithoutLF
        }
        if (chapterContent != null) {
            val start = max(0, rangeOfChapterContent.start - length)
            rangeOfChapterContent = start until rangeOfChapterContent.start
            content = chapterContent.substring(rangeOfChapterContent)
            editorBorderTextInfo.text = content
        }
        return content
    }

    fun changeChapter(chapterIndex: Int, rangeToEnd: Boolean = false) {
        if (chapterIndex < 0) return
        val file = this.file ?: return
        val fileInfo = selectedFileInfoStore.state ?: return
        if (chapterIndex < fileInfo.chapters.size) {
            val chapterInfo = fileInfo.chapters[chapterIndex]
            val chapterContent = TextReaderUtil.readTextFileContent(
                file = file,
                charset = charset(fileInfo.charset!!),
                pos = chapterInfo.pos,
                length = chapterInfo.length.toInt(),
            )
            val liens = chapterContent.split("\n").map { it.trim() }.filter { it.isNotBlank() }
            this._chapter = Chapter(
                title = chapterInfo.title!!,
                pos = chapterInfo.pos,
                length = chapterInfo.length,
                rawContent = chapterContent,
                lines = liens,
                contentWithoutLF = liens.joinToString("//"),
            ).also {
                if (rangeToEnd) {
                    this.rangeOfChapterContent = it.contentWithoutLF.length until it.contentWithoutLF.length
                } else {
                    this.rangeOfChapterContent = 0 until 0
                }
            }
            selectedFileInfoStore.state?.chapterIndex = chapterIndex

            TextReaderEvents.syncPublisher().onEvent(TextReaderEvents.ChangeChapterEvent)
        }
    }

    fun clear() {
        this.file = null
        this._chapter = null
        TextReaderEvents.syncPublisher().onEvent(TextReaderEvents.ChangeChapterEvent)
        selectedFileInfoStore.clear()
        TextReaderEvents.syncPublisher().onEvent(TextReaderEvents.ChangeChapterListEvent)
    }

    companion object {
        @JvmStatic
        fun getInstance() = service<TextReaderService>()
    }
}

val IntRange.endExclusive: Int
    get() = endInclusive + 1