package org.babyfish.kimmer.sql.mutation

import org.babyfish.kimmer.new
import org.babyfish.kimmer.sql.model.*
import org.babyfish.kimmer.sql.common.*
import org.babyfish.kimmer.sql.model.Author
import org.babyfish.kimmer.sql.model.Book
import org.babyfish.kimmer.sql.model.BookStore
import org.babyfish.kimmer.sql.model.Gender
import org.junit.Test
import java.math.BigDecimal
import java.util.*
import kotlin.test.expect

class SaveTest : AbstractMutationTest() {

    @Test
    fun testUpsertNotMatched() {
        val newId = UUID.fromString("56506a3c-801b-4f7d-a41d-e889cdc3d67d")
        sqlClient.entities.saveCommand(
            new(BookStore::class).by {
                id = newId
                name = "TURING"
            }
        ).executeAndExpectResult {
            statement {
                sql("select tb_1_.ID, tb_1_.NAME, tb_1_.WEBSITE from BOOK_STORE as tb_1_ where tb_1_.ID = $1")
                variables(newId)
            }
            statement {
                sql("insert into BOOK_STORE(ID, NAME) values($1, $2)")
                variables(newId, "TURING")
            }
            result {
                """{
                    |totalAffectedRowCount:1,
                    |type:INSERT,
                    |affectedRowCount:1,
                    |entity:{
                        |"name":"TURING",
                        |"id":"56506a3c-801b-4f7d-a41d-e889cdc3d67d"
                    |},
                    |associations:[]
                |}""".trimMargin()
            }
        }
    }

    @Test
    fun testUpsertMatched() {
        sqlClient.entities.saveCommand(
            new(BookStore::class).by {
                id = oreillyId
                name = "TURING"
            }
        ).executeAndExpectResult {
            statement {
                sql("select tb_1_.ID, tb_1_.NAME, tb_1_.WEBSITE from BOOK_STORE as tb_1_ where tb_1_.ID = $1")
                variables(oreillyId)
            }
            statement {
                sql("update BOOK_STORE set NAME = $1 where ID = $2")
                variables("TURING", oreillyId)
            }
            result {
                """{
                    |totalAffectedRowCount:1,
                    |type:UPDATE,
                    |affectedRowCount:1,
                    |entity:{
                        |"name":"TURING",
                        |"id":"d38c10da-6be8-4924-b9b9-5e81899612a0"},
                        |associations:[]
                    |}""".trimMargin()
            }
        }
    }

    @Test
    fun testInsert() {
        val newId = UUID.fromString("56506a3c-801b-4f7d-a41d-e889cdc3d67d")
        sqlClient.entities.saveCommand(
            new(BookStore::class).by {
                id = newId
                name = "TURING"
            }
        ) {
            insertOnly()
        }.executeAndExpectResult {
            statement {
                sql("insert into BOOK_STORE(ID, NAME) values($1, $2)")
                variables(newId, "TURING")
            }
            result {
                """{
                    |totalAffectedRowCount:1,
                    |type:INSERT,
                    |affectedRowCount:1,
                    |entity:{
                        |"name":"TURING",
                        |"id":"56506a3c-801b-4f7d-a41d-e889cdc3d67d"
                    |},
                    |associations:[]
                |}""".trimMargin()
            }
        }
    }

    @Test
    fun testUpdate() {
        sqlClient.entities.saveCommand(
            new(BookStore::class).by {
                id = oreillyId
                name = "TURING"
            }
        ) {
            updateOnly()
        }.executeAndExpectResult {
            statement {
                sql("update BOOK_STORE set NAME = $1 where ID = $2")
                variables("TURING", oreillyId)
            }
            result {
                """{
                    |totalAffectedRowCount:1,
                    |type:UPDATE,
                    |affectedRowCount:1,
                    |entity:{
                        |"name":"TURING",
                        |"id":"d38c10da-6be8-4924-b9b9-5e81899612a0"
                    |},
                    |associations:[]
                |}""".trimMargin()
            }
        }
    }

