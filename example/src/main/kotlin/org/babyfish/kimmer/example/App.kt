package org.babyfish.kimmer.example

import org.babyfish.kimmer.new

fun main(args: Array<String>) {
    val book = new(Book::class).by {
        name = "book"
        store().name = "parent"
        authors() += new(Author::class).by {
            name = "child-1"
        }
        authors() += new(Author::class).by {
            name = "child-2"
        }
    }
    println("Old object is")
    println(book)

    val book2 = new(Book::class).by(book) {
        name += "!"
        store().name += "!"
        for (author in authors()) {
            author.name += "!"
        }
    }
    println("New object is")
    println(book2)
}