package com.kedia.textstyling

data class BinarySearchTreeNode(
    var data: Int,
    var left: BinarySearchTreeNode? = null,
    var right: BinarySearchTreeNode? = null
) {

    fun insert(value: Int) {
        if (value <= data && left != null) {
            left?.insert(value)
        } else if (value <= data) {
            left = BinarySearchTreeNode(value)
        } else if (value > data && right != null) {
            right?.insert(value)
        } else {
            right = BinarySearchTreeNode(value)
        }
    }

    fun contains(value: Int): Boolean? {
        if (value < data && left != null) {
            return left?.contains(value)
        }
        if (value > data && right != null) {
            return right?.contains(value)
        }
        return value == data
    }

    fun search(value: Int): BinarySearchTreeNode? {
        if (value < data && left != null) {
            return left?.search(value)
        }
        if (value > data && right != null) {
            return right?.search(value)
        }
        return if (value == data) {
            this
        } else {
            null
        }
    }

    fun remove(value: Int): Boolean? {
        return remove(value, null)
    }

    fun remove(value: Int, parent: BinarySearchTreeNode?): Boolean? {
        if (value < data && left != null) {
            return left?.remove(value, this)
        } else if (value < data) {
            return false
        } else if (value > data && right != null) {
            return right?.remove(value, this)
        } else if (value > data) {
            return false
        } else {
            if (left == null && right == null && this == parent?.left) {
                parent.left = null
            } else if (left == null && right == null && this == parent?.right) {
                parent.right = null
            } else if (left != null && right == null && this == parent?.left) {
                parent.left = this.left
            } else if (left != null && right == null && this == parent?.right) {
                parent.right = this.left
            } else if (right != null && left == null && this == parent?.left) {
                parent.left = this.right
            } else if (right != null && left == null && this == parent?.right) {
                parent.right = this.right
            } else {
                data = right?.min() ?: -1
                right?.remove(data, this)
            }
            return true
        }
    }

    fun min(): Int? = if (left == null) data else left?.min()
}