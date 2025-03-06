package com.muedsa.jetbrains.textReader.setting

import com.intellij.openapi.options.Configurable
import com.muedsa.jetbrains.textReader.bus.TextReaderEvents
import org.jetbrains.annotations.Nls
import javax.swing.JComponent

class TextReaderSettingsConfigurable : Configurable {

    private lateinit var component: TextReaderSettingsComponent

    @Nls(capitalization = Nls.Capitalization.Title)
    override fun getDisplayName() = "TextReaderSidebarToolSettings"

    override fun createComponent(): JComponent {
        val model = TextReaderSettingsComponent.Model.from(TextReaderSettings.getInstance().state)
        component = TextReaderSettingsComponent(model = model)
        return component.getPanel()
    }

    override fun isModified(): Boolean = component.isModify()

    override fun apply() {
        component.apply()
        TextReaderEvents.syncPublisher().onEvent(TextReaderEvents.ChangeSettingsEvent)
    }

    override fun reset() {
        component.reset()
    }

    override fun disposeUIResources() {
        component.dispose()
    }
}