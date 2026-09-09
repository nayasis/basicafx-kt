package io.github.nayasis.kotlin.javafx.control.tableview

import io.github.nayasis.kotlin.javafx.app.FxView
import io.github.nayasis.kotlin.javafx.collection.FilterSortList
import io.github.nayasis.kotlin.javafx.common.DemoApp
import javafx.application.Application
import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.TextField
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import java.time.LocalDate
import java.time.Period
import java.util.concurrent.Callable

fun main(args: Array<String>) {
    Application.launch(ReactiveTableApp::class.java, *args)
}

class ReactiveTableApp: DemoApp(ReactiveTable())

class ReactiveTable: FxView("reactive table") {

    private val persons = FilterSortList(samplePersons())

    private val table = TableView<Person>().apply {
        columns.addAll(
            TableColumn<Person,Number>("ID").apply {
                setCellValueFactory { it.value.idProperty }
            },
            TableColumn<Person,String>("Name").apply {
                setCellValueFactory { it.value.nameProperty }
            },
            TableColumn<Person,LocalDate>("Birthday").apply {
                setCellValueFactory { it.value.birthdayProperty }
            },
            TableColumn<Person,Number>("Age").apply {
                setCellValueFactory { it.value.ageProperty }
                setCellFactory { ageCell() }
            },
        )
    }

    private val filter = TextField().apply {
        promptText = "filter by name"
        textProperty().addListener { _, _, keyword ->
            persons.predicate = { keyword.isNullOrBlank() || it.name.contains(keyword, ignoreCase = true) }
        }
    }

    override val root = VBox().apply {

        children.add(HBox().apply {
            children.add(filter)
            children.add(Button("change").apply { setOnAction {
                persons.items[0].apply { name += "A" }
                persons.items[1].apply { birthday = birthday.plusDays(1).plusMonths(1) }
                persons.items.forEach { println(it) }
                println("-".repeat(20))
            }})
            children.add(Button("refresh").apply { setOnAction {
                table.refresh()
            }})
            children.add(Button("scrollTo").apply {
                var i = 1
                setOnAction {
                    if(i >= persons.size) i = 0
                    persons.elementAt(i++).let {
                        table.focusBy(it)
                        table.scrollBy(it, ScrollMode.OUTBOUND_TO_MIDDLE)
                    }
                }
            })
            alignment = Pos.CENTER_RIGHT
            spacing = 5.0
        })

        children.add(table)

        // shows the filtered/sorted view and follows the column sort order
        persons.bindTo(table)

    }

}

/** paints ages under 20 on a dark red background */
private fun ageCell() = object: TableCell<Person,Number>() {
    override fun updateItem(item: Number?, empty: Boolean) {
        super.updateItem(item, empty)
        if(empty || item == null) {
            text  = null
            style = ""
            return
        }
        text  = "$item"
        style = if(item.toInt() < 20) "-fx-background-color: #8b0000; -fx-text-fill: white;"
                else                  "-fx-background-color: transparent; -fx-text-fill: black;"
    }
}

private fun samplePersons() = javafx.collections.FXCollections.observableArrayList(
    Person(1,"S",LocalDate.of(1981,12,4)),
    Person(2,"Tom Marks",LocalDate.of(2011,1,23)),
    Person(3,"Stuart Gills",LocalDate.of(1989,5,23)),
    Person(4,"Nicole Williams",LocalDate.of(1998,8,11)),
).apply {
    for(i in 5..100) {
        add(Person(i,"$i", LocalDate.now()))
    }
}

class Person(id: Int = 0, name: String = "", birthday: LocalDate = LocalDate.now()) {

    val idProperty = SimpleIntegerProperty(id)
    var id: Int
        get() = idProperty.get()
        set(value) = idProperty.set(value)

    val nameProperty = SimpleStringProperty(name)
    var name: String
        get() = nameProperty.get()
        set(value) = nameProperty.set(value)

    val birthdayProperty = SimpleObjectProperty(birthday)
    var birthday: LocalDate
        get() = birthdayProperty.get()
        set(value) = birthdayProperty.set(value)

    val ageProperty = Bindings.createIntegerBinding(
        Callable { Period.between(birthdayProperty.get(), LocalDate.now()).years },
        birthdayProperty,
    )

    override fun toString(): String =
        "Person(id=$id, name=$name, birthday=$birthday, age=${ageProperty.value})"

}
