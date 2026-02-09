package com.example.horiz.model

import java.util.Calendar

data class SubjectNode(
    var i: Int,
    var n: String,
    var t: String,
    var p: String,
    var s: Int,
    var f: Int,
    var c: Long,
    var d: Int
) {
    fun isNow(currentDayIndex: Int = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 5) % 7): Boolean {
        if (this.d != currentDayIndex) return false
        val current = Calendar.getInstance().let { it.get(Calendar.HOUR_OF_DAY) * 60 + it.get(Calendar.MINUTE) }
        return current in s until f
    }
}
