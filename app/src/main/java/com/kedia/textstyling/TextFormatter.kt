package com.kedia.textstyling

import android.text.Spannable
import android.text.style.CharacterStyle
import android.widget.EditText
import androidx.core.text.getSpans

/**
 * [textFormatter] can be used as an extension function, for EditText
 * You can specify characters to a particular text format that needs to
 * be added. For example, if you wish to specify that the text between
 * two asterisks (*) should be bold, and text between underscores (_) should be
 * italicized, then you need to pass a list of TextFormat [TextFormat].
 * The function will automatically take care of
 */

fun EditText.textFormatter(textFormats: List<TextFormat>) {

    val emptyTriple = Triple(-1, -1, false)

    if (textFormats.isEmpty()) {
        logE("You should pass at least one text format")
        return
    }

    val characterFormatMap = textFormats.associate {
        it.character to it.textStyle
    }

    val addedSpanList: MutableMap<Char, MutableList<Triple<Int, Int, Boolean>>> = mutableMapOf()
    val ongoingSpanList: MutableMap<Char, Triple<Int, Int, Boolean>> = mutableMapOf()

    this.addTextChangedListener(CharacterWatcher(object : CharacterWatcher.OnSequenceChanged {
        override fun characterAdded(
            index: Int,
            addedCharacter: Char?,
            sequence: CharSequence?,
            addedAt: POSITION
        ) {
            addedCharacter?.let {
                if (it in characterFormatMap.keys) {

                    /**
                     * If pair = Pair(-1,-1), then check for the pair in the list
                     * of pairs too
                     */
                    var triple = emptyTriple
                    if ((ongoingSpanList.get(it) ?: emptyTriple) == emptyTriple) {
                        if (addedSpanList.containsKey(it)) {
                            triple = addedSpanList.get(it)?.last() ?: emptyTriple
                        }
                    } else {
                        triple = ongoingSpanList.get(it) ?: emptyTriple
                    }

                    if (triple.isComplete()) {
                        if (!(index `in` triple)) {
                            triple = emptyTriple
                        }
                    }

                    if (triple.first == -1) {
                        triple = Triple(index, -1, false)
                    }
                    else if (triple.second == -1) {
                        addedSpanList?.get(it)?.remove(triple) // change it so it ignore boolean
                        if (addedSpanList.get(it)?.isEmpty() == true)
                            addedSpanList.remove(it)
                        triple = Triple(triple.first, index, false)
                        if (addedSpanList.containsKey(it).not())
                            addedSpanList[it] = mutableListOf()

                        addedSpanList[it]?.add(triple)
                        triple = emptyTriple
                    }
                    ongoingSpanList[it] = triple

                    if (addedSpanList.isNotEmpty()) {
                        for (character in addedSpanList.keys) {
                            val list = addedSpanList.get(character) ?: listOf()
                            val style = characterFormatMap.get(character)
                            val span = getStyleSpan(style)
                            for (addedPair in list) {
                                if (addedPair.first == -1 || addedPair.second == -1)
                                    return
                                span?.let {
                                    if (addedPair.third.not()) {
                                        this@textFormatter.text.setSpan(
                                            it,
                                            addedPair.first + 1,
                                            addedPair.second,
                                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                        )
                                        addedSpanList.get(character)?.remove(addedPair)
                                        addedSpanList.get(character)
                                            ?.add(Triple(addedPair.first, addedPair.second, true))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        override fun characterDeleted(
            index: Int,
            deletedCharacter: Char?,
            sequence: CharSequence?,
            deletedFrom: POSITION
        ) {
            deletedCharacter?.let {
                if (it in characterFormatMap.keys) {
                    var triple = ongoingSpanList.get(it) ?: emptyTriple

                    if (triple == emptyTriple)
                        triple = if (addedSpanList.containsKey(it)) addedSpanList.get(it)?.last() ?: emptyTriple else emptyTriple

                    addedSpanList.get(it)?.remove(triple)
                    if (addedSpanList.get(it)?.isEmpty() == true)
                        addedSpanList.remove(it)
                    if (triple.second != -1) {
                        triple = Triple(triple.first, -1, false)
                        if (addedSpanList.containsKey(it).not())
                            addedSpanList[it] = mutableListOf()
                        addedSpanList?.get(it)?.add(triple)
                    } else {
                        triple = emptyTriple
                    }

                    if (addedSpanList.isNotEmpty()) {
                        for (character in addedSpanList.keys) {
                            val list = addedSpanList.get(character) ?: listOf()
                            val style = characterFormatMap.get(character)
                            val span = getStyleSpan(style)
                            for (addedPair in list) {
                                span?.let {
                                    if (addedPair.second == -1) {
                                        val addedSpan = this@textFormatter.text.getSpans<CharacterStyle>(addedPair.first)
                                        if (addedSpan.isNotEmpty()) {
                                            this@textFormatter.text.removeSpan(addedSpan.first())
                                        }
                                    }
                                }
                            }
                        }
                    }

                }
            }

        }

    }))
}