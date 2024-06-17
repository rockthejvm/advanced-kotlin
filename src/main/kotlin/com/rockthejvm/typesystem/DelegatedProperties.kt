package com.rockthejvm.typesystem

import kotlin.reflect.KProperty

object DelegatedProperties {

    // access (get/set) properties and trigger side effects

    class LoggingClassN(val id: Int) {
        var property: Int = 0
            get() {
                // logging of the change of the value
                println("[logging $id] getting property")
                return field
            }
            set(value) {
                println("[logging $id] setting property to new value $value")
                field = value
            }
    }

    fun demoNaiveLogger() {
        val logger = LoggingClassN(42)
        logger.property = 2
        val x = logger.property // getting
        println(x)
        logger.property = 3
        println(logger.property)
    }

    // delegated properties
    class LoggingProp<A>(val id: String, val default: A) { // must have AT MOST one type argument
        var property: A = default

        operator fun getValue(currentRef: Any, prop: KProperty<*>): A {
            // logging of the change of the value
            println("[logging $id] getting property")
            return property
        }

        operator fun setValue(currentRef: Any, prop: KProperty<*>, value: A) {
            println("[logging $id] setting property to new value $value")
            property = value
        }
    }

    class LoggingClass(id: Int) {
        var firstProperty: Int by LoggingProp("$id-firstProperty", 0) // <-- delegated property
        var secondProperty: Int by LoggingProp("$id-secondProperty", 0) // same behavior, reused!
        var stringProperty: String by LoggingProp("$id-stringProperty", "")
    }

    // how delegates work
    class LoggingClass_v2(id: Int) {
        var myProperty: Int by LoggingProp("$id-myProperty", 0)
    }

    // translates to
    class LoggingClass_v2_Expanded(id: Int) {
        private var prop_delegate = LoggingProp("$id-myProperty", 0)
        var myProperty: Int
            get() {
                return prop_delegate.getValue(this, this::prop_delegate)
                //                                  ^^^^^^^^^^^^^^^^^^^ reflective call
            }
            set(value) {
                prop_delegate.setValue(this, this::prop_delegate, value)
                //                           ^^^^^^^^^^^^^^^^^^^ reflective reference
            }
    }


    fun demoLogger() {
        val loggingClass = LoggingClass(42)
        loggingClass.firstProperty = 34
        val x = loggingClass.firstProperty
        loggingClass.secondProperty = 23
        val y = x + loggingClass.secondProperty
        val z = loggingClass.stringProperty
    }

    /**
     * Exercise: implement a class Delayed
     */
    class Delayed<A>(private val func: () -> A) {
        private var content: A? = null

        operator fun getValue(currentRef: Any, prop: KProperty<*>): A {
            if (content == null) {
                content = func()
            }
            return content!!
        }
    }

    // lazy evaluation = variable is not set until first use
    class DelayedClass {
        val delayedProp: Int by Delayed { // usage as a delegated property
            println("I'm setting up!")
            42
        }
    }

    fun demoDelayed() {
        val delayed = DelayedClass()
        val x = delayed.delayedProp
        val y = delayed.delayedProp // no more prints
    }

    @JvmStatic
    fun main(args: Array<String>) {
        demoDelayed()
    }
}