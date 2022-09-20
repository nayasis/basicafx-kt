package com.github.nayasis.kotlin.javafx.control.tableview

import com.github.nayasis.kotlin.basica.exception.implements.NotFound
import com.github.nayasis.kotlin.javafx.control.tableview.ScrollMode.*
import com.github.nayasis.kotlin.javafx.control.tableview.column.children
import com.github.nayasis.kotlin.javafx.control.tableview.column.findBy
import javafx.scene.control.IndexedCell
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.skin.TableViewSkin
import javafx.scene.control.skin.VirtualFlow
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

data class Position(val row: Int = -1, val col: Int = -1)

fun <S> TableView<S>.select(row: Int, col: Int = -1): TableView<S> {
    selectionModel.clearSelection()
    when {
        row >= 0 && col >= 0 -> {
            val colIndex = min( max(col, 0), visibleLeafColumns.size - 1 )
            val column = visibleLeafColumns[colIndex]
            selectionModel.select( row, column )
        }
        row >=0 && col < 0 -> selectionModel.select(row)
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

fun <S> TableView<S>.selectBy(row: S?, col: Int = -1): TableView<S> {
    return select(indexOf(row),col)
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
    when {
        row >= 0 && col >= 0 -> {
            val colIndex = min( max(col, 0), visibleLeafColumns.size - 1 )
            val column = visibleLeafColumns[colIndex]
            focusModel.focus(row, column)
        }
        row >= 0  && col < 0 -> focusModel.focus(row)
    }
    return this
}

fun <S> TableView<S>.focusBy(row: S?, col: Int = -1): TableView<S> {
    return focus(indexOf(row),col)
}

enum class ScrollMode {
    DEFAULT,
    ALWAYS_TO_MIDDLE,
    OUTBOUND_TO_MIDDLE,
}

fun <S> TableView<S>.scroll(row: Int, mode: ScrollMode = ALWAYS_TO_MIDDLE): TableView<S> {
    val index = when(mode) {
        ALWAYS_TO_MIDDLE -> getMiddleIndex(row,visibleRows)
        OUTBOUND_TO_MIDDLE -> {
            if( visibleIndex.`in`(row) ) null else getMiddleIndex(row,visibleRows)
        }
        else -> row
    }
    index?.let { scrollTo(it) }
    return this
}

private fun getMiddleIndex(row: Int, visibleRows: Int): Int {
    return if(visibleRows % 2 == 0) {
        max( row - (visibleRows / 2) + 1, 0)
    } else {
        max( row - (visibleRows / 2), 0)
    }
}

fun <S> TableView<S>.scrollBy(row: S?, mode: ScrollMode = ALWAYS_TO_MIDDLE): TableView<S> {
    return scroll(indexOf(row),mode)
}

@Suppress("UNCHECKED_CAST")
val <S> TableView<S>.virtualFlow: VirtualFlow<IndexedCell<S>>?
    get() = (skin as TableViewSkin<S>?)?.children?.firstOrNull { it is VirtualFlow<*> } as VirtualFlow<IndexedCell<S>>?


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

val <S> TableView<S>.visibleIndex: Range
    get() {
        return virtualFlow?.let{
            if(it.firstVisibleCell == null) {
                Range()
            } else {
                Range(
                    it.firstVisibleCell.index,
                    it.lastVisibleCell.index,
                )
            }
        } ?: Range()
    }

data class Range(
    val start: Int = -1,
    val end: Int = -1,
) {
    fun `in`(index: Int?): Boolean {
        return index?.let {
            start <= index && index <= end
        } ?: false
    }
}