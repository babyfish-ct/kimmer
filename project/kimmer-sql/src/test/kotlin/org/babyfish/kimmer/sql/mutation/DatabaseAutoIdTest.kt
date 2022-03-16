package org.babyfish.kimmer.sql.mutation

import com.fasterxml.uuid.impl.UUIDUtil
import org.babyfish.kimmer.new
import org.babyfish.kimmer.sql.common.AbstractMutationTest
import org.babyfish.kimmer.sql.common.graphQLInActionId3
import org.babyfish.kimmer.sql.common.manningId
import org.babyfish.kimmer.sql.meta.config.OnDeleteAction
import org.babyfish.kimmer.sql.model.Announcement
import org.babyfish.kimmer.sql.model.Chapter
import org.babyfish.kimmer.sql.model.by
import org.babyfish.kimmer.sql.runtime.dialect.H2Dialect
import org.babyfish.kimmer.sql.runtime.dialect.MysqlDialect
import kotlin.test.Test

class DatabaseAutoIdTest : AbstractMutationTest() {

    @Test
    fun testSequenceByH2() {

        // restore sequence and identity
        initDatabase()

        using(H2Dialect()) {
            sqlClient.entities
                .saveCommand(
                    new(Chapter::class).by {
                        name = "Chapter-1"
                        book().id = graphQLInActionId3
                    }
                ) {
                    insertOnly()
                }
                .executeAndExpectResult {
                    statement {
                        sql("select nextval('chapter_id_seq')")
                        variables()
                    }
                    statement {
                        sql("insert into CHAPTER(BOOK_ID, ID, NAME) values($1, $2, $3)")
                        variables(graphQLInActionId3, 1L, "Chapter-1")
                    }
                    result {
                        """{
                            |totalAffectedRowCount:1,
                            |type:INSERT,
                            |affectedRowCount:1,
                            |entity:{
                                |"book":{"id":"780bdf07-05af-48bf-9be9-f8c65236fecc"},
                                |"name":"Chapter-1",
                                |"id":1
                            |},
                            |associationMap:{
                                |book:{
                                    |totalAffectedRowCount:0,
                                    |targets:[
                                        |{
                                            |totalAffectedRowCount:0,
                                            |type:NONE,
                                            |affectedRowCount:0,
                                            |entity:{"id":"780bdf07-05af-48bf-9be9-f8c65236fecc"},
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
    }

    @Test
    fun testIdentityByMysql() {
        using(MysqlDialect()) {
            sqlClient(OnDeleteAction.NONE, MYSQL_UUID_PROVIDER).entities
                .saveCommand(
                    new(Announcement::class).by {
                        message = "Hello world"
                        store().id = manningId
                    }
                ) {
                    insertOnly()
                }
                .executeAndExpectResult(
                    dataSource = MYSQL_DATA_SOURCE,
                    connectionFactory = MYSQL_CONNECTION_FACTORY,
                    init = {
                        createStatement()
                            .executeUpdate("alter table announcement auto_increment = 1")
                    }
                ) {
                    statement {
                        sql("insert into ANNOUNCEMENT(MESSAGE, STORE_ID) values($1, $2)")
                        variables("Hello world", UUIDUtil.asByteArray(manningId))
                    }
                    statement {
                        sql("select last_insert_id()")
                        variables()
                    }
                    result {
                        """{
                            |totalAffectedRowCount:1,
                            |type:INSERT,
                            |affectedRowCount:1,
                            |entity:{
                                |"message":"Hello world",
                                |"store":{"id":"2fa3955e-3e83-49b9-902e-0465c109c779"},
                                |"id":1
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
                                                |"id":"2fa3955e-3e83-49b9-902e-0465c109c779"
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
    }
}