package com.github.nayasis.kotlin.javafx.property

import com.github.nayasis.kotlin.basica.core.extention.ifNotNull
import com.github.nayasis.kotlin.javafx.control.tableview.Position
import com.github.nayasis.kotlin.javafx.control.tableview.fillFxId
import com.github.nayasis.kotlin.javafx.control.tableview.focused
import com.github.nayasis.kotlin.javafx.control.tableview.scroll
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import tornadofx.runLater
import java.io.Serializable

private var tableIndex = 0

data class TableProperty(
    val columns: LinkedHashMap<String,TableColumnProperty> = LinkedHashMap(),
    var columnSortOrder: TableColumnSortOrderProperty? = null,
    var visible: Boolean? = null,
    var focused: Position? = null,
): Serializable{

    constructor(tableview: TableView<*>): this() {
        read(tableview)
    }

    fun read(tableview: TableView<*>) {
        setId(tableview)
        visible = tableview.isVisible
        focused = tableview.focused
        columnSortOrder = TableColumnSortOrderProperty(tableview)
        tableview.columns.forEach {
            columns[it.id] = TableColumnProperty(it)
        }
    }

    fun bind(tableview: TableView<Any>) {
        setId(tableview)
        visible?.let { tableview.isVisible = it }
        reorderColumns(tableview)
        columnSortOrder?.bind(tableview)
        focused?.let {
            runLater {
                tableview.focused = it
                tableview.scroll(it.row)
            }
        }
    }

    private fun setId(tableview: TableView<*>) {
        if (tableview.id.isNullOrEmpty())
            tableview.id = "table${tableIndex++}"
        tableview.fillFxId()
    }

    private fun reorderColumns(tableview: TableView<Any>) {

        val sorted = arrayListOf<TableColumn<Any, *>>()
        val remain = linkedMapOf<String, TableColumn<Any, *>>()
            tableview.columns.forEach { remain[it.id] = it }

        columns.forEach { (fxid,property) ->
            remain.remove(fxid).ifNotNull {
                sorted.add(it)
                property.bind(it)
            }
        }

        sorted.addAll(remain.values)

        tableview.columns.clear()
        tableview.columns.addAll(sorted)

    }

}