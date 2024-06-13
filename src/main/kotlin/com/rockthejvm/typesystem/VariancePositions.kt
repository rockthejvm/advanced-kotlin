package com.rockthejvm.typesystem

object VariancePositions {

    abstract class Animal
    class Dog: Animal()
    class Cat: Animal()
    class Crocodile: Animal()

    // out = Covariant, in = Contravariant
    // this is illegal
    //    class Vet<in A>(val favoriteAnimal: A) {
    //        fun treat(animal: A): Boolean = true
    //    }

    /*
        assume that this was legal
        class Vet<in A>(val favoriteAnimal: A)

        val garfield = Cat()
        val lassie = Dog()
        val theVet: Vet<Animal> = Vet<Animal>(garfield)
        // contravariance
        val dogVet: Vet<Dog> = theVet // Vet<supertype of Dog>
        val favAnimal = dogVet.favoriteAnimal // guaranteed to be a Dog, is actually a Cat

        types of properties (val or var) are in "out" (aka covariant) position
        "in" types cannot be placed in "out" positions
     */

    // class MutableContainer<out A>(var contents: A) // var properties are ALSO in contravariant ("in") position

    /*
        class Liquid
        class Water: Liquid()
        class Gasoline: Liquid()

        val container: MutableContainer<Liquid> = MutableContainer<Water>(Water())
        container.contents = Gasoline() // guarantee that I can write any Liquid inside, but have to keep it to Water

        types of vars are in "in" position (aka contravariant)
        => must be INVARIANT
     */

    //    class LList<out A> {
    // illegal here
    //        fun add(elem: A): LList<A> = TODO()
    //    }

    /*
        val myList: LList<Animal> = LList<Dog>()
        val newList = myList.add(Crocodile()) // guaranteed to be able to add any animal, BUT have to guarantee just Dog

        method arg types are in "in" (aka contravariant) position
        => cannot use covariant types in method args
     */

    //    class Vet<in A> {
    //        fun rescueAnimal(): A = TODO()
    //    }

    /*
        assume this compiled:
        class Vet<in A> {
            fun rescueAnimal(): A = TODO()
        }

        val myVet: Vet<Animal> = object: Vet<Animal> {
            override fun rescueAnimal(): Animal = Cat()
        }
        val dogVet: Vet<Dog> = myVet // legal because of contravariance
        val rescuedDog = dogVet.rescueAnimal // guaranteed to return a Dog, but returns a Cat

        method return types are in "out" (aka covariant) position
     */

    /*
        solve variance positions problems
     */
    // 1 - consume elements in a covariant type
    abstract class LList<out A>
    data object EmptyList: LList<Nothing>()
    data class Cons<out A>(val head: A, val tail: LList<A>): LList<A>()

    // how do we add an element?
    // [lassie, hachi, laika].add(togo) => List<Dog>
    // [lassie, hachi, laika].add(garfield) => [lassie, hachi, laika, garfield] => List<Animal>
    // [lassie, hachi, laika].add(45) => [lassie, hachi, laika, 45] => List<Any>
    // solution = widening the type
    fun <B, A:B> LList<A>.add(elem: B): LList<B> = Cons(elem, this)

    // 2 - return elements in a contravariant type
    // solution = narrow the type
    abstract class Vehicle
    open class Car: Vehicle()
    class Supercar: Car()

    class RepairShop<in A: Vehicle> {
        fun <B:A> repair(elem: B): B = elem
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val myList: LList<Dog> = EmptyList
        val dogs = myList.add(Dog()).add(Dog()) // LList<Dog>
        val animals = dogs.add(Cat()) // LList<Animal>

        // contravariant problem
        val myRepairShop: RepairShop<Car> = RepairShop<Vehicle>() // contravariance
        val myBeatUpVW = Car()
        val damagedFerrari = Supercar()

        val freshCar = myRepairShop.repair(myBeatUpVW) // Car
        val freshFerrari = myRepairShop.repair(damagedFerrari) // Supercar
    }
}