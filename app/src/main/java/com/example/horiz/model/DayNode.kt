package com.example.horiz.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList

class DayNode(
    val i: Int,
    a: Boolean = true,
    val e: SnapshotStateList<SubjectNode> = mutableStateListOf()
) {
    var a by mutableStateOf(a)

    fun toggleStatusDay() {
        a = !a
    }

    fun addElement(node: SubjectNode): Boolean {
        if (e.size >= 10) return false
        if (hasConflict(node.s, node.f)) return false
        e.add(node)
        sortAndReindex()
        return true
    }

    fun replaceElement(node: SubjectNode): Boolean {
        val idx = e.indexOfFirst { it.i == node.i }
        if (idx < 0) return false
        if (hasConflict(node.s, node.f, node.i)) return false

        e.removeAt(idx)
        e.add(idx, node)
        sortAndReindex()
        return true
    }


    fun removeElement(node: SubjectNode) {
        e.removeAll { it.i == node.i }
        sortAndReindex()
    }

    fun clearElements() {
        e.clear()
    }

    fun hasConflict(s: Int, f: Int, ignoreId: Int? = null): Boolean {
        return e.any {
            if (ignoreId != null && it.i == ignoreId) return@any false
            s < it.f && f > it.s  // No bloquea si s == it.f
        }
    }

    private fun sortAndReindex() {
        e.sortBy { it.s }
        e.forEachIndexed { idx, sub -> sub.i = idx + 1 }
    }
}
