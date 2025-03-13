package com.muedsa.jetbrains.textReader.editor

import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.Editor
import com.intellij.reference.SoftReference
import com.intellij.util.ui.JBUI
import com.muedsa.jetbrains.textReader.action.ReaderNextAction
import com.muedsa.jetbrains.textReader.action.ReaderPreviousAction
import com.muedsa.jetbrains.textReader.services.TextReaderService
import java.awt.Component
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Insets
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseWheelEvent
import java.awt.event.MouseWheelListener
import javax.swing.SwingUtilities
import javax.swing.border.Border

class EditorBorder(
    val editor: Editor,
) : Border {

    override fun paintBorder(
        c: Component,
        g: Graphics,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
    ) {
        if (this != SoftReference.dereference(LAST)) return
        val service = TextReaderService.getInstance()
        val chapterInfo = service.editorBorderChapterState
        val visibleArea = editor.scrollingModel.visibleArea
        val g2d = g as Graphics2D
        chapterInfo.paint(g2d, visibleArea.x, visibleArea.y, visibleArea.width, visibleArea.height)
    }

    override fun getBorderInsets(c: Component): Insets {
        return JBUI.emptyInsets()
    }

    override fun isBorderOpaque(): Boolean {
        return true
    }

    companion object {
        private var LAST: SoftReference<EditorBorder>? = null
        private var LAST_EDITOR: SoftReference<Editor>? = null

        private val MOUSE_LISTENER = object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    val actionManager = ActionManager.getInstance()
                    val dataManager = DataManager.getInstance()
                    dataManager.dataContextFromFocusAsync.then {
                        val actionEvent = AnActionEvent.createFromInputEvent(
                            e,
                            "mouse click",
                            null,
                            it,
                        )
                        if (e.isAltDown) {
                            actionManager.getAction(ReaderPreviousAction.ID).actionPerformed(actionEvent)
                        } else {
                            actionManager.getAction(ReaderNextAction.ID).actionPerformed(actionEvent)
                        }
                    }
                }
            }
        }

//        private val EDITOR_MOUSE_LISTENER = object : EditorMouseListener {
//            override fun mouseClicked(event: EditorMouseEvent) {
//                if (SwingUtilities.isLeftMouseButton(event.mouseEvent)) {
//                    val actionManager = ActionManager.getInstance()
//                    val dataManager = DataManager.getInstance()
//                    val actionEvent = AnActionEvent.createFromInputEvent(
//                        event.mouseEvent,
//                        "mouse click",
//                        null,
//                        dataManager.getDataContext(event.editor.component),
//                    )
//                    if (event.mouseEvent.isAltDown) {
//                        actionManager.getAction(ReaderPreviousAction.ID).actionPerformed(actionEvent)
//                    } else {
//                        actionManager.getAction(ReaderNextAction.ID).actionPerformed(actionEvent)
//                    }
//                }
//            }
//        }

        private val MOUSE_WHEEL_LISTENER = object : MouseWheelListener {
            override fun mouseWheelMoved(e: MouseWheelEvent) {
                if (e.scrollType == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
                    val actionManager = ActionManager.getInstance()
                    val dataManager = DataManager.getInstance()
                    dataManager.dataContextFromFocusAsync.then {
                        val actionEvent = AnActionEvent.createFromInputEvent(
                            e,
                            "mouse wheel",
                            null,
                            it,
                        )
                        if (e.wheelRotation > 0) {
                            actionManager.getAction(ReaderNextAction.ID).actionPerformed(actionEvent)
                        } else {
                            actionManager.getAction(ReaderPreviousAction.ID).actionPerformed(actionEvent)
                        }
                    }
                }
            }

        }

        fun appendToEditor(
            editor: Editor,
            enableControlByMouseClick: Boolean,
            enableControlByMouseWheel: Boolean,
        ) {
            val lastEditor = SoftReference.dereference(LAST_EDITOR)
            if (editor != lastEditor) {
                editor.contentComponent.border = EditorBorder(
                    editor = editor,
                ).also {
                    clear()
                    LAST = SoftReference(it)
                    LAST_EDITOR = SoftReference(editor)
                }
                if (enableControlByMouseClick) {
                    // editor.addEditorMouseListener(EDITOR_MOUSE_LISTENER)
                    editor.contentComponent.addMouseListener(MOUSE_LISTENER)
                }
                if (enableControlByMouseWheel) {
                    editor.contentComponent.addMouseWheelListener(MOUSE_WHEEL_LISTENER)
                }
            } else {
                editor.component.repaint()
            }
        }

        fun clear() {
            SoftReference.dereference(LAST_EDITOR)?.let { editor ->
                // editor.removeEditorMouseListener(EDITOR_MOUSE_LISTENER)
                editor.contentComponent.removeMouseListener(MOUSE_LISTENER)
                editor.contentComponent.removeMouseWheelListener(MOUSE_WHEEL_LISTENER)
                editor.component.repaint()
            }
            LAST = null
            LAST_EDITOR = null
        }
    }
}