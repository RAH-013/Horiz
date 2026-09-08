package com.horiz.data.model

data class Location(
    val id: Long,
    var name: String
) {
    init {
        require(name.isNotBlank())
    }
}