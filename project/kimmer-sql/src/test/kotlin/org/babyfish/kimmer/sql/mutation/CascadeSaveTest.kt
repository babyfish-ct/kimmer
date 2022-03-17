package org.babyfish.kimmer.sql.mutation

import org.babyfish.kimmer.new
import org.babyfish.kimmer.sql.common.*
import org.babyfish.kimmer.sql.model.*
import org.junit.Test
import java.math.BigDecimal
import java.util.*
import kotlin.test.expect

class CascadeSaveTest : AbstractMutationTest() {

    @Test
    fun testCascadeInsertWithManyToOne() {
        val newId = UUID.fromString("56506a3c-801b-4f7d-a41d-e889cdc3d67d")
        val newStoreId = UUID.fromString("4749d255-2745-4f6b-99ae-61aa8fd463e0")
        autoIds(Book::class, newId)
        autoIds(BookStore::class, newStoreId)
        sqlClient.entities.saveCommand(
            new(Book::class).by {
                name = "Kotlin in Action"
                edition = 1
                price = BigDecimal(40)
                store().apply {
                    name = "TURING"
                    website = "http://www.turing.com"
                }
            }
        ) {
            keyProps(Book::name)
            reference(Book::store) {
                keyProps(BookStore::name)
                createAttachedObjects()
            }
        }.executeAndExpectResult {
            statement {
                sql("select tb_1_.ID, tb_1_.NAME, tb_1_.WEBSITE from BOOK_STORE as tb_1_ where tb_1_.NAME = $1")
                variables("TURING")
            }
            statement {
                sql("insert into BOOK_STORE(ID, NAME, WEBSITE) values($1, $2, $3)")
                variables(newStoreId, "TURING", "http://www.turing.com")
            }
            statement {
                sql {
                    """select tb_1_.ID, tb_1_.EDITION, tb_1_.NAME, tb_1_.PRICE, tb_1_.STORE_ID 
                        |from BOOK as tb_1_ 
                        |where tb_1_.NAME = $1""".trimMargin()
                }
                variables("Kotlin in Action")
            }
            statement {
                sql("insert into BOOK(EDITION, ID, NAME, PRICE, STORE_ID) values($1, $2, $3, $4, $5)")
                variables(1, newId, "Kotlin in Action", BigDecimal(40), newStoreId)
            }
            result {
                """{
                    |totalAffectedRowCount:2,
                    |type:INSERT,
                    |affectedRowCount:1,
                    |entity:{
                        |"edition":1,
                        |"name":"Kotlin in Action",
                        |"price":40,
                        |"store":{
                            |"name":"TURING",
                            |"website":"http://www.turing.com",
                            |"id":"4749d255-2745-4f6b-99ae-61aa8fd463e0"
                        |},
                        |"id":"56506a3c-801b-4f7d-a41d-e889cdc3d67d"
                    |},
                    |associationMap:{
                        |store:{
                            |totalAffectedRowCount:1,
                            |targets:[
                                |{
                                    |totalAffectedRowCount:1,
                                    |type:INSERT,
                                    |affectedRowCount:1,
                                    |entity:{
                                        |"name":"TURING",
                                        |"website":"http://www.turing.com",
                                        |"id":"4749d255-2745-4f6b-99ae-61aa8fd463e0"
                                    |},
                                    |associationMap:{},
                                    |middleTableChanged:false
                                |}
                            |],
                            |detachedTargets:[],
                            |middleTableInsertedRowCount:0,
                            |middleTableDeletedRowCount:0
                            |}
                        |}
                    |}""".trimMargin()
            }
        }
    }

    @Test
    fun testCascadeUpdateWithManyToOne() {
        sqlClient.entities.saveCommand(
            new(Book::class).by {
                id = learningGraphQLId3
                price = BigDecimal(40)
                store().apply {
                    id = oreillyId
                    website = "http://www.oreilly.com"
                }
            }
        ).executeAndExpectResult {
            statement {
                sql("update BOOK_STORE set WEBSITE = $1 where ID = $2")
                variables("http://www.oreilly.com", oreillyId)
            }
            statement {
                sql {
                    """select tb_1_.ID, tb_1_.EDITION, tb_1_.NAME, tb_1_.PRICE, tb_1_.STORE_ID 
                        |from BOOK as tb_1_ 
                        |where tb_1_.ID = $1""".trimMargin()
                }
                variables(learningGraphQLId3)
            }
            statement {
                sql("update BOOK set PRICE = $1 where ID = $2")
                variables(BigDecimal(40), learningGraphQLId3)
            }
            result {
                """{
                    |totalAffectedRowCount:2,
                    |type:UPDATE,
                    |affectedRowCount:1,
                    |entity:{
                        |"price":40,
                        |"store":{
                            |"website":"http://www.oreilly.com",
                            |"id":"d38c10da-6be8-4924-b9b9-5e81899612a0"
                        |},
                        |"id":"64873631-5d82-4bae-8eb8-72dd955bfc56"
                    |},
                    |associationMap:{
                        |store:{
                            |totalAffectedRowCount:1,
                            |targets:[
                                |{
                                    |totalAffectedRowCount:1,
                                    |type:UPDATE,
                                    |affectedRowCount:1,
                                    |entity:{
                                        |"website":"http://www.oreilly.com",
                                        |"id":"d38c10da-6be8-4924-b9b9-5e81899612a0"},
                                        |associationMap:{},
                                        |middleTableChanged:false
                                    |}
                                |],
                                |detachedTargets:[],
                                |middleTableInsertedRowCount:0,
                                |middleTableDeletedRowCount:0
                            |}
                        |}
                    |}""".trimMargin()
            }
        }
    }

