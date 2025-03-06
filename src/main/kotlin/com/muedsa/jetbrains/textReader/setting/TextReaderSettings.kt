package com.muedsa.jetbrains.textReader.setting

import com.intellij.openapi.components.*
import com.intellij.ui.JBColor
import com.intellij.util.ui.UIUtil
import com.intellij.util.xmlb.annotations.OptionTag
import java.awt.Font


@Service(Service.Level.APP)
@State(name = "TextReaderSettings", storages = [Storage(StoragePathMacros.NON_ROAMABLE_FILE)])
class TextReaderSettings : PersistentStateComponent<TextReaderSettings.State> {

    data class State(
        // 【切分章节】匹配标题正则
        @OptionTag(converter = RegexConverter::class)
        var parseChapterTitleRegex: Regex = "第[0-9零一二三四五六七八九十百千万]+章|\\s*前言\\s*|\\s*引子\\s*".toRegex(),

        // 【切分章节】标题行最大长度限制
        var chapterTitleLineLengthLimit: Int = 30,

        // 字体
        var fontFamily: String = "SansSerif",

        // 字体大小
        var fontSize: Int = 12,

        // 【章节阅读】行间距
        var lineSpace: Double = 0.5,

        // 【章节阅读】首行缩进
        var firstLineIndent: Int = 2,

        // 【章节阅读】段落间隔
        var paragraphSpace: Int = 1,

        //【单行阅读】每次读取的文本长度
        var singleLineTextLength: Int = 30,

        //【单行阅读】展示位置
        var singleLineTextShowType: ReaderLineShowType = ReaderLineShowType.EDITOR_BACKGROUND,

        //【单行阅读】文本颜色
        @OptionTag(converter = JBColorConverter::class)
        var singleLineTextColor: JBColor =
            JBColor(UIUtil.getPanelBackground().brighter(), UIUtil.getPanelBackground().brighter()),

        // 【单行阅读-编辑器】文本偏移类型
        var editorBackgroundOffsetType: OffsetType = OffsetType.LEFT_BOTTOM,

        // 【单行阅读-编辑器】文本相对于编辑器的水平偏移量
        var editorBackgroundOffsetX: Int = 5,

        // 【单行阅读-编辑器】文本相对于编辑器的垂直偏移量
        var editorBackgroundOffsetY: Int = 20,

        // 【单行阅读-编辑器】启用鼠标滚轮控制
        var enableControlByMouseClick: Boolean = false,

        // 【单行阅读-编辑器】启用鼠标滚轮控制
        var enableControlByMouseWheel: Boolean = false,
    ) {
        fun toFont(): Font = Font(fontFamily, Font.PLAIN, fontSize)
    }

    private var state: State = State()

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
    }

    companion object {
        @JvmStatic
        fun getInstance() = service<TextReaderSettings>()
    }
}