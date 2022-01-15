package org.babyfish.kimmer.runtime

import com.fasterxml.jackson.core.type.TypeReference
import kotlinx.coroutines.*
import org.babyfish.kimmer.*
import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.jackson.immutableObjectMapper
import org.babyfish.kimmer.new
import org.babyfish.kimmer.newAsync
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.expect

class KimmerTest {

    @Test
    fun testSimple() {
        val book = new(Book::class).by {
            name = "book"
            store().name = "store"
            authors() += new(Author::class).by {
                name = "Jim"
            }
            authors() += new(Author::class).by {
                name = "Kate"
            }
        }
        val book2 = new(Book::class).by(book) {}
        val book3 = new(Book::class).by(book2) {
            name = "book!"
            name = "book"
            store().name = "store!"
            store().name = "store"
            authors()[0].name = "Jim!"
            authors()[0].name = "Jim"
            authors()[1].name = "Kate!"
            authors()[1].name = "Kate"
        }
        val book4 = new(Book::class).by(book3) {
            name += "!"
            store().name += "!"
            for (author in authors()) {
                author.name += "!"
            }
        }
        expect("book") {
            book.name
        }
        expect("store") {
            book.store?.name
        }
        expect(listOf("Jim", "Kate")) {
            book.authors.map { it.name }
        }
        expect(true) {
            book === book2
        }
        expect(true) {
            book2 === book3
        }
        expect("book!") {
            book4.name
        }
        expect("store!") {
            book4.store?.name
        }
        expect(listOf("Jim!", "Kate!")) {
            book4.authors.map { it.name }
        }

        assertFailsWith<UnloadedException> {
            book4.id
        }
        assertFailsWith<UnloadedException> {
            book4.store?.books
        }
        assertFailsWith<UnloadedException> {
            book4.authors[0].books
        }

        val json = """{"authors":[{"name":"Jim!"},{"name":"Kate!"}],"name":"book!","store":{"name":"store!"}}"""
        expect(json) {
            book4.toString()
        }
        expect(json) {
            immutableObjectMapper().writeValueAsString(book4)
        }
        expect(book4) {
            Immutable.fromString(json, Book::class)
        }
        expect(book4) {
            immutableObjectMapper().readValue(json, Book::class.java)
        }
    }

    @Test
    fun testOneToManyPolymorphic() {
        val zoo = new(SealedZoo::class).by {
            location = "city center"
            animals() += new(Tiger::class).by {
                weight = 600
            }
            animals() += new(Otter::class).by {
                length = 50
            }
        }
        val json = """{"__typename":"SealedZoo","location":"city center","animals":[{"__typename":"Tiger","weight":600},{"__typename":"Otter","length":50}]}"""
        expect(json) {
            zoo.toString()
        }
        val deserializedZoo = Immutable.fromString(json, SealedZoo::class)
        expect(false) {
            zoo === deserializedZoo
        }
        expect(zoo) {
            deserializedZoo
        }
        expect(true) {
            deserializedZoo.animals[0] is Tiger
        }
        expect(600) {
            (deserializedZoo.animals[0] as Tiger).weight
        }
        expect(true) {
            deserializedZoo.animals[1] is Otter
        }
        expect(50) {
            (deserializedZoo.animals[1] as Otter).length
        }
    }

    @Test
    fun testManyToOnePolymorphic() {
        val typeReference = object: TypeReference<List<Animal>>() {}
        val animals = listOf(
            new(Tiger::class).by {
                weight = 600
                zoo = new(SealedZoo::class).by {
                    location = "city center"
                }
            },
            new(Otter::class).by {
                length = 50
                zoo = new(WildZoo::class).by {
                    area = 3000
                }
            }
        )
        val json = """[{"__typename":"Tiger","weight":600,"zoo":{"__typename":"SealedZoo","location":"city center"}},{"__typename":"Otter","length":50,"zoo":{"__typename":"WildZoo","area":3000}}]"""
        expect(json) {
            immutableObjectMapper().writerFor(typeReference).writeValueAsString(animals)
        }
        val deserializedAnimals = immutableObjectMapper().readValue(json, typeReference)
        expect(false) {
            animals === deserializedAnimals
        }
        expect(animals) {
            deserializedAnimals
        }
        expect(true) {
            deserializedAnimals[0].zoo is SealedZoo
        }
        expect("city center") {
            (deserializedAnimals[0].zoo as SealedZoo).location
        }
        expect(true) {
            deserializedAnimals[1].zoo is WildZoo
        }
        expect(3000) {
            (deserializedAnimals[1].zoo as WildZoo).area
        }
    }

