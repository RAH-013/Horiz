package com.horiz.data.model

data class Subject(
    val id: Long,
    var name: String,
    var color: Long
) {
    init {
        require(name.isNotBlank())
    }
}