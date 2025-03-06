package com.muedsa.jetbrains.textReader.notification

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import javax.swing.Icon

object TextReaderNotifications {

    private const val NOTIFICATION_GROUP_ID = "TextReaderSidebarToolNotificationGroup"
    private const val CHAPTER_CONTENT_GROUP_ID = "TextReaderSidebarToolChapterContentGroup"

    fun info(content: String, icon: Icon? = null, project: Project? = null) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup(NOTIFICATION_GROUP_ID)
            ?.createNotification(content, NotificationType.INFORMATION)
            ?.setIcon(icon)
            ?.notify(project)
    }

    fun info(title: String, content: String, icon: Icon? = null, project: Project? = null) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup(NOTIFICATION_GROUP_ID)
            ?.createNotification(title, content, NotificationType.INFORMATION)
            ?.setIcon(icon)
            ?.notify(project)
    }

    fun error(content: String, icon: Icon? = null, project: Project? = null) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup(NOTIFICATION_GROUP_ID)
            ?.createNotification(content, NotificationType.ERROR)
            ?.setIcon(icon)
            ?.notify(project)
    }

    fun error(title: String, content: String, icon: Icon? = null, project: Project? = null) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup(NOTIFICATION_GROUP_ID)
            ?.createNotification(title, content, NotificationType.ERROR)
            ?.setIcon(icon)
            ?.notify(project)
    }

    fun chapterContent(content: String, project: Project? = null) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup(CHAPTER_CONTENT_GROUP_ID)
            ?.createNotification(content, NotificationType.INFORMATION)
            ?.notify(project)
    }
}