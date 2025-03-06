package com.muedsa.jetbrains.textReader.bus

interface TextReaderEventListener {
    fun onEvent(event: TextReaderEvents)
}