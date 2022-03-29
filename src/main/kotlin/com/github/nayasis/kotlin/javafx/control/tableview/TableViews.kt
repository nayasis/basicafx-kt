package com.github.nayasis.kotlin.javafx.control.tableview

import com.github.nayasis.kotlin.basica.exception.implements.NotFound
import com.github.nayasis.kotlin.javafx.control.tableview.column.children
import com.github.nayasis.kotlin.javafx.control.tableview.column.findBy
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.skin.TableViewSkin
import javafx.scene.control.skin.VirtualFlow
import tornadofx.selectedItem
import java.lang.Integer.min
import kotlin.math.max

@Suppress("UNCHECKED_CAST")
fun <S,T:Any> TableView<S>.findColumnBy(fxId: String): TableColumn<S,T> {
    for( col in columns )
        return (col.findBy(fxId) ?: continue) as TableColumn<S,T>
    throw NotFound("fxId:$fxId")
}

val <S> TableView<S>.allColumns: List<TableColumn<S,*>>
    get() {
        return ArrayList<TableColumn<S,*>>().apply {
            columns.forEach{
                this.add(it as TableColumn<S,*> )
                it.children(true).let { children ->
                    if(children.isNotEmpty()) this.addAll(children)
                }
            }
        }
    }

fun <S> TableView<S>.fillFxId(): TableView<S> {
    this.allColumns.withIndex().forEach {
        if(it.value.id.isNullOrEmpty()) {
            it.value.id = "${this.id}-${it.index}"
        }
    }
    return this
}

val <S> TableView<S>.focusedItem: S?
    get() = this.focused.row.let { row ->
        if( row >= 0 ) items[row] else null
    }

var <S> TableView<S>.focused: Position
    get() {
        return try {
            focusModel.focusedCellProperty().get().let {
                Position(it.row, it.column)
            }
        } catch (e: Exception) {
            Position(-1,-1)
        }
    }
    set(pos) {
        pos.run{ focus(row,col) }
    }

data class Position(val row: Int = 0, val col: Int = 0)

fun <S> TableView<S>.select(row: Int, col: Int = -1): TableView<S> {
    selectionModel.clearSelection()
    if( col < 0 ) {
        selectionModel.select( row )
    } else {
        val colIndex = min( max(col, 0), visibleLeafColumns.size - 1 )
        val column = visibleLeafColumns[colIndex]
        selectionModel.select( row, column )
    }
    return this
}

var <S> TableView<S>.selected: Position
    get() {
        return selectionModel.selectedCells.firstOrNull()?.let {
            Position(it.row,it.column)
        } ?: Position(-1,-1)
    }
    set(pos) {
        pos.run{ select(row,col) }
    }

fun <S> TableView<S>.selectBy(row: S?): TableView<S> {
    return select(indexOf(row),-1)
}

fun <S> TableView<S>.indexOf(row: S?): Int {
    return when (row) {
        null -> -1
        is Int -> row
        else -> items.indexOf(row)
    }
}

fun <S> TableView<S>.focus(row: Int, col: Int = -1): TableView<S> {
    select(row, col)
    requestFocus()
    if( col < 0 ) {
        focusModel.focus(row)
    } else {
        val colIndex = min( max(col, 0), visibleLeafColumns.size - 1 )
        val column = visibleLeafColumns[colIndex]
        focusModel.focus(row, column)
    }
    return this
}

fun <S> TableView<S>.focusBy(row: S?): TableView<S> {
    return focus(indexOf(row),-1)
}

fun <S> TableView<S>.scroll(row: Int, middle: Boolean = true): TableView<S> {
    val index = if( middle ) {
        max( row - (visibleRows / 2), 0)
    } else {
        row
    }
    scrollTo(index)
    return this
}

fun <S> TableView<S>.scrollBy(row: S?, middle: Boolean = true): TableView<S> {
    return scroll(indexOf(row),middle)
}

val <S> TableView<S>.visibleRows: Int
    get() {
        return virtualFlow?.let{
            if(it.firstVisibleCell == null) {
                0
            } else {
                val first = it.firstVisibleCell.index
                val last  = it.lastVisibleCell.index
                last - first + 1
            }
        } ?: 0
    }

@Suppress("UNCHECKED_CAST")
val <S> TableView<S>.virtualFlow: VirtualFlow<*>?
    get() = (skin as TableViewSkin<S>?)?.children?.firstOrNull { it is VirtualFlow<*> } as VirtualFlow<*>?