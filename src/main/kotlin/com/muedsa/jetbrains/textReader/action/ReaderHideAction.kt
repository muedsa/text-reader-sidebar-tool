package com.muedsa.jetbrains.textReader.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.muedsa.jetbrains.textReader.editor.EditorBorder

class ReaderHideAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        EditorBorder.clear()
    }

    companion object {
        // const val ID = "com.muedsa.jetbrains.textReader.action.ReaderHideAction"
    }
}