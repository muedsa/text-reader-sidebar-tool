package com.muedsa.jetbrains.textReader.model

import com.intellij.openapi.components.BaseState

class SimpleChapterInfo : BaseState() {
    var title by string()
    var pos by property(0L)
    var length by property(0L)

    override fun toString(): String = "● $title"
}