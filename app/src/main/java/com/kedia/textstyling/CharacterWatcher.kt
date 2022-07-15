package com.kedia.textstyling

import android.text.Editable
import android.text.TextWatcher
import android.util.Log

class CharacterWatcher constructor(private val listener: OnSequenceChanged): TextWatcher {

    private var lastLength: Int = 0
    private var previousLength = 0
    private var backspaceIndex = -1
    private var backspaceCharacter: Char? = Char.MIN_VALUE

    private var lastTime = System.currentTimeMillis()

    private val debounceTime = 200

    override fun beforeTextChanged(sequence: CharSequence?, start: Int, count: Int, after: Int) {
        val index = if (start == 0) after else start
        previousLength = sequence?.length ?: 0
        backspaceIndex = index
        if (index < (sequence?.length ?: 0)) {
            backspaceCharacter = sequence?.get(index)
        }
    }

    override fun onTextChanged(sequence: CharSequence?, start: Int, before: Int, count: Int) {
        if (lastLength <= (sequence?.length ?: 0)) {
            /**
             * Strange behavior in Android, method called twice (or more than that) randomly
             */
            if (System.currentTimeMillis() - lastTime < debounceTime)
                return
            lastTime = System.currentTimeMillis()

            /**
             * When character is added
             * The 'index' variable specifies the index of newly added character
             */
            var index = start + before // if (before == 0) start else if (before < count) before else before// - 1

            if (index == sequence?.length)
                index -= 1

            listener.characterAdded(index, sequence?.get(index), sequence)
        }
        if (previousLength > (sequence?.length ?: 0)) {
            /**
             * When character is deleted
             * The variable [backspaceCharacter] specifies the character just deleted
             * The variable [backspaceIndex] specified index of deleted character
             */
            listener.characterDeleted(backspaceIndex, backspaceCharacter, sequence)
        }
    }

    override fun afterTextChanged(sequence: Editable?) {
        lastLength = sequence?.length ?: 0
    }

    interface OnSequenceChanged {
        fun characterAdded(index: Int, addedCharacter: Char?, sequence: CharSequence?)
        fun characterDeleted(index: Int, deletedCharacter: Char?, sequence: CharSequence?)
    }
}