    @Test
    fun testCascadeInsertWithOneToMany() {
        val newId = UUID.fromString("56506a3c-801b-4f7d-a41d-e889cdc3d67d")
        val newBookId1 = UUID.fromString("4749d255-2745-4f6b-99ae-61aa8fd463e0")
        val newBookId2 = UUID.fromString("4f351857-6cbc-4aad-ac3a-140a20034a3b")
        autoIds(BookStore::class, newId)
        autoIds(Book::class, newBookId1, newBookId2)
        sqlClient.entities.saveCommand(
            new(BookStore::class).by {
                name = "TURING"
                books().add.by {
                    name = "SQL Cookbook"
                    edition = 1
                    price = BigDecimal(50)
                }
                books().add.by {
                    name = "Learning SQL"
                    edition = 1
                    price = BigDecimal(40)
                }
            }
        ) {
            keyProps(BookStore::name)
            list(BookStore::books) {
                keyProps(Book::name, Book::edition)
                createAttachedObjects()
            }
        }.executeAndExpectResult {
            statement {
                sql("select tb_1_.ID, tb_1_.NAME, tb_1_.WEBSITE from BOOK_STORE as tb_1_ where tb_1_.NAME = $1")
                variables("TURING")
            }
            statement {
                sql("insert into BOOK_STORE(ID, NAME) values($1, $2)")
                variables(newId, "TURING")
            }
            statement {
                sql {
                    """select tb_1_.ID, tb_1_.EDITION, tb_1_.NAME, tb_1_.PRICE, tb_1_.STORE_ID 
                        |from BOOK as tb_1_ 
                        |where tb_1_.NAME = $1 and tb_1_.EDITION = $2""".trimMargin()
                }
                variables("SQL Cookbook", 1)
            }
            statement {
                sql("insert into BOOK(EDITION, ID, NAME, PRICE, STORE_ID) values($1, $2, $3, $4, $5)")
                variables(1, newBookId1, "SQL Cookbook", BigDecimal(50), newId)
            }
            statement {
                sql {
                    """select tb_1_.ID, tb_1_.EDITION, tb_1_.NAME, tb_1_.PRICE, tb_1_.STORE_ID 
                        |from BOOK as tb_1_ 
                        |where tb_1_.NAME = $1 and tb_1_.EDITION = $2""".trimMargin()
                }
                variables("Learning SQL", 1)
            }
            statement {
                sql("insert into BOOK(EDITION, ID, NAME, PRICE, STORE_ID) values($1, $2, $3, $4, $5)")
                variables(1, newBookId2, "Learning SQL", BigDecimal(40), newId)
            }
            statement {
                sql("select ID from BOOK where STORE_ID = $1")
                variables(newId)
            }
            result {
                """{
                    |totalAffectedRowCount:3,
                    |type:INSERT,
                    |affectedRowCount:1,
                    |entity:{
                        |"books":[
                            |{
                                |"edition":1,
                                |"name":"SQL Cookbook",
                                |"price":50,
                                |"store":{"id":"56506a3c-801b-4f7d-a41d-e889cdc3d67d"},
                                |"id":"4749d255-2745-4f6b-99ae-61aa8fd463e0"
                            |},{"
                                |edition":1,
                                |"name":"Learning SQL",
                                |"price":40,
                                |"store":{"id":"56506a3c-801b-4f7d-a41d-e889cdc3d67d"},
                                |"id":"4f351857-6cbc-4aad-ac3a-140a20034a3b"
                            |}
                        |],
                        |"name":"TURING",
                        |"id":"56506a3c-801b-4f7d-a41d-e889cdc3d67d"
                    |},
                    |associationMap:{
                        |books:{
                            |totalAffectedRowCount:2,
                            |targets:[
                                |{
                                    |totalAffectedRowCount:1,
                                    |type:INSERT,
                                    |affectedRowCount:1,
                                    |entity:{
                                        |"edition":1,
                                        |"name":"SQL Cookbook",
                                        |"price":50,
                                        |"store":{"id":"56506a3c-801b-4f7d-a41d-e889cdc3d67d"},
                                        |"id":"4749d255-2745-4f6b-99ae-61aa8fd463e0"
                                    |},
                                    |associationMap:{},
                                    |middleTableChanged:false
                                |},{
                                    |totalAffectedRowCount:1,
                                    |type:INSERT,
                                    |affectedRowCount:1,
                                    |entity:{
                                        |"edition":1,
                                        |"name":"Learning SQL",
                                        |"price":40,
                                        |"store":{"id":"56506a3c-801b-4f7d-a41d-e889cdc3d67d"},
                                        |"id":"4f351857-6cbc-4aad-ac3a-140a20034a3b"
                                    |},
                                    |associationMap:{},
                                    |middleTableChanged:false
                                |}],
                                |detachedTargets:[],
                                |middleTableInsertedRowCount:0,
                                |middleTableDeletedRowCount:0
                            |}
                        |}
                    |}""".trimMargin()
            }
        }
    }

