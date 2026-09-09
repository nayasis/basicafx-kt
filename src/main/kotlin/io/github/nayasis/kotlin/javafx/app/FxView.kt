package io.github.nayasis.kotlin.javafx.app

import io.github.nayasis.kotlin.javafx.control.basic.allChildrenById
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.Parent
import javafx.stage.Stage
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * a view: its [root] node plus the lifecycle hooks an application needs.
 *
 * ```
 * class Main: FxView("application.title".message()) {
 *     override val root: AnchorPane by fxml("/view/main/main.fxml")
 *     val tableMain: TableView<Link> by fxid()
 *     override fun onBeforeShow() { ... }
 * }
 * ```
 */
abstract class FxView(val title: String? = null) {

    abstract val root: Parent

    /** set by the [fxml] delegate so [fxid] can read the FXML namespace. */
    internal var fxmlLoader: FXMLLoader? = null

    /** the stage this view is currently shown on, if any. */
    val currentStage: Stage?
        get() = root.scene?.window as? Stage

    /** the application's primary stage. */
    val primaryStage: Stage
        get() = FxApp.primaryStage

    /** called after the view is put on a stage, before the stage is shown. */
    open fun onBeforeShow() {}

    /** called after the view's stage is hidden. */
    open fun onUndock() {}

}

/**
 * load [root] from an FXML resource.
 *
 * this does not localize the loaded tree; call [io.github.nayasis.kotlin.javafx.stage.Localizator]
 * where localization is wanted.
 *
 * @param path resource path of the FXML
 */
fun <T: Parent> fxml(path: String): ReadOnlyProperty<FxView, T> = object: ReadOnlyProperty<FxView, T> {
    private var value: T? = null
    override fun getValue(thisRef: FxView, property: KProperty<*>): T {
        value?.let { return it }
        val resource = thisRef::class.java.getResource(path)
            ?: throw IllegalArgumentException("fxml not found: $path")
        val loader = FXMLLoader(resource)
        val loaded = loader.load<T>()
        thisRef.fxmlLoader = loader
        value = loaded
        return loaded
    }
}

/**
 * bind a node declared with `fx:id`. defaults to the property's own name.
 *
 * @param propName fx:id to look up. defaults to the property name.
 */
@Suppress("UNCHECKED_CAST")
fun <T: Node> fxid(propName: String? = null): ReadOnlyProperty<FxView, T> = object: ReadOnlyProperty<FxView, T> {
    private var value: T? = null
    override fun getValue(thisRef: FxView, property: KProperty<*>): T {
        value?.let { return it }
        val key = propName ?: property.name
        // touch root first: it may be the `fxml` delegate that fills fxmlLoader
        val root = thisRef.root
        val node = thisRef.fxmlLoader?.namespace?.get(key) ?: root.allChildrenById[key]
            ?: throw IllegalArgumentException("no fx:id '$key' declared in ${thisRef::class.simpleName}")
        return (node as T).also { value = it }
    }
}
