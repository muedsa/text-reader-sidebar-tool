package com.muedsa.jetbrains.textReader.setting

import com.intellij.ui.JBColor
import com.intellij.util.xmlb.Converter

class JBColorConverter : Converter<JBColor>() {
    override fun fromString(value: String): JBColor = value.toInt().let { JBColor(it, it) }
    override fun toString(value: JBColor): String = value.rgb.toInt().toString()
}