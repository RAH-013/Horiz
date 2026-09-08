package com.horiz.data.model

class DayNode(
    val index: Int,
    enabled: Boolean = true,
    val entries: MutableList<ScheduleEntry> = mutableListOf()
) {
    var enabled: Boolean = enabled
        private set

    companion object {
        const val MAX_ENTRIES = 10
    }

    fun toggleStatus() {
        enabled = !enabled
    }

    fun setEnabled(value: Boolean) {
        enabled = value
    }

    fun addEntry(entry: ScheduleEntry): Boolean {
        if (entries.size >= MAX_ENTRIES) {
            return false
        }

        if (
            hasConflict(
                entry.startMinute,
                entry.endMinute
            )
        ) {
            return false
        }

        entries.add(
            entry.copy(dayIndex = index)
        )

        sortEntries()
        return true
    }

    fun replaceEntry(entry: ScheduleEntry): Boolean {
        val position = entries.indexOfFirst {
            it.id == entry.id
        }

        if (position == -1) {
            return false
        }

        if (
            hasConflict(
                startMinute = entry.startMinute,
                endMinute = entry.endMinute,
                ignoredEntryId = entry.id
            )
        ) {
            return false
        }

        entries[position] = entry.copy(
            dayIndex = index
        )

        sortEntries()
        return true
    }

    fun removeEntry(entryId: Long): Boolean {
        val removed = entries.removeIf {
            it.id == entryId
        }

        if (removed) {
            sortEntries()
        }

        return removed
    }

    fun clearEntries() {
        entries.clear()
    }

    fun hasConflict(
        startMinute: Int,
        endMinute: Int,
        ignoredEntryId: Long? = null
    ): Boolean {
        return entries.any { entry ->
            entry.id != ignoredEntryId &&
                    startMinute < entry.endMinute &&
                    endMinute > entry.startMinute
        }
    }

    private fun sortEntries() {
        entries.sortBy {
            it.startMinute
        }
    }
}