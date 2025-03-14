package com.muedsa.jetbrains.textReader.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.muedsa.jetbrains.textReader.TextReaderSidebarToolIcons
import com.muedsa.jetbrains.textReader.bus.TextReaderEvents
import com.muedsa.jetbrains.textReader.editor.EditorBorder
import com.muedsa.jetbrains.textReader.editor.EditorBorderChapterState
import com.muedsa.jetbrains.textReader.model.Chapter
import com.muedsa.jetbrains.textReader.notification.TextReaderNotifications
import com.muedsa.jetbrains.textReader.setting.TextReaderSettings
import com.muedsa.jetbrains.textReader.util.TextReaderUtil
import java.io.RandomAccessFile

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

    val editorBorderChapterState by lazy {
        EditorBorderChapterState()
    }

    fun nextChapter() {
        val index = selectedFileInfoStore.state?.chapterIndex ?: return
        changeChapter(index + 1)
    }

    fun previousChapter() {
        val index = selectedFileInfoStore.state?.chapterIndex ?: return
        changeChapter(index - 1, true)
    }

    fun nextScroll() {
        chapter?.let {
            if (editorBorderChapterState.windowChapterEnd) {
                nextChapter()
            } else {
                editorBorderChapterState.nextScroll()
            }
        }

    }

    fun previousScroll() {
        chapter?.let {
            if (editorBorderChapterState.windowStartPercentage == 0f) {
                previousChapter()
            } else {
                editorBorderChapterState.previousScroll()
            }
        }
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
                editorBorderChapterState.changeChapter(it.lines, rangeToEnd)
            }
            selectedFileInfoStore.state?.chapterIndex = chapterIndex

            TextReaderEvents.syncPublisher().onEvent(TextReaderEvents.ChangeChapterEvent)
        }
    }

    fun clear() {
        EditorBorder.clear()
        this.editorBorderChapterState.clear()
        this.file = null
        this._chapter = null
        this.selectedFileInfoStore.clear()
        TextReaderEvents.syncPublisher().onEvent(TextReaderEvents.ChangeChapterEvent)
        TextReaderEvents.syncPublisher().onEvent(TextReaderEvents.ChangeChapterListEvent)
    }

    companion object {
        @JvmStatic
        fun getInstance() = service<TextReaderService>()
    }
}