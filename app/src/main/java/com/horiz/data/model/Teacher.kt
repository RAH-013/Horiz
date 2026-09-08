package com.horiz.data.model

data class Teacher(
    val id: Long,
    var name: String
) {
    init {
        require(name.isNotBlank())
    }
}