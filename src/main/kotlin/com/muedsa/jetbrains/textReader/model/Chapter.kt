package com.muedsa.jetbrains.textReader.model

data class Chapter(
    var title: String,
    var pos: Long,
    var length: Long,
    var rawContent: String,
    var lines: List<String>,
    var contentWithoutLF: String,
)
