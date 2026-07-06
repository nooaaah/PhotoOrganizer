package com.noah.photoorganizer.data.mediastore

import android.content.Context
import android.net.Uri
import com.noah.photoorganizer.ui.DateSection
import com.noah.photoorganizer.ui.groupByDate

fun groupUrisByDate(context: Context, uris: List<String>): List<DateSection<String>> {
    val helper = MediaStoreHelper(context)
    val withDates = uris.mapNotNull { uriString ->
        val info = helper.getPhotoInfo(Uri.parse(uriString))
        if (info != null) uriString to info.dateAjout * 1000L else null
    }
    return groupByDate(withDates) { it.second }.map { section ->
        DateSection(section.label, section.items.map { it.first })
    }
}