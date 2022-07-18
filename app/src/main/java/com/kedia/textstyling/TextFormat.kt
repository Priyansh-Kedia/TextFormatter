package com.kedia.textstyling

/**
 * This class is used to specify how the text should be formatted.
 * This is used inside the extension function for EditText [textFormatter].
 */

data class TextFormat(
    val character: Char,
    val textStyle: TextStyle
)

enum class TextStyle {
    BOLD,
    ITALICS,
    UNDERLINE,
    STRIKETHROUGH
}
