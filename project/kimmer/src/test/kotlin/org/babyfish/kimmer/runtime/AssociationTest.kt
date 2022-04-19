package org.babyfish.kimmer.runtime

import kotlinx.coroutines.runBlocking
import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.model.Consumer
import org.babyfish.kimmer.model.Producer
import org.babyfish.kimmer.model.by
import org.babyfish.kimmer.sql.Association
import org.babyfish.kimmer.sql.AssociationId
import org.babyfish.kimmer.sql.produceAssociation
import org.babyfish.kimmer.sql.produceAssociationAsync
import org.junit.Test
import kotlin.test.expect

class AssociationTest {

    @Suppress("UNCHECKED_CAST")
    @Test
    fun test() {

        val association = produceAssociation(Producer::class, Consumer::class) {
            source = new(Producer::class).by {
                id = "p1"
                name = "Producer-1"
            }
            target = new(Consumer::class).by {
                id = "c1"
                name = "Consumer-1"
            }
        }
        val association2 = Immutable.fromString(association.toString(), Association::class)
            as Association<Producer, String, Consumer, String>
        val association3 = produceAssociation(Producer::class, Consumer::class, association2) {
            source = new(Producer::class).by(source) {
                id += "*"
                name += "*"
            }
            target = new(Consumer::class).by(target) {
                id += "*"
                name += "*"
            }
        }

        expect(
            """{
                    |"__genericSourceType":"org.babyfish.kimmer.model.Producer",
                    |"__genericTargetType":"org.babyfish.kimmer.model.Consumer",
                    |"source":{"name":"Producer-1","id":"p1"},
                    |"target":{"name":"Consumer-1","id":"c1"}
                |}""".trimMargin().toOneLine()
        ) {
            association.toString()
        }
        expect(AssociationId("p1", "c1")) {
            association.id
        }

        expect(
            """{
                    |"__genericSourceType":"org.babyfish.kimmer.model.Producer",
                    |"__genericTargetType":"org.babyfish.kimmer.model.Consumer",
                    |"source":{"name":"Producer-1","id":"p1"},
                    |"target":{"name":"Consumer-1","id":"c1"}
                |}""".trimMargin().toOneLine()
        ) {
            association2.toString()
        }
        expect(AssociationId("p1", "c1")) {
            association2.id
        }

        expect(
            """{
                    |"__genericSourceType":"org.babyfish.kimmer.model.Producer",
                    |"__genericTargetType":"org.babyfish.kimmer.model.Consumer",
                    |"source":{"name":"Producer-1*","id":"p1*"},
                    |"target":{"name":"Consumer-1*","id":"c1*"}
                |}""".trimMargin().toOneLine()
        ) {
            association3.toString()
        }
        expect(AssociationId("p1*", "c1*")) {
            association3.id
        }
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun testAsync() {

        runBlocking {

            val association = produceAssociationAsync(Producer::class, Consumer::class) {
                source = newAsync(Producer::class).by {
                    id = "p1"
                    name = "Producer-1"
                }
                target = newAsync(Consumer::class).by {
                    id = "c1"
                    name = "Consumer-1"
                }
            }
            val association2 = Immutable.fromString(association.toString(), Association::class)
                as Association<Producer, String, Consumer, String>
            val association3 = produceAssociationAsync(Producer::class, Consumer::class, association2) {
                source = newAsync(Producer::class).by(source) {
                    id += "*"
                    name += "*"
                }
                target = newAsync(Consumer::class).by(target) {
                    id += "*"
                    name += "*"
                }
            }

            expect(
                """{
                    |"__genericSourceType":"org.babyfish.kimmer.model.Producer",
                    |"__genericTargetType":"org.babyfish.kimmer.model.Consumer",
                    |"source":{"name":"Producer-1","id":"p1"},
                    |"target":{"name":"Consumer-1","id":"c1"}
                |}""".trimMargin().toOneLine()
            ) {
                association.toString()
            }
            expect(AssociationId("p1", "c1")) {
                association.id
            }

            expect(
                """{
                    |"__genericSourceType":"org.babyfish.kimmer.model.Producer",
                    |"__genericTargetType":"org.babyfish.kimmer.model.Consumer",
                    |"source":{"name":"Producer-1","id":"p1"},
                    |"target":{"name":"Consumer-1","id":"c1"}
                |}""".trimMargin().toOneLine()
            ) {
                association2.toString()
            }
            expect(AssociationId("p1", "c1")) {
                association2.id
            }

            expect(
                """{
                    |"__genericSourceType":"org.babyfish.kimmer.model.Producer",
                    |"__genericTargetType":"org.babyfish.kimmer.model.Consumer",
                    |"source":{"name":"Producer-1*","id":"p1*"},
                    |"target":{"name":"Consumer-1*","id":"c1*"}
                |}""".trimMargin().toOneLine()
            ) {
                association3.toString()
            }
            expect(AssociationId("p1*", "c1*")) {
                association3.id
            }
        }
    }

    companion object {
        private fun String.toOneLine(): String =
            replace("\r", "").replace("\n", "")
    }
}