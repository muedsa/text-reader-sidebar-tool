package com.muedsa.jetbrains.textReader.model

import com.intellij.openapi.components.BaseState

class TextFileInfo(): BaseState() {
    var path by string()
    var size by property(defaultValue = 0L)
    var hash by string()
    var charset by string()
    var chapters by list<SimpleChapterInfo>()
    var chapterIndex by property(defaultValue = 0)
}