    @Test
    fun testCascadeUpdateWithOneToMany() {
        sqlClient.entities.saveCommand(
            new(BookStore::class).by {
                name = "O'REILLY"
                books().add.by {
                    name = "Learning GraphQL"
                    edition = 3
                    price = BigDecimal(45)
                }
                books().add.by {
                    name = "GraphQL in Action"
                    edition = 3
                    price = BigDecimal(42)
                }
            }
        ) {
            keyProps(BookStore::name)
            list(BookStore::books) {
                keyProps(Book::name, Book::edition)
                createAttachedObjects()
            }
        }.executeAndExpectResult {
            statement {
                sql("select tb_1_.ID, tb_1_.NAME, tb_1_.WEBSITE from BOOK_STORE as tb_1_ where tb_1_.NAME = $1")
                variables("O'REILLY")
            }
            statement {
                sql {
                    """select tb_1_.ID, tb_1_.EDITION, tb_1_.NAME, tb_1_.PRICE, tb_1_.STORE_ID 
                        |from BOOK as tb_1_ 
                        |where tb_1_.NAME = $1 and tb_1_.EDITION = $2""".trimMargin()
                }
                variables("Learning GraphQL", 3)
            }
            statement {
                sql("update BOOK set PRICE = $1 where ID = $2")
                variables(BigDecimal(45), learningGraphQLId3)
            }
            statement {
                sql {
                    """select tb_1_.ID, tb_1_.EDITION, tb_1_.NAME, tb_1_.PRICE, tb_1_.STORE_ID 
                        |from BOOK as tb_1_ 
                        |where tb_1_.NAME = $1 and tb_1_.EDITION = $2""".trimMargin()
                }
                variables("GraphQL in Action", 3)
            }
            statement {
                sql("update BOOK set PRICE = $1, STORE_ID = $2 where ID = $3")
                variables(BigDecimal(42), oreillyId, graphQLInActionId3)
            }
            statement {
                sql("select ID from BOOK where STORE_ID = $1")
                variables(oreillyId)
            }
            statement {
                sql("update BOOK set STORE_ID = null where ID in ($1, $2, $3, $4, $5, $6, $7, $8)")
                variables {
                    expect(8) { size }
                    expect(true) { learningGraphQLId1 in this }
                    expect(true) { learningGraphQLId2 in this }
                    expect(true) { effectiveTypeScriptId1 in this }
                    expect(true) { effectiveTypeScriptId2 in this }
                    expect(true) { effectiveTypeScriptId3 in this }
                    expect(true) { programmingTypeScriptId1 in this }
                    expect(true) { programmingTypeScriptId2 in this }
                    expect(true) { programmingTypeScriptId3 in this }
                }
            }
            result {
                """{
                    |totalAffectedRowCount:10,
                    |type:NONE,
                    |affectedRowCount:0,
                    |entity:{
                        |"books":[
                            |{
                                |"edition":3,
                                |"name":"Learning GraphQL",
                                |"price":45,
                                |"store":{"id":"d38c10da-6be8-4924-b9b9-5e81899612a0"},
                                |"id":"64873631-5d82-4bae-8eb8-72dd955bfc56"
                            |},{
                                |"edition":3,
                                |"name":"GraphQL in Action",
                                |"price":42,
                                |"store":{"id":"d38c10da-6be8-4924-b9b9-5e81899612a0"},
                                |"id":"780bdf07-05af-48bf-9be9-f8c65236fecc"
                            |}
                        |],
                        |"name":"O'REILLY",
                        |"id":"d38c10da-6be8-4924-b9b9-5e81899612a0"
                    |},
                    |associationMap:{
                        |books:{
                            |totalAffectedRowCount:10,
                            |targets:[
                                |{
                                    |totalAffectedRowCount:1,
                                    |type:UPDATE,
                                    |affectedRowCount:1,
                                    |entity:{
                                        |"edition":3,
                                        |"name":"Learning GraphQL",
                                        |"price":45,
                                        |"store":{"id":"d38c10da-6be8-4924-b9b9-5e81899612a0"},
                                        |"id":"64873631-5d82-4bae-8eb8-72dd955bfc56"
                                    |},
                                    |associationMap:{},
                                    |middleTableChanged:false
                                |},{
                                    |totalAffectedRowCount:1,
                                    |type:UPDATE,
                                    |affectedRowCount:1,
                                    |entity:{
                                        |"edition":3,
                                        |"name":"GraphQL in Action",
                                        |"price":42,
                                        |"store":{"id":"d38c10da-6be8-4924-b9b9-5e81899612a0"},
                                        |"id":"780bdf07-05af-48bf-9be9-f8c65236fecc"
                                    |},
                                    |associationMap:{},
                                    |middleTableChanged:false
                                |}
                            |],
                            |detachedTargets:[
                                |{
                                    |totalAffectedRowCount:1,
                                    |type:UPDATE,
                                    |affectedRowCount:1,
                                    |entity:{"store":null,"id":"e110c564-23cc-4811-9e81-d587a13db634"},
                                    |associationMap:{},
                                    |middleTableChanged:false
                                |},{
                                    |totalAffectedRowCount:1,
                                    |type:UPDATE,
                                    |affectedRowCount:1,
                                    |entity:{"store":null,"id":"b649b11b-1161-4ad2-b261-af0112fdd7c8"},
                                    |associationMap:{},
                                    |middleTableChanged:false
                                |},{
                                    |totalAffectedRowCount:1,
                                    |type:UPDATE,
                                    |affectedRowCount:1,
                                    |entity:{"store":null,"id":"8f30bc8a-49f9-481d-beca-5fe2d147c831"},
                                    |associationMap:{},
                                    |middleTableChanged:false
                                |},{
                                    |totalAffectedRowCount:1,
                                    |type:UPDATE,
                                    |affectedRowCount:1,
                                    |entity:{"store":null,"id":"8e169cfb-2373-4e44-8cce-1f1277f730d1"},
                                    |associationMap:{},
                                    |middleTableChanged:false
                                |},{
                                    |totalAffectedRowCount:1,
                                    |type:UPDATE,
                                    |affectedRowCount:1,
                                    |entity:{"store":null,"id":"9eded40f-6d2e-41de-b4e7-33a28b11c8b6"}
                                    |,associationMap:{},
                                    |middleTableChanged:false
                                |},{
                                    |totalAffectedRowCount:1,
                                    |type:UPDATE,
                                    |affectedRowCount:1,
                                    |entity:{"store":null,"id":"914c8595-35cb-4f67-bbc7-8029e9e6245a"},
                                    |associationMap:{},
                                    |middleTableChanged:false
                                |},{
                                    |totalAffectedRowCount:1,
                                    |type:UPDATE,
                                    |affectedRowCount:1,
                                    |entity:{"store":null,"id":"058ecfd0-047b-4979-a7dc-46ee24d08f08"},
                                    |associationMap:{},
                                    |middleTableChanged:false
                                |},{
                                    |totalAffectedRowCount:1,
                                    |type:UPDATE,
                                    |affectedRowCount:1,
                                    |entity:{"store":null,"id":"782b9a9d-eac8-41c4-9f2d-74a5d047f45a"},
                                    |associationMap:{},
                                    |middleTableChanged:false
                                |}
                            |],
                            |middleTableInsertedRowCount:0,
                            |middleTableDeletedRowCount:0
                        |}
                    |}
                |}""".trimMargin()
            }
        }
    }

