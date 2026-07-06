package com.noah.photoorganizer.ui

import java.text.SimpleDateFormat
import java.util.*

data class DateSection<T>(val label: String, val items: List<T>)

fun <T> groupByDate(items: List<T>, timestampMillis: (T) -> Long): List<DateSection<T>> {
    val now = Calendar.getInstance()
    val today = dateKey(now)
    val yesterday = dateKey(Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) })

    val groups = LinkedHashMap<String, MutableList<T>>()
    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.FRENCH)
    val sorted = items.sortedByDescending { timestampMillis(it) }

    for (item in sorted) {
        val cal = Calendar.getInstance().apply { timeInMillis = timestampMillis(item) }
        val key = dateKey(cal)
        val label = when {
            key == today -> "Aujourd'hui"
            key == yesterday -> "Hier"
            isSameWeek(cal, Calendar.getInstance()) -> "Cette semaine"
            else -> monthFormat.format(cal.time).replaceFirstChar { it.uppercase() }
        }
        groups.getOrPut(label) { mutableListOf() }.add(item)
    }
    return groups.map { (label, list) -> DateSection(label, list) }
}

private fun dateKey(cal: Calendar) = "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.DAY_OF_YEAR)}"
private fun isSameWeek(a: Calendar, b: Calendar) =
    a.get(Calendar.YEAR) == b.get(Calendar.YEAR) && a.get(Calendar.WEEK_OF_YEAR) == b.get(Calendar.WEEK_OF_YEAR)