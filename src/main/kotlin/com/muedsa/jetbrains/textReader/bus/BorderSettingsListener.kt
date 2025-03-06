package com.muedsa.jetbrains.textReader.bus

import com.muedsa.jetbrains.textReader.services.TextReaderService
import com.muedsa.jetbrains.textReader.setting.TextReaderSettings

class BorderSettingsListener : TextReaderEventListener {

    override fun onEvent(event: TextReaderEvents) {
        if (event is TextReaderEvents.ChangeSettingsEvent) {
            val settings = TextReaderSettings.Companion.getInstance().state
            val info = TextReaderService.Companion.getInstance().editorBorderTextInfo
            info.font = settings.toFont()
            info.offsetType = settings.editorBackgroundOffsetType
            info.offsetX = settings.editorBackgroundOffsetX
            info.offsetY = settings.editorBackgroundOffsetY
        }
    }
}