    @Test
    fun testCascadeInsertWithManyToMany() {

        val newId = UUID.fromString("56506a3c-801b-4f7d-a41d-e889cdc3d67d")
        val newAuthorId1 = UUID.fromString("4749d255-2745-4f6b-99ae-61aa8fd463e0")
        val newAuthorId2 = UUID.fromString("4f351857-6cbc-4aad-ac3a-140a20034a3b")
        autoIds(Book::class, newId)
        autoIds(Author::class, newAuthorId1, newAuthorId2)

        sqlClient.entities.saveCommand(
            new(Book::class).by {
                name = "Kotlin in Action"
                price = BigDecimal(49)
                edition = 1
                authors().add.by {
                    firstName = "Andrey"
                    lastName = "Breslav"
                    gender = Gender.MALE
                }
                authors().add.by {
                    firstName = "Pierre-Yves"
                    lastName = "Saumont"
                    gender = Gender.MALE
                }
            }
        ) {
            keyProps(Book::name, Book::edition)
            list(Book::authors) {
                keyProps(Author::firstName, Author::lastName)
                createAttachedObjects()
            }
        }.executeAndExpectResult {
            statement {
                sql {
                    """select tb_1_.ID, tb_1_.EDITION, tb_1_.NAME, tb_1_.PRICE, tb_1_.STORE_ID 
                        |from BOOK as tb_1_ 
                        |where tb_1_.NAME = $1 and tb_1_.EDITION = $2""".trimMargin()
                }
                variables("Kotlin in Action", 1)
            }
            statement {
                sql("insert into BOOK(EDITION, ID, NAME, PRICE) values($1, $2, $3, $4)")
                variables(1, newId, "Kotlin in Action", BigDecimal(49))
            }
            statement {
                sql {
                    """select 
                            |tb_1_.ID, 
                            |tb_1_.FIRST_NAME, 
                            |concat(tb_1_.FIRST_NAME, $1, tb_1_.LAST_NAME), 
                            |tb_1_.GENDER, 
                            |tb_1_.LAST_NAME 
                        |from AUTHOR as tb_1_ 
                        |where tb_1_.FIRST_NAME = $2 and tb_1_.LAST_NAME = $3""".trimMargin()
                }
                variables(" ", "Andrey", "Breslav")
            }
            statement {
                sql("insert into AUTHOR(FIRST_NAME, GENDER, ID, LAST_NAME) values($1, $2, $3, $4)")
                variables("Andrey", "M", newAuthorId1, "Breslav")
            }
            statement {
                sql {
                    """select 
                            |tb_1_.ID, 
                            |tb_1_.FIRST_NAME, 
                            |concat(tb_1_.FIRST_NAME, $1, tb_1_.LAST_NAME), 
                            |tb_1_.GENDER, 
                            |tb_1_.LAST_NAME 
                        |from AUTHOR as tb_1_ 
                        |where tb_1_.FIRST_NAME = $2 and tb_1_.LAST_NAME = $3""".trimMargin()
                }
                variables(" ", "Pierre-Yves", "Saumont")
            }
            statement {
                sql("insert into AUTHOR(FIRST_NAME, GENDER, ID, LAST_NAME) values($1, $2, $3, $4)")
                variables("Pierre-Yves", "M", newAuthorId2, "Saumont")
            }
            statement {
                sql("select AUTHOR_ID from BOOK_AUTHOR_MAPPING where BOOK_ID = $1")
                variables(newId)
            }
            statement {
                sql("insert into BOOK_AUTHOR_MAPPING(BOOK_ID, AUTHOR_ID) values($1, $2), ($3, $4)")
                variables(newId, newAuthorId1, newId, newAuthorId2)
            }
            result {
                """{
                    |totalAffectedRowCount:5,
                    |type:INSERT,
                    |affectedRowCount:1,
                    |entity:{
                        |"authors":[
                            |{
                                |"firstName":"Andrey",
                                |"gender":"MALE",
                                |"lastName":"Breslav",
                                |"id":"4749d255-2745-4f6b-99ae-61aa8fd463e0"
                            |},{
                                |"firstName":"Pierre-Yves",
                                |"gender":"MALE",
                                |"lastName":"Saumont",
                                |"id":"4f351857-6cbc-4aad-ac3a-140a20034a3b"
                            |}
                        |],
                        |"edition":1,
                        |"name":"Kotlin in Action",
                        |"price":49,
                        |"id":"56506a3c-801b-4f7d-a41d-e889cdc3d67d"
                    |},associationMap:{
                        |authors:{
                            |totalAffectedRowCount:4,
                            |targets:[
                                |{
                                    |totalAffectedRowCount:2,
                                    |type:INSERT,
                                    |affectedRowCount:1,
                                    |entity:{
                                        |"firstName":"Andrey",
                                        |"gender":"MALE",
                                        |"lastName":"Breslav",
                                        |"id":"4749d255-2745-4f6b-99ae-61aa8fd463e0"
                                    |},
                                    |associationMap:{},
                                    |middleTableChanged:true
                                |},{
                                    |totalAffectedRowCount:2,
                                    |type:INSERT,
                                    |affectedRowCount:1,
                                    |entity:{
                                        |"firstName":"Pierre-Yves",
                                        |"gender":"MALE",
                                        |"lastName":"Saumont",
                                        |"id":"4f351857-6cbc-4aad-ac3a-140a20034a3b"
                                    |},
                                    |associationMap:{},
                                    |middleTableChanged:true
                                |}],
                                |detachedTargets:[],
                                |middleTableInsertedRowCount:2,
                                |middleTableDeletedRowCount:0
                            |}
                        |}
                    |}""".trimMargin()
            }
        }
    }

