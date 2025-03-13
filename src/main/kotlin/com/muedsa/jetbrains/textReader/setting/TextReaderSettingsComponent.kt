package com.muedsa.jetbrains.textReader.setting

import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.util.Disposer
import com.intellij.ui.JBColor
import com.intellij.ui.dsl.builder.*
import java.awt.GraphicsEnvironment
import javax.swing.JEditorPane
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextPane
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.StyleConstants
import javax.swing.text.StyledDocument

class TextReaderSettingsComponent(
    val model: Model = Model(),
) {
    val fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().availableFontFamilyNames.toList()
    private lateinit var panel: DialogPanel
    private val disposable = Disposer.newDisposable("TextReaderSettingsComponent")


    fun getPanel(): JPanel {
        val testTextPane = JTextPane()
        lateinit var testTextComponent: JEditorPane
        testTextPane.isEditable = false
        testTextPane.text = createTestText(model.paragraphSpace)
        val styledDocument: StyledDocument = testTextPane.styledDocument
        val attributes = SimpleAttributeSet()
        StyleConstants.setFontFamily(attributes, model.fontFamily)
        StyleConstants.setFontSize(attributes, model.fontSize)
        StyleConstants.setLineSpacing(attributes, model.lineSpace.toFloat())
        StyleConstants.setFirstLineIndent(attributes, model.firstLineIndent.toFloat() * model.fontSize.toFloat())
        StyleConstants.setForeground(attributes, model.getEditorBorderTextColor())
        styledDocument.setParagraphAttributes(0, styledDocument.length, attributes, false)

        panel = panel {
            group("章节解析") {
                row("标题正则") {
                    textField().bindText(model::parseChapterTitleRegex)
                        .align(AlignX.FILL)
                }.rowComment("切分章节时使用, 匹配章节标题的正则")

                row("标题长度") {
                    slider(min = 10, max = 210, minorTickSpacing = 10, majorTickSpacing = 50)
                        .labelTable(
                            mapOf(
                                10 to JLabel("10"),
                                60 to JLabel("60"),
                                110 to JLabel("110"),
                                160 to JLabel("160"),
                                210 to JLabel("210"),
                            )
                        )
                        .bindValue(model::chapterTitleLineLengthLimit)
                }.rowComment("切分章节时使用, 章节标题行的最大长度限制")
            }.layout(RowLayout.PARENT_GRID)

            group("格式设置") {
                row("选择字体") {
                    comboBox(fonts)
                        .bindItem(model::fontFamily.toNullableProperty())
                        .whenItemSelectedFromUi {
                            StyleConstants.setFontFamily(attributes, it)
                            styledDocument.setParagraphAttributes(0, styledDocument.length, attributes, false)
                        }
                }
                row("字体大小") {
                    spinner(range = 1..100, step = 1)
                        .bindIntValue(model::fontSize)
                        .applyToComponent {
                            addChangeListener {
                                StyleConstants.setFontSize(attributes, this.value as Int)
                                styledDocument.setParagraphAttributes(0, styledDocument.length, attributes, false)
                            }
                        }
                }
                row("行间距") {
                    spinner(range = 0.0..10.0, step = 0.1)
                        .bindValue(model::lineSpace)
                        .applyToComponent {
                            addChangeListener {
                                StyleConstants.setLineSpacing(attributes, (this.value as Double).toFloat())
                                styledDocument.setParagraphAttributes(0, styledDocument.length, attributes, false)
                            }
                        }
                }
                row("首行缩进") {
                    spinner(range = 0..4, step = 1)
                        .bindIntValue(model::firstLineIndent)
                        .applyToComponent {
                            addChangeListener {
                                val size = StyleConstants.getFontSize(attributes)
                                StyleConstants.setFirstLineIndent(
                                    attributes,
                                    (this.value as Int).toFloat() * size.toFloat()
                                )
                                styledDocument.setParagraphAttributes(0, styledDocument.length, attributes, false)
                            }
                        }
                }
                row("段落间隔") {
                    spinner(range = 0..10, step = 1)
                        .bindIntValue(model::paragraphSpace)
                        .applyToComponent {
                            addChangeListener {
                                testTextPane.text = createTestText(this.value as Int)
                            }
                        }
                }
            }

            group("示例文本") {
                row {
                    cell(testTextPane)
                }
            }

            group("编辑器内阅读") {
                row("文本窗大小") {
                    spinner(range = 10..1000, step = 1)
                        .bindIntValue(model::editorBorderTextWindowWidth)
                    text("X")
                    spinner(range = 10..1000, step = 1)
                        .bindIntValue(model::editorBorderTextWindowHeight)
                }

                row("文本颜色") {
                    text("RGBA(")
                    spinner(range = 0..255, step = 1)
                        .bindIntValue(model::editorBorderTextColorR)
                        .applyToComponent {
                            addChangeListener {
                                val red = this@applyToComponent.value as Int
                                val oldColor = testTextComponent.foreground
                                val colorValue = ((oldColor.alpha and 0xFF) shl 24) or
                                        ((red and 0xFF) shl 16) or
                                        ((oldColor.green and 0xFF) shl 8) or
                                        ((oldColor.blue and 0xFF) shl 0)
                                val color = JBColor(colorValue, colorValue)
                                testTextComponent.foreground = color
                                testTextComponent.text =
                                    "rgba(${red},${oldColor.green},${oldColor.blue},${oldColor.alpha})"
                                StyleConstants.setForeground(attributes, color)
                                styledDocument.setParagraphAttributes(0, styledDocument.length, attributes, false)
                            }
                        }
                    text(",")
                    spinner(range = 0..255, step = 1)
                        .bindIntValue(model::editorBorderTextColorG)
                        .applyToComponent {
                            addChangeListener {
                                val green = this@applyToComponent.value as Int
                                val oldColor = testTextComponent.foreground
                                val colorValue = ((oldColor.alpha and 0xFF) shl 24) or
                                        ((oldColor.red and 0xFF) shl 16) or
                                        ((green and 0xFF) shl 8) or
                                        ((oldColor.blue and 0xFF) shl 0)
                                val color = JBColor(colorValue, colorValue)
                                testTextComponent.foreground = color
                                testTextComponent.text =
                                    "rgba(${oldColor.red},${green},${oldColor.blue},${oldColor.alpha})"
                                StyleConstants.setForeground(attributes, color)
                                styledDocument.setParagraphAttributes(0, styledDocument.length, attributes, false)
                            }
                        }
                    text(",")
                    spinner(range = 0..255, step = 1)
                        .bindIntValue(model::editorBorderTextColorB)
                        .applyToComponent {
                            addChangeListener {
                                val blue = this@applyToComponent.value as Int
                                val oldColor = testTextComponent.foreground
                                val colorValue = ((oldColor.alpha and 0xFF) shl 24) or
                                        ((oldColor.red and 0xFF) shl 16) or
                                        ((oldColor.green and 0xFF) shl 8) or
                                        ((blue and 0xFF) shl 0)
                                val color = JBColor(colorValue, colorValue)
                                testTextComponent.foreground = color
                                testTextComponent.text =
                                    "rgba(${oldColor.red},${oldColor.green},${blue},${oldColor.alpha})"
                                StyleConstants.setForeground(attributes, color)
                                styledDocument.setParagraphAttributes(0, styledDocument.length, attributes, false)
                            }
                        }
                    spinner(range = 0..255, step = 1)
                        .bindIntValue(model::editorBorderTextColorA)
                        .applyToComponent {
                            addChangeListener {
                                val alpha = this@applyToComponent.value as Int
                                val oldColor = testTextComponent.foreground
                                val colorValue = ((alpha and 0xFF) shl 24) or
                                        ((oldColor.red and 0xFF) shl 16) or
                                        ((oldColor.green and 0xFF) shl 8) or
                                        ((oldColor.blue and 0xFF) shl 0)
                                val color = JBColor(colorValue, colorValue)
                                testTextComponent.foreground = color
                                testTextComponent.text =
                                    "rgba(${oldColor.red},${oldColor.green},${oldColor.blue},${alpha})"
                                StyleConstants.setForeground(attributes, color)
                                styledDocument.setParagraphAttributes(0, styledDocument.length, attributes, false)
                            }
                        }
                    text(")")
                    text("rgba(${model.editorBorderTextColorR},${model.editorBorderTextColorG},${model.editorBorderTextColorB},${model.editorBorderTextColorA})").applyToComponent {
                        testTextComponent = this@applyToComponent
                        foreground = model.getEditorBorderTextColor()
                    }
                }

                buttonsGroup {
                    row("偏移类型") {
                        radioButton("左上↖", OffsetType.LEFT_TOP)
                        radioButton("左下↙", OffsetType.LEFT_BOTTOM)
                        radioButton("右上↗", OffsetType.RIGHT_TOP)
                        radioButton("右上↘", OffsetType.RIGHT_BOTTOM)
                    }
                }.bind(model::editorBorderTextWindowOffsetType)

                row("偏移量") {
                    spinner(range = 1..255, step = 1)
                        .label("X=", LabelPosition.LEFT)
                        .bindIntValue(model::editorBorderTextWindowOffsetX)
                    spinner(range = 1..255, step = 1)
                        .label("Y=", LabelPosition.LEFT)
                        .bindIntValue(model::editorBorderTextWindowOffsetY)
                }

                row("窗口滚动速率") {
                    spinner(range = 0.01..1.0, step = 0.01)
                        .bindValue(model::editorBorderTextWindowScrollSpeedRate)
                }

                buttonsGroup {
                    row("鼠标单击控制") {
                        radioButton("启用", true)
                        radioButton("关闭", false)
                    }
                }.bind(model::enableControlByMouseClick)

                buttonsGroup {
                    row("鼠标滚轮控制") {
                        radioButton("启用", true)
                        radioButton("关闭", false)
                    }
                }.bind(model::enableControlByMouseWheel)
            }
        }
        return panel
    }

    fun isModify(): Boolean = panel.isModified()

    fun apply() {
        panel.apply()
        model.update(TextReaderSettings.getInstance().state)
    }

    fun reset() {
        panel.reset()
    }

    fun dispose() {
        disposable.dispose()
    }

    class Model(
        // 【切分章节】匹配标题正则
        var parseChapterTitleRegex: String = "第[0-9零一二三四五六七八九十百千万]+章|\\s*前言\\s*|\\s*引子\\s*",

        // 【切分章节】标题行最大长度限制
        var chapterTitleLineLengthLimit: Int = 30,

        // 字体
        var fontFamily: String = "SansSerif",

        // 字体大小
        var fontSize: Int = 12,

        // 行间距
        var lineSpace: Double = 0.5,

        // 首行缩进
        var firstLineIndent: Int = 2,

        // 段落间隔
        var paragraphSpace: Int = 1,

        //【编辑器内阅读】编辑器内文本窗口的宽度
        var editorBorderTextWindowWidth: Int = 500,
        //【编辑器内阅读】编辑器内文本窗口的高度
        var editorBorderTextWindowHeight: Int = 200,

        //【编辑器内阅读】文本颜色
        var editorBorderTextColorR: Int = 64,
        var editorBorderTextColorG: Int = 64,
        var editorBorderTextColorB: Int = 64,
        var editorBorderTextColorA: Int = 255,

        // 【编辑器内阅读】文本偏移类型
        var editorBorderTextWindowOffsetType: OffsetType = OffsetType.LEFT_BOTTOM,

        // 【编辑器内阅读】文本相对于编辑器的水平偏移量
        var editorBorderTextWindowOffsetX: Int = 5,
        // 【编辑器内阅读】文本相对于编辑器的垂直偏移量
        var editorBorderTextWindowOffsetY: Int = 20,

        // 【编辑器内阅读】窗口滚动速率
        var editorBorderTextWindowScrollSpeedRate: Double = 0.15,

        // 【编辑器内阅读】启用鼠标滚轮控制
        var enableControlByMouseClick: Boolean = false,

        // 【编辑器内阅读】启用鼠标滚轮控制
        var enableControlByMouseWheel: Boolean = false,
    ) {

        fun update(state: TextReaderSettings.State) {
            state.parseChapterTitleRegex = parseChapterTitleRegex.toRegex()
            state.chapterTitleLineLengthLimit = chapterTitleLineLengthLimit
            state.fontFamily = fontFamily
            state.fontSize = fontSize
            state.lineSpace = lineSpace
            state.firstLineIndent = firstLineIndent
            state.paragraphSpace = paragraphSpace
            state.editorBorderTextWindowWidth = editorBorderTextWindowWidth
            state.editorBorderTextWindowHeight = editorBorderTextWindowHeight
            state.editorBorderTextColor = getEditorBorderTextColor()
            state.editorBorderTextWindowOffsetType = editorBorderTextWindowOffsetType
            state.editorBorderTextWindowOffsetX = editorBorderTextWindowOffsetX
            state.editorBorderTextWindowOffsetY = editorBorderTextWindowOffsetY
            state.editorBorderTextWindowScrollSpeedRate = editorBorderTextWindowScrollSpeedRate
            state.enableControlByMouseClick = enableControlByMouseClick
            state.enableControlByMouseWheel = enableControlByMouseWheel
        }

        fun getEditorBorderTextColor(
            r: Int? = null,
            g: Int? = null,
            b: Int? = null,
            a: Int? = null,
        ): JBColor {
            val colorValue = (((a ?: editorBorderTextColorA) and 0xFF) shl 24) or
                    (((r ?: editorBorderTextColorR) and 0xFF) shl 16) or
                    (((g ?: editorBorderTextColorG) and 0xFF) shl 8) or
                    (((b ?: editorBorderTextColorB) and 0xFF) shl 0)
            return JBColor(colorValue, colorValue)
        }

        companion object {
            fun from(state: TextReaderSettings.State): Model {
                val color = state.editorBorderTextColor
                return Model(
                    parseChapterTitleRegex = state.parseChapterTitleRegex.toString(),
                    fontFamily = state.fontFamily,
                    fontSize = state.fontSize,
                    lineSpace = state.lineSpace,
                    firstLineIndent = state.firstLineIndent,
                    paragraphSpace = state.paragraphSpace,
                    editorBorderTextWindowWidth = state.editorBorderTextWindowWidth,
                    editorBorderTextWindowHeight = state.editorBorderTextWindowHeight,
                    editorBorderTextColorR = color.red,
                    editorBorderTextColorG = color.green,
                    editorBorderTextColorB = color.blue,
                    editorBorderTextColorA = color.alpha,
                    editorBorderTextWindowOffsetType = state.editorBorderTextWindowOffsetType,
                    editorBorderTextWindowOffsetX = state.editorBorderTextWindowOffsetX,
                    editorBorderTextWindowOffsetY = state.editorBorderTextWindowOffsetY,
                    editorBorderTextWindowScrollSpeedRate = state.editorBorderTextWindowScrollSpeedRate,
                    enableControlByMouseClick = state.enableControlByMouseClick,
                    enableControlByMouseWheel = state.enableControlByMouseWheel,
                )
            }
        }
    }

    companion object {
        fun createTestText(paragraphSpace: Int): String {
            return ("这是一段测试测试文字。。。Test......".repeat(2) + "\n".repeat(paragraphSpace + 1)).repeat(3)
        }
    }
}