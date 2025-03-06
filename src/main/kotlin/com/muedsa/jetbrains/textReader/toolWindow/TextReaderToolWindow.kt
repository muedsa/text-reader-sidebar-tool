package com.muedsa.jetbrains.textReader.toolWindow

import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import com.intellij.util.ui.JBFont
import com.intellij.util.ui.JBUI
import com.muedsa.jetbrains.textReader.model.SimpleChapterInfo
import com.muedsa.jetbrains.textReader.services.TextReaderService
import com.muedsa.jetbrains.textReader.setting.TextReaderSettings
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.Box
import javax.swing.JPanel
import javax.swing.JTextPane
import javax.swing.SwingUtilities
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.StyleConstants
import javax.swing.text.StyledDocument

class TextReaderToolWindow(
    private val toolWindow: ToolWindow,
) {

    private val readerService = TextReaderService.getInstance()

    private lateinit var chapterSearchTextField: JBTextField

    private var chapterListData: Array<SimpleChapterInfo> = emptyArray()

    private val chapterListComponent = JBList<SimpleChapterInfo>().apply {
        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (SwingUtilities.isLeftMouseButton(e) && e.clickCount == 2) {
                    val selectedIndex = this@apply.selectedIndex
                    val selectedTitle = this@apply.selectedValue?.title
                    if (selectedIndex >= 0 && selectedTitle != null) {
                        if (this@apply.itemsCount < chapterListData.size) {
                            val model = this@apply.model
                            var repetitions = 0
                            for (i in 0 until selectedIndex) {
                                if (model.getElementAt(i).title == selectedTitle) {
                                    repetitions++
                                }
                            }
                            for (i in 0 until chapterListData.size) {
                                if (chapterListData[i].title == selectedTitle) {
                                    if (repetitions > 0) {
                                        repetitions--
                                    } else {
                                        readerService.changeChapter(i)
                                        panel.selectedIndex = 1
                                        break
                                    }
                                }
                            }
                        } else {
                            readerService.changeChapter(selectedIndex)
                            panel.selectedIndex = 1
                        }
                    }
                }
            }
        })
    }

    private val contentTextComponent = JTextPane()

    private val titlePanel = panel {
        row {
            button("File") {
                val file = FileChooser.chooseFile(
                    FileChooserDescriptor(
                        /* chooseFiles = */ true,
                        /* chooseFolders = */ false,
                        /* chooseJars = */ false,
                        /* chooseJarsAsFiles = */ false,
                        /* chooseJarContents = */ false,
                        /* chooseMultiple = */ false,
                    ).withFileFilter { it.extension == "txt" },
                    toolWindow.project,
                    null
                )
                if (file != null) {
                    readerService.loadFile(file = file, project = toolWindow.project)
                }
            }
            button("Clear") {
                readerService.clear()
            }
            cell(Box.createHorizontalBox().apply {
                add(Box.createHorizontalGlue())
            })
            textField()
                .also {
                    chapterSearchTextField = it.component
                    chapterSearchTextField.addActionListener {
                        val search = chapterSearchTextField.text
                        if (search.isNotEmpty()) {
                            val listData = chapterListData.filter {
                                val title = it.title
                                title != null && title.contains(search)
                            }.toTypedArray()
                            chapterListComponent.setListData(listData)
                        } else {
                            chapterListComponent.setListData(chapterListData)
                        }
                    }
                }
                .horizontalAlign(HorizontalAlign.RIGHT)
        }
        row {
            scrollCell(chapterListComponent)
                .align(Align.FILL)
                .resizableColumn()
        }.resizableRow()
    }

    private val contentPanel = panel {
        row {
            button("Previous") {
                readerService.previousChapter()
            }
                .align(Align.FILL)
                .resizableColumn()
            button("Next") {
                readerService.nextChapter()
            }
                .align(Align.FILL)
                .resizableColumn()
        }
        row {
            scrollCell(contentTextComponent)
                .align(Align.FILL)
                .applyToComponent {
                    isEditable = false
                }
        }.resizableRow()
    }

    val panel = JBTabbedPane()
        .apply {
            font = JBFont.regular()
            background = JBUI.CurrentTheme.ComplexPopup.HEADER_BACKGROUND
            isFocusable = false
        }

    init {
        panel.add("Title", titlePanel)
        panel.add("Content", contentPanel)
        panel.add("Hidden", JPanel())
        panel.selectedIndex = 0
    }

    fun updateChapterList(list: List<SimpleChapterInfo>) {
        chapterSearchTextField.text = ""
        chapterListData = list.toTypedArray()
        chapterListComponent.setListData(chapterListData)
    }

    fun updateChapter(text: String, settings: TextReaderSettings.State) {
        if (chapterSearchTextField.text.isNotEmpty() && chapterListComponent.itemsCount < chapterListData.size) {
            chapterSearchTextField.text = ""
            chapterListComponent.setListData(chapterListData)
        }
        contentTextComponent.text = text
        contentTextComponent.setCaretPosition(0)
        val attributes = SimpleAttributeSet()
        StyleConstants.setFontFamily(attributes, settings.fontFamily)
        StyleConstants.setFontSize(attributes, settings.fontSize)
        StyleConstants.setLineSpacing(attributes, settings.lineSpace.toFloat())
        StyleConstants.setFirstLineIndent(attributes, settings.firstLineIndent.toFloat() * settings.fontSize.toFloat())
        val styledDocument: StyledDocument = contentTextComponent.styledDocument
        styledDocument.setParagraphAttributes(0, styledDocument.length, attributes, false)
    }
}