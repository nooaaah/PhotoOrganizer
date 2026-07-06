package com.noah.photoorganizer.ui

import android.content.Context
import android.content.Intent
import android.net.Uri

fun tryOpenPhotoExternally(context: Context, uri: Uri): Boolean {
    val isVideo = uri.toString().contains("/video/")
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, if (isVideo) "video/*" else "image/*")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    return if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
        true
    } else {
        false
    }
}