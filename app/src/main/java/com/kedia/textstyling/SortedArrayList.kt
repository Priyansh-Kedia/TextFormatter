package com.kedia.textstyling

class SortedArrayList<T>(
    vararg elements: T,
    private val comp: Comparator<T>
) : SortedMutableList<T> {

    private val list = ArrayList(elements.toList())

    override val size: Int
        get() = list.size

    override fun add(element: T) = findIndex(element)
        .let { index -> list.add(if (index < 0) -(index + 1) else index, element) }

    override fun remove(element: T) = findIndex(element)
        .let { index -> if (index >= 0) list.removeAt(index) }

    override fun get(index: Int): T = list[index]

    override fun contains(element: T) = findIndex(element).let { index ->
        index >= 0 && element == list[index] || (findEquals(index + 1, element, 1) || findEquals(index - 1, element, -1))
    }

    override fun iterator(): Iterator<T> = list.iterator()

    private fun findIndex(element: T): Int = list.binarySearch(element, comp)

    private tailrec fun findEquals(index: Int, element: T, step: Int): Boolean = when {
        index !in 0 until size -> false
        comp.compare(element, list[index]) != 0 -> false
        list[index] == element -> true
        else -> findEquals(index + step, element, step)
    }
}
