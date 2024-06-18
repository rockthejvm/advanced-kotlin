package com.rockthejvm.internals

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.random.Random

object Contracts {

    // contracts = code that helps the compiler make some deductions

    // info to the compiler about the return value of a function
    @OptIn(ExperimentalContracts::class)
    fun containsJustDigits(str: String?): Boolean {
        contract {
            // small DSL for testing things about this function
            returns(true) implies (str != null)
        }
        return str?.all { it.isDigit() } ?: false
    }

    fun demoNullableString() {
        val maybeString: String? =
            if (Random.nextBoolean()) "123456"
            else null

        println("I haz maybe string $maybeString")
        if (containsJustDigits(maybeString)) { // containsJustDigits(...) == true AND maybeString != null
            println("String is just a number, I want the length: ${maybeString.length}")
        }
    }

    // more complex example
    open class User(open val username: String, open val email: String) {
        @OptIn(ExperimentalContracts::class)
        fun isValidAdmin(): Boolean {
            contract {
                returns(true) implies (this@User is Admin)
            }
            return this is Admin && email.endsWith("@rockthejvmadmins.com")
        }
    }

    class Admin(override val username: String, override val email: String, val permissions: List<String>): User(username, email) {
        fun purgeData() =
            println("ALL DATA REMOVED.")
    }

    fun attemptAdminTasks(user: User) {
        if (user.isValidAdmin()) { // if it's true => user is Admin
            // user is Admin in this scope
            println("Running admin tasks...")
            user.purgeData()
        } else {
            println("User ${user.username} is not a valid admin")
        }
    }

    fun demoAdmin() {
        val admin = Admin("adminuser", "admin@rockthejvmadmins.com", listOf("READ", "WRITE"))
        attemptAdminTasks(admin)

        val simpleUser = User("fakeAdmin", "fake@rockthejvmadmins.com")
        attemptAdminTasks(simpleUser)
    }

    // 2 - callsInPlace - guarantee that a lambda was invoked in a certain way

    // comes from some external library
    open class Resource {
        fun open() {
            println("Resource opened")
        }

        fun close() {
            println("Resource closed")
        }

        fun getMotivation(): String {
            println("Resource was accessed")
            return "BELIEVE IN YOURSELF!"
        }
    }

    @OptIn(ExperimentalContracts::class)
    fun <R: Resource, A> R.bracket(block: (R) -> A): A { // "bracket" pattern
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        }

        this.open()
        try {
            return block(this)
        } finally {
            this.close()
        }
    }

    fun demoResource() {
        val resource = Resource()
        val result: String
        resource.bracket {
            // this code runs just once
            result = it.getMotivation() // allowed because this is a ONE-TIME assignment
        }
        println("I got what I wanted: $result")
    }

    /*
        Exercise
     */
    class GuardianService {
        init {
            println("Service initialized.")
        }

        fun monitorSystem() {
            println("I shall watch over you...")
        }
    }

    object GuardianResource {
        private var resource: GuardianService? = null

        @OptIn(ExperimentalContracts::class)
        fun getOrCreate(initializer: () -> GuardianService): GuardianService {
            contract {
                callsInPlace(initializer, InvocationKind.AT_MOST_ONCE)
            }
            if (resource == null) {
                resource = initializer()
            }
            return resource!!
        }
    }

    fun demoGuardian() {
        val guardianResource = GuardianResource

        val guardian = guardianResource.getOrCreate {
            println("creating guardian")
            GuardianService()
        }
        guardian.monitorSystem()

        val guardian_v2 = guardianResource.getOrCreate {
            println("YOU SHOULD NOT SEE THIS") // this should NOT be printed!
            GuardianService()
        }
        guardian_v2.monitorSystem()
        println(guardian == guardian_v2)
    }

    @JvmStatic
    fun main(args: Array<String>) {
        demoGuardian()
    }
}