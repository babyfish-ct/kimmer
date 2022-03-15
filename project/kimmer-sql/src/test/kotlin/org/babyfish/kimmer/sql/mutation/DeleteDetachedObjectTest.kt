package org.babyfish.kimmer.sql.mutation

import org.babyfish.kimmer.new
import org.babyfish.kimmer.sql.common.*
import org.babyfish.kimmer.sql.model.BookStore
import org.babyfish.kimmer.sql.model.by
import org.junit.Test

class DeleteDetachedObjectTest : AbstractMutationTest() {

    @Test
    fun test() {
        sqlClient.entities.saveCommand(
            new(BookStore::class).by {
                id = manningId
                books().add.by {
                    id = graphQLInActionId2
                }
                books().add.by {
                    id = graphQLInActionId3
                }
            }
        ) {
            list(BookStore::books) {
                deleteDetachedObject()
            }
        }.executeAndExpect {
            statement {
                sql("select tb_1_.ID, tb_1_.NAME, tb_1_.WEBSITE from BOOK_STORE as tb_1_ where tb_1_.ID = $1")
                variables(manningId)
            }
            statement {
                sql("select ID from BOOK where STORE_ID = $1")
                variables(manningId)
            }
            statement {
                sql("select BOOK_ID, AUTHOR_ID from BOOK_AUTHOR_MAPPING where BOOK_ID in ($1)")
                variables(graphQLInActionId1)
            }
            statement {
                sql("delete from BOOK_AUTHOR_MAPPING where BOOK_ID = $1")
                variables(graphQLInActionId1)
            }
            statement {
                sql("delete from BOOK where ID in ($1)")
                variables(graphQLInActionId1)
            }
            result {
                """{
                    |totalAffectedRowCount:2,
                    |type:NONE,
                    |affectedRowCount:0,
                    |entity:{
                        |"books":[
                            |{"id":"e37a8344-73bb-4b23-ba76-82eac11f03e6"},
                            |{"id":"780bdf07-05af-48bf-9be9-f8c65236fecc"}
                        |],
                        |"id":"2fa3955e-3e83-49b9-902e-0465c109c779"
                    |},
                    |associationMap:{
                        |books:{
                            |totalAffectedRowCount:2,
                            |targets:[
                                |{
                                    |totalAffectedRowCount:0,
                                    |type:NONE,
                                    |affectedRowCount:0,
                                    |entity:{"id":"e37a8344-73bb-4b23-ba76-82eac11f03e6"},
                                    |associationMap:{},
                                    |middleTableChanged:false
                                |},{
                                    |totalAffectedRowCount:0,
                                    |type:NONE,
                                    |affectedRowCount:0,
                                    |entity:{"id":"780bdf07-05af-48bf-9be9-f8c65236fecc"},
                                    |associationMap:{},
                                    |middleTableChanged:false
                                |}
                            |],
                            |detachedTargets:[
                                |{
                                    |totalAffectedRowCount:2,
                                    |type:DELETE,
                                    |affectedRowCount:1,
                                    |entity:{"id":"a62f7aa3-9490-4612-98b5-98aae0e77120"},
                                    |associationMap:{
                                        |authors:{
                                            |totalAffectedRowCount:1,
                                            |targets:[],
                                            |detachedTargets:[
                                                |{
                                                    |totalAffectedRowCount:1,
                                                    |type:NONE,
                                                    |affectedRowCount:0,
                                                    |entity:{"id":"eb4963fd-5223-43e8-b06b-81e6172ee7ae"
                                                |},
                                                |associationMap:{},
                                                |middleTableChanged:true}
                                            |],
                                            |middleTableInsertedRowCount:0,
                                            |middleTableDeletedRowCount:1
                                        |}
                                    |},middleTableChanged:false
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
}