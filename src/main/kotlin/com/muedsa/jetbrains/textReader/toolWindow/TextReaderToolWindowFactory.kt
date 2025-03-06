package com.muedsa.jetbrains.textReader.toolWindow

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.muedsa.jetbrains.textReader.bus.TextReaderEventListener
import com.muedsa.jetbrains.textReader.bus.TextReaderEvents
import com.muedsa.jetbrains.textReader.services.TextReaderFileInfoStore
import com.muedsa.jetbrains.textReader.services.TextReaderService
import com.muedsa.jetbrains.textReader.setting.TextReaderSettings

class TextReaderToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(
        project: Project,
        toolWindow: ToolWindow,
    ) {
        val textReaderToolWindow = TextReaderToolWindow(toolWindow)
        val content = ContentFactory.getInstance().createContent(textReaderToolWindow.panel, null, false)
        toolWindow.contentManager.addContent(content)
        ApplicationManager.getApplication().messageBus.connect()
            .also {
                Disposer.register(toolWindow.disposable, it)
            }
            .subscribe(
                topic = TextReaderEvents.TOPIC,
                handler = object : TextReaderEventListener {
                    override fun onEvent(event: TextReaderEvents) {
                        if (event is TextReaderEvents.ChangeChapterListEvent) {
                            val fileInfo = TextReaderFileInfoStore.getInstance().state
                            textReaderToolWindow.updateChapterList(fileInfo?.chapters ?: emptyList())
                        } else if (event is TextReaderEvents.ChangeChapterEvent
                            || event is TextReaderEvents.ChangeSettingsEvent
                        ) {
                            val settings = TextReaderSettings.getInstance().state
                            val chapter = TextReaderService.getInstance().chapter
                            textReaderToolWindow.updateChapter(
                                text = chapter?.lines?.joinToString(
                                    "\n".repeat(settings.paragraphSpace + 1)
                                ) ?: "",
                                settings = settings
                            )
                        }
                    }
                }
            )
        val fileInfo = TextReaderFileInfoStore.getInstance().state
        val settings = TextReaderSettings.getInstance().state
        val chapter = TextReaderService.getInstance().chapter
        fileInfo?.chapters?.let { textReaderToolWindow.updateChapterList(it) }
        textReaderToolWindow.updateChapter(
            text = chapter?.lines?.joinToString(
                "\n".repeat(settings.paragraphSpace + 1)
            ) ?: "",
            settings = settings
        )
    }
}