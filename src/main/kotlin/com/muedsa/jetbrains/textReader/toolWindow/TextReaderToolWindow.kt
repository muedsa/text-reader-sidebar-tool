package com.muedsa.jetbrains.textReader.toolWindow

import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBFont
import com.intellij.util.ui.JBUI
import com.muedsa.jetbrains.textReader.model.SimpleChapterInfo
import com.muedsa.jetbrains.textReader.services.TextReaderService
import com.muedsa.jetbrains.textReader.setting.TextReaderSettings
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.StyleConstants
import javax.swing.text.StyledDocument

class TextReaderToolWindow(
    private val toolWindow: ToolWindow,
) {

    private val readerService = TextReaderService.getInstance()

    val panel = JBTabbedPane().also {
        it.font = JBFont.regular()
        it.background = JBUI.CurrentTheme.ComplexPopup.HEADER_BACKGROUND
        it.isFocusable = false
    }

    private var chapterListData: Array<SimpleChapterInfo> = emptyArray()

    private val chapterSearchTextField: JBTextField = JBTextField()
        .apply {
            addActionListener {
                if (it.source is JBTextField) {
                    val search = (it.source as JBTextField).text
                    if (search.isNotEmpty()) {
                        val listData = chapterListData.filter {
                            val title = it.title
                            title != null && title.contains(search)
                        }.toTypedArray()
                        chapterList.setListData(listData)
                    } else {
                        chapterList.setListData(chapterListData)
                    }
                }
            }
        }

    private val chapterListControlRow = JPanel().also {
        it.layout = BoxLayout(it, BoxLayout.X_AXIS)
        val fileButton = JButton("File").apply {
            addActionListener {
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
        }
        val clearButton = JButton("Clear").apply {
            addActionListener {
                readerService.clear()
            }
        }
        it.add(fileButton)
        it.add(clearButton)
        it.add(Box.createHorizontalGlue())
        it.add(chapterSearchTextField)
    }

    private val chapterList = JBList<SimpleChapterInfo>().apply {
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

    private val titlePanel = JPanel(BorderLayout()).also {
        it.add(chapterListControlRow, BorderLayout.NORTH)
        it.add(JScrollPane(chapterList), BorderLayout.CENTER)
    }

    private val chapterContentControlRow = JPanel().also {
        it.layout = BoxLayout(it, BoxLayout.X_AXIS)
        val previousButton = JButton("Previous").apply {
            maximumSize = Dimension(Int.MAX_VALUE, preferredSize.height)
            alignmentX = Component.CENTER_ALIGNMENT
            addActionListener {
                readerService.previousChapter()
            }
        }
        val nextButton = JButton("Previous").apply {
            maximumSize = Dimension(Int.MAX_VALUE, preferredSize.height)
            alignmentX = Component.CENTER_ALIGNMENT
            addActionListener {
                readerService.nextChapter()
            }
        }
        it.add(previousButton)
        it.add(Box.createHorizontalGlue())
        it.add(nextButton)
    }

    private val chapterContentText = JTextPane()

    private val contentPanel = JPanel(BorderLayout()).also {
        it.add(chapterContentControlRow, BorderLayout.NORTH)
        it.add(JScrollPane(chapterContentText), BorderLayout.CENTER)
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
        chapterList.setListData(chapterListData)
    }

    fun updateChapter(text: String, settings: TextReaderSettings.State) {
        if (chapterSearchTextField.text.isNotEmpty() && chapterList.itemsCount < chapterListData.size) {
            chapterSearchTextField.text = ""
            chapterList.setListData(chapterListData)
        }
        chapterContentText.text = text
        chapterContentText.setCaretPosition(0)
        val attributes = SimpleAttributeSet()
        StyleConstants.setFontFamily(attributes, settings.fontFamily)
        StyleConstants.setFontSize(attributes, settings.fontSize)
        StyleConstants.setLineSpacing(attributes, settings.lineSpace.toFloat())
        StyleConstants.setFirstLineIndent(attributes, settings.firstLineIndent.toFloat() * settings.fontSize.toFloat())
        val styledDocument: StyledDocument = chapterContentText.styledDocument
        styledDocument.setParagraphAttributes(0, styledDocument.length, attributes, false)
    }
}