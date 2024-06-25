package com.rockthejvm.internals

import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation

object ReflectionAnnotations {

    // annotations = metadata for other declarations

    // meta-annotations
    @Target(AnnotationTarget.CLASS) // can be attached to class/abstract class/interface...
    @Retention(AnnotationRetention.RUNTIME)
    // SOURCE - only inspected by source tools, e.g. compiler + plugins
    // BINARY - copied to the binary
    // RUNTIME - copied to the binary, AND can be inspected via reflection
    annotation class TestAnnotation(val value: String)

    @TestAnnotation(value = "Example") // TestAnnotation instance per class declaration
    class AnnotatedClass {
        // @TestAnnotation("A property") // illegal - this annotation can only be used for classes
        val aProperty: Int = 0
    }

    @TestAnnotation("an interface") // legal
    interface MyInterface
    @TestAnnotation("an abstract class")
    abstract class MyAbstractClass
    @TestAnnotation("an object")
    object MyObject

    // example: generate table declarations for a data class

    // sits in the library
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Table(val name: String)

    @Target(AnnotationTarget.PROPERTY)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Column(val name: String)

    fun generateTableStatement(clazz: KClass<*>): String? {
        val tableAnnotation: Table? = clazz.findAnnotation<Table>()
        val tableName = tableAnnotation?.name ?: return null

        val columns = clazz.declaredMemberProperties.mapNotNull { prop ->
            val columnAnnotation = prop.findAnnotation<Column>()
            val colName = columnAnnotation?.name
            val colType = when (prop.returnType.classifier) {
                Int::class -> "INTEGER"
                String::class -> "TEXT"
                else -> null
            }

            if (colName == null || colType == null) null
            else "$colName $colType"
        }

        return "CREATE TABLE $tableName ${columns.joinToString(", ", "(", ")")};"
    }

    // user-space
    @Table(name = "users")
    data class User(
        @Column(name = "user_id") val id: Int,
        @Column(name = "user_name") val name: String,
        @Column(name = "user_age") val age: Int
    )

    @JvmStatic
    fun main(args: Array<String>) {
        val createTableStatement = generateTableStatement(User::class)
        println(createTableStatement)
    }
}