    @Test
    fun testCascadeUpdateWithManyToMany() {
        sqlClient.entities.saveCommand(
            new(Book::class).by {
                name = "Learning GraphQL"
                price = BigDecimal(49)
                edition = 3
                authors().add.by {
                    firstName = "Dan"
                    lastName = "Vanderkam"
                    gender = Gender.FEMALE
                }
                authors().add.by {
                    firstName = "Boris"
                    lastName = "Cherny"
                    gender = Gender.FEMALE
                }
            }
        ) {
            keyProps(Book::name, Book::edition)
            list(Book::authors) {
                keyProps(Author::firstName, Author::lastName)
                createAttachedObjects()
            }
        }.executeAndExpectResult {
            statement {
                sql {
                    """select tb_1_.ID, tb_1_.EDITION, tb_1_.NAME, tb_1_.PRICE, tb_1_.STORE_ID 
                        |from BOOK as tb_1_ 
                        |where tb_1_.NAME = $1 and tb_1_.EDITION = $2""".trimMargin()
                }
                variables("Learning GraphQL", 3)
            }
            statement {
                sql("update BOOK set PRICE = $1 where ID = $2")
                variables(BigDecimal(49), learningGraphQLId3)
            }
            statement {
                sql {
                    """select 
                            |tb_1_.ID, 
                            |tb_1_.FIRST_NAME, 
                            |concat(tb_1_.FIRST_NAME, $1, tb_1_.LAST_NAME), 
                            |tb_1_.GENDER, 
                            |tb_1_.LAST_NAME 
                        |from AUTHOR as tb_1_ 
                        |where tb_1_.FIRST_NAME = $2 and 
                        |tb_1_.LAST_NAME = $3""".trimMargin()
                }
                variables(" ", "Dan", "Vanderkam")
            }
            statement {
                sql("update AUTHOR set GENDER = $1 where ID = $2")
                variables("F", danId)
            }
            statement {
                sql {
                    """select 
                            |tb_1_.ID, 
                            |tb_1_.FIRST_NAME, 
                            |concat(tb_1_.FIRST_NAME, $1, tb_1_.LAST_NAME), 
                            |tb_1_.GENDER, 
                            |tb_1_.LAST_NAME 
                        |from AUTHOR as tb_1_ 
                        |where tb_1_.FIRST_NAME = $2 and 
                        |tb_1_.LAST_NAME = $3""".trimMargin()
                }
                variables(" ", "Boris", "Cherny")
            }
            statement {
                sql("update AUTHOR set GENDER = $1 where ID = $2")
                variables("F", borisId)
            }
            statement {
                sql("select AUTHOR_ID from BOOK_AUTHOR_MAPPING where BOOK_ID = $1")
                variables(learningGraphQLId3)
            }
            statement {
                sql("delete from BOOK_AUTHOR_MAPPING where BOOK_ID = $1 and AUTHOR_ID in($2, $3)")
                variables {
                    expect(3) { size }
                    expect(learningGraphQLId3) { this[0] }
                    expect(true) { eveId in this }
                    expect(true) { alexId in this }
                }
            }
            statement {
                sql("insert into BOOK_AUTHOR_MAPPING(BOOK_ID, AUTHOR_ID) values($1, $2), ($3, $4)")
                variables(learningGraphQLId3, danId, learningGraphQLId3, borisId)
            }
            result {
                """{
                    |totalAffectedRowCount:7,
                    |type:UPDATE,
                    |affectedRowCount:1,
                    |entity:{
                        |"authors":[
                            |{
                                |"firstName":"Dan",
                                |"gender":"FEMALE",
                                |"lastName":"Vanderkam",
                                |"id":"c14665c8-c689-4ac7-b8cc-6f065b8d835d"
                            |},{
                                |"firstName":"Boris",
                                |"gender":"FEMALE",
                                |"lastName":"Cherny",
                                |"id":"718795ad-77c1-4fcf-994a-fec6a5a11f0f"
                            |}
                        |],
                        |"edition":3,
                        |"name":"Learning GraphQL",
                        |"price":49,
                        |"id":"64873631-5d82-4bae-8eb8-72dd955bfc56"
                    |},
                    |associationMap:{
                        |authors:{
                            |totalAffectedRowCount:6,
                            |targets:[
                                |{
                                    |totalAffectedRowCount:2,
                                    |type:UPDATE,
                                    |affectedRowCount:1,
                                    |entity:{
                                        |"firstName":"Dan",
                                        |"gender":"FEMALE",
                                        |"lastName":"Vanderkam",
                                        |"id":"c14665c8-c689-4ac7-b8cc-6f065b8d835d"
                                    |},
                                    |associationMap:{},
                                    |middleTableChanged:true
                                |},{
                                    |totalAffectedRowCount:2,
                                    |type:UPDATE,
                                    |affectedRowCount:1,
                                    |entity:{
                                        |"firstName":"Boris",
                                        |"gender":"FEMALE",
                                        |"lastName":"Cherny",
                                        |"id":"718795ad-77c1-4fcf-994a-fec6a5a11f0f"
                                    |},
                                    |associationMap:{},
                                    |middleTableChanged:true
                                |}
                            |],detachedTargets:[
                                |{
                                    |totalAffectedRowCount:1,
                                    |type:NONE,
                                    |affectedRowCount:0,
                                    |entity:{"id":"1e93da94-af84-44f4-82d1-d8a9fd52ea94"},
                                    |associationMap:{},
                                    |middleTableChanged:true
                                |},{
                                    |totalAffectedRowCount:1,
                                    |type:NONE,
                                    |affectedRowCount:0,
                                    |entity:{"id":"fd6bb6cf-336d-416c-8005-1ae11a6694b5"},
                                    |associationMap:{},
                                    |middleTableChanged:true
                                |}
                            |],
                            |middleTableInsertedRowCount:2,
                            |middleTableDeletedRowCount:2}
                        |}
                    |}""".trimMargin()
            }
        }
    }

