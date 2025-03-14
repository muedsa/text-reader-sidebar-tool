package com.muedsa.jetbrains.textReader.editor

import com.muedsa.jetbrains.textReader.setting.OffsetType
import com.muedsa.jetbrains.textReader.setting.TextReaderSettings
import java.awt.Graphics2D
import java.awt.Rectangle
import java.awt.font.FontRenderContext
import java.awt.font.LineBreakMeasurer
import java.awt.font.TextLayout
import java.text.AttributedString
import kotlin.math.max
import kotlin.math.min

class EditorBorderChapterState {
    private var lines: List<String> = emptyList()
    private var layouts: List<ChapterTextLayout> = emptyList()
    var windowStartPercentage = 0f
        private set
    var windowEndPercentage = 0f
        private set
    var windowChapterEnd: Boolean = false
    private var needLayout = true
    private var frc: FontRenderContext? = null
    private var height = 0f
    private var reverse = false

    fun textStyleChange() {
        needLayout = true
    }

    fun changeChapter(lines: List<String>, rangeToEnd: Boolean = false) {
        this.lines = lines
        height = 0f
        needLayout = true
        if (rangeToEnd) {
            windowStartPercentage = 1f
            windowEndPercentage = 1f
            reverse = true
        } else {
            windowStartPercentage = 0f
            windowEndPercentage = 0f
        }
        windowChapterEnd = false
    }

    fun nextScroll() {
        val settings = TextReaderSettings.getInstance().state
        windowStartPercentage += (windowEndPercentage - windowStartPercentage) * settings.editorBorderTextWindowScrollSpeedRate.toFloat()
    }

    fun previousScroll() {
        val settings = TextReaderSettings.getInstance().state
        windowEndPercentage -= (windowEndPercentage - windowStartPercentage) * settings.editorBorderTextWindowScrollSpeedRate.toFloat()
        reverse = true
    }

    private fun layout(g2d: Graphics2D) {
        if (!needLayout && g2d.fontRenderContext == frc) return
        if (lines.isEmpty()) return
        val settings = TextReaderSettings.getInstance().state
        val windowWidth = settings.editorBorderTextWindowWidth
        var dy = 0f
        val layouts = mutableListOf<ChapterTextLayout>()
        var lineHeight = 0f
        lines.forEach { line ->
            val attributedString = AttributedString(
                "\u3000".repeat(settings.firstLineIndent) + line,
                settings.toFont().attributes
            )
            val charIterator = attributedString.iterator
            val measurer = LineBreakMeasurer(charIterator, g2d.fontRenderContext)
            while (measurer.position < charIterator.endIndex) {
                val layout = measurer.nextLayout(windowWidth.toFloat())
                dy += layout.ascent
                layouts.add(
                    ChapterTextLayout(
                        textLayout = layout,
                        x = 0f,
                        y = dy,
                    )
                )
                val temp = layout.descent + (layout.ascent + layout.descent) * settings.lineSpace.toFloat()
                dy += temp
                lineHeight = layout.ascent + temp
            }
            dy += lineHeight * settings.paragraphSpace
        }
        this.height = dy
        this.frc = g2d.fontRenderContext
        needLayout = false
        this.layouts = layouts
    }

    fun paint(g2d: Graphics2D, x: Int, y: Int, width: Int, height: Int) {
        layout(g2d)
        if (layouts.isEmpty()) return
        val settings = TextReaderSettings.getInstance().state
        val windowWidth = settings.editorBorderTextWindowWidth
        val windowHeight = settings.editorBorderTextWindowHeight
        var windowX = 0
        var windowY = 0
        when (settings.editorBorderTextWindowOffsetType) {
            OffsetType.LEFT_TOP -> {
                windowX = x + settings.editorBorderTextWindowOffsetX
                windowY = y + settings.editorBorderTextWindowOffsetY
            }

            OffsetType.LEFT_BOTTOM -> {
                windowX = x + settings.editorBorderTextWindowOffsetX
                windowY = y + height - windowHeight - settings.editorBorderTextWindowOffsetY
            }

            OffsetType.RIGHT_TOP -> {
                windowX = x + width - windowWidth - settings.editorBorderTextWindowOffsetX
                windowY = y + settings.editorBorderTextWindowOffsetY
            }

            OffsetType.RIGHT_BOTTOM -> {
                windowX = x + width - windowWidth - settings.editorBorderTextWindowOffsetX
                windowY = y + height - windowHeight - settings.editorBorderTextWindowOffsetY
            }
        }
        val originalClip = g2d.clip
        g2d.clip = Rectangle(windowX, windowY, windowWidth, windowHeight)
        if (reverse) {
            this.windowStartPercentage = max(0f, (this.height * this.windowEndPercentage - windowHeight) / this.height)
            this.reverse = false
        }
        val offsetY = windowY - this.height * this.windowStartPercentage
        g2d.color = settings.editorBorderTextColor
        layouts.forEachIndexed { index, layout ->
            val dy = offsetY + layout.y
            layout.textLayout.draw(g2d, windowX + layout.x, dy)
            if (index == layouts.lastIndex && dy < windowY) {
                windowChapterEnd = true
            }
        }
        this.windowEndPercentage = min(1f, (this.height * this.windowStartPercentage + windowHeight) / this.height)
        g2d.clip(originalClip)
    }

    fun clear() {
        lines = emptyList()
        layouts = emptyList()
        windowStartPercentage = 0f
        windowEndPercentage = 0f
        windowChapterEnd = false
        needLayout = true
        frc = null
        height = 0f
        reverse = false
    }

    data class ChapterTextLayout(
        val textLayout: TextLayout,
        val x: Float,
        val y: Float,
    )
}