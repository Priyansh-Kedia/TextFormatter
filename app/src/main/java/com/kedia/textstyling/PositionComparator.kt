package com.kedia.textstyling

class PositionComparator {
    companion object : Comparator<Pair<Int, Boolean>> {
        override fun compare(a: Pair<Int, Boolean>, b: Pair<Int, Boolean>): Int = when {
            a.first == b.first -> 0
            a.first > b.first -> 1
            else -> -1
        }
    }
}