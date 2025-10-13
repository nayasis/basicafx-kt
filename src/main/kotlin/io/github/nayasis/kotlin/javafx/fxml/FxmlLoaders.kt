@file:Suppress("unused")

package io.github.nayasis.kotlin.javafx.fxml

import io.github.nayasis.kotlin.basica.core.klass.isSubclassOf
import io.github.nayasis.kotlin.basica.etc.error
import io.github.nayasis.kotlin.javafx.control.basic.allChildrenById
import io.github.nayasis.kotlin.javafx.stage.Localizator
import io.github.oshai.kotlinlogging.KotlinLogging
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import tornadofx.UIComponent
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaType

private val logger = KotlinLogging.logger {}

/**
 * load FXML with field injection
 *
 * @param injectionBean target bean to field injection
 * @return FXML loaded object
 */
fun <T: Node> FXMLLoader.loadWith(injectionBean: Any): T {

    val root  = load<T>()
    val nodes = root.allChildrenById

    val properties = injectionBean::class.memberProperties
        .filterIsInstance<KMutableProperty<*>>()
        .filter { it.returnType.javaType.isSubclassOf(Node::class.java) }
        as ArrayList<KMutableProperty<*>>

    properties.forEach { field ->
        nodes[field.name]?.let { node ->
            val inaccessible = ! field.isAccessible
            if( inaccessible )
                field.isAccessible = true
            try {
                field.setter.call(injectionBean,node)
            } catch (e: Exception) {
                logger.error(e)
            } finally {
                if( inaccessible ) field.isAccessible = false
            }
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