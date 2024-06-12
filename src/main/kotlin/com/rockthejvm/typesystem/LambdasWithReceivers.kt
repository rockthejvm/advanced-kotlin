package com.rockthejvm.typesystem

object LambdasWithReceivers {

    // create a behavior
    // option 1 - OO way
    data class Person(val name: String, val age: Int) {
        fun greet() = "hi, I'm $name"
    }

    // option 2 (procedural way) - create a function that takes a person
    fun greet(p: Person) =
        "Hi, I'm ${p.name}"

    // option 3 - extension method (Kotlin/Scala)
    fun Person.greetExt() =
    //  ^^^^^^ RECEIVER type => gives us access to the `this` reference
        "Hi, I'm $name"

    // option 4 - function value (lambda)
    val greetFun: (Person) -> String = { p: Person -> "Hi, I'm ${p.name}" }

    // option 5 - lambda with receiver (an "extension lambda")
    val greetFunRec: Person.() -> String = { "Hi, I'm $name" }
    //               ^^^^^^ RECEIVER => gives us access to the `this` reference

    // APIs that look "baked into Kotlin" aka DSL
    // examples: Ktor, Arrow, coroutines

    // mini-"library" for JSON serialization
    // { "name" : "Daniel", "age" : 12 }
    // support numbers (ints), strings, JSON objects
    sealed interface JsonValue
    data class JsonNumber(val value: Int): JsonValue {
        override fun toString(): String = value.toString()
    }
    data class JsonString(val value: String): JsonValue {
        override fun toString(): String = "\"$value\""
    }
    data class JsonObject(val attributes: Map<String, JsonValue>): JsonValue {
        override fun toString(): String =
            attributes.toList().joinToString(",","{","}") { pair -> "\"${pair.first}\": ${pair.second}"}
    }

    // "mutable builder" of a JsonObject
    class JSONScope {
        private var props: MutableMap<String, JsonValue> = mutableMapOf()

        fun toValue(): JsonValue = JsonObject(props)

        // "not so nice API"
        fun addString(name: String, value: String) {
            props[name] = JsonString(value)
        }

        fun addInt(name: String, value: Int) {
            props[name] = JsonNumber(value)
        }

        fun addValue(name: String, value: JsonValue) {
            props[name] = value
        }

        // "nice API"
        infix fun String.to(value: String) { // "name" to "Daniel"
            props[this] = JsonString(value)
        }

        infix fun String.to(value: Int) { // "age" to 12
            props[this] = JsonNumber(value)
        }

        infix fun String.to(value: JsonValue) { // "credentials" to ...
            props[this] = value
        }
    }

    fun jsonNotSoNice(init: (JSONScope) -> Unit): JsonValue {
        val obj = JSONScope()
        init(obj)
        return obj.toValue()
    }

    fun json(init: JSONScope.() -> Unit): JsonValue {
        val obj = JSONScope()
        obj.init()
        return obj.toValue()
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val jsonObj = JsonObject(mapOf(
            "user" to JsonObject(mapOf(
                "name" to JsonString("Daniel"),
                "age" to JsonNumber(99)
            )),
            "credentials" to JsonObject(mapOf(
                "type" to JsonString("password"),
                "value" to JsonString("rockthejvm")
            ))
        ))

        val jsonObj_v2 = jsonNotSoNice { j ->
            j.addValue("user", jsonNotSoNice { j2 ->
                j2.addString("name", "Daniel")
                j2.addInt("age", 99)
            })
            j.addValue("credentials", jsonNotSoNice { j2 ->
                j2.addString("type", "password")
                j2.addString("value", "rockthejvm")
            })
        }

        val jsonObj_v3 = json { // in this scope, I have access to all the extension methods defined earlier
            "user" to json {
                "name" to "Daniel"
                "age" to 99
            }
            "credentials" to json {
                "type" to "password"
                "value" to "rockthejvm"
            }
        }

        println(jsonObj)
        println(jsonObj_v2)
        println(jsonObj_v3)
    }
}