package com.kedia.textstyling

import android.text.Spannable
import android.text.style.CharacterStyle
import android.widget.EditText
import androidx.core.text.getSpans

class TextFormatter {

}

fun EditText.textFormatter(textFormats: List<TextFormat>) {

    val emptyTriple = Triple(-1, -1, false)

    if (textFormats.isEmpty()) {
        logE("You should pass at least one text format")
        return
    }

    val characterFormatMap = textFormats.associate {
        it.character to it.textStyle
    }

    val charactersTripleList: MutableMap<Char, MutableList<Triple<Int, Int, Boolean>>> = mutableMapOf()
    val positionTripleList: MutableMap<Char, Triple<Int, Int, Boolean>> = mutableMapOf()

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
                    if ((positionTripleList.get(it) ?: emptyTriple) == emptyTriple) {
                        if (charactersTripleList.containsKey(it)) {
                            triple = charactersTripleList.get(it)?.last() ?: emptyTriple
                        }
                    } else {
                        triple = positionTripleList.get(it) ?: emptyTriple
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
                        charactersTripleList?.get(it)?.remove(triple) // change it so it ignore boolean
                        if (charactersTripleList.get(it)?.isEmpty() == true)
                            charactersTripleList.remove(it)
                        triple = Triple(triple.first, index, false)
                        if (charactersTripleList.containsKey(it).not())
                            charactersTripleList[it] = mutableListOf()

                        charactersTripleList[it]?.add(triple)
                        triple = emptyTriple
                    }
                    positionTripleList[it] = triple

                    if (charactersTripleList.isNotEmpty()) {
                        for (character in charactersTripleList.keys) {
                            val list = charactersTripleList.get(character) ?: listOf()
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
                                        charactersTripleList.get(character)?.remove(addedPair)
                                        charactersTripleList.get(character)
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
                    var triple = positionTripleList.get(it) ?: emptyTriple

                    if (triple == emptyTriple)
                        triple = if (charactersTripleList.containsKey(it)) charactersTripleList.get(it)?.last() ?: emptyTriple else emptyTriple

                    charactersTripleList.get(it)?.remove(triple)
                    if (charactersTripleList.get(it)?.isEmpty() == true)
                        charactersTripleList.remove(it)
                    if (triple.second != -1) {
                        triple = Triple(triple.first, -1, false)
                        if (charactersTripleList.containsKey(it).not())
                            charactersTripleList[it] = mutableListOf()
                        charactersTripleList?.get(it)?.add(triple)
                    } else {
                        triple = emptyTriple
                    }

                    if (charactersTripleList.isNotEmpty()) {
                        for (character in charactersTripleList.keys) {
                            val list = charactersTripleList.get(character) ?: listOf()
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