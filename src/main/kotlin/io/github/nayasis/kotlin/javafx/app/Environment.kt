package io.github.nayasis.kotlin.javafx.app

import com.github.nayasis.kotlin.basica.core.collection.flattenKeys
import com.github.nayasis.kotlin.basica.core.string.toResource
import com.github.nayasis.kotlin.basica.core.url.toInputStream
import com.github.nayasis.kotlin.basica.core.validator.cast
import org.yaml.snakeyaml.Yaml

class Environment(
    args: Array<String>? = null,
    configYamlPath: String = "application.yml",
) {

    val all: LinkedHashMap<String,Any> = loadYml(configYamlPath)

    init {
        args?.let { merge(it) }
    }

    @Suppress("UNCHECKED_CAST")
    private fun loadYml(path: String): LinkedHashMap<String,Any> {
        val map = LinkedHashMap<String,Any>()
        path.toResource()?.toInputStream()?.let {
            Yaml().loadAll(it)
        }?.map { it as Map<*,*> }?.map {
            it.flattenKeys() as Map<String,Any>
        }?.forEach {
            map.putAll(it)
        }
        return map
    }

    fun merge(args: Array<String>): Environment {
        args.filter { it.contains("=") }
            .map { it.split("=", limit = 2) }
            .associate { (key, value) -> key to value }
            .run { all.putAll(this) }
        return this
    }

    inline operator fun <reified T: Any> get(key: String): T? {
        return all[key]?.cast()
    }

    operator fun set(key: String, value: Any) {
        all[key] = value
    }

}