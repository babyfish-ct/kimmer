package org.babyfish.kimmer.sql.mutation

import org.babyfish.kimmer.sql.common.AbstractMutationTest
import org.babyfish.kimmer.sql.common.alexId
import org.babyfish.kimmer.sql.common.borisId
import org.babyfish.kimmer.sql.common.learningGraphQLId1
import org.babyfish.kimmer.sql.model.Author
import org.babyfish.kimmer.sql.model.Book
import org.junit.Test

class AssociationMutationTest: AbstractMutationTest() {

    @Test
    fun testInsertIgnore() {
        sqlClient.associations.byList(Book::authors).saveCommand(
            learningGraphQLId1, alexId
        ).executeAndExpectResult {
            statement {
                sql {
                    """select 1 
                        |from BOOK_AUTHOR_MAPPING as tb_1_ 
                        |where tb_1_.BOOK_ID = $1 and tb_1_.AUTHOR_ID = $2""".trimMargin()
                }
                variables(learningGraphQLId1, alexId)
            }
            result {
                """{
                    |totalAffectedRowCount:0,
                    |type:NONE,
                    |affectedRowCount:0,
                    |entity:{
                        |"__genericSourceType":"org.babyfish.kimmer.sql.model.Book",
                        |"__genericTargetType":"org.babyfish.kimmer.sql.model.Author",
                        |"source":{"id":"e110c564-23cc-4811-9e81-d587a13db634"},
                        |"target":{"id":"1e93da94-af84-44f4-82d1-d8a9fd52ea94"}
                    |},
                    |associations:[
                        |{
                            |associationName:"source",
                            |totalAffectedRowCount:0,
                            |targets:[
                                |{
                                    |totalAffectedRowCount:0,
                                    |type:NONE,
                                    |affectedRowCount:0,
                                    |entity:{"id":"e110c564-23cc-4811-9e81-d587a13db634"},
                                    |associations:[],
                                    |middleTableChanged:false
                                |}
                            |],
                            |detachedTargets:[],
                            |middleTableInsertedRowCount:0,middleTableDeletedRowCount:0
                        |},
                        |{
                            |associationName:"target",
                            |totalAffectedRowCount:0,
                            |targets:[
                                |{
                                    |totalAffectedRowCount:0,
                                    |type:NONE,
                                    |affectedRowCount:0,
                                    |entity:{"id":"1e93da94-af84-44f4-82d1-d8a9fd52ea94"},
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
    fun testInsert() {
        sqlClient.associations.byList(Book::authors).saveCommand(
            learningGraphQLId1, borisId
        ).executeAndExpectResult {
            statement {
                sql {
                    """select 1 
                        |from BOOK_AUTHOR_MAPPING as tb_1_ 
                        |where tb_1_.BOOK_ID = $1 and tb_1_.AUTHOR_ID = $2""".trimMargin()
                }
                variables(learningGraphQLId1, borisId)
            }
            statement {
                sql("insert into BOOK_AUTHOR_MAPPING(BOOK_ID, AUTHOR_ID) values(\$1, \$2)")
                variables(learningGraphQLId1, borisId)
            }
            result {
                """{
                    |totalAffectedRowCount:1,
                    |type:INSERT,
                    |affectedRowCount:1,
                    |entity:{
                        |"__genericSourceType":"org.babyfish.kimmer.sql.model.Book",
                        |"__genericTargetType":"org.babyfish.kimmer.sql.model.Author",
                        |"source":{"id":"e110c564-23cc-4811-9e81-d587a13db634"},
                        |"target":{"id":"718795ad-77c1-4fcf-994a-fec6a5a11f0f"}
                    |},
                    |associations:[
                        |{
                            |associationName:"source",
                            |totalAffectedRowCount:0,
                            |targets:[
                                |{
                                    |totalAffectedRowCount:0,
                                    |type:NONE,
                                    |affectedRowCount:0,
                                    |entity:{"id":"e110c564-23cc-4811-9e81-d587a13db634"},
                                    |associations:[],
                                    |middleTableChanged:false
                                |}
                            |],
                            |detachedTargets:[],
                            |middleTableInsertedRowCount:0,
                            |middleTableDeletedRowCount:0
                        |},
                        |{
                            |associationName:"target",
                            |totalAffectedRowCount:0,
                            |targets:[
                                |{
                                    |totalAffectedRowCount:0,
                                    |type:NONE,
                                    |affectedRowCount:0,
                                    |entity:{"id":"718795ad-77c1-4fcf-994a-fec6a5a11f0f"},
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
    fun testDelete() {
        sqlClient.associations.byList(Book::authors).deleteCommand(
            learningGraphQLId1, alexId
        ).executeAndExpectResult {
            statement {
                sql {
                    """select 
                            |tb_1_.BOOK_ID, tb_1_.AUTHOR_ID 
                        |from BOOK_AUTHOR_MAPPING as tb_1_ 
                        |where (tb_1_.BOOK_ID, tb_1_.AUTHOR_ID) in (
                            |($1, $2)
                        |)""".trimMargin()
                }
                variables(learningGraphQLId1, alexId)
            }
            statement {
                sql {
                    """delete from BOOK_AUTHOR_MAPPING 
                        |where (BOOK_ID, AUTHOR_ID) in (
                            |($1, $2)
                        |)""".trimMargin()
                }
                variables(learningGraphQLId1, alexId)
            }
            result {
                """{
                    |totalAffectedRowCount:1,
                    |type:DELETE,
                    |affectedRowCount:1,
                    |entity:{
                        |"__genericSourceType":"org.babyfish.kimmer.sql.model.Book",
                        |"__genericTargetType":"org.babyfish.kimmer.sql.model.Author",
                        |"source":{"id":"e110c564-23cc-4811-9e81-d587a13db634"},
                        |"target":{"id":"1e93da94-af84-44f4-82d1-d8a9fd52ea94"}
                    |},
                    |associations:[]
                    |}""".trimMargin()
            }
        }
    }

    @Test
    fun testInverseInsertIgnore() {
        sqlClient.associations.byList(Author::books).saveCommand(
            alexId, learningGraphQLId1
        ).executeAndExpectResult {
            statement {
                sql {
                    """select 1 
                        |from BOOK_AUTHOR_MAPPING as tb_1_ 
                        |where tb_1_.AUTHOR_ID = $1 and tb_1_.BOOK_ID = $2""".trimMargin()
                }
                variables(alexId, learningGraphQLId1)
            }
            result {
                """{
                    |totalAffectedRowCount:0,
                    |type:NONE,
                    |affectedRowCount:0,
                    |entity:{
                        |"__genericSourceType":"org.babyfish.kimmer.sql.model.Author",
                        |"__genericTargetType":"org.babyfish.kimmer.sql.model.Book",
                        |"source":{"id":"1e93da94-af84-44f4-82d1-d8a9fd52ea94"},
                        |"target":{"id":"e110c564-23cc-4811-9e81-d587a13db634"}
                    |},
                    |associations:[
                        |{
                            |associationName:"source",
                            |totalAffectedRowCount:0,
                            |targets:[
                                |{
                                    |totalAffectedRowCount:0,
                                    |type:NONE,
                                    |affectedRowCount:0,
                                    |entity:{"id":"1e93da94-af84-44f4-82d1-d8a9fd52ea94"},
                                    |associations:[],
                                    |middleTableChanged:false
                                |}
                            |],
                            |detachedTargets:[],
                            |middleTableInsertedRowCount:0,
                            |middleTableDeletedRowCount:0
                        |},
                        |{
                            |associationName:"target",
                            |totalAffectedRowCount:0,
                            |targets:[
                                |{
                                    |totalAffectedRowCount:0,
                                    |type:NONE,
                                    |affectedRowCount:0,
                                    |entity:{"id":"e110c564-23cc-4811-9e81-d587a13db634"},
                                    |associations:[],
                                    |middleTableChanged:false
                                |}
                            |],
                            |detachedTargets:[],
                            |middleTableInsertedRowCount:0,middleTableDeletedRowCount:0
                        |}
                    |]
                |}""".trimMargin()
            }
        }
    }

    @Test
    fun testInverseInsert() {
        sqlClient.associations.byList(Author::books).saveCommand(
            borisId, learningGraphQLId1
        ).executeAndExpectResult {
            statement {
                sql {
                    """select 1 
                        |from BOOK_AUTHOR_MAPPING as tb_1_ 
                        |where tb_1_.AUTHOR_ID = $1 and tb_1_.BOOK_ID = $2""".trimMargin()
                }
                variables(borisId, learningGraphQLId1)
            }
            statement {
                sql("insert into BOOK_AUTHOR_MAPPING(AUTHOR_ID, BOOK_ID) values(\$1, \$2)")
                variables(borisId, learningGraphQLId1)
            }
            result {
                """{
                    |totalAffectedRowCount:1,
                    |type:INSERT,
                    |affectedRowCount:1,
                    |entity:{
                        |"__genericSourceType":"org.babyfish.kimmer.sql.model.Author",
                        |"__genericTargetType":"org.babyfish.kimmer.sql.model.Book",
                        |"source":{"id":"718795ad-77c1-4fcf-994a-fec6a5a11f0f"},
                        |"target":{"id":"e110c564-23cc-4811-9e81-d587a13db634"}
                    |},
                    |associations:[
                        |{
                            |associationName:"source",
                            |totalAffectedRowCount:0,
                            |targets:[
                                |{
                                    |totalAffectedRowCount:0,
                                    |type:NONE,
                                    |affectedRowCount:0,
                                    |entity:{"id":"718795ad-77c1-4fcf-994a-fec6a5a11f0f"},
                                    |associations:[],
                                    |middleTableChanged:false
                                |}
                            |],
                            |detachedTargets:[],
                            |middleTableInsertedRowCount:0,
                            |middleTableDeletedRowCount:0
                        |},
                        |{
                            |associationName:"target",
                            |totalAffectedRowCount:0,
                            |targets:[
                                |{
                                    |totalAffectedRowCount:0,
                                    |type:NONE,
                                    |affectedRowCount:0,
                                    |entity:{"id":"e110c564-23cc-4811-9e81-d587a13db634"},
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
    fun testInverseDelete() {
        sqlClient.associations.byList(Author::books).deleteCommand(
            alexId, learningGraphQLId1
        ).executeAndExpectResult {
            statement {
                sql {
                    """select 
                            |tb_1_.AUTHOR_ID, tb_1_.BOOK_ID 
                        |from BOOK_AUTHOR_MAPPING as tb_1_ 
                        |where (tb_1_.AUTHOR_ID, tb_1_.BOOK_ID) in (
                            |($1, $2)
                        |)""".trimMargin()
                }
                variables(alexId, learningGraphQLId1)
            }
            statement {
                sql {
                    """delete from BOOK_AUTHOR_MAPPING 
                        |where (AUTHOR_ID, BOOK_ID) in (
                            |($1, $2)
                        |)""".trimMargin()
                }
                variables(alexId, learningGraphQLId1)
            }
            result {
                """{
                    |totalAffectedRowCount:1,
                    |type:DELETE,
                    |affectedRowCount:1,
                    |entity:{
                        |"__genericSourceType":"org.babyfish.kimmer.sql.model.Author",
                        |"__genericTargetType":"org.babyfish.kimmer.sql.model.Book",
                        |"source":{"id":"1e93da94-af84-44f4-82d1-d8a9fd52ea94"},
                        |"target":{"id":"e110c564-23cc-4811-9e81-d587a13db634"}
                    |},
                    |associations:[]
                    |}""".trimMargin()
            }
        }
    }
}