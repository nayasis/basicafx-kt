package io.github.nayasis.kotlin.javafx.collection

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.transformation.FilteredList
import javafx.collections.transformation.SortedList
import javafx.scene.control.TableView

/**
 * observable list with a predicate filter and a table driven sort.
 *
 * [items] is the full backing list. iteration and [size] see the
 * filtered and sorted view, which is what a bound table renders.
 *
 * ```
 * val links = FilterSortList<Link>()
 * links.bindTo(tableView)
 * links.predicate = { it.title.contains(keyword) }
 * ```
 */
class FilterSortList<T>(
    val items: ObservableList<T> = FXCollections.observableArrayList(),
): Iterable<T> {

    private val filtered = FilteredList(items)
    private val sorted   = SortedList(filtered)

    /** the filtered and sorted view, as put into a table by [bindTo]. */
    val sortedItems: ObservableList<T>
        get() = sorted

    /** which items are visible. applies to items added later as well. */
    var predicate: (T) -> Boolean = { true }
        set(value) {
            field = value
            filtered.setPredicate(value)
        }

    /** number of visible items. use `items.size` for the total. */
    val size: Int
        get() = sorted.size

    /** put the sorted view into [table] and follow its column sort order. */
    fun bindTo(table: TableView<T>) {
        sorted.comparatorProperty().bind(table.comparatorProperty())
        table.items = sorted
    }

    fun add(item: T): Boolean = items.add(item)
    fun addAll(list: Collection<T>): Boolean = items.addAll(list)
    fun remove(item: T): Boolean = items.remove(item)
    fun clear() = items.clear()

    override fun iterator(): Iterator<T> = sorted.iterator()

}
