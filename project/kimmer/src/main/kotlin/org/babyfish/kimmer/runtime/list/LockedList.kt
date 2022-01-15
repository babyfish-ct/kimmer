package org.babyfish.kimmer.runtime.list

internal open class LockedList<E>(
    protected val target: MutableList<E>,
    private val mutext: Any
): MutableList<E> {

    override fun isEmpty(): Boolean = synchronized(mutext) {
        target.isEmpty()
    }

    override val size: Int
        get() = synchronized(mutext) {
            target.size
        }

    override fun contains(element: E): Boolean = synchronized(mutext) {
        target.contains(element)
    }

    override fun containsAll(elements: Collection<E>): Boolean = synchronized(mutext) {
        target.containsAll(elements)
    }

    override fun indexOf(element: E): Int = synchronized(mutext) {
        target.indexOf(element)
    }

    override fun lastIndexOf(element: E): Int = synchronized(mutext) {
        target.lastIndexOf(element)
    }

    override fun get(index: Int): E = synchronized(mutext) {
        target[index]
    }

    override fun add(element: E): Boolean = synchronized(mutext) {
        target.add(element)
    }

    override fun add(index: Int, element: E) = synchronized(mutext) {
        target.add(index, element)
    }

    override fun addAll(elements: Collection<E>): Boolean = synchronized(mutext) {
        target.addAll(elements)
    }

    override fun addAll(index: Int, elements: Collection<E>): Boolean = synchronized(mutext) {
        target.addAll(index, elements)
    }

    override fun clear() = synchronized(mutext) {
        target.clear()
    }

    override fun remove(element: E): Boolean = synchronized(mutext) {
        target.remove(element)
    }

    override fun removeAt(index: Int): E = synchronized(mutext) {
        target.removeAt(index)
    }

    override fun removeAll(elements: Collection<E>): Boolean = synchronized(mutext) {
        target.removeAll(elements)
    }

    override fun retainAll(elements: Collection<E>): Boolean = synchronized(mutext) {
        target.retainAll(elements)
    }

    override fun set(index: Int, element: E): E = synchronized(mutext) {
        target.set(index, element)
    }

    override fun iterator(): MutableIterator<E> = synchronized(mutext) {
        Itr(target.iterator(), mutext)
    }

    override fun listIterator(): MutableListIterator<E> = synchronized(mutext) {
        ListItr(target.listIterator(), mutext)
    }

    override fun listIterator(index: Int): MutableListIterator<E> = synchronized(mutext) {
        ListItr(target.listIterator(index), mutext)
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<E> = synchronized(mutext) {
        SubList(target.subList(fromIndex, toIndex), mutext)
    }

    override fun hashCode(): Int = synchronized(mutext) {
        target.hashCode()
    }

    override fun equals(other: Any?): Boolean = synchronized(mutext) {
        target == other
    }

    override fun toString(): String = synchronized(mutext) {
        target.toString()
    }

    private abstract class AbstractItr<E>(
        private val mutex: Any
    ): MutableIterator<E> {

        override fun hasNext(): Boolean = synchronized(mutex) {
            target.hasNext()
        }

        override fun next(): E = synchronized(mutex) {
            target.next()
        }

        override fun remove() {
            synchronized(mutex) {
                target.remove()
            }
        }

        protected abstract val target: MutableIterator<E>
    }

    private class Itr<E>(
        override val target: MutableIterator<E>,
        private val mutex: Any
    ): AbstractItr<E>(mutex)

    private class ListItr<E>(
        override val target: MutableListIterator<E>,
        private val mutex: Any
    ): AbstractItr<E>(mutex), MutableListIterator<E> {

        override fun nextIndex(): Int = synchronized(mutex) {
            target.nextIndex()
        }

        override fun hasPrevious(): Boolean = synchronized(mutex) {
            target.hasPrevious()
        }

        override fun previous(): E = synchronized(mutex) {
            target.previous()
        }

        override fun previousIndex(): Int = synchronized(mutex) {
            target.previousIndex()
        }

        override fun add(element: E) = synchronized(mutex) {
            target.add(element)
        }

        override fun set(element: E) = synchronized(mutex) {
            target.set(element)
        }
    }

    private class SubList<E>(
        target: MutableList<E>,
        mutex: Any
    ): LockedList<E>(target, mutex)
}