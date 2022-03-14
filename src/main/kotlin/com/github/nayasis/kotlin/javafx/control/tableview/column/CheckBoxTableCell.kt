package com.github.nayasis.kotlin.javafx.control.tableview.column

import javafx.beans.property.BooleanProperty
import javafx.scene.control.CheckBox
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn


class CheckBoxTableCell<S>: TableCell<S,Boolean> {

    private var checkbox = CheckBox()
    private var lastBoundProperty: BooleanProperty? = null
    private var column: TableColumn<S,Boolean>

    constructor(column: TableColumn<S,Boolean>) {
        styleClass.add("check-box-table-cell")
        this.column = column
        checkbox.setOnAction {
            startEdit()
            commitEdit(!checkbox.isSelected)
        }
    }

    override fun startEdit() {
        super.startEdit()
        // WORKAROUND: the following line is necessary for the edit event to be complete
        tableView.edit(index,getTableColumn())
    }

    override fun updateItem(item: Boolean?,empty: Boolean) {
        super.updateItem(item,false)
        if (empty || item == null) {
            graphic = null
            text = null
        } else {
            val observableValue = column.getCellObservableValue(index)
            if (observableValue is BooleanProperty) {
                if (lastBoundProperty != null) checkbox!!.selectedProperty().unbindBidirectional(lastBoundProperty)
                lastBoundProperty = observableValue
                checkbox.selectedProperty().bindBidirectional(lastBoundProperty)
            }
            // calling checkbox.setSelected(item) here is redundant and might cause errors
            setGraphic(checkbox)
        }
    }

}