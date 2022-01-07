package com.github.nayasis.kotlin.javafx.fxml

import com.github.nayasis.kotlin.basica.core.extention.isNotEmpty
import com.github.nayasis.kotlin.javafx.control.basic.allChildren
import com.github.nayasis.kotlin.javafx.stage.Localizator
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import mu.KotlinLogging
import tornadofx.UIComponent
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.memberProperties

private val logger = KotlinLogging.logger {}

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

@Suppress("UNCHECKED_CAST")
fun <T : Node> fxid(propName: String? = null) = object : ReadOnlyProperty<UIComponent, T> {
    override fun getValue(thisRef: UIComponent, property: KProperty<*>): T {
        val key = propName ?: property.name
        val value = thisRef.fxmlLoader.namespace[key]
        if (value == null) {
            logger.warn { "Property $key of $thisRef was not resolved because there is no matching fx:id in ${thisRef.fxmlLoader.location}" }
        } else {
            return value as T
        }
        throw IllegalArgumentException("Property $key does not match fx:id declaration")
    }
}