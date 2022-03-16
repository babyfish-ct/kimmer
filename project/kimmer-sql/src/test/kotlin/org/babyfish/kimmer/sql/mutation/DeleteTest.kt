package org.babyfish.kimmer.sql.mutation

import org.babyfish.kimmer.sql.ExecutionException
import org.babyfish.kimmer.sql.common.*
import org.babyfish.kimmer.sql.meta.config.OnDeleteAction
import org.babyfish.kimmer.sql.model.Author
import org.babyfish.kimmer.sql.model.Book
import org.babyfish.kimmer.sql.model.BookStore
import java.util.*
import kotlin.test.Test

class DeleteTest : AbstractMutationTest() {

    @Test
    fun testDeleteBookStore() {
        sqlClient.entities
            .deleteCommand(BookStore::class, manningId)
            .executeAndExpectResult {
                statement {
                    sql {
                        """select tb_1_.ID, tb_1_.NAME, tb_1_.WEBSITE 
                            |from BOOK_STORE as tb_1_ 
                            |where tb_1_.ID in ($1)""".trimMargin()
                    }
                    variables(manningId)
                }
                statement {
                    sql {
                        """select tb_1_.STORE_ID, tb_1_.ID, tb_1_.EDITION, tb_1_.NAME, tb_1_.PRICE, tb_1_.STORE_ID 
                            |from BOOK as tb_1_ 
                            |where tb_1_.STORE_ID in ($1)""".trimMargin()
                    }
                    variables(manningId)
                }
                throwable {
                    type(ExecutionException::class)
                    message {
                        """Cannot delete the entity 
                        |'{"name":"MANNING","website":null,"id":"2fa3955e-3e83-49b9-902e-0465c109c779"}', 
                        |the 'onDelete' of parent property 
                        |'val org.babyfish.kimmer.sql.model.Book.store: org.babyfish.kimmer.sql.model.BookStore?' 
                        |is 'NONE' 
                        |but there are some child objects whose type is 'org.babyfish.kimmer.sql.model.Book': [
                            |{
                                |"edition":1,
                                |"name":"GraphQL in Action",
                                |"price":80.00,
                                |"store":{"id":"2fa3955e-3e83-49b9-902e-0465c109c779"},
                                |"id":"a62f7aa3-9490-4612-98b5-98aae0e77120"
                            |}, {
                                |"edition":2,
                                |"name":"GraphQL in Action",
                                |"price":81.00,
                                |"store":{"id":"2fa3955e-3e83-49b9-902e-0465c109c779"},
                                |"id":"e37a8344-73bb-4b23-ba76-82eac11f03e6"
                            |}, {
                                |"edition":3,
                                |"name":"GraphQL in Action",
                                |"price":80.00,
                                |"store":{"id":"2fa3955e-3e83-49b9-902e-0465c109c779"},
                                |"id":"780bdf07-05af-48bf-9be9-f8c65236fecc"
                            |}
                        |]""".trimMargin()
                    }
                }
            }
    }

