package com.rockthejvm.typesystem

import java.util.UUID
import kotlin.properties.Delegates
import kotlin.random.Random
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

    class DelegatedProp<A>(constrArgs: Int, default: A) {
        var store: A = default
        operator fun getValue(currentRef: Any, prop: KProperty<*>): A {
            // perform side effect whenever the property is accessed
            // must return a value of the correct type
            return store
        }

        operator fun setValue(currentRef: Any, prop: KProperty<*>, value: A) {
            // perform side effect whenever the property is changed
        }
    }

    class MyUsefulClass {
        var myProperty: Int by DelegatedProp(constrArgs = 42, default = 0)
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

    /*
        Standard Delegated Properties
     */

    // 1 - lazy
    data class UserData(val name: String, val email: String)

    class Person(val id: String) {
        private fun fetchUserData(): UserData {
            // complex or it takes a while
            println("Fetching user data from remote server...")
            // simulate something long
            Thread.sleep(3000)
            return UserData(name = "John Doe", email = "johndoe@rockthejvm.com")
        }

        val userData: UserData by lazy { // lazy evaluation - a property is NOT computed until first use
            // perform long computation or network call
            // triggered on first use
            fetchUserData()
        }

        fun showUserData() {
            println("User data: $userData")
        }
    }

    fun demoLazy() {
        val person = Person(id = "abc-123")
        println("user created.") // at this point, fetchUserData() is NOT triggered
        println("About to show user data...")
        person.showUserData() // userData is first accessed, fetchUserData() will be triggered
        // userData is fetched and cached
        println("Showing user data once more...")
        person.showUserData() // fetchUserData() will NOT be triggered anymore, userData exists
    }

    // vetoable
    class BankAccount(initialBalance: Double) { // NEVER use double for money
        var balance: Double by Delegates.vetoable(initialBalance) { prop, oldValue, newValue ->
            // must return a boolean
            // if true -> var will be changed, if not, the change will be DENIED
            newValue >= 0
        }
    }

    fun demoVeto() {
        val account = BankAccount(100.0)
        println("Initial balance: ${account.balance}")
        account.balance = 150.0 // this should succeed
        println("Updated balance: ${account.balance}") // 150
        account.balance = -50.0 // this should be vetoed
        println("Final balance: ${account.balance}") // 150
    }

    // observable - perform side effects on changing of your properties
    // example: monitoring the staleness of a dataset
    enum class State {
        NONE, NEW, PROCESSED, STALE
    }

    class MonitoredDataset(name: String) {
        var state: State by Delegates.observable(State.NONE) { prop, oldValue, newValue ->
            // can alert a system if the state changes
            println("[dataset - $name] State changed: $oldValue -> $newValue")
            if (newValue == State.STALE)
                println("[dataset - $name] Alert: dataset is now stale, refresh data")
        }

        private var data: List<String> = listOf()

        fun consumeData() {
            if (state == State.PROCESSED)
                state = State.STALE
            else if (data.isNotEmpty()) {
                state = State.PROCESSED
                // we dump the data to some persistent store
                data = listOf()
            }
        }

        fun fetchData() {
            if (Random.nextBoolean()) { // data exists upstream
                data = (1..5).map { UUID.randomUUID().toString() } // get the data
                state = State.NEW // reset the state
            }
        }
    }

    fun demoObservable() {
        val dataset = MonitoredDataset("sensor-data-incremental")
        dataset.fetchData()
        dataset.consumeData()
        dataset.fetchData()
        dataset.consumeData()
        dataset.consumeData()
        dataset.consumeData()
    }

    // map - bridge connection between Kotlin and weakly typed data (e.g. JSON)
    class WeakObject(val attributes: Map<String, Any>) {
        val name: String by attributes // this is a delegated property
        val size: Int by attributes
    }

    fun demoMapDelegated() {
        val myDict = WeakObject(mapOf(
            "size" to 123456,
            "name" to "Rock the JVM videos"
        ))
        println("Name of dataset: ${myDict.name}") // actually uses attributes.get("name") as String,beware this can crash if "name" is not a key in the map
        println("Size of dataset: ${myDict.size}")
    }

    @JvmStatic
    fun main(args: Array<String>) {
        demoMapDelegated()
    }
}