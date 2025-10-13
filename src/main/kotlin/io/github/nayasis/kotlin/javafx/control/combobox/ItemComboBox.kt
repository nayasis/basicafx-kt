package io.github.nayasis.kotlin.javafx.control.combobox

import io.github.nayasis.kotlin.basica.etc.error
import io.github.nayasis.kotlin.javafx.control.basic.addKeyPressed
import io.github.oshai.kotlinlogging.KotlinLogging
import javafx.scene.control.ComboBox
import javafx.scene.control.ListView
import javafx.scene.control.skin.ComboBoxListViewSkin
import javafx.scene.input.KeyCode.*
import javafx.util.StringConverter
import tornadofx.runLater
import kotlin.math.max
import kotlin.math.min

private val logger = KotlinLogging.logger {}

@Suppress("unused")
class ItemComboBox: ComboBox<ItemCombo> {

    constructor(items: Collection<ItemCombo>? = null) {
        if( ! items.isNullOrEmpty() )
            this.items.addAll(items)
    }

    var value: String?
        get() = super.getValue()?.value
        set(value) = super.setValue(getItem(value))

    @Suppress("UNCHECKED_CAST")
    var select: Int
        get() = selectionModel.selectedIndex
        set(value) {
            selectionModel.select(value)
            try {
                (skin as ComboBoxListViewSkin<ItemCombo>?)?.also {
                    (it.popupContent as ListView<ItemCombo>).scrollTo(value)
                }
            } catch (e: Exception) {
                logger.error(e)
            }
        }

    val selectedItem: ItemCombo?
        get() {
            return try {
                selectionModel.selectedItem
            } catch (_: Exception) {
                null
            }
        }

    fun selectFirst(): ItemCombo? {
        select = 0
        return selectedItem
    }

    fun selectLast(): ItemCombo? {
        select = items.size - 1
        return selectedItem
    }

    fun clear(): ItemComboBox {
        items.clear()
        return this
    }

    fun getItem(value: String?): ItemCombo? {
        return items.firstOrNull{ it.value == value }
    }

    fun addItem(value: String, label: String = value, ref: Any? = null, index: Int? = null, overwriteOnExist: Boolean = true): ItemComboBox {
        return addItem(ItemCombo(value,label,ref),index,overwriteOnExist)
    }

    fun addItem(item: ItemCombo, index: Int? = null, overwriteOnExist: Boolean = true): ItemComboBox {
        items.firstOrNull{ it.value == item.value }.let {
            if( it != null && overwriteOnExist ) {
                it.label = item.label
                it.ref   = item.ref
            } else {
                if( index == null ) {
                    items.add(item)
                } else {
                    items.add(index,item)
                }
            }
        }
        return this
    }

    fun addItems(items: Collection<ItemCombo>, overwriteOnExist: Boolean = true): ItemComboBox {
        if( overwriteOnExist ) {
            val checker = this.items.associateBy { it.value }
            for( item in items ) {
                if( checker.containsKey(item.value) ) {
                    checker[item.value]!!.run {
                        label = item.label
                        ref   = item.ref
                    }
                } else {
                    this.items.add(item)
                }
            }
        } else {
            this.items.addAll(items)
        }
        return this
    }

    fun removeItem(value: String): Boolean {
        return removeItem(ItemCombo(value))
    }

    fun removeItem(item: ItemCombo): Boolean {
        return items.remove(item)
    }

    fun removeItem(items: Collection<ItemCombo>): Boolean {
        return this.items.removeAll(items)
    }

    init {

        converter = object: StringConverter<ItemCombo>() {
            override fun toString(item: ItemCombo?): String? = item?.label
            override fun fromString(value: String): ItemCombo? = getItem(value)
        }

        addKeyPressed { event ->
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
                HOME -> selectFirst()
                END -> selectLast()
                ENTER -> if (isEditable) {
                    getItem(editor.text)?.also { event.consume() }
                }
                else -> {}
            }
        }

    }

}