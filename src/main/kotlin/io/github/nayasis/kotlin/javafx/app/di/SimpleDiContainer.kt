package io.github.nayasis.kotlin.javafx.app.di

import io.github.nayasis.kotlin.basica.core.klass.Classes
import tornadofx.DIContainer
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.valueParameters

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
class SimpleDiContainer: DIContainer {

    private val instances = HashMap<KClass<*>,LinkedHashMap<String,Any>>()
    private val creatingInstances = mutableSetOf<KClass<*>>()

    override fun <T: Any> getInstance(klass: KClass<T>): T {
        return get(klass, null) ?:
            throw AssertionError("No bean ($klass) found in container")
    }

    override fun <T: Any> getInstance(klass: KClass<T>, beamName: String): T {
        return get(klass, beamName) ?:
            throw AssertionError("No bean ($klass[$beamName]) found in container")
    }

    fun <T: Any> get(klass: KClass<T>, beanName: String? = null): T? {
        @Suppress("UNCHECKED_CAST")
        return if (beanName != null) {
            instances[klass]?.get(beanName)
        } else {
            instances[klass]?.values?.firstOrNull()
        } as? T
    }

    fun set(bean: Any, beanName: String? = null) {
        if (bean is KClass<*>) create(bean) else {
            if(get(bean::class, beanName) == null) {
                bean.remember(beanName)
            }
        }
    }

    fun <T: Any> create(klass: KClass<T>, name: String? = null): T {

        if(! klass.isCreatable())
            throw IllegalArgumentException("Class (${klass}) should have '${Inject::class}' annotation for creating bean")

        // Return existing instance if available (directly from instances map)
        get(klass, name)?.let { return it }

        // Check for circular dependency
        if (klass in creatingInstances) {
            throw IllegalStateException("Circular dependency detected: ${creatingInstances.joinToString(" -> ")} -> $klass")
        }
        creatingInstances.add(klass)

        // create instance
        try {
            val constructor = klass.primaryConstructor ?: return klass.createBean(name)
            val parameters  = constructor.valueParameters.map { param ->
                val paramKlass = param.type.classifier as? KClass<*>
                    ?: throw IllegalArgumentException("Cannot resolve parameter (${param.name}) in creating bean (${klass})")
                create(paramKlass)
            }
            return if(parameters.isEmpty()) {
                klass.createBean(name)
            } else {
                constructor.call(*parameters.toTypedArray()).remember(name)
            }
        } finally {
            creatingInstances.remove(klass)
        }
    }

    fun <T: Any> create(klassName: String): T {
        @Suppress("UNCHECKED_CAST")
        val klass = Class.forName(klassName).kotlin as KClass<T>
        return create(klass, null)
    }

    fun set(vararg bean: Any) {
        bean.forEach { set(it) }
    }

    fun remove(bean: Any) {
        val klass = bean as? KClass<*> ?: bean::class
        instances.remove(klass)
    }

    fun remove(bean: Any, beanName: String) {
        val klass = bean as? KClass<*> ?: bean::class
        instances[klass]?.remove(beanName)
    }

    /**
     * Scan packages and automatically create instances for classes annotated with @Inject
     *
     * @param packages Package names to scan (e.g., "com.example.service", "com.example.repository")
     */
    fun scanPackages(vararg packages: String) {
        packages.forEach { packageName ->
            scanPackage(packageName)
        }
    }

    private fun scanPackage(packageName: String) {
        val packagePath = packageName.replace('.', '/')
        Classes.findResources("$packagePath/*.class", "$packagePath/**/*.class").forEach { url ->
            val path      = url.path.replace('\\', '/')
            val start     = path.indexOf(packagePath).takeIf { it >= 0 } ?: return@forEach
            val classPath = path.substring(start).takeIf { it.endsWith(".class") } ?: return@forEach
            val className = classPath.removeSuffix(".class").replace('/', '.')
            runCatching {
                create<Any>(className)
            }
        }
    }

    private fun <T: Any> KClass<T>.createBean(name: String?): T {
        return this.createInstance().remember(name)
    }

    private fun <T: Any> T.remember(name: String?): T {
        val klass = this::class
        instances.getOrPut(klass) { LinkedHashMap() }[klass.getBeanName(name)] = this
        return this
    }

    private fun KClass<*>.getBeanName(requestName: String? = null): String {
        return requestName ?: this.findAnnotation<Inject>()?.name?.takeIf { it.isNotBlank() } ?: this.simpleName ?: "anonymous"
    }

    private fun KClass<*>.isCreatable(): Boolean {
        return this.findAnnotation<Inject>() != null
    }

}