    @Test
    fun testPrimitive() {
        val primitiveInfo = new(PrimitiveInfo::class).by {
            boolean = true
            char = 'X'
            byte = 23
            short = 234
            int = 2345
            long = 23456
            float = 23456.7F
            double = 23456.78
        }
        val json = """{"boolean":true,"byte":23,"char":"X","double":23456.78,"float":23456.7,"int":2345,"long":23456,"short":234}"""
        expect(json) {
            primitiveInfo.toString()
        }
        val deserializedPrimitiveInfo = Immutable.fromString(json, PrimitiveInfo::class)
        expect(false) {
            primitiveInfo === deserializedPrimitiveInfo
        }
        expect(primitiveInfo) {
            deserializedPrimitiveInfo
        }
    }

    @Test
    fun testAsync() {
        val (book, time) = executeAndCollectTime {
            newAsync(Book::class).by {
                delay(500)
                name = "The book"
                store().name = "The store"
                authors() += newAsync(Author::class).by {
                    delay(500)
                    name = "Jim"
                }
                authors() += newAsync(Author::class).by {
                    delay(500)
                    name = "Kate"
                }
            }
        }
        expect(true) {
            time >= 1500
        }
        expect("""{"authors":[{"name":"Jim"},{"name":"Kate"}],"name":"The book","store":{"name":"The store"}}""") {
            book.toString()
        }
        val (book2, time2) = executeAndCollectTime {
            newAsync(Book::class).by(book) {
                name += "!"
                store().name += "!"
                delay(500)
                for (author in authors()) {
                    delay(500)
                    author.name += "!"
                }
            }
        }
        expect(true) {
            time2 >= 1500
        }
        expect("""{"authors":[{"name":"Jim!"},{"name":"Kate!"}],"name":"The book!","store":{"name":"The store!"}}""") {
            book2.toString()
        }
    }

    private fun <T> executeAndCollectTime(block: suspend () -> T): Pair<T, Long> {
        val start = System.currentTimeMillis()
        return runBlocking {
            block()
        } to System.currentTimeMillis() - start
    }

    @Test
    fun testPrimitiveAsync() {
        val primitiveInfo = runBlocking {
            newAsync(PrimitiveInfo::class).by {
                boolean = true
                char = 'X'
                byte = 23
                short = 234
                int = 2345
                long = 23456
                float = 23456.7F
                double = 23456.78
            }
        }
        val json = """{"boolean":true,"byte":23,"char":"X","double":23456.78,"float":23456.7,"int":2345,"long":23456,"short":234}"""
        expect(json) {
            primitiveInfo.toString()
        }
        val deserializedPrimitiveInfo = Immutable.fromString(json, PrimitiveInfo::class)
        expect(false) {
            primitiveInfo === deserializedPrimitiveInfo
        }
        expect(primitiveInfo) {
            deserializedPrimitiveInfo
        }
    }

    @Test
    fun testCircularReference() {
        assertFailsWith<CircularReferenceException> {
            new(Employee::class).by {
                supervisor = this
            }
        }

        assertFailsWith<CircularReferenceException> {
            new(Employee::class).by {
                val self = this
                supervisor = new(Employee::class).by {
                    supervisor = self
                }
            }
        }

        assertFailsWith<CircularReferenceException> {
            new(Employee::class).by {
                val self = this
                supervisor = new(Employee::class).by {
                    supervisor = new(Employee::class).by {
                        supervisor = self
                    }
                }
            }
        }
    }
}