    @Test
    fun testCascadeInsertWithInverseManyToMany() {

        val newId = UUID.fromString("56506a3c-801b-4f7d-a41d-e889cdc3d67d")
        val newBookId1 = UUID.fromString("4749d255-2745-4f6b-99ae-61aa8fd463e0")
        val newBookId2 = UUID.fromString("4f351857-6cbc-4aad-ac3a-140a20034a3b")
        autoIds(Author::class, newId)
        autoIds(Book::class, newBookId1, newBookId2)

        sqlClient.entities.saveCommand(
            new(Author::class).by {
                firstName = "Jim"
                lastName = "Green"
                gender = Gender.MALE
                books().add.by {
                    name = "Learning SQL"
                    edition = 1
                    price = BigDecimal(30)
                }
                books().add.by {
                    name = "SQL Cookbook"
                    edition = 1
                    price = BigDecimal(40)
                }
            }
        ) {
            keyProps(Author::firstName, Author::lastName)
            list(Author::books) {
                keyProps(Book::name, Book::edition)
                createAttachedObjects()
            }
        }.executeAndExpectResult {
            statement {
                sql {
                    """select 
                            |tb_1_.ID, 
                            |tb_1_.FIRST_NAME, 
                            |concat(tb_1_.FIRST_NAME, $1, tb_1_.LAST_NAME), 
                            |tb_1_.GENDER, 
                            |tb_1_.LAST_NAME 
                        |from AUTHOR as tb_1_ 
                        |where tb_1_.FIRST_NAME = $2 and tb_1_.LAST_NAME = $3""".trimMargin()
                }
                variables(" ", "Jim", "Green")
            }
            statement {
                sql("insert into AUTHOR(FIRST_NAME, GENDER, ID, LAST_NAME) values($1, $2, $3, $4)")
                variables("Jim", "M", newId, "Green")
            }
            statement {
                sql {
                    """select tb_1_.ID, tb_1_.EDITION, tb_1_.NAME, tb_1_.PRICE, tb_1_.STORE_ID 
                        |from BOOK as tb_1_ 
                        |where tb_1_.NAME = $1 and tb_1_.EDITION = $2""".trimMargin()
                }
                variables("Learning SQL", 1)
            }
            statement {
                sql("insert into BOOK(EDITION, ID, NAME, PRICE) values($1, $2, $3, $4)")
                variables(1, newBookId1, "Learning SQL", BigDecimal(30))
            }
            statement {
                sql {
                    """select tb_1_.ID, tb_1_.EDITION, tb_1_.NAME, tb_1_.PRICE, tb_1_.STORE_ID 
                        |from BOOK as tb_1_ 
                        |where tb_1_.NAME = $1 and tb_1_.EDITION = $2""".trimMargin()
                }
                variables("SQL Cookbook", 1)
            }
            statement {
                sql("insert into BOOK(EDITION, ID, NAME, PRICE) values($1, $2, $3, $4)")
                variables(1, newBookId2, "SQL Cookbook", BigDecimal(40))
            }
            statement {
                sql("select BOOK_ID from BOOK_AUTHOR_MAPPING where AUTHOR_ID = $1")
                variables(newId)
            }
            statement {
                sql("insert into BOOK_AUTHOR_MAPPING(AUTHOR_ID, BOOK_ID) values($1, $2), ($3, $4)")
                variables(newId, newBookId1, newId, newBookId2)
            }
            result {
                """{
                    |totalAffectedRowCount:5,
                    |type:INSERT,
                    |affectedRowCount:1,
                    |entity:{
                        |"books":[
                            |{
                                |"edition":1,
                                |"name":"Learning SQL",
                                |"price":30,
                                |"id":"4749d255-2745-4f6b-99ae-61aa8fd463e0"
                            |},{
                                |"edition":1,
                                |"name":"SQL Cookbook",
                                |"price":40,
                                |"id":"4f351857-6cbc-4aad-ac3a-140a20034a3b"
                            |}
                        |],
                        |"firstName":"Jim",
                        |"gender":"MALE",
                        |"lastName":"Green",
                        |"id":"56506a3c-801b-4f7d-a41d-e889cdc3d67d"
                    |},
                    |associationMap:{
                        |books:{
                            |totalAffectedRowCount:4,
                            |targets:[
                                |{
                                    |totalAffectedRowCount:2,
                                    |type:INSERT,
                                    |affectedRowCount:1,
                                    |entity:{
                                        |"edition":1,
                                        |"name":"Learning SQL",
                                        |"price":30,
                                        |"id":"4749d255-2745-4f6b-99ae-61aa8fd463e0"
                                    |},
                                    |associationMap:{},
                                    |middleTableChanged:true
                                |},{
                                    |totalAffectedRowCount:2,
                                    |type:INSERT,
                                    |affectedRowCount:1,
                                    |entity:{
                                        |"edition":1,
                                        |"name":"SQL Cookbook",
                                        |"price":40,
                                        |"id":"4f351857-6cbc-4aad-ac3a-140a20034a3b"
                                    |},
                                    |associationMap:{},
                                    |middleTableChanged:true
                                |}
                            |],
                            |detachedTargets:[],
                            |middleTableInsertedRowCount:2,
                            |middleTableDeletedRowCount:0}
                        |}
                    |}""".trimMargin()
            }
        }
    }

