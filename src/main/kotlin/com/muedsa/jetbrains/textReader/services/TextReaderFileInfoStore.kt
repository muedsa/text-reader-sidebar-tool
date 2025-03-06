package com.muedsa.jetbrains.textReader.services

import com.intellij.openapi.components.*
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.util.io.isFile
import com.muedsa.jetbrains.textReader.TextReaderSidebarToolIcons
import com.muedsa.jetbrains.textReader.model.TextFileInfo
import com.muedsa.jetbrains.textReader.notification.TextReaderNotifications
import com.muedsa.jetbrains.textReader.util.TextReaderUtil
import kotlin.io.path.Path

@Service(Service.Level.APP)
@State(name = "TextReaderFileInfo", storages = [Storage(StoragePathMacros.NON_ROAMABLE_FILE)])
class TextReaderFileInfoStore : PersistentStateComponent<TextFileInfo> {

    private var state: TextFileInfo? = null

    override fun getState() = state

    override fun loadState(state: TextFileInfo) {
        this.state = state
    }

    override fun initializeComponent() {
        state?.path?.let {
            if (it.isNotEmpty()) {
                val path = Path(it)
                if (path.isFile() && path.toFile().canRead()) {
                    val hash = TextReaderUtil.calculateFileHash(path)
                    if (hash != state?.hash) {
                        this.state = null
                        TextReaderNotifications.error(
                            title = "文件变动",
                            content = "检测到文件变动，请重新选择文件",
                            icon = TextReaderSidebarToolIcons.BOOK_SKULL,
                        )
                        thisLogger().warn("`$path` file change, $hash vs ${state?.hash}")
                    }
                } else {
                    this.state = null
                    TextReaderNotifications.error(
                        title = "文件变动",
                        content = "检测到文件变动，请重新选择文件",
                        icon = TextReaderSidebarToolIcons.BOOK_SKULL,
                    )
                    thisLogger().warn("`$path` is not readable file")
                }
            }
        }
    }

    fun clear() {
        this.state = null
    }

    companion object {
        @JvmStatic
        fun getInstance() = service<TextReaderFileInfoStore>()
    }
}