    @Test
    fun testDeleteBookStoreOnDeleteSetNull() {
        sqlClient(OnDeleteAction.SET_NULL).entities
            .deleteCommand(BookStore::class, manningId)
            .executeAndExpectResult {
                statement {
                    sql {
                        """select tb_1_.ID, tb_1_.NAME, tb_1_.WEBSITE 
                            |from BOOK_STORE as tb_1_ 
                            |where tb_1_.ID in ($1)""".trimMargin()
                    }
                    variables(manningId)
                }
                statement {
                    sql {
                        """select tb_1_.STORE_ID, tb_1_.ID, tb_1_.EDITION, tb_1_.NAME, tb_1_.PRICE, tb_1_.STORE_ID 
                            |from BOOK as tb_1_ 
                            |where tb_1_.STORE_ID in ($1)""".trimMargin()
                    }
                    variables(manningId)
                }
                statement {
                    sql("update BOOK set STORE_ID = null where STORE_ID = $1")
                    variables(manningId)
                }
                statement {
                    sql("delete from BOOK_STORE where ID in ($1)")
                    variables(manningId)
                }
                result {
                    """{
                        |totalAffectedRowCount:4,
                        |type:DELETE,
                        |affectedRowCount:1,
                        |entity:{
                            |"name":"MANNING",
                            |"website":null,
                            |"id":"2fa3955e-3e83-49b9-902e-0465c109c779"
                        |},
                        |associationMap:{
                            |books:{
                                |totalAffectedRowCount:3,
                                |targets:[],
                                |detachedTargets:[
                                    |{
                                        |totalAffectedRowCount:1,
                                        |type:UPDATE,
                                        |affectedRowCount:1,
                                        |entity:{
                                            |"edition":1,
                                            |"name":"GraphQL in Action",
                                            |"price":80.00,
                                            |"store":{"id":"2fa3955e-3e83-49b9-902e-0465c109c779"},
                                            |"id":"a62f7aa3-9490-4612-98b5-98aae0e77120"
                                        |}
                                        |,associationMap:{},
                                        |middleTableChanged:false
                                    |},{
                                        |totalAffectedRowCount:1,
                                        |type:UPDATE,
                                        |affectedRowCount:1,
                                        |entity:{
                                            |"edition":2,
                                            |"name":"GraphQL in Action",
                                            |"price":81.00,
                                            |"store":{"id":"2fa3955e-3e83-49b9-902e-0465c109c779"},
                                            |"id":"e37a8344-73bb-4b23-ba76-82eac11f03e6"
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
                                            |"price":80.00,
                                            |"store":{"id":"2fa3955e-3e83-49b9-902e-0465c109c779"},
                                            |"id":"780bdf07-05af-48bf-9be9-f8c65236fecc"
                                        |},
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
    fun testDeleteBookStoreOnDeleteCascade() {
        sqlClient(OnDeleteAction.CASCADE).entities
            .deleteCommand(BookStore::class, manningId)
            .executeAndExpectResult {
                statement {
                    sql {
                        """select tb_1_.ID, tb_1_.NAME, tb_1_.WEBSITE 
                            |from BOOK_STORE as tb_1_ 
                            |where tb_1_.ID in ($1)""".trimMargin()
                    }
                    variables(manningId)
                }
                statement {
                    sql {
                        """select tb_1_.STORE_ID, tb_1_.ID, tb_1_.EDITION, tb_1_.NAME, tb_1_.PRICE, tb_1_.STORE_ID 
                            |from BOOK as tb_1_ 
                            |where tb_1_.STORE_ID in ($1)""".trimMargin()
                    }
                    variables(manningId)
                }
                statement {
                    sql("select BOOK_ID, AUTHOR_ID from BOOK_AUTHOR_MAPPING where BOOK_ID in ($1, $2, $3)")
                    variables(graphQLInActionId1, graphQLInActionId2, graphQLInActionId3)
                }
                statement {
                    sql("delete from BOOK_AUTHOR_MAPPING where BOOK_ID = $1")
                    variables(graphQLInActionId1)
                }
                statement {
                    sql("delete from BOOK_AUTHOR_MAPPING where BOOK_ID = $1")
                    variables(graphQLInActionId2)
                }
                statement {
                    sql("delete from BOOK_AUTHOR_MAPPING where BOOK_ID = $1")
                    variables(graphQLInActionId3)
                }
                statement {
                    sql {
                        """select tb_1_.BOOK_ID, tb_1_.ID, tb_1_.BOOK_ID, tb_1_.NAME 
                        |from CHAPTER as tb_1_ 
                        |where tb_1_.BOOK_ID in ($1, $2, $3)""".trimMargin()
                    }
                    variables(graphQLInActionId1, graphQLInActionId2, graphQLInActionId3)
                }
                statement {
                    sql("delete from BOOK where ID in ($1, $2, $3)")
                    variables(graphQLInActionId1, graphQLInActionId2, graphQLInActionId3)
                }
                statement {
                    sql("delete from BOOK_STORE where ID in ($1)")
                    variables(manningId)
                }
                result {
                    """{
                        |totalAffectedRowCount:7,
                        |type:DELETE,
                        |affectedRowCount:1,
                        |entity:{
                            |"name":"MANNING",
                            |"website":null,
                            |"id":"2fa3955e-3e83-49b9-902e-0465c109c779"
                        |},
                        |associationMap:{
                            |books:{
                                |totalAffectedRowCount:6,
                                |targets:[],
                                |detachedTargets:[
                                    |{
                                        |totalAffectedRowCount:2,
                                        |type:DELETE,
                                        |affectedRowCount:1,
                                        |entity:{
                                            |"edition":1,
                                            |"name":"GraphQL in Action",
                                            |"price":80.00,
                                            |"store":{"id":"2fa3955e-3e83-49b9-902e-0465c109c779"},
                                            |"id":"a62f7aa3-9490-4612-98b5-98aae0e77120"
                                        |},
                                        |associationMap:{
                                            |authors:{
                                                |totalAffectedRowCount:1,
                                                |targets:[],
                                                |detachedTargets:[
                                                    |{
                                                        |totalAffectedRowCount:1,
                                                        |type:NONE,
                                                        |affectedRowCount:0,
                                                        |entity:{"id":"eb4963fd-5223-43e8-b06b-81e6172ee7ae"},
                                                        |associationMap:{},
                                                        |middleTableChanged:true
                                                    |}
                                                |],
                                                |middleTableInsertedRowCount:0,
                                                |middleTableDeletedRowCount:1
                                            |}
                                        |},
                                        |middleTableChanged:false
                                    |},{
                                        |totalAffectedRowCount:2,
                                        |type:DELETE,
                                        |affectedRowCount:1,
                                        |entity:{
                                            |"edition":2,
                                            |"name":"GraphQL in Action",
                                            |"price":81.00,
                                            |"store":{"id":"2fa3955e-3e83-49b9-902e-0465c109c779"},
                                            |"id":"e37a8344-73bb-4b23-ba76-82eac11f03e6"
                                        |},
                                        |associationMap:{
                                            |authors:{
                                                |totalAffectedRowCount:1,
                                                |targets:[],
                                                |detachedTargets:[
                                                    |{
                                                        |totalAffectedRowCount:1,
                                                        |type:NONE,
                                                        |affectedRowCount:0,
                                                        |entity:{"id":"eb4963fd-5223-43e8-b06b-81e6172ee7ae"},
                                                        |associationMap:{},
                                                        |middleTableChanged:true
                                                    |}
                                                |],
                                                |middleTableInsertedRowCount:0,
                                                |middleTableDeletedRowCount:1
                                            |}
                                        |},
                                        |middleTableChanged:false
                                    |},{
                                        |totalAffectedRowCount:2,
                                        |type:DELETE,
                                        |affectedRowCount:1,
                                        |entity:{
                                            |"edition":3,
                                            |"name":"GraphQL in Action",
                                            |"price":80.00,
                                            |"store":{"id":"2fa3955e-3e83-49b9-902e-0465c109c779"},
                                            |"id":"780bdf07-05af-48bf-9be9-f8c65236fecc"
                                        |},associationMap:{
                                            |authors:{
                                                |totalAffectedRowCount:1,
                                                |targets:[],
                                                |detachedTargets:[
                                                    |{
                                                        |totalAffectedRowCount:1,
                                                        |type:NONE,
                                                        |affectedRowCount:0,
                                                        |entity:{"id":"eb4963fd-5223-43e8-b06b-81e6172ee7ae"},
                                                        |associationMap:{},
                                                        |middleTableChanged:true
                                                    |}
                                                |],
                                                |middleTableInsertedRowCount:0,
                                                |middleTableDeletedRowCount:1
                                            |}
                                        |},
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
    fun testDeleteBook() {
        val nonExistingId = UUID.fromString("56506a3c-801b-4f7d-a41d-e889cdc3d67d")
        sqlClient.entities
            .deleteCommand(
                Book::class,
                listOf(learningGraphQLId1, learningGraphQLId2, nonExistingId)
            )
            .executeAndExpectResults {
                statement {
                    sql {
                        """select tb_1_.ID, tb_1_.EDITION, tb_1_.NAME, tb_1_.PRICE, tb_1_.STORE_ID 
                            |from BOOK as tb_1_ 
                            |where tb_1_.ID in ($1, $2, $3)""".trimMargin()
                    }
                    variables(learningGraphQLId1, learningGraphQLId2, nonExistingId)
                }
                statement {
                    sql("select BOOK_ID, AUTHOR_ID from BOOK_AUTHOR_MAPPING where BOOK_ID in ($1, $2)")
                    variables(learningGraphQLId1, learningGraphQLId2)
                }
                statement {
                    sql("delete from BOOK_AUTHOR_MAPPING where BOOK_ID = $1")
                    variables(learningGraphQLId1)
                }
                statement {
                    sql("delete from BOOK_AUTHOR_MAPPING where BOOK_ID = $1")
                    variables(learningGraphQLId2)
                }
                statement {
                    sql {
                        """select tb_1_.BOOK_ID, tb_1_.ID, tb_1_.BOOK_ID, tb_1_.NAME 
                        |from CHAPTER as tb_1_ 
                        |where tb_1_.BOOK_ID in ($1, $2)""".trimMargin()
                    }
                    variables(learningGraphQLId1, learningGraphQLId2)
                }
                statement {
                    sql("delete from BOOK where ID in ($1, $2)")
                    variables(learningGraphQLId1, learningGraphQLId2)
                }
                result {
                    """{
                        |totalAffectedRowCount:3,
                        |type:DELETE,
                        |affectedRowCount:1,
                        |entity:{
                            |"edition":1,
                            |"name":"Learning GraphQL",
                            |"price":50.00,
                            |"store":{"id":"d38c10da-6be8-4924-b9b9-5e81899612a0"},
                            |"id":"e110c564-23cc-4811-9e81-d587a13db634"
                        |},
                        |associationMap:{
                            |authors:{
                                |totalAffectedRowCount:2,
                                |targets:[],
                                |detachedTargets:[
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
                                |middleTableInsertedRowCount:0,
                                |middleTableDeletedRowCount:2
                            |}
                        |}
                    |}""".trimMargin()
                }
                result {
                    """{
                        |totalAffectedRowCount:3,
                        |type:DELETE,
                        |affectedRowCount:1,
                        |entity:{
                            |"edition":2,
                            |"name":"Learning GraphQL",
                            |"price":55.00,
                            |"store":{"id":"d38c10da-6be8-4924-b9b9-5e81899612a0"},
                            |"id":"b649b11b-1161-4ad2-b261-af0112fdd7c8"
                        |},
                        |associationMap:{
                            |authors:{
                                |totalAffectedRowCount:2,
                                |targets:[],
                                |detachedTargets:[
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
                                |middleTableInsertedRowCount:0,
                                |middleTableDeletedRowCount:2
                            |}
                        |}
                    |}""".trimMargin()
                }
                result {
                    """{
                        |totalAffectedRowCount:0,
                        |type:NONE,
                        |affectedRowCount:0,
                        |entity:{
                            |"id":"56506a3c-801b-4f7d-a41d-e889cdc3d67d"
                        |},
                        |associationMap:{}
                    |}""".trimMargin()
                }
            }
    }

    @Test
    fun deleteAuthor() {
        sqlClient.entities
            .deleteCommand(Author::class, alexId)
            .executeAndExpectResult {
                statement {
                    sql {
                        """select 
                                |tb_1_.ID, 
                                |tb_1_.FIRST_NAME, 
                                |concat(tb_1_.FIRST_NAME, $1, tb_1_.LAST_NAME), 
                                |tb_1_.GENDER, 
                                |tb_1_.LAST_NAME 
                            |from AUTHOR as tb_1_ 
                            |where tb_1_.ID in ($2)""".trimMargin()
                    }
                    variables(" ", alexId)
                }
                statement {
                    sql("select AUTHOR_ID, BOOK_ID from BOOK_AUTHOR_MAPPING where AUTHOR_ID in ($1)")
                    variables(alexId)
                }
                statement {
                    sql("delete from BOOK_AUTHOR_MAPPING where AUTHOR_ID = $1")
                    variables(alexId)
                }
                statement {
                    sql("delete from AUTHOR where ID in ($1)")
                    variables(alexId)
                }
                result {
                    """{
                        |totalAffectedRowCount:4,
                        |type:DELETE,
                        |affectedRowCount:1,
                        |entity:{
                            |"firstName":"Alex",
                            |"fullName":"Alex Banks",
                            |"gender":"MALE",
                            |"lastName":"Banks",
                            |"id":"1e93da94-af84-44f4-82d1-d8a9fd52ea94"
                        |},
                        |associationMap:{
                            |books:{
                                |totalAffectedRowCount:3,
                                |targets:[],
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
                                    |},{
                                        |totalAffectedRowCount:1,
                                        |type:NONE,
                                        |affectedRowCount:0,
                                        |entity:{"id":"64873631-5d82-4bae-8eb8-72dd955bfc56"},
                                        |associationMap:{},
                                        |middleTableChanged:true
                                    |}
                                |],
                                |middleTableInsertedRowCount:0,
                                |middleTableDeletedRowCount:3
                            |}
                        |}
                    |}""".trimMargin()
                }
            }
    }
}