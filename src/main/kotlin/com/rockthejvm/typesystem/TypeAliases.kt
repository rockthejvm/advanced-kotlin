package com.rockthejvm.typesystem

import kotlin.jvm.Throws

// type aliases have to be declared top-level
typealias Phonebook = Map<String, String>

// type aliases can have generic type arguments
typealias Table<A> = Map<String, A>

// example
class Either<out E, out A>
// variance modifiers carry over to the type aliases
typealias ErrorOr<A> = Either<Throwable, A>

object TypeAliases {
    val phonebook: Phonebook = mapOf("Superman" to "123-456") // Map<String, String>
    val theMap: Map<String, String> = phonebook // also okay
    val stringTable: Table<String> = phonebook // also ok

    @JvmStatic
    fun main(args: Array<String>) {

    }
}