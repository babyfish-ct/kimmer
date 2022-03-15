package org.babyfish.kimmer.sql.mutation

import org.babyfish.kimmer.new
import org.babyfish.kimmer.sql.common.AbstractMutationTest
import org.babyfish.kimmer.sql.model.*
import java.math.BigDecimal
import java.util.*
import kotlin.test.Test

class SaveListTest: AbstractMutationTest() {

    @Test
    fun test() {

        val newId1 = UUID.fromString("56506a3c-801b-4f7d-a41d-e889cdc3d67d")
        val newId2 = UUID.fromString("4749d255-2745-4f6b-99ae-61aa8fd463e0")
        val newStoreId = UUID.fromString("4f351857-6cbc-4aad-ac3a-140a20034a3b")
        val newAuthorId1 = UUID.fromString("6939d7e2-4f84-40b6-b486-f48a9a21af4d")
        val newAuthorId2 = UUID.fromString("c5b73398-e4c0-41be-b091-d3161ac0a52e")
        val newAuthorId3 = UUID.fromString("b4ec96a2-ab48-44ac-a941-b10a9f5bc14c")

        autoIds(Book::class, newId1, newId2)
        autoIds(BookStore::class, newStoreId)
        autoIds(Author::class, newAuthorId1, newAuthorId2, newAuthorId3)

        sqlClient.entities.saveCommand(
            listOf(
                new(Book::class).by {
                    name = "Learning SQL"
                    edition = 1
                    price = BigDecimal(60)
                    store().apply {
                        name = "TURING"
                    }
                    authors().add.by {
                        firstName = "Alan"
                        lastName = "Beaulieu"
                        gender = Gender.MALE
                    }
                    authors().add.by {
                        firstName = "Sylvia"
                        lastName = "Vasilik"
                        gender = Gender.FEMALE
                    }
                },
                new(Book::class).by {
                    name = "SQL Cookbook"
                    edition = 1
                    price = BigDecimal(65)
                    store().apply {
                        name = "TURING"
                    }
                    authors().add.by {
                        firstName = "Anthony"
                        lastName = "Molinaro"
                        gender = Gender.MALE
                    }
                    authors().add.by {
                        firstName = "Sylvia"
                        lastName = "Vasilik"
                        gender = Gender.FEMALE
                    }
                }
            )
        ) {
            keyProps(Book::name, Book::edition)
            reference(Book::store) {
                keyProps(BookStore::name)
                createAttachingObject()
            }
            list(Book::authors) {
                keyProps(Author::firstName, Author::lastName)
                createAttachingObject()
            }
        }.multipleExecuteAndExpect {
            statement {
                sql("select tb_1_.ID, tb_1_.NAME, tb_1_.WEBSITE from BOOK_STORE as tb_1_ where tb_1_.NAME = $1")
                variables("TURING")
            }
            statement {
                sql("insert into BOOK_STORE(ID, NAME) values($1, $2)")
                variables(newStoreId, "TURING")
            }
            statement {
                sql {
                    """select tb_1_.ID, tb_1_.EDITION, tb_1_.NAME, tb_1_.PRICE, tb_1_.STORE_ID 
                        |from BOOK as tb_1_ where tb_1_.NAME = $1 and tb_1_.EDITION = $2""".trimMargin()
                }
                variables("Learning SQL", 1)
            }
            statement {
                sql("insert into BOOK(EDITION, ID, NAME, PRICE, STORE_ID) values($1, $2, $3, $4, $5)")
                variables(1, newId1, "Learning SQL", BigDecimal(60), newStoreId)
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
                variables(" ", "Alan", "Beaulieu")
            }
            statement {
                sql("insert into AUTHOR(FIRST_NAME, GENDER, ID, LAST_NAME) values($1, $2, $3, $4)")
                variables("Alan", "M", newAuthorId1, "Beaulieu")
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
                variables(" ", "Sylvia", "Vasilik")
            }
            statement {
                sql("insert into AUTHOR(FIRST_NAME, GENDER, ID, LAST_NAME) values($1, $2, $3, $4)")
                variables("Sylvia", "F", newAuthorId2, "Vasilik")
            }
            statement {
                sql("select AUTHOR_ID from BOOK_AUTHOR_MAPPING where BOOK_ID = $1")
                variables(newId1)
            }
            statement {
                sql("insert into BOOK_AUTHOR_MAPPING(BOOK_ID, AUTHOR_ID) values($1, $2), ($3, $4)")
                variables(newId1, newAuthorId1, newId1, newAuthorId2)
            }
            statement {
                sql("select tb_1_.ID, tb_1_.NAME, tb_1_.WEBSITE from BOOK_STORE as tb_1_ where tb_1_.NAME = $1")
                variables("TURING")
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
                variables(1, newId2, "SQL Cookbook", BigDecimal(65), newStoreId)
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
                variables(" ", "Anthony", "Molinaro")
            }
            statement {
                sql("insert into AUTHOR(FIRST_NAME, GENDER, ID, LAST_NAME) values($1, $2, $3, $4)")
                variables("Anthony", "M", newAuthorId3, "Molinaro")
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
                variables(" ", "Sylvia", "Vasilik")
            }
            statement {
                sql("select AUTHOR_ID from BOOK_AUTHOR_MAPPING where BOOK_ID = $1")
                variables(newId2)
            }
            statement {
                sql("insert into BOOK_AUTHOR_MAPPING(BOOK_ID, AUTHOR_ID) values($1, $2), ($3, $4)")
                variables(newId2, newAuthorId3, newId2, newAuthorId2)
            }
            result {
                """{
                    |totalAffectedRowCount:6,
                    |type:INSERT,
                    |affectedRowCount:1,
                    |entity:{
                        |"authors":[
                            |{
                                |"firstName":"Alan",
                                |"gender":"MALE",
                                |"lastName":"Beaulieu",
                                |"id":"6939d7e2-4f84-40b6-b486-f48a9a21af4d"
                            |},{
                                |"firstName":"Sylvia",
                                |"gender":"FEMALE",
                                |"lastName":"Vasilik",
                                |"id":"c5b73398-e4c0-41be-b091-d3161ac0a52e"
                            |}
                        |],
                        |"edition":1,
                        |"name":"Learning SQL",
                        |"price":60,
                        |"store":{
                            |"name":"TURING",
                            |"id":"4f351857-6cbc-4aad-ac3a-140a20034a3b"
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
                                        |"id":"4f351857-6cbc-4aad-ac3a-140a20034a3b"
                                    |},
                                    |associationMap:{},
                                    |middleTableChanged:false
                                |}
                            |],
                            |detachedTargets:[],
                            |middleTableInsertedRowCount:0,
                            |middleTableDeletedRowCount:0
                        |},authors:{
                            |totalAffectedRowCount:4,
                            |targets:[
                                |{
                                    |totalAffectedRowCount:2,
                                    |type:INSERT,
                                    |affectedRowCount:1,
                                    |entity:{
                                        |"firstName":"Alan",
                                        |"gender":"MALE",
                                        |"lastName":"Beaulieu",
                                        |"id":"6939d7e2-4f84-40b6-b486-f48a9a21af4d"
                                    |},
                                    |associationMap:{},
                                    |middleTableChanged:true
                                |},{
                                    |totalAffectedRowCount:2,
                                    |type:INSERT,
                                    |affectedRowCount:1,
                                    |entity:{
                                        |"firstName":"Sylvia",
                                        |"gender":"FEMALE",
                                        |"lastName":"Vasilik",
                                        |"id":"c5b73398-e4c0-41be-b091-d3161ac0a52e"
                                    |},
                                    |associationMap:{},
                                    |middleTableChanged:true}
                                |],
                                |detachedTargets:[],
                                |middleTableInsertedRowCount:2,
                                |middleTableDeletedRowCount:0
                            |}
                        |}
                    |}""".trimMargin()
            }
            result {
                """{
                    |totalAffectedRowCount:4,
                    |type:INSERT,
                    |affectedRowCount:1,
                    |entity:{
                        |"authors":[
                            |{
                                |"firstName":"Anthony",
                                |"gender":"MALE",
                                |"lastName":"Molinaro",
                                |"id":"b4ec96a2-ab48-44ac-a941-b10a9f5bc14c"
                            |},{
                                |"firstName":"Sylvia",
                                |"gender":"FEMALE",
                                |"lastName":"Vasilik",
                                |"id":"c5b73398-e4c0-41be-b091-d3161ac0a52e"
                            |}
                        |],
                        |"edition":1,
                        |"name":"SQL Cookbook",
                        |"price":65,
                        |"store":{
                            |"name":"TURING",
                            |"id":"4f351857-6cbc-4aad-ac3a-140a20034a3b"
                        |},
                        |"id":"4749d255-2745-4f6b-99ae-61aa8fd463e0"
                    |},
                    |associationMap:{
                        |store:{
                            |totalAffectedRowCount:0,
                            |targets:[
                                |{
                                    |totalAffectedRowCount:0,
                                    |type:NONE,
                                    |affectedRowCount:0,
                                    |entity:{
                                        |"name":"TURING",
                                        |"id":"4f351857-6cbc-4aad-ac3a-140a20034a3b"
                                    |},
                                    |associationMap:{},
                                    |middleTableChanged:false
                                |}
                            |],
                            |detachedTargets:[],
                            |middleTableInsertedRowCount:0,
                            |middleTableDeletedRowCount:0
                        |},
                        |authors:{
                            |totalAffectedRowCount:3,
                            |targets:[
                                |{
                                    |totalAffectedRowCount:2,
                                    |type:INSERT,
                                    |affectedRowCount:1,
                                    |entity:{
                                        |"firstName":"Anthony",
                                        |"gender":"MALE",
                                        |"lastName":"Molinaro",
                                        |"id":"b4ec96a2-ab48-44ac-a941-b10a9f5bc14c"
                                    |},
                                    |associationMap:{},
                                    |middleTableChanged:true
                                |},{
                                    |totalAffectedRowCount:1,
                                    |type:NONE,
                                    |affectedRowCount:0,
                                    |entity:{
                                        |"firstName":"Sylvia",
                                        |"gender":"FEMALE",
                                        |"lastName":"Vasilik",
                                        |"id":"c5b73398-e4c0-41be-b091-d3161ac0a52e"
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
}