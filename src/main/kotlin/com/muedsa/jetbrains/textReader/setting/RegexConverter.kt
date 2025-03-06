package com.muedsa.jetbrains.textReader.setting

import com.intellij.util.xmlb.Converter

class RegexConverter : Converter<Regex>() {
    override fun fromString(value: String): Regex = value.toRegex()
    override fun toString(value: Regex): String = value.toString()
}