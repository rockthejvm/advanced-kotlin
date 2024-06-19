package com.rockthejvm.internals

object ReifiedTypes {

    // does not work
    //    fun <T> filterByType(list: List<Any>): List<T> =
    //        list.filter { it is T }.map { it as T }

    // JVM has type erasure
    // Generics were added in Java 5 (2004)
    // Java pre-5
    // List thing = new ArrayList()
    // Java 5
    // List<String> thing = new ArrayList<>()
    // type erasure
    // List thing = new ArrayList()

    // solution is inline fun + reified type
    inline fun <reified T> List<Any>.filterByType(): List<T> =
        this.filter { it is T }.map { it as T }

    data class Person(val name: String, val age: Int)
    data class Car(val make: String, val model: String)

    @JvmStatic
    fun main(args: Array<String>) {
        val mixedList: List<Any> = listOf(
            Person("John", 30),
            Car("Toyota", "Corolla"),
            Person("Jane", 25),
            Car("Honda", "Civic"),
            "A random string",
            42,
            "rock the jvm"
        )

        val persons: List<Person> = mixedList.filterByType()
        // rewritten to:
        // mixedList.filter { it is Person }.map { it as Person } which is legal, can be performed at runtime

        val cars: List<Car> = mixedList.filterByType()
        val strings: List<String> = mixedList.filterByType()
        val numbers: List<Int> = mixedList.filterByType()

        println("Persons: $persons")
        println("Cars: $cars")
        println("Strings: $strings")
        println("Numbers: $numbers")

    }
}