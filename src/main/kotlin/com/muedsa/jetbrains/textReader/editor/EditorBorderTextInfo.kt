package com.muedsa.jetbrains.textReader.editor

import com.intellij.util.ui.UIUtil
import com.muedsa.jetbrains.textReader.setting.OffsetType
import java.awt.Color
import java.awt.Font

data class EditorBorderTextInfo(
    var text: String = "",
    var font: Font = Font(Font.SERIF, Font.PLAIN, 12),
    var color: Color = UIUtil.getPanelBackground().brighter(),
    var offsetX: Int = 0,
    var offsetY: Int = 0,
    var offsetType: OffsetType = OffsetType.LEFT_BOTTOM,
)