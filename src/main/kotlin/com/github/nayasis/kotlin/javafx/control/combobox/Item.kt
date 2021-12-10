package com.github.nayasis.kotlin.javafx.control.combobox

import au.com.console.kassava.kotlinEquals
import au.com.console.kassava.kotlinHashCode

data class Item(
    val value: String?,
    var label: String?,
    var ref: Any? = null
) {

    constructor(value: String): this(value,value)

    override fun equals(other: Any?): Boolean = kotlinEquals(other, arrayOf(Item::value))
    override fun hashCode(): Int = kotlinHashCode(arrayOf(Item::value))

}