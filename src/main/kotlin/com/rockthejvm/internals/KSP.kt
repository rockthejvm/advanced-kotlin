package com.rockthejvm.internals

import com.rockthejvm.builderksp.Builder

// Use-case: generate builder patterns for data classes
@Builder
data class Person(val name: String, val age: Int)

@Builder
data class Pet(val name: String, val nickname: String)

object KSP {
    /*
        - analyze source code and generate
        - new source code
        - compile
        - access methods/functionality at COMPILE TIME
     */

    // module 1 - symbol definitions (annotations)
    // module 2 - KSP logic for generating the source
    // module 3 - source + the place where the generated source will be created

    @JvmStatic
    fun main(args: Array<String>) {
        val masterYoda = PersonBuilder().name("Master Yoda").age(800).build()
        val dino = PetBuilder().name("Dino").nickname("The destroyer").build()
        println(masterYoda)
        println(dino)
    }
}