package com.github.nayasis.kotlin.javafx.control.tableview

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.control.TableView
import javafx.scene.paint.Color
import tornadofx.*
import java.time.LocalDate
import java.time.Period

fun main() {
    launch<ReactiveTableApp>()
}

class ReactiveTableApp: App(ReactiveTable::class)

class ReactiveTable: View() {
    private lateinit var table: TableView<Person>
    override val root = vbox {
        hbox{
            button("change") {
                action {
                    persons[0].apply {
                        name += "A"
                    }
                    persons[1].apply {
                        birthday = birthday.plusDays(1).plusMonths(1)
                    }
                    persons.forEach {
                        println(it)
                    }
                    println("-".repeat(20))
                }
            }
            button("refresh") {
                action {
                    table.refresh()
                }
            }
            button("scrollTo") {
                var i = 1
                action {
                    if(i >= persons.size)
                        i = 0
                    persons[i++].let{
                        table.focusBy(it)
//                        table.scrollBy(it)
                        table.scrollBy(it, ScrollMode.OUTBOUND_TO_MIDDLE)
                    }
                }
            }
            alignment = Pos.CENTER_RIGHT
            spacing = 5.0
        }
        table = tableview(persons) {
            column("ID",Person::idProperty)
            column("Name", Person::nameProperty)
            column("Birthday", Person::birthdayProperty)
            column("Age",Person::ageProperty) {
                cellFormat {
                    if( it != null ) {

                    }
                }
            }
            column("Age",Person::ageProperty).cellFormat {
                if( it != null ) {
                    text = "${it}"
                    style {
                        if( it < 20 ) {
                            backgroundColor += c("#8b0000")
                            textFill = Color.WHITE
                        } else {
                            backgroundColor += Color.TRANSPARENT
                            textFill = Color.BLACK
                        }
                    }
                } else {
                    graphic = null
                    tooltip = null
                    text    = null
                }
            }
            column("Etc",Person::birthdayProperty).cellFormat {
                text = this.rowItem.ageProperty.value.toString()
            }
        }
    }
}

private val persons = mutableListOf(
    Person(1,"S",LocalDate.of(1981,12,4)),
    Person(2,"Tom Marks",LocalDate.of(2011,1,23)),
    Person(3,"Stuart Gills",LocalDate.of(1989,5,23)),
    Person(4,"Nicole Williams",LocalDate.of(1998,8,11)),
).apply {
    for(i in 5..100) {
        add(Person(i,"$i", LocalDate.now()))
    }
}.asObservable()

class Person{

    val idProperty = SimpleIntegerProperty()
    var id by idProperty

    val nameProperty = SimpleStringProperty()
    var name by nameProperty

    val birthdayProperty = SimpleObjectProperty<LocalDate>()
    var birthday by birthdayProperty

    val ageProperty = birthdayProperty.objectBinding {
        Period.between(it, LocalDate.now()).years
    }

    constructor()
    constructor(
        id: Int,
        name: String,
        birthday: LocalDate,
    ) {
        this.id = id
        this.name = name
        this.birthday = birthday
    }

    override fun toString(): String {
        return "Person(id=$id, name=$name, birthday=$birthday, age=${ageProperty.value})"
    }


//    override fun toString(): String {
//        return kotlinToString(arrayOf(Person::id,Person::name,Person::birthday,Person::ageProperty))
//    }
}