    @Test
    fun testCascadeUpdateWithInverseManyToMany() {
        sqlClient.entities.saveCommand(
            new(Author::class).by {
                firstName = "Eve"
                lastName = "Procello"
                gender = Gender.FEMALE
                books().add.by {
                    name = "Learning GraphQL"
                    edition = 3
                    price = BigDecimal(35)
                }
                books().add.by {
                    name = "GraphQL in Action"
                    edition = 3
                    price = BigDecimal(28)
                }
            }
        ) {
            keyProps(Author::firstName, Author::lastName)
            list(Author::books) {
                keyProps(Book::name, Book::edition)
                createAttachedObjects()
            }
        }.executeAndExpectResult {
            statement {
                sql {
                    """select 
                            |tb_1_.ID, 
                            |tb_1_.FIRST_NAME, 
                            |concat(tb_1_.FIRST_NAME, $1, tb_1_.LAST_NAME), 
                            |tb_1_.GENDER, 
                            |tb_1_.LAST_NAME 
                        |from AUTHOR as tb_1_ 
                        |where tb_1_.FIRST_NAME = $2 and tb_1_.LAST_NAME = $3""".trimMargin()
                }
                variables(" ", "Eve", "Procello")
            }
            statement {
                sql("update AUTHOR set GENDER = $1 where ID = $2")
                variables("F", eveId)
            }
            statement {
                sql {
                    """select tb_1_.ID, tb_1_.EDITION, tb_1_.NAME, tb_1_.PRICE, tb_1_.STORE_ID 
                        |from BOOK as tb_1_ 
                        |where tb_1_.NAME = $1 and tb_1_.EDITION = $2""".trimMargin()
                }
                variables("Learning GraphQL", 3)
            }
            statement {
                sql("update BOOK set PRICE = $1 where ID = $2")
                variables(BigDecimal(35), learningGraphQLId3)
            }
            statement {
                sql {
                    """select tb_1_.ID, tb_1_.EDITION, tb_1_.NAME, tb_1_.PRICE, tb_1_.STORE_ID 
                        |from BOOK as tb_1_ 
                        |where tb_1_.NAME = $1 and tb_1_.EDITION = $2""".trimMargin()
                }
                variables("GraphQL in Action", 3)
            }
            statement {
                sql("update BOOK set PRICE = $1 where ID = $2")
                variables(BigDecimal(28), graphQLInActionId3)
            }
            statement {
                sql("select BOOK_ID from BOOK_AUTHOR_MAPPING where AUTHOR_ID = $1")
                variables(eveId)
            }
            statement {
                sql("delete from BOOK_AUTHOR_MAPPING where AUTHOR_ID = $1 and BOOK_ID in($2, $3)")
                variables {
                    expect(3) { size }
                    expect(eveId) { this[0] }
                    expect(true) { learningGraphQLId1 in this }
                    expect(true) { learningGraphQLId2 in this }
                }
            }
            statement {
                sql("insert into BOOK_AUTHOR_MAPPING(AUTHOR_ID, BOOK_ID) values($1, $2)")
                variables(eveId, graphQLInActionId3)
            }
            result {
                """{
                    |totalAffectedRowCount:6,
                    |type:UPDATE,
                    |affectedRowCount:1,
                    |entity:{
                        |"books":[
                            |{
                                |"edition":3,
                                |"name":"Learning GraphQL",
                                |"price":35,
                                |"id":"64873631-5d82-4bae-8eb8-72dd955bfc56"
                            |},{
                                |"edition":3,
                                |"name":"GraphQL in Action",
                                |"price":28,
                                |"id":"780bdf07-05af-48bf-9be9-f8c65236fecc"
                            |}
                        |],
                        |"firstName":"Eve",
                        |"gender":"FEMALE",
                        |"lastName":"Procello",
                        |"id":"fd6bb6cf-336d-416c-8005-1ae11a6694b5"
                    |},
                    |associationMap:{
                        |books:{
                            |totalAffectedRowCount:5,
                            |targets:[
                                |{
                                    |totalAffectedRowCount:1,
                                    |type:UPDATE,
                                    |affectedRowCount:1,
                                    |entity:{
                                        |"edition":3,
                                        |"name":"Learning GraphQL",
                                        |"price":35,"id":"64873631-5d82-4bae-8eb8-72dd955bfc56"
                                    |},
                                    |associationMap:{},
                                    |middleTableChanged:false
                                |},{
                                    |totalAffectedRowCount:2,
                                    |type:UPDATE,
                                    |affectedRowCount:1,
                                    |entity:{
                                        |"edition":3,
                                        |"name":"GraphQL in Action",
                                        |"price":28,
                                        |"id":"780bdf07-05af-48bf-9be9-f8c65236fecc"
                                    |},
                                    |associationMap:{},
                                    |middleTableChanged:true
                                |}
                            |],
                            |detachedTargets:[
                                |{
                                    |totalAffectedRowCount:1,
                                    |type:NONE,
                                    |affectedRowCount:0,
                                    |entity:{"id":"e110c564-23cc-4811-9e81-d587a13db634"},
                                    |associationMap:{},
                                    |middleTableChanged:true
                                |},{
                                    |totalAffectedRowCount:1,
                                    |type:NONE,
                                    |affectedRowCount:0,
                                    |entity:{"id":"b649b11b-1161-4ad2-b261-af0112fdd7c8"},
                                    |associationMap:{},
                                    |middleTableChanged:true
                                |}
                            |],
                            |middleTableInsertedRowCount:1,
                            |middleTableDeletedRowCount:2}
                        |}
                    |}""".trimMargin()
            }
        }
    }
}