    @Test
    fun testInsertByDuplicatedKey() {
        val newId = UUID.fromString("56506a3c-801b-4f7d-a41d-e889cdc3d67d")
        autoIds(BookStore::class, newId)
        sqlClient.entities.saveCommand(
            new(BookStore::class).by {
                name = "TURING"
            }
        ) {
            keyProps(BookStore::name)
        }.executeAndExpectResult {
            statement {
                sql("select tb_1_.ID, tb_1_.NAME, tb_1_.WEBSITE from BOOK_STORE as tb_1_ where tb_1_.NAME = $1")
                variables("TURING")
            }
            statement {
                sql("insert into BOOK_STORE(ID, NAME) values($1, $2)")
                variables(newId, "TURING")
            }
            result {
                """{
                    |totalAffectedRowCount:1,
                    |type:INSERT,
                    |affectedRowCount:1,
                    |entity:{
                        |"name":"TURING",
                        |"id":"56506a3c-801b-4f7d-a41d-e889cdc3d67d"
                    |}
                    |,associations:[]
                |}""".trimMargin()
            }
        }
    }

    @Test
    fun testUpdateByDuplicatedKey() {
        sqlClient.entities.saveCommand(
            new(BookStore::class).by {
                name = "O'REILLY"
                website = "http://www.oreilly.com"
            }
        ) {
            keyProps(BookStore::name)
        }.executeAndExpectResult {
            statement {
                sql("select tb_1_.ID, tb_1_.NAME, tb_1_.WEBSITE from BOOK_STORE as tb_1_ where tb_1_.NAME = $1")
                variables("O'REILLY")
            }
            statement {
                sql("update BOOK_STORE set WEBSITE = $1 where ID = $2")
                variables("http://www.oreilly.com", oreillyId)
            }
            result {
                """{
                    |totalAffectedRowCount:1,
                    |type:UPDATE,
                    |affectedRowCount:1,
                    |entity:{
                        |"name":"O'REILLY",
                        |"website":"http://www.oreilly.com",
                        |"id":"d38c10da-6be8-4924-b9b9-5e81899612a0"
                    |},
                    |associations:[]
                |}""".trimMargin()
            }
        }
    }

    @Test
    fun testUpsertNotMatchedWithManyToOne() {
        val manningId = manningId
        val newId = UUID.fromString("56506a3c-801b-4f7d-a41d-e889cdc3d67d")
        autoIds(Book::class, newId)
        sqlClient.entities.saveCommand(
            new(Book::class).by {
                name = "Kotlin in Action"
                edition = 1
                price = BigDecimal(30)
                store().id = manningId
            }
        ) {
            keyProps(Book::name)
        }.executeAndExpectResult {
            statement {
                sql("select tb_1_.ID, tb_1_.EDITION, tb_1_.NAME, tb_1_.PRICE, tb_1_.STORE_ID from BOOK as tb_1_ where tb_1_.NAME = $1")
                variables("Kotlin in Action")
            }
            statement {
                sql("insert into BOOK(EDITION, ID, NAME, PRICE, STORE_ID) values($1, $2, $3, $4, $5)")
                variables(1, newId, "Kotlin in Action", BigDecimal(30), manningId)
            }
            result {
                """{
                    |totalAffectedRowCount:1,
                    |type:INSERT,
                    |affectedRowCount:1,
                    |entity:{
                        |"edition":1,
                        |"name":"Kotlin in Action",
                        |"price":30,
                        |"store":{
                            |"id":"2fa3955e-3e83-49b9-902e-0465c109c779"
                        |},
                        |"id":"56506a3c-801b-4f7d-a41d-e889cdc3d67d"
                    |},
                    |associations:[
                        |{
                            |associationName:"store",
                            |totalAffectedRowCount:0,
                            |targets:[
                                |{
                                    |totalAffectedRowCount:0,
                                    |type:NONE,
                                    |affectedRowCount:0,
                                    |entity:{
                                        |"id":"2fa3955e-3e83-49b9-902e-0465c109c779"
                                    |},associations:[],
                                    |middleTableChanged:false
                                |}
                            |],
                            |detachedTargets:[],
                            |middleTableInsertedRowCount:0,
                            |middleTableDeletedRowCount:0
                        |}
                    |]
                |}""".trimMargin()
            }
        }
    }

