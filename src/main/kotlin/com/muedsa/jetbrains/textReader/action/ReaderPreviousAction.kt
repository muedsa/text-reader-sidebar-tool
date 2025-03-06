package com.muedsa.jetbrains.textReader.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.muedsa.jetbrains.textReader.editor.EditorBorder
import com.muedsa.jetbrains.textReader.notification.TextReaderNotifications
import com.muedsa.jetbrains.textReader.services.TextReaderService
import com.muedsa.jetbrains.textReader.setting.ReaderLineShowType
import com.muedsa.jetbrains.textReader.setting.TextReaderSettings

class ReaderPreviousAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val textReaderService = TextReaderService.getInstance()
        val settings = TextReaderSettings.getInstance().state
        val text = textReaderService.previousContent(settings.chapterTitleLineLengthLimit) ?: return
        when (settings.singleLineTextShowType) {
            ReaderLineShowType.EDITOR_BACKGROUND -> {
                e.getData(PlatformDataKeys.EDITOR)?.let {
                    EditorBorder.appendToEditor(
                        editor = it,
                        textInfo = textReaderService.editorBorderTextInfo,
                        enableControlByMouseClick = settings.enableControlByMouseClick,
                        enableControlByMouseWheel = settings.enableControlByMouseWheel,
                    )
                }
            }
            ReaderLineShowType.STATUS_BAR -> {
                EditorBorder.clear()
                TextReaderNotifications.error("状态栏类型暂不可用")
            }
            ReaderLineShowType.NOTIFY -> {
                EditorBorder.clear()
                TextReaderNotifications.chapterContent(content = text, project = e.project)
            }
        }
    }

    companion object {
        const val ID = "com.muedsa.jetbrains.textReader.action.ReaderPreviousAction"
    }
}