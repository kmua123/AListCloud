package com.kmuaz.alistcloud.ui.util

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.webkit.MimeTypeMap

object DownloadHelper {

    fun download(

        context: Context,

        url: String,

        fileName: String

    ) {

        val request = DownloadManager.Request(

            Uri.parse(url)

        )

        request.setTitle(fileName)

        request.setDescription("正在下载")

        val extension = fileName
            .substringAfterLast('.', "")
            .lowercase()
        MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(extension)
            ?.let(request::setMimeType)

        request.setNotificationVisibility(

            DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED

        )

        request.setVisibleInDownloadsUi(true)

        request.setDestinationInExternalPublicDir(

            Environment.DIRECTORY_DOWNLOADS,

            fileName

        )

        val manager =

            context.getSystemService(

                DownloadManager::class.java

            )

        manager.enqueue(request)

    }

}
