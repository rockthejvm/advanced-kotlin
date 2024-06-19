package com.rockthejvm.internals

import com.rockthejvm.internals.InlineFunctions.applyDiscountFast

object InlineFunctions {

    // ecommerce platform
    data class Product(val name: String, var price: Double)

    fun List<Product>.applyDiscount(discountPercentage: Double, operation: (Product) -> Unit) {
        for (product in this) {
            product.price *= (1 - discountPercentage / 100)
            operation(product)
        }
    }

    inline fun List<Product>.applyDiscountFast(discountPercentage: Double, operation: (Product) -> Unit) {
        for (product in this) {
            product.price *= (1 - discountPercentage / 100)
            operation(product)
        }
    }

    fun demoDiscounts() {
        val products = listOf(
            Product("Laptop Pro", 1000.0),
            Product("Phone 25 BIG", 500.0),
            Product("Table 17 Thin", 300.0)
        )

        println("Applying a 10% discount:")
        products.applyDiscountFast(10.0) { product ->
            println("${product.name} is now ${product.price} USD")
        }
        // the inline call is rewritten (= inlined) to:
        /*
            for (product in products) {
                product.price *= (1 - 10.0 / 100)
                println("${product.name} is now ${product.price} USD")
            }
         */
    }

    fun demoPerf() {
        val products = listOf(
            Product("Laptop Pro", 1000.0),
            Product("Phone 25 BIG", 500.0),
            Product("Table 17 Thin", 300.0)
        )

        var productsReduced = 0
        val startNonInline = System.nanoTime()
        repeat(1000000) {
            products.applyDiscount(10.0) { product ->
//                println("${product.name} is now ${product.price} USD")
                productsReduced += 1
            }
        }
        val durationNonInline = System.nanoTime() - startNonInline

        productsReduced = 0
        val startInline = System.nanoTime()
        repeat(1000000) {
            products.applyDiscountFast(10.0) { product ->
//                println("${product.name} is now ${product.price} USD")
                productsReduced += 1
            }
        }
        val durationInline = System.nanoTime() - startInline

        println("Times:")
        println("Non-Inline: $durationNonInline")
        println("Inline: $durationInline")
    }

    /*
        1. code rewritten by the compiler with no overhead (function calls, lambda instatiations)
        2. potential perf benefits, best if the functions are small and repeat in your codebase
     */

    // sometimes useful NOT to inline some lambdas
    inline fun performOperation(
        noinline storeOperation: () -> Unit,
        executeOperation: () -> Unit
    ) {
        // store noinline lambdas as regular values
        GlobalStore.store(storeOperation)

        // execute the other
        executeOperation()
    }

    object GlobalStore {
        private var storedOperation: (() -> Unit)? = null

        fun store(op: () -> Unit) {
            storedOperation = op
        }

        fun executeStored() {
            storedOperation?.invoke()
        }
    }

    fun demoNoInline() {
        performOperation(
            storeOperation = { println("This op should be called later.") },
            executeOperation = { println("This should be called immediately.") }
        )

        // later
        GlobalStore.executeStored()
    }

    @JvmStatic
    fun main(args: Array<String>) {
        demoNoInline()
    }
}