package com.horiz.data.model

data class ScheduleEntry(
    val id: Long,
    val subjectId: Long?,
    val teacherId: Long?,
    val locationId: Long?,
    var startMinute: Int,
    var endMinute: Int,
    val dayIndex: Int,
    var color: Long,
    val type: SubjectType = SubjectType.CLASS,
    val name: String? = null
) {
    init {
        require(startMinute in 0..1439)
        require(endMinute in 1..1440)
        require(startMinute < endMinute)
        require(dayIndex in 0..6)

        if (type == SubjectType.BREAK) {
            require(subjectId == null)
            require(teacherId == null)
            require(locationId == null)
            require(!name.isNullOrBlank())
        }

        if (type == SubjectType.CLASS) {
            require(subjectId != null)
        }
    }

    fun isNow(
        currentDayIndex: Int,
        currentMinute: Int
    ): Boolean {
        return dayIndex == currentDayIndex &&
                currentMinute in startMinute until endMinute
    }
}