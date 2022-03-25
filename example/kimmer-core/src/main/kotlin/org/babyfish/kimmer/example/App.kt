package org.babyfish.kimmer.example

import org.babyfish.kimmer.example.model.Book
import org.babyfish.kimmer.example.model.by
import org.babyfish.kimmer.new

fun main(args: Array<String>) {
    val book = new(Book::class).by {
        name = "book"
        store().name = "parent"
        authors().add.by {
            name = "child-1"
        }
        authors().add.by {
            name = "child-2"
        }
    }

    val book2 = new(Book::class).by(book) {
        name += "!"
        store().name += "!"
        for (author in authors()) {
            author.name += "!"
        }
    }

    println("Old object is")
    println(book)
    println("New object is")
    println(book2)
}