package com.rockthejvm.internals

import java.io.File
import kotlin.reflect.*
import kotlin.reflect.full.*

object ReflectionTypes {

    // example: avoid/circumvent type erasure

    // this is useless at best
    //    fun processList(list: List<*>) = when(list) {
    //        is List<*> -> list.forEach { println(it) }
    //        else -> println("not supported")
    //    }

    fun processList(list: List<*>, type: KType) {
        if (type.isSubtypeOf(typeOf<List<String>>())) {
            println("Processing a list of strings")
        } else if (type.isSubtypeOf(typeOf<List<Int>>())) {
            println("List of ints")
        } else {
            println("not supported")
        }
    }

    inline fun <reified T> processListGeneric(list: List<T>) =
        processList(list, typeOf<List<T>>())

    fun processList_v2(list: List<*>) {
        val typeParams = list::class.typeParameters.map{it.name}
        println("Type arguments: $typeParams")
        if (typeParams.contains("String")) {
            println("Processing list of Strings")
        } else if (typeParams.contains("Int")) {
            println("Processing list of Ints")
        } else {
            println("not supported")
        }
    }

    // more complex example
    // parsing conf files
    /*
        example conf file
            host = localhost
            port = 8080
            debug = true
            maxConnections = 100
            timeout = 3.2
     */
    data class MyConfig(
        val host: String,
        val port: Int,
        val debug: Boolean,
        val maxConnections: Int,
        val timeout: Double
    )

    @JvmStatic
    fun main(args: Array<String>) {
        val config = ConfigLoader.default().loadAs<MyConfig>()
        println(config)
        val timeout = config.timeout
        println("Configured timeout is $timeout seconds")
    }
}

class ConfigLoader private constructor(val path: String = "src/main/resources/application.conf") {
    fun parseFile(): Map<String, String> {
        val file = File(path)
        val configMap = mutableMapOf<String, String>()
        file.forEachLine { line ->
            val trimmedLine = line.trim()
            if (trimmedLine.isNotEmpty() && !trimmedLine.startsWith("#")) {
                val (key, value) = trimmedLine.split("=").map { it.trim() }
                configMap[key] = value
            }
        }
        return configMap
    }

    fun deserializeValue(value: String, type: KType): Any =
        when (type.classifier) { // KType.classifier => KClassifier (supertype of KClass)
            String::class -> value
            Int::class -> value.toInt()
            Boolean::class -> value.toBoolean()
            Double::class -> value.toDouble()
            else -> throw IllegalArgumentException("Unsupported type: $type")
        }

    inline fun <reified T:Any> deserializeObject(props: Map<String, String>): T {
        // KClass<T> to be able to build an instance of T
        val kClass = T::class
        val constructor = kClass.primaryConstructor ?: throw IllegalArgumentException("Type ${kClass.simpleName} does not have an accessible primary constructor")
        val args: Map<KParameter, Any> = constructor.parameters.associateWith { param ->
            val key = param.name ?: throw IllegalArgumentException("Unnamed constructor param for ${kClass.simpleName}")
            val value = props[key] ?: throw IllegalArgumentException("Missing value for the constructor param ${param.name} in class ${kClass.simpleName}")
            deserializeValue(value, param.type)
        }

        return constructor.callBy(args)
    }

    inline fun <reified T: Any> loadAs(): T {
        val props = parseFile()
        return deserializeObject<T>(props)
    }

    companion object {
        fun default() = ConfigLoader()
        fun at(path: String) = ConfigLoader(path)
    }
}