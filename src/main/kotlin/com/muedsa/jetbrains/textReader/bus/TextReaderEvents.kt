package com.muedsa.jetbrains.textReader.bus

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.util.ThrowableComputable
import com.intellij.util.messages.Topic

sealed interface TextReaderEvents {

    object ChangeChapterListEvent : TextReaderEvents
    object ChangeChapterEvent: TextReaderEvents
    object ChangeSettingsEvent : TextReaderEvents

    companion object {
        @Topic.AppLevel
        @JvmStatic
        val TOPIC = Topic.create("TextReaderEvent", TextReaderEventListener::class.java, Topic.BroadcastDirection.TO_DIRECT_CHILDREN)

        fun syncPublisher(): TextReaderEventListener = ReadAction.compute<TextReaderEventListener, RuntimeException?>(ThrowableComputable {
            if (ApplicationManager.getApplication().isDisposed) throw ProcessCanceledException()
            ApplicationManager.getApplication().messageBus.syncPublisher(TOPIC)
        })
    }
}