    @Test
    fun testUpsertMatchedWithManyToOne() {
        val manningId = manningId
        val newId = UUID.fromString("56506a3c-801b-4f7d-a41d-e889cdc3d67d")
        autoIds(Book::class, newId)
        sqlClient.entities.saveCommand(
            new(Book::class).by {
                name = "Learning GraphQL"
                edition = 3
                store().id = manningId
            }
        ) {
            keyProps(Book::name, Book::edition)
        }.executeAndExpectResult {
            statement {
                sql(
                    "select tb_1_.ID, tb_1_.EDITION, tb_1_.NAME, tb_1_.PRICE, tb_1_.STORE_ID " +
                        "from BOOK as tb_1_ " +
                        "where tb_1_.NAME = $1 and tb_1_.EDITION = $2"
                )
                variables("Learning GraphQL", 3)
            }
            statement {
                sql("update BOOK set STORE_ID = $1 where ID = $2")
                variables(manningId, learningGraphQLId3)
            }
            result {
                """{
                    |totalAffectedRowCount:1,
                    |type:UPDATE,
                    |affectedRowCount:1,
                    |entity:{
                        |"edition":3,
                        |"name":"Learning GraphQL",
                        |"store":{
                            |"id":"2fa3955e-3e83-49b9-902e-0465c109c779"
                        |},
                        |"id":"64873631-5d82-4bae-8eb8-72dd955bfc56"
                    |},
                    |associations:[
                        |{
                            |associationName:"store",
                            |totalAffectedRowCount:0,
                            |targets:[
                                |{
                                    |totalAffectedRowCount:0,
                                    |type:NONE,
                                    |affectedRowCount:0,
                                    |entity:{
                                        |"id":"2fa3955e-3e83-49b9-902e-0465c109c779"
                                    |},
                                    |associations:[],
                                    |middleTableChanged:false
                                |}
                            |],
                            |detachedTargets:[],
                            |middleTableInsertedRowCount:0,
                            |middleTableDeletedRowCount:0
                        |}
                    |]
                |}""".trimMargin()
            }
        }
    }

