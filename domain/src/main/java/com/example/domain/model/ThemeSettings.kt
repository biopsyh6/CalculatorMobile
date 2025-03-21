package com.example.domain.model

data class ThemeSettings(
    val backgroundColor: String = "#FFFFFF",
    val textColor: String = "#000000",
//    val isDarkMode: Boolean = false
) {
    constructor() : this("#FFFFFF", "#000000")
}