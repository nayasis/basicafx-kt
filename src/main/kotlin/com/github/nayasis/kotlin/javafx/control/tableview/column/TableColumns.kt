package com.github.nayasis.kotlin.javafx.control.tableview.column

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ObservableValue
import javafx.geometry.Pos
import javafx.scene.control.TableColumn
import javafx.scene.control.TableColumnBase
import javafx.util.Callback
import tornadofx.observable
import kotlin.collections.set
import kotlin.reflect.KProperty1

inline fun <reified S,T> TableColumn<S,T>.cellValue(prop: KProperty1<S,T?>): TableColumn<S,T> {
    this.cellValueFactory = Callback { observable(it.value, prop) }
    return this
}

@JvmName("cellProperty")
inline fun <reified S,T> TableColumn<S,T>.cellValue(prop: KProperty1<S,ObservableValue<T>>): TableColumn<S,T> {
    this.cellValueFactory = Callback { prop.call(it.value) }
    return this
}

inline fun <reified S,T> TableColumn<S,T>.cellValue(callback: Callback<TableColumn.CellDataFeatures<S,T>, ObservableValue<T>>): TableColumn<S,T> {
    this.cellValueFactory = callback
    return this
}

@Suppress("UNCHECKED_CAST")
inline fun <reified S,T> TableColumn<S,T>.cellValueByDefault(): TableColumn<S,T> {
    this.cellValueFactory = Callback { SimpleObjectProperty(it.value as T) }
    return this
}

@Suppress("UNCHECKED_CAST")
fun <S,T:Any> TableColumn<S,T>.findBy(fxId: String): TableColumn<S,T>? {
    if( id == fxId ) return this
    for( col in columns )
        return (col.findBy(fxId) ?: continue) as TableColumn<S,T>
    return null
}

@Suppress("UNCHECKED_CAST")
fun <S,T:Any> TableColumn<S,T>.children(recursive: Boolean = false): List<TableColumn<S,T>> {
    val list = ArrayList<TableColumn<S,T>>()
    (this.columns as Collection<TableColumn<S,T>>?)?.forEach {
        list.add(it)
        if(recursive)
            list.addAll(it.children(recursive))
    }
    return list
}

/**
 * it could cause flickering when observable item changed.
 * use cellFormat instead.
 *
 * ```
 *  col.cellFormat { amount ->
 *    text = "$ $amount"
 *    alignment = CENTER_RIGHT
 *  }
 * ```
 * @receiver TableColumn<S,T>
 * @param align Pos
 * @return TableColumn<S,T>
 */
@Deprecated("use #cellFormat instead")
fun <S,T> TableColumn<S,T>.setAlign(align: Pos): TableColumn<S,T> {
    this.setStyle("-fx-alignment", align.name)
    return this
}

val <S,T> TableColumnBase<S,T>.allStyles: Map<String,String>
    get() = this.style.split(";").mapNotNull {
        val words = it.split(":")
        when {
            words.size >= 2 -> if(words[1].isEmpty()) null else words[0] to words[1]
            else -> null
        }
    }.toMap()


fun <S,T> TableColumnBase<S,T>.setStyle(style: Map<String,String?>) =
    style.filter { ! it.value.isNullOrEmpty() }.map{ "${it.key}:${it.value};" }.joinToString("").let { this.style = it }

fun <S,T> TableColumnBase<S,T>.getStyle(key: String): String? = this.allStyles[key]

fun <S,T> TableColumnBase<S,T>.setStyle(key: String,value: String?) {
    this.allStyles.toMutableMap().let {
        if(value.isNullOrEmpty()) {
            it.remove(key)
        } else {
            it[key] = value
        }
        setStyle(it)
    }
}

fun <S,T> TableColumnBase<S,T>.removeStyle(key: String) {
    setStyle(key,null)
}