package io.github.nayasis.kotlin.javafx.collection

import io.kotest.matchers.shouldBe
import javafx.collections.FXCollections
import org.junit.jupiter.api.Test

internal class FilterSortListTest {

    @Test
    fun `predicate hides items but keeps the backing list intact`() {
        val list = FilterSortList(FXCollections.observableArrayList(1, 2, 3, 4))

        list.size shouldBe 4

        list.predicate = { it % 2 == 0 }

        list.size shouldBe 2          // visible count
        list.items.size shouldBe 4    // total count
        list.toList() shouldBe listOf(2, 4)
    }

    @Test
    fun `mutations go through to the backing list`() {
        val list = FilterSortList<Int>()

        list.add(1)
        list.addAll(listOf(2, 3))
        list.remove(2)

        list.items.toList() shouldBe listOf(1, 3)

        list.clear()
        list.items.size shouldBe 0
    }

    @Test
    fun `items added after the predicate is set are filtered too`() {
        val list = FilterSortList<Int>()
        list.predicate = { it % 2 == 0 }

        list.addAll(listOf(1, 2, 3, 4))

        list.toList() shouldBe listOf(2, 4)
        list.items.size shouldBe 4
    }

}