    @Test
    fun testUpsertNotMatchedWithOneToMany() {

        val newId = UUID.fromString("56506a3c-801b-4f7d-a41d-e889cdc3d67d")
        autoIds(BookStore::class, newId)

        sqlClient.entities.saveCommand(
            new(BookStore::class).by {
                name = "TURING"
                books().add.by {
                    id = learningGraphQLId3
                }
                books().add.by {
                    id = graphQLInActionId3
                }
            }
        ) {
            keyProps(BookStore::name)
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
                sql("select ID from BOOK where STORE_ID = $1")
                variables(newId)
            }
            statement {
                sql("update BOOK set STORE_ID = $1 where ID = $2")
                variables(newId, learningGraphQLId3)
            }
            statement {
                sql("update BOOK set STORE_ID = $1 where ID = $2")
                variables(newId, graphQLInActionId3)
            }
            result {
                """{
                    |totalAffectedRowCount:3,
                    |type:INSERT,
                    |affectedRowCount:1,
                    |entity:{
                        |"books":[
                            |{
                                |"store":{
                                    |"id":"56506a3c-801b-4f7d-a41d-e889cdc3d67d"
                                |},
                                |"id":"64873631-5d82-4bae-8eb8-72dd955bfc56"
                            |},
                            |{
                                |"store":{
                                    |"id":"56506a3c-801b-4f7d-a41d-e889cdc3d67d"
                                |},
                                |"id":"780bdf07-05af-48bf-9be9-f8c65236fecc"
                            |}
                        |],
                        |"name":"TURING",
                        |"id":"56506a3c-801b-4f7d-a41d-e889cdc3d67d"
                    |},
                    |associations:[
                        |{
                            |associationName:"books",
                            |totalAffectedRowCount:2,
                            |targets:[
                                |{
                                    |totalAffectedRowCount:1,
                                    |type:UPDATE,
                                    |affectedRowCount:1,
                                    |entity:{
                                        |"store":{
                                            |"id":"56506a3c-801b-4f7d-a41d-e889cdc3d67d"
                                        |},
                                        |"id":"64873631-5d82-4bae-8eb8-72dd955bfc56"
                                    |},
                                    |associations:[],
                                    |middleTableChanged:false
                                |},{
                                    |totalAffectedRowCount:1,
                                    |type:UPDATE,
                                    |affectedRowCount:1,
                                    |entity:{
                                        |"store":{
                                            |"id":"56506a3c-801b-4f7d-a41d-e889cdc3d67d"
                                        |},
                                        |"id":"780bdf07-05af-48bf-9be9-f8c65236fecc"
                                    |},
                                    |associations:[],
                                    |middleTableChanged:false
                                |}
                            |],
                            |detachedTargets:[],
                            |middleTableInsertedRowCount:0,
                            |middleTableDeletedRowCount:0
                        |}
                    |]
                |}""".trimMargin()
            }
        }
    }

    @Test
    fun testUpsertMatchedWithOneToMany() {

        sqlClient.entities.saveCommand(
            new(BookStore::class).by {
                name = "O'REILLY"
                books().add.by {
                    id = learningGraphQLId1
                }
                books().add.by {
                    id = learningGraphQLId2
                }
                books().add.by {
                    id = learningGraphQLId3
                }
                books().add.by {
                    id = graphQLInActionId3
                }
            }
        ) {
            keyProps(BookStore::name)
        }.executeAndExpectResult {
            statement {
                sql("select tb_1_.ID, tb_1_.NAME, tb_1_.WEBSITE from BOOK_STORE as tb_1_ where tb_1_.NAME = $1")
                variables("O'REILLY")
            }
            statement {
                sql("select ID from BOOK where STORE_ID = $1")
                variables(oreillyId)
            }
            statement {
                sql("update BOOK set STORE_ID = null where ID in ($1, $2, $3, $4, $5, $6)")
                variables {
                    expect(6) { size }
                    expect(true) { effectiveTypeScriptId1 in this }
                    expect(true) { effectiveTypeScriptId2 in this }
                    expect(true) { effectiveTypeScriptId3 in this }
                    expect(true) { programmingTypeScriptId1 in this }
                    expect(true) { programmingTypeScriptId2 in this }
                    expect(true) { programmingTypeScriptId3 in this }
                }
            }
            statement {
                sql("update BOOK set STORE_ID = $1 where ID = $2")
                variables(oreillyId, graphQLInActionId3)
            }
            result {
                """{
                    |totalAffectedRowCount:7,
                    |type:NONE,
                    |affectedRowCount:0,
                    |entity:{
                        |"books":[
                            |{"id":"e110c564-23cc-4811-9e81-d587a13db634"},
                            |{"id":"b649b11b-1161-4ad2-b261-af0112fdd7c8"},
                            |{"id":"64873631-5d82-4bae-8eb8-72dd955bfc56"},
                            |{
                                |"store":{
                                    |"id":"d38c10da-6be8-4924-b9b9-5e81899612a0"
                                |},
                                |"id":"780bdf07-05af-48bf-9be9-f8c65236fecc"
                            |}
                        |],
                        |"name":"O'REILLY",
                        |"id":"d38c10da-6be8-4924-b9b9-5e81899612a0"
                    |},
                    |associations:[
                        |{
                            |associationName:"books",
                            |totalAffectedRowCount:7,
                            |targets:[
                                |{
                                    |totalAffectedRowCount:0,
                                    |type:NONE,
                                    |affectedRowCount:0,
                                    |entity:{"id":"e110c564-23cc-4811-9e81-d587a13db634"},
                                    |associations:[],
                                    |middleTableChanged:false
                                |},{
                                    |totalAffectedRowCount:0,
                                    |type:NONE,
                                    |affectedRowCount:0,
                                    |entity:{"id":"b649b11b-1161-4ad2-b261-af0112fdd7c8"},
                                    |associations:[],
                                    |middleTableChanged:false
                                |},{
                                    |totalAffectedRowCount:0,
                                    |type:NONE,
                                    |affectedRowCount:0,
                                    |entity:{"id":"64873631-5d82-4bae-8eb8-72dd955bfc56"},
                                    |associations:[],
                                    |middleTableChanged:false
                                |},{
                                    |totalAffectedRowCount:1,
                                    |type:UPDATE,
                                    |affectedRowCount:1,
                                    |entity:{
                                        |"store":{"id":"d38c10da-6be8-4924-b9b9-5e81899612a0"},
                                        |"id":"780bdf07-05af-48bf-9be9-f8c65236fecc"
                                    |},associations:[],
                                    |middleTableChanged:false
                                |}
                            |],
                            |detachedTargets:[
                                |{
                                    |totalAffectedRowCount:1,
                                    |type:UPDATE,
                                    |affectedRowCount:1,
                                    |entity:{
                                        |"store":null,
                                        |"id":"8f30bc8a-49f9-481d-beca-5fe2d147c831"
                                    |},
                                    |associations:[],
                                    |middleTableChanged:false
                                |},{
                                    |totalAffectedRowCount:1,
                                    |type:UPDATE,
                                    |affectedRowCount:1,
                                    |entity:{
                                        |"store":null,
                                        |"id":"8e169cfb-2373-4e44-8cce-1f1277f730d1"
                                    |},
                                    |associations:[],
                                    |middleTableChanged:false
                                |},{
                                    |totalAffectedRowCount:1,
                                    |type:UPDATE,
                                    |affectedRowCount:1,
                                    |entity:{
                                        |"store":null,
                                        |"id":"9eded40f-6d2e-41de-b4e7-33a28b11c8b6"
                                    |},
                                    |associations:[],
                                    |middleTableChanged:false
                                |},{
                                    |totalAffectedRowCount:1,
                                    |type:UPDATE,
                                    |affectedRowCount:1,
                                    |entity:{
                                        |"store":null,
                                        |"id":"914c8595-35cb-4f67-bbc7-8029e9e6245a"
                                    |},
                                    |associations:[],
                                    |middleTableChanged:false
                                |},{
                                    |totalAffectedRowCount:1,
                                    |type:UPDATE,
                                    |affectedRowCount:1,
                                    |entity:{
                                        |"store":null,
                                        |"id":"058ecfd0-047b-4979-a7dc-46ee24d08f08"
                                    |},
                                    |associations:[],
                                    |middleTableChanged:false
                                |},{
                                    |totalAffectedRowCount:1,
                                    |type:UPDATE,
                                    |affectedRowCount:1,
                                    |entity:{
                                        |"store":null,
                                        |"id":"782b9a9d-eac8-41c4-9f2d-74a5d047f45a"
                                    |},
                                    |associations:[],
                                    |middleTableChanged:false
                                |}
                            |],
                            |middleTableInsertedRowCount:0,
                            |middleTableDeletedRowCount:0
                        |}
                    |]
                |}""".trimMargin()
            }
        }
    }

    @Test
    fun testUpsertNotMatchedWithManyToMany() {
        val newId = UUID.fromString("56506a3c-801b-4f7d-a41d-e889cdc3d67d")
        autoIds(Book::class, newId)
        sqlClient.entities.saveCommand(
            new(Book::class).by {
                name = "Kotlin in Action"
                edition = 1
                price = BigDecimal(30)
                authors().add.by {
                    id = danId
                }
                authors().add.by {
                    id = borisId
                }
            }
        ) {
            keyProps(Book::name)
        }.executeAndExpectResult {
            statement {
                sql("select tb_1_.ID, tb_1_.EDITION, tb_1_.NAME, tb_1_.PRICE, tb_1_.STORE_ID from BOOK as tb_1_ where tb_1_.NAME = $1")
                variables("Kotlin in Action")
            }
            statement {
                sql("insert into BOOK(EDITION, ID, NAME, PRICE) values($1, $2, $3, $4)")
                variables(1, newId, "Kotlin in Action", BigDecimal(30))
            }
            statement {
                sql("select AUTHOR_ID from BOOK_AUTHOR_MAPPING where BOOK_ID = $1")
                variables(newId)
            }
            statement {
                sql("insert into BOOK_AUTHOR_MAPPING(BOOK_ID, AUTHOR_ID) values($1, $2), ($3, $4)")
                variables(newId, danId, newId, borisId)
            }
            result {
                """{
                    |totalAffectedRowCount:3,
                    |type:INSERT,
                    |affectedRowCount:1,
                    |entity:{
                        |"authors":[
                            |{"id":"c14665c8-c689-4ac7-b8cc-6f065b8d835d"},
                            |{"id":"718795ad-77c1-4fcf-994a-fec6a5a11f0f"}
                        |],
                        |"edition":1,
                        |"name":"Kotlin in Action",
                        |"price":30,
                        |"id":"56506a3c-801b-4f7d-a41d-e889cdc3d67d"
                    |},
                    |associations:[
                        |{
                            |associationName:"authors",
                            |totalAffectedRowCount:2,
                            |targets:[
                                |{
                                    |totalAffectedRowCount:1,
                                    |type:NONE,
                                    |affectedRowCount:0,
                                    |entity:{"id":"c14665c8-c689-4ac7-b8cc-6f065b8d835d"},
                                    |associations:[],
                                    |middleTableChanged:true
                                |},{
                                    |totalAffectedRowCount:1,
                                    |type:NONE,
                                    |affectedRowCount:0,
                                    |entity:{"id":"718795ad-77c1-4fcf-994a-fec6a5a11f0f"},
                                    |associations:[],
                                    |middleTableChanged:true
                                |}
                            |],
                            |detachedTargets:[],
                            |middleTableInsertedRowCount:2,
                            |middleTableDeletedRowCount:0
                        |}
                    |]
                |}""".trimMargin()
            }
        }
    }

    @Test
    fun testUpsertMatchedWithManyToMany() {
        sqlClient.entities.saveCommand(
            new(Book::class).by {
                name = "Learning GraphQL"
                edition = 3
                authors().add.by {
                    id = danId
                }
                authors().add.by {
                    id = borisId
                }
            }
        ) {
            keyProps(Book::name, Book::edition)
        }.executeAndExpectResult {
            statement {
                sql(
                    "select tb_1_.ID, tb_1_.EDITION, tb_1_.NAME, tb_1_.PRICE, tb_1_.STORE_ID " +
                        "from BOOK as tb_1_ " +
                        "where tb_1_.NAME = $1 and tb_1_.EDITION = $2")
                variables("Learning GraphQL", 3)
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
                    |totalAffectedRowCount:4,
                    |type:NONE,
                    |affectedRowCount:0,
                    |entity:{
                        |"authors":[
                            |{"id":"c14665c8-c689-4ac7-b8cc-6f065b8d835d"},
                            |{"id":"718795ad-77c1-4fcf-994a-fec6a5a11f0f"}
                        |],
                        |"edition":3,
                        |"name":"Learning GraphQL",
                        |"id":"64873631-5d82-4bae-8eb8-72dd955bfc56"
                    |},
                    |associations:[
                        |{
                            |associationName:"authors",
                            |totalAffectedRowCount:4,
                            |targets:[
                                |{
                                    |totalAffectedRowCount:1,
                                    |type:NONE,
                                    |affectedRowCount:0,
                                    |entity:{"id":"c14665c8-c689-4ac7-b8cc-6f065b8d835d"},
                                    |associations:[],
                                    |middleTableChanged:true
                                |},
                                |{
                                    |totalAffectedRowCount:1,
                                    |type:NONE,
                                    |affectedRowCount:0,
                                    |entity:{"id":"718795ad-77c1-4fcf-994a-fec6a5a11f0f"},
                                    |associations:[],
                                    |middleTableChanged:true
                                |}
                            |],
                            |detachedTargets:[
                                |{
                                    |totalAffectedRowCount:1,
                                    |type:NONE,
                                    |affectedRowCount:0,
                                    |entity:{"id":"1e93da94-af84-44f4-82d1-d8a9fd52ea94"},
                                    |associations:[],
                                    |middleTableChanged:true
                                |},
                                |{
                                    |totalAffectedRowCount:1,
                                    |type:NONE,
                                    |affectedRowCount:0,
                                    |entity:{"id":"fd6bb6cf-336d-416c-8005-1ae11a6694b5"},
                                    |associations:[],
                                    |middleTableChanged:true
                                |}
                            |],
                            |middleTableInsertedRowCount:2,
                            |middleTableDeletedRowCount:2
                        |}
                    |]
                |}""".trimMargin()
            }
        }
    }

    @Test
    fun testUpsertNotMatchedWithInverseManyToMany() {

        val newId = UUID.fromString("56506a3c-801b-4f7d-a41d-e889cdc3d67d")
        autoIds(Author::class, newId)

        sqlClient.entities.saveCommand(
            new(Author::class).by {
                firstName = "Jim"
                lastName = "Green"
                gender = Gender.MALE
                books().add.by {
                    id = effectiveTypeScriptId3
                }
                books().add.by {
                    id = programmingTypeScriptId3
                }
            }
        ) {
            keyProps(Author::firstName, Author::lastName)
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
                sql("select BOOK_ID from BOOK_AUTHOR_MAPPING where AUTHOR_ID = $1")
                variables(newId)
            }
            statement {
                sql("insert into BOOK_AUTHOR_MAPPING(AUTHOR_ID, BOOK_ID) values($1, $2), ($3, $4)")
                variables(newId, effectiveTypeScriptId3, newId, programmingTypeScriptId3)
            }
            result {
                """{
                    |totalAffectedRowCount:3,
                    |type:INSERT,
                    |affectedRowCount:1,
                    |entity:{
                        |"books":[
                            |{"id":"9eded40f-6d2e-41de-b4e7-33a28b11c8b6"},
                            |{"id":"914c8595-35cb-4f67-bbc7-8029e9e6245a"}
                        |],
                        |"firstName":"Jim",
                        |"gender":"MALE",
                        |"lastName":"Green",
                        |"id":"56506a3c-801b-4f7d-a41d-e889cdc3d67d"
                    |},
                    |associations:[
                        |{
                            |associationName:"books",
                            |totalAffectedRowCount:2,
                            |targets:[
                                |{
                                    |totalAffectedRowCount:1,
                                    |type:NONE,
                                    |affectedRowCount:0,
                                    |entity:{"id":"9eded40f-6d2e-41de-b4e7-33a28b11c8b6"},
                                    |associations:[],
                                    |middleTableChanged:true
                                |},{
                                    |totalAffectedRowCount:1,
                                    |type:NONE,
                                    |affectedRowCount:0,
                                    |entity:{"id":"914c8595-35cb-4f67-bbc7-8029e9e6245a"},
                                    |associations:[],
                                    |middleTableChanged:true}
                                |],
                                |detachedTargets:[],
                                |middleTableInsertedRowCount:2,
                                |middleTableDeletedRowCount:0
                            |}
                        |]
                    |}""".trimMargin()
            }
        }
    }

    @Test
    fun testUpsertMatchedWithInverseManyToMany() {

        sqlClient.entities.saveCommand(
            new(Author::class).by {
                firstName = "Eve"
                lastName = "Procello"
                books().add.by {
                    id = effectiveTypeScriptId3
                }
                books().add.by {
                    id = programmingTypeScriptId3
                }
            }
        ) {
            keyProps(Author::firstName, Author::lastName)
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
                sql("select BOOK_ID from BOOK_AUTHOR_MAPPING where AUTHOR_ID = $1")
                variables(eveId)
            }
            statement {
                sql("delete from BOOK_AUTHOR_MAPPING where AUTHOR_ID = $1 and BOOK_ID in($2, $3, $4)")
                variables {
                    expect(4) { size }
                    expect(eveId) { this[0] }
                    expect(true) { learningGraphQLId1 in this }
                    expect(true) { learningGraphQLId2 in this }
                    expect(true) { learningGraphQLId3 in this }
                }
            }
            statement {
                sql("insert into BOOK_AUTHOR_MAPPING(AUTHOR_ID, BOOK_ID) values($1, $2), ($3, $4)")
                variables(eveId, effectiveTypeScriptId3, eveId, programmingTypeScriptId3)
            }
            result {
                """{
                    |totalAffectedRowCount:5,type:NONE,
                    |affectedRowCount:0,
                    |entity:{
                        |"books":[
                            |{"id":"9eded40f-6d2e-41de-b4e7-33a28b11c8b6"},
                            |{"id":"914c8595-35cb-4f67-bbc7-8029e9e6245a"}],
                            |"firstName":"Eve",
                            |"lastName":"Procello",
                            |"id":"fd6bb6cf-336d-416c-8005-1ae11a6694b5"
                        |},
                        |associations:[
                            |{
                                |associationName:"books",
                                |totalAffectedRowCount:5,
                                |targets:[
                                    |{
                                        |totalAffectedRowCount:1,
                                        |type:NONE,
                                        |affectedRowCount:0,
                                        |entity:{"id":"9eded40f-6d2e-41de-b4e7-33a28b11c8b6"},
                                        |associations:[],
                                        |middleTableChanged:true
                                    |},{
                                        |totalAffectedRowCount:1,
                                        |type:NONE,
                                        |affectedRowCount:0,
                                        |entity:{"id":"914c8595-35cb-4f67-bbc7-8029e9e6245a"},
                                        |associations:[],
                                        |middleTableChanged:true
                                    |}
                                |],
                                |detachedTargets:[
                                    |{
                                        |totalAffectedRowCount:1,
                                        |type:NONE,
                                        |affectedRowCount:0,
                                        |entity:{"id":"e110c564-23cc-4811-9e81-d587a13db634"},
                                        |associations:[],
                                        |middleTableChanged:true
                                    |},{
                                        |totalAffectedRowCount:1,
                                        |type:NONE,
                                        |affectedRowCount:0,
                                        |entity:{"id":"b649b11b-1161-4ad2-b261-af0112fdd7c8"},
                                        |associations:[],
                                        |middleTableChanged:true
                                    |},{
                                        |totalAffectedRowCount:1,
                                        |type:NONE,
                                        |affectedRowCount:0,
                                        |entity:{"id":"64873631-5d82-4bae-8eb8-72dd955bfc56"},
                                        |associations:[],
                                        |middleTableChanged:true
                                    |}
                                |],
                                |middleTableInsertedRowCount:2,
                                |middleTableDeletedRowCount:3
                            |}
                        |]
                    |}""".trimMargin()
            }
        }
    }
}