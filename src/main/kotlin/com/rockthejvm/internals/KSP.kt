package com.rockthejvm.internals

object KSP {
    /*
        - analyze source code and generate
        - new source code
        - compile
        - access methods/functionality at COMPILE TIME
     */

    // Use-case: generate builder patterns for data classes
    data class Person(val name: String, val age: Int)

    // module 1 - symbol definitions (annotations)
    // module 2 - KSP logic for generating the source
    // module 3 - source + the place where the generated source will be created

    @JvmStatic
    fun main(args: Array<String>) {

    }
}