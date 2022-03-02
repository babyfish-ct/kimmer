package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.new
import org.babyfish.kimmer.sql.ast.common.AbstractTest
import org.babyfish.kimmer.sql.ast.model.Book
import org.babyfish.kimmer.sql.ast.model.BookStore
import org.babyfish.kimmer.sql.ast.model.by
import org.babyfish.kimmer.sql.saveOptions
import org.junit.Ignore
import org.junit.Test
import java.math.BigDecimal
import java.util.*

@Ignore
class EntitiesTest: AbstractTest() {

    @Test
    fun testInsert() {
        sqlClient.entities.save(
            new(Book::class).by {
                name = "NewBook"
                price = BigDecimal(45)
                edition = 1
                store().apply{ 
                    id = UUID.fromString("2fa3955e-3e83-49b9-902e-0465c109c779") 
                }
                authors().add.by {
                    id = UUID.fromString("c14665c8-c689-4ac7-b8cc-6f065b8d835d")
                }
                authors().add.by {
                    id = UUID.fromString("eb4963fd-5223-43e8-b06b-81e6172ee7ae")
                }
            },
            saveOptions {
                insertOnly()
            }
        )
    }

    @Test
    fun testUpdate() {
        sqlClient.entities.save(
            new(Book::class).by {
                name = "NewBook"
                price = BigDecimal(45)
                edition = 1
                store().apply { 
                    id = UUID.fromString("2fa3955e-3e83-49b9-902e-0465c109c779") 
                }
                authors().add.by {
                    id = UUID.fromString("c14665c8-c689-4ac7-b8cc-6f065b8d835d")
                }
                authors().add.by {
                    id = UUID.fromString("eb4963fd-5223-43e8-b06b-81e6172ee7ae")
                }
            },
            saveOptions {
                updateOnly()
            }
        )
    }

    @Test
    fun testSave() {
        sqlClient.entities.save(
            new(Book::class).by {
                name = "NewBook"
                price = BigDecimal(45)
                edition = 1
                store().apply {
                    id = UUID.fromString("2fa3955e-3e83-49b9-902e-0465c109c779")
                }
                authors().add.by {
                    id = UUID.fromString("c14665c8-c689-4ac7-b8cc-6f065b8d835d")
                }
                authors().add.by {
                    id = UUID.fromString("eb4963fd-5223-43e8-b06b-81e6172ee7ae")
                }
            }
        )
    }

    @Test
    fun testSaveCase() {
        sqlClient.entities.save(
            new(Book::class).by {
                name = "NewBook"
                price = BigDecimal(45)
                edition = 1
                store().apply {
                    name = "New store"
                }
                authors().add.by {
                    id = UUID.fromString("c14665c8-c689-4ac7-b8cc-6f065b8d835d")
                }
                authors().add.by {
                    id = UUID.fromString("eb4963fd-5223-43e8-b06b-81e6172ee7ae")
                }
            },
            saveOptions {
                reference(Book::store) {
                    createAttachingObject(BookStore::name)
                }
            }
        )
    }
}