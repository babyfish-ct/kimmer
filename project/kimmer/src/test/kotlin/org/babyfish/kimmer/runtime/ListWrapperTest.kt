package org.babyfish.kimmer.runtime

import org.babyfish.kimmer.runtime.list.ListProxy
import org.babyfish.kimmer.runtime.list.LockedList
import org.junit.Test
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.test.assertFailsWith
import kotlin.test.expect

class ListWrapperTest {

    @Test
    fun testReadList() {
        val base = listOf("a", "b", "c")
        testRead0(ListProxy(base, null))
        testRead0(LockedList(ListProxy(base, null), ReentrantReadWriteLock()))

        val base2 = listOf("-4", "-3", "-2", "-1", "a", "b", "c", "+1", "+2", "+3", "+4", "+5", "+6")
        testRead0(createSubList(ListProxy(base2, null)))
        testRead0(LockedList(createSubList(ListProxy(base2, null)), ReentrantReadWriteLock()))
    }

    @Test
    fun testWriteList() {
        val base = listOf("a", "b", "c")
        testWrite0(ListProxy(base, null))
        testWrite0(LockedList(ListProxy(base, null), ReentrantReadWriteLock()))

        val base2 = listOf("-4", "-3", "-2", "-1", "a", "b", "c", "+1", "+2", "+3", "+4", "+5", "+6")
        testWrite0(createSubList(ListProxy(base2, null)))
        testWrite0(LockedList(createSubList(ListProxy(base2, null)), ReentrantReadWriteLock()))

        expect(listOf("a", "b", "c")) {
            base
        }
    }

    private fun testRead0(proxy: List<String>) {
        expect(false) {
            proxy.isEmpty()
        }
        expect(3) {
            proxy.size
        }
        expect(true) {
            proxy.contains("a")
        }
        expect(false) {
            proxy.contains("A")
        }
        expect(true) {
            proxy.containsAll(listOf("a", "b"))
        }
        expect(false) {
            proxy.containsAll(listOf("A", "B"))
        }
        proxy.listIterator().let {
            expect(true) { it.hasNext() }
            expect("a") { it.next() }
            expect(1) { it.nextIndex() }
            expect(true) { it.hasNext()}
            expect("b") { it.next() }
            expect(2) { it.nextIndex() }
            expect(true) { it.hasNext()}
            expect("c") { it.next() }
            expect(3) { it.nextIndex() }
            expect(false) { it.hasNext() }
            assertFailsWith(NoSuchElementException::class) { it.next() }
            expect(3) { it.nextIndex() }
        }
        proxy.listIterator(3).let {
            expect(true) { it.hasPrevious() }
            expect("c") { it.previous() }
            expect(1) { it.previousIndex() }
            expect(true) { it.hasPrevious()}
            expect("b") { it.previous() }
            expect(0) { it.previousIndex() }
            expect(true) { it.hasPrevious()}
            expect("a") { it.previous() }
            expect(-1) { it.previousIndex() }
            expect(false) { it.hasPrevious() }
            assertFailsWith(NoSuchElementException::class) { it.previous() }
            expect(-1) { it.previousIndex() }
        }
    }

    private fun testWrite0(proxy: MutableList<String>) {

        proxy.add("d")
        expect(listOf("a", "b", "c", "d")) {
            proxy
        }

        proxy.addAll(listOf("e", "f"))
        expect(listOf("a", "b", "c", "d", "e", "f")) {
            proxy
        }

        proxy.remove("d")
        expect(listOf("a", "b", "c", "e", "f")) {
            proxy
        }

        proxy.removeAll(listOf("d", "e", "f"))
        expect(listOf("a", "b", "c")) {
            proxy
        }

        proxy.retainAll(listOf("b", "c", "d"))
        expect(listOf("b", "c")) {
            proxy
        }

        proxy.add(0, "a")
        proxy.addAll(listOf("d", "e", "f", "g"))
        expect(listOf("a", "b", "c", "d", "e", "f", "g")) {
            proxy
        }

        proxy.iterator().let {
            while (it.hasNext()) {
                val v = it.next()
                if ((v[0] - 'a') % 2 == 0) {
                    it.remove()
                }
            }
        }
        expect(listOf("b", "d", "f")) {
            proxy
        }

        proxy.add(0, "a")
        proxy.add(2, "c")
        proxy.add(4, "e")
        proxy.add("g")
        expect(listOf("a", "b", "c", "d", "e", "f", "g")) {
            proxy
        }

        proxy.listIterator(proxy.size).let {
            while (it.hasPrevious()) {
                val v = it.previous()
                if ((v[0] - 'a') % 2 == 0) {
                    it.remove()
                }
            }
        }
        expect(listOf("b", "d", "f")) {
            proxy
        }

        proxy.listIterator().let {
            while (it.hasNext()) {
                val new = (it.next()[0] + 1).toChar().toString()
                it.add(new)
            }
        }
        expect(listOf("b", "c", "d", "e", "f", "g")) {
            proxy
        }

        proxy.listIterator(proxy.size).let {
            while (it.hasPrevious()) {
                val old = it.previous()
                val upper = old.uppercase()
                if (upper != old) {
                    it.add(upper)
                }
            }
        }
        expect(listOf("B", "b", "C", "c", "D", "d", "E", "e", "F", "f", "G", "g")) {
            proxy
        }

        proxy.listIterator().let {
            while (it.hasNext()) {
                it.set(it.next() + "*")
            }
        }
        expect(listOf("B*", "b*", "C*", "c*", "D*", "d*", "E*", "e*", "F*", "f*", "G*", "g*")) {
            proxy
        }

        proxy.listIterator(proxy.size).let {
            while (it.hasPrevious()) {
                it.set("*" + it.previous())
            }
        }
        expect(listOf("*B*", "*b*", "*C*", "*c*", "*D*", "*d*", "*E*", "*e*", "*F*", "*f*", "*G*", "*g*")) {
            proxy
        }
    }
}

private fun <E> createSubList(base: List<E>): MutableList<E> =
    ListProxy(base, null)
        .let {
            it.subList(2, it.size - 3)
        }
        .let {
            it.subList(2, it.size - 3)
        }
