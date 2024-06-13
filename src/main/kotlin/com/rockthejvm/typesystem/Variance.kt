package com.rockthejvm.typesystem

object Variance {

    abstract class Pet
    class Dog(name: String): Pet()
    class Cat(name: String): Pet()

    // Dog extends Pet => List<Dog> "extends" List<Pet>?
    // Variance question for the List type: A extends B => List<A> extends List<B>?
    // yes => List is a COVARIANT TYPE
    // Dog is a Pet => List<Dog> is a List<Pet>

    val lassie = Dog("Lassie")
    val hachi = Dog("Hachi")
    val laika = Dog("Laika")
    val myDogs: List<Dog> = listOf(lassie, hachi, laika)
    val myPets: List<Pet> = myDogs // legal

    // COVARIANT generic types
    class MyList<out A> // <out A> => COVARIANT IN A
    val aListOfPets: MyList<Pet> = MyList<Cat>() // legal

    // no - INVARIANT
    interface Combiner<A> { // semigroup
        fun combine(x: A, y: A): A
    }

    // java standard library - all Java generics are invariant
    // val aJavaList: java.util.List<Pet> = java.util.ArrayList<Dog>() // type mismatch

    // HELL NO - CONTRAVARIANCE
    // Dog is a Pet, then Vet<Pet> is a Vet<Dog>
    class Vet<in A> {
        fun heal(pet: A): Boolean = true
    }

    val myVet: Vet<Dog> = Vet<Pet>()

    // covariant types "produce" or "get" elements => "output" elements
    // contravariant types "consume" or "act on" elements => "input" elements

    /*
        Rule of thumb, how to decide variance:
        - if it "outputs" elements => COVARIANT (out)
        - if it "consumes" elements => CONTRAVARIANT (in)
        - otherwise, INVARIANT (no modifier)
     */

    /*
        Exercise: add variance modifiers
     */
    class RandomGenerator<out A>
    class MyOption<out A> // holds at most one item
    class JSONSerializer<in A> // turns values of type A into JSONs
    interface MyFunction<in A, out B> // takes a value of type A and returns a B

    /*
        Exercise:
        1. add variance modifiers where appropriate
        2. EmptyList should be empty regardless of the type - can you make it an object?
        3. add an "add" method to the generic list type
            fun add(element: A): LList<A>
     */
    abstract class LList<out A> { // "produces" elements, I want to "consume" an element
        abstract fun head(): A // first item in the list
        abstract fun tail(): LList<A> // the rest of the list without the head
    }

    fun <B, A:B> LList<A>.add(elem: B): LList<B> =
        Cons(elem, this)

    data object EmptyList: LList<Nothing>() { // is a subtype of ALL POSSIBLE LISTS!
        override fun head(): Nothing = throw NoSuchElementException()
        override fun tail(): LList<Nothing> = throw NoSuchElementException()
    }

    data class Cons<out A>(val h: A, val t: LList<A>): LList<A>() {
        override fun head(): A = h
        override fun tail(): LList<A> = t
    }

    val myLPets: LList<Pet> = EmptyList
    val myStrings: LList<String> = EmptyList

    @JvmStatic
    fun main(args: Array<String>) {

    }
}