package com.github.nayasis.kotlin.javafx.control.combobox

import javafx.scene.control.ComboBox
import javafx.scene.input.KeyCode.*
import javafx.scene.input.KeyEvent
import javafx.util.StringConverter
import tornadofx.runLater
import kotlin.math.max
import kotlin.math.min

class ItemComboBox: ComboBox<Item>() {

    var value: String?
        get() = super.getValue()?.value
        set(value) = super.setValue(getItem(value))

    var select: Int
        get() = selectionModel.selectedIndex
        set(value) = selectionModel.select(value)


    fun getItem(value: String?): Item? {
        return items.firstOrNull{ it.value == value }
    }

    fun setItem(value: String, label: String = value): ItemComboBox {
        return setItem(Item(value,label)) {
            it.label = label
        }
    }

    fun setItem(value: String, label: String = value, ref: Any?): ItemComboBox {
        return setItem(Item(value,label,ref)) {
            it.label = label
            it.ref   = ref
        }
    }

    fun setItem(item: Item): ItemComboBox {
        return setItem(item) {
            it.label = item.label
            it.ref   = item.ref
        }
    }

    fun setItem(item: Item, fn:(Item)->Unit): ItemComboBox {
        items.firstOrNull{ it.value == value }.let {
            if( it == null ) {
                items.add(item)
            } else {
                fn(it)
            }
        }
        return this
    }

    fun setItem(items: Collection<Item>): ItemComboBox {
        val checker = this.items.associateBy { it.value }
        for( item in items ) {
            if( checker.containsKey(item.value) ) {
                checker[item.value]!!.label = item.label
            } else {
                this.items.add(item)
            }
        }
        return this
    }

    init {

        converter = object: StringConverter<Item>() {
            override fun toString(item: Item?): String? = item?.label
            override fun fromString(value: String): Item? = getItem(value)
        }

        addEventFilter(KeyEvent.KEY_PRESSED) { event ->
                when (event.code) {
                    UP, KP_UP -> {
                        event.consume()
                        show()
                        if (isEditable) runLater{
                            select = max(select + 1, items.size - 1)
                        }
                    }
                    DOWN, KP_DOWN -> {
                        event.consume()
                        show()
                        if (isEditable) runLater{
                            select = min(select - 1, 0)
                        }
                    }
                    ESCAPE -> {
                        parent.fireEvent(event)
                        event.consume()
                    }
                    ENTER -> if (isEditable) {
                        getItem(editor.text)?.also { event.consume() }
                    }
                }
            }

    }

}