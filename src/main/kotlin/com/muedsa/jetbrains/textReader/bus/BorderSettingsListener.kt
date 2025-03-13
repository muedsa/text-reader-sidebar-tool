package com.muedsa.jetbrains.textReader.bus

import com.muedsa.jetbrains.textReader.services.TextReaderService

class BorderSettingsListener : TextReaderEventListener {

    override fun onEvent(event: TextReaderEvents) {
        if (event is TextReaderEvents.ChangeSettingsEvent) {
            val info = TextReaderService.Companion.getInstance().editorBorderChapterState
            info.textStyleChange()
        }
    }
}