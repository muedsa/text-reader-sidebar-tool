package com.muedsa.jetbrains.textReader.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.muedsa.jetbrains.textReader.editor.EditorBorder
import com.muedsa.jetbrains.textReader.services.TextReaderService
import com.muedsa.jetbrains.textReader.setting.TextReaderSettings

class ReaderNextAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        e.getData(PlatformDataKeys.EDITOR)?.let {
            val textReaderService = TextReaderService.getInstance()
            val settings = TextReaderSettings.getInstance().state
            textReaderService.nextScroll()
            EditorBorder.appendToEditor(
                editor = it,
                enableControlByMouseClick = settings.enableControlByMouseClick,
                enableControlByMouseWheel = settings.enableControlByMouseWheel,
            )
        }
    }

    companion object {
        const val ID = "com.muedsa.jetbrains.textReader.action.ReaderNextAction"
    }
}