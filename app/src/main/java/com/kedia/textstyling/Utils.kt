package com.kedia.textstyling

import android.graphics.Typeface
import android.text.style.*
import android.util.Log

fun logE(message: String, tag: String = "TextStyling") {
    Log.e(tag, message)
}

fun getStyleSpan(textStyle: TextStyle?): CharacterStyle? {
    return when(textStyle) {
        TextStyle.BOLD -> StyleSpan(Typeface.BOLD)
        TextStyle.ITALICS -> StyleSpan(Typeface.ITALIC)
        TextStyle.UNDERLINE -> UnderlineSpan()
        TextStyle.STRIKETHROUGH -> StrikethroughSpan()
        null -> null
    }
}

fun Triple<Int, Int, Any>.isComplete(): Boolean {
    return this.first != -1 && this.second != -1
}

infix fun Int.`in`(triple: Triple<Int, Int, Any>): Boolean {
    return triple.first <= this && triple.second >= this
}

fun findNearestIncompleteTriple(currentIndex: Int, characterMapping: MutableList<CharacterPositionMap>): CharacterPositionMap? {
    val nearestMappings = characterMapping.filter { it.second >= currentIndex && it.first < currentIndex }.sortedBy { it.first }
    return if (nearestMappings.isEmpty()) null else nearestMappings.first()
}

fun <T : Comparable<T>> sortedMutableListOf(vararg elements: T): SortedMutableList<T> =
    SortedArrayList(*elements, comp = compareBy { it })

fun <T> sortedMutableListOf(comparator: Comparator<T>, vararg elements: T): SortedMutableList<T> =
    SortedArrayList(elements = elements, comp = comparator)

fun <T> SortedArrayList<T>?.perform(ifNull: () -> Unit, ifNotNull: (SortedArrayList<T>) -> Unit) {
    if (this == null)
        ifNull()
    else ifNotNull(this)
}

inline fun <T,R> T?.performOnList(ifNull: () -> Unit, ifNotNull: (T) -> R) {
    if (this == null)
        ifNull()
    else ifNotNull(this)
}