package com.kedia.textstyling

import android.text.Spannable
import android.text.style.CharacterStyle
import android.widget.EditText
import androidx.core.text.getSpans

typealias CharacterPositionMap = Triple<Int, Int, Boolean>

/**
 * [textFormatter] can be used as an extension function, for EditText
 * You can specify characters to a particular text format that needs to
 * be added. For example, if you wish to specify that the text between
 * two asterisks (*) should be bold, and text between underscores (_) should be
 * italicized, then you need to pass a list of TextFormat [TextFormat].
 * The function will automatically take care of
 */

fun EditText.textFormatter(textFormats: List<TextFormat>) {

    val emptyTriple = CharacterPositionMap(-1, -1, false)

    if (textFormats.isEmpty()) {
        logE("You should pass at least one text format")
        return
    }

    val characterFormatMap = textFormats.associate {
        it.character to it.textStyle
    }

    val addedSpanList: MutableMap<Char, MutableList<CharacterPositionMap>> = mutableMapOf()
    val ongoingSpanList: MutableMap<Char, CharacterPositionMap> = mutableMapOf()

    fun updateOngoingSpan(triple: CharacterPositionMap, char: Char, index: Int) {
        var onGoingTriple = ongoingSpanList.get(char) ?: emptyTriple
        if (onGoingTriple.first == -1)
            onGoingTriple = CharacterPositionMap(triple.second, -1, false)
        else if (onGoingTriple.second == -1)
            onGoingTriple = CharacterPositionMap(onGoingTriple.first, triple.second, false)
        ongoingSpanList[char] = onGoingTriple
    }

    fun removeCurrentSpan(index: Int, lastIndex: Int) {
        val addedSpan = this.text.getSpans<CharacterStyle>(index + 1, lastIndex)
        if (addedSpan.isNotEmpty()) {
            this@textFormatter.text.removeSpan(addedSpan.first())
        }
    }

    fun addToSpanList(triple: CharacterPositionMap, char: Char) {
        if (addedSpanList.containsKey(char).not())
            addedSpanList[char] = mutableListOf()

        addedSpanList[char]?.add(triple)
    }

    fun getNextCharacterInfo(char: Char, index: Int): CharacterPositionMap? {
        val nextSpans = addedSpanList[char]?.sortedBy { it.first }?.filter { it.first > index }?.toMutableList() ?: mutableListOf()
        var nextSpan = emptyTriple
//        if (nextSpans.isEmpty()) {
//            if (ongoingSpanList.containsKey(char)) {
//                nextSpans.add(ongoingSpanList.get(char) ?: emptyTriple)
//            }
//        }
        if (ongoingSpanList.containsKey(char)) {
            nextSpan = ongoingSpanList.get(char) ?: emptyTriple
        }
        logE("in this $nextSpan $nextSpans $ongoingSpanList")
        if (nextSpans.isNotEmpty()) {
            nextSpan = if (nextSpans.first().first < nextSpan.first) nextSpans.first() else nextSpan
        }

        return nextSpan
    }

    fun getPreviousCharacterInfo(char: Char, index: Int): CharacterPositionMap? {
        val prevSpans = addedSpanList[char]?.sortedBy { it.first }?.filter { it.second < index }?.toMutableList() ?: mutableListOf()
        if (prevSpans.isEmpty()) {
            val prevIndex = this.text.toString().indexOf(char)
            prevSpans.add(CharacterPositionMap(-1, prevIndex, false))
        }
        if (prevSpans.isEmpty()) {
            if (ongoingSpanList.containsKey(char)) {
                prevSpans.add(ongoingSpanList.get(char) ?: emptyTriple)
            }
        }
        return if (prevSpans.isEmpty()) null else prevSpans.last()
    }

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
                    if (addedAt == POSITION.BETWEEN) {
                        triple = findNearestIncompleteTriple(index, addedSpanList.get(it) ?: mutableListOf()) ?: emptyTriple
                        // Update ongoing span
                        // Remove the current span
                        updateOngoingSpan(triple, it, index)
                        removeCurrentSpan(index, triple.second)
                        addedSpanList.get(it)?.remove(triple)
                        triple = CharacterPositionMap(triple.first, index, false)
                        addToSpanList(triple, it)
                    } else {
                        if ((ongoingSpanList[it] ?: emptyTriple) == emptyTriple) {
                            if (addedSpanList.containsKey(it)) {
                                triple = addedSpanList[it]?.last() ?: emptyTriple
                            }
                        } else {
                            triple = ongoingSpanList[it] ?: emptyTriple
                        }
                    }

                    if (triple.isComplete()) {
                        if (!(index `in` triple)) {
                            triple = emptyTriple
                        }
                    }

                    if (triple.first == -1) {
                        triple = CharacterPositionMap(index, -1, false)
                    }
                    else if (triple.second == -1) {
                        addedSpanList?.get(it)?.remove(triple) // change it so it ignore boolean
                        if (addedSpanList.get(it)?.isEmpty() == true)
                            addedSpanList.remove(it)
                        triple = CharacterPositionMap(triple.first, index, false)
                        addToSpanList(triple, it)
                        triple = emptyTriple
                    }

                    if (triple.isComplete().not()) {
                        ongoingSpanList[it] = triple
                    }

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
                                        this@textFormatter.text.setSpan(it, addedPair.first + 1, addedPair.second, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                                        addedSpanList.get(character)?.remove(addedPair)
                                        addedSpanList.get(character)?.add(CharacterPositionMap(addedPair.first, addedPair.second, true))
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

                    if (deletedFrom == POSITION.BETWEEN) {
                        val nextInfo = getNextCharacterInfo(it, index)
                        val previousInfo = getPreviousCharacterInfo(it, index)
                        logE("called here $nextInfo $previousInfo ${addedSpanList[it]} ${ongoingSpanList.get(it)}")
                    } else {
                        if (triple == emptyTriple)
                            triple = if (addedSpanList.containsKey(it)) addedSpanList.get(it)?.last() ?: emptyTriple else emptyTriple
                    }

                    addedSpanList.get(it)?.remove(triple)
                    if (addedSpanList.get(it)?.isEmpty() == true)
                        addedSpanList.remove(it)
                    if (triple.second != -1) {
                        triple = CharacterPositionMap(triple.first, -1, false)
                        addToSpanList(triple, it)
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