package com.github.nayasis.kotlin.javafx.fxml

import com.github.nayasis.kotlin.basica.core.extention.isNotEmpty
import com.github.nayasis.kotlin.javafx.control.basic.allChildren
import com.github.nayasis.kotlin.javafx.stage.Localizator
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties

/**
 * load FXML with field injection
 *
 * @param injectionBean target bean to field injection
 * @return FXML loaded object
 */
inline fun <reified T: Node> FXMLLoader.loadWith(injectionBean: Any): T {

    val root  = load<T>()
    val nodes = root.allChildren.filter { it.id.isNotEmpty() }.associateBy { it.id }

    injectionBean::class.memberProperties
        .filterIsInstance<Node>()
        .filterIsInstance<KMutableProperty<*>>()
        .forEach { field ->
            nodes[field.name]?.let { node ->
                field.setter.call(injectionBean,node)
            }
        }

    Localizator(root)
    return root

}