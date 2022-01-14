package org.babyfish.kimmer.runtime.list

internal open class ListProxy<E>(
    private val base: List<E>,
    private val elementHandler: ListElementHandler<E>?
): MutableList<E> {

    init {
        val handler = elementHandler
        if (handler !== null) {
            for (e in base) {
                handler.input(e)
            }
        }
    }

    private var modified: MutableList<E>? = null

    private var modCount = 0

    override fun isEmpty(): Boolean =
        list.isEmpty()

    override val size: Int
        get() = list.size

    override fun contains(element: E): Boolean =
        list.contains(element)

    override fun containsAll(elements: Collection<E>): Boolean =
        list.containsAll(elements)

    override fun get(index: Int): E =
        output(list[index])

    override fun indexOf(element: E): Int =
        list.indexOf(element)

    override fun lastIndexOf(element: E): Int =
        list.lastIndexOf(element)

    override fun add(element: E): Boolean {
        elementHandler?.let {
            it.input(element)
        }
        return mutableList.add(element).also {
            modCount++
        }
    }

    override fun add(index: Int, element: E) {
        elementHandler?.let {
            it.input(element)
        }
        mutableList.add(index, element).also {
            modCount++
        }
    }

    override fun addAll(elements: Collection<E>): Boolean {
        elementHandler?.let {
            for (e in elements) {
                it.input(e)
            }
        }
        return mutableList.addAll(elements).also {
            modCount++
        }
    }

    override fun addAll(index: Int, elements: Collection<E>): Boolean {
        elementHandler?.let {
            for (e in elements) {
                it.input(e)
            }
        }
        return mutableList.addAll(index, elements).also {
            modCount++
        }
    }

    override fun clear() {
        mutableList.clear().also {
            modCount++
        }
    }

    override fun remove(element: E): Boolean =
        mutableList.remove(element).also {
            modCount++
        }

    override fun removeAt(index: Int): E =
        mutableList.removeAt(index).also {
            modCount++
        }.let {
            output(it)
        }

    override fun removeAll(elements: Collection<E>): Boolean =
        mutableList.removeAll(elements).also {
            modCount++
        }

    override fun retainAll(elements: Collection<E>): Boolean =
        mutableList.retainAll(elements).also {
            modCount++
        }

    override fun set(index: Int, element: E): E {
        elementHandler?.let {
            it.input(element)
        }
        return mutableList.set(index, element).also {
            modCount++
        }.let {
            output(it)
        }
    }

    private fun removeRange(headHide: Int, tailHide: Int) {
        mutableList.apply {
            subList(headHide, size - tailHide).clear()
        }
        modCount++
    }

    private fun removeRange(headHide: Int, tailHide: Int, block: (element: E) -> Boolean): Boolean {
        modCount++
        return mutableList.run {
            val itr = subList(headHide, size - tailHide).listIterator(size - headHide - tailHide)
            var changed = false
            while (itr.hasPrevious()) {
                val element = itr.previous()
                if (block(element)) {
                    itr.remove()
                    changed = true
                }
            }
            changed
        }
    }

    override fun iterator(): MutableIterator<E> =
        listIterator(0)

    override fun listIterator(): MutableListIterator<E> =
        listIterator(0)

    override fun listIterator(index: Int): MutableListIterator<E> =
        Itr(0, 0, index)

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<E> {
        if (fromIndex < 0 || toIndex > list.size) {
            throw IndexOutOfBoundsException()
        }
        return SubList(this, fromIndex, list.size - toIndex)
    }

    override fun hashCode(): Int =
        list.hashCode()

    override fun equals(other: Any?): Boolean =
        list == other

    override fun toString(): String =
        list.toString()

    private inline val list: List<E>
        get() = modified ?: base

    private inline val mutableList: MutableList<E>
        get() = modified ?: base.toMutableList().also {
            modified = it
        }

    private inline fun output(e: E): E {
        val handler = elementHandler
        return if (handler !== null) {
            handler.output(e)
        } else {
            e
        }
    }

    fun resolve(): List<E> {

        resolveElements()

        val b = base
        val m = modified
        if (m === null) {
            return b
        }
        if (b.size === m.size) {
            val h = elementHandler
            val itr1 = b.iterator()
            val itr2 = m.iterator()
            var changed = false
            if (h === null) {
                while (!changed && itr1.hasNext() && itr2.hasNext()) {
                    changed = itr1.next() != itr2.next()
                }
            } else {
                while (!changed && itr1.hasNext() && itr2.hasNext()) {
                    changed = h.changed(itr1.next(), itr2.next())
                }
            }
            if (!changed) {
                return b
            }
        }
        return m
    }

    private fun resolveElements() {
        val h = elementHandler
        if (h !== null) {
            val itr = listIterator()
            while (itr.hasNext()) {
                val unresolved = itr.next()
                val resolved = h.resolve(unresolved)
                if (unresolved !== resolved) {
                    itr.set(resolved)
                }
            }
        }
    }

    private inner class Itr(
        private val headHide: Int,
        private val tailHide: Int,
        index: Int,
        private val modCountChanged: ((Int) -> Unit)? = null
    ): MutableListIterator<E> {

        private var absIndex = headHide + index

        private var cursor: Cursor? = null

        private var modCount = this@ListProxy.modCount

        private var base: ListIterator<E>? = this@ListProxy.list.listIterator(absIndex)

        private var modified: MutableListIterator<E>? = null

        override fun hasNext(): Boolean = execute {
            absIndex < this@ListProxy.list.size - tailHide
        }

        override fun next(): E  = execute {
            if (absIndex >= this@ListProxy.list.size - tailHide) {
                throw NoSuchElementException()
            }
            cursor = Cursor(true, absIndex++)
            output(itr.next())
        }

        override fun nextIndex(): Int = execute {
            absIndex - headHide
        }

        override fun hasPrevious(): Boolean = execute {
            absIndex > headHide
        }

        override fun previous(): E = execute {
            if (absIndex <= headHide) {
                throw NoSuchElementException()
            }
            cursor = Cursor(false, --absIndex)
            output(itr.previous())
        }

        override fun previousIndex(): Int = execute {
            absIndex - headHide - 1
        }

        override fun remove() {
            execute(true) {
                val pos = cursor?.pos ?: throw IllegalStateException()
                mutableItr.remove()
                if (pos < absIndex) {
                    absIndex--
                }
                cursor = null
            }
        }

        override fun add(element: E) {
            execute(true) {
                elementHandler?.input(element)
                mutableItr.add(element)
                absIndex++
                cursor = null
            }
        }

        override fun set(element: E) {
            execute(true) {
                cursor ?: throw IllegalStateException()
                elementHandler?.input(element)
                mutableItr.set(element)
            }
        }

        private inline fun <T> execute(forUpdate: Boolean = false, block: () -> T): T {
            if (modCount != this@ListProxy.modCount) {
                throw ConcurrentModificationException()
            }
            return if (forUpdate) {
                val result = block()
                modCount = ++this@ListProxy.modCount
                modCountChanged?.let {
                    it(modCount)
                }
                result
            } else {
                block()
            }
        }

        private val itr: ListIterator<E>
            get() = modified ?: base ?: error("Internal bug")

        private val mutableItr: MutableListIterator<E>
            get() = modified ?:
                createMutableItr().also {
                    modified = it
                    base = null
                }

        private fun createMutableItr(): MutableListIterator<E> =
            cursor?.recreate(this@ListProxy.mutableList)
                ?: this@ListProxy.mutableList.listIterator(absIndex)
    }

    private class SubList<E>(
        private val proxy: ListProxy<E>,
        private val headHide: Int,
        private val tailHide: Int
    ): MutableList<E> {

        private var modCount = proxy.modCount

        override fun isEmpty(): Boolean = execute {
            proxy.size <= headHide + tailHide
        }

        override val size: Int
            get() = execute {
                proxy.size - headHide - tailHide
            }

        override fun contains(element: E): Boolean = execute {
            val absIndex = proxy.indexOf(element)
            absIndex >= headHide && absIndex < proxy.size - tailHide
        }

        override fun containsAll(elements: Collection<E>): Boolean = execute {
            for (element in elements) {
                val absIndex = proxy.indexOf(element)
                if (absIndex < headHide || absIndex >= proxy.size - tailHide) {
                    return@execute false
                }
            }
            true
        }

        override fun get(index: Int): E = execute {
            if (index < 0 || index >= proxy.size - headHide - tailHide) {
                throw IndexOutOfBoundsException()
            }
            proxy[headHide + index]
        }

        override fun indexOf(element: E): Int = execute {
            val absIndex = proxy.indexOf(element)
            if (absIndex >= headHide && absIndex < proxy.size - tailHide) {
                absIndex - headHide
            } else {
                -1
            }
        }

        override fun lastIndexOf(element: E): Int = execute {
            val absIndex = proxy.lastIndexOf(element)
            if (absIndex >= headHide && absIndex < proxy.size - tailHide) {
                absIndex - headHide
            } else {
                -1
            }
        }

        override fun add(element: E): Boolean = execute(true) {
            proxy.add(proxy.size - tailHide, element)
            true
        }

        override fun add(index: Int, element: E) = execute(true) {
            if (index < 0 || index >= proxy.size - headHide - tailHide) {
                throw IndexOutOfBoundsException()
            }
            proxy.add(index + headHide, element)
        }

        override fun addAll(elements: Collection<E>): Boolean = execute(true) {
            proxy.addAll(proxy.size - tailHide, elements)
        }

        override fun addAll(index: Int, elements: Collection<E>): Boolean = execute(true) {
            if (index < 0 || index >= proxy.size - headHide - tailHide) {
                throw IndexOutOfBoundsException()
            }
            proxy.addAll(index + headHide, elements)
            true
        }

        override fun clear() = execute(true) {
            proxy.removeRange(headHide, tailHide)
        }

        override fun remove(element: E): Boolean = execute(true) {
            proxy.removeRange(headHide, tailHide) {
                it == element
            }
        }

        override fun removeAt(index: Int): E = execute(true) {
            if (index < 0 || index >= proxy.size - headHide - tailHide) {
                throw IndexOutOfBoundsException()
            }
            proxy.removeAt(index + headHide)
        }

        override fun removeAll(elements: Collection<E>): Boolean = execute(true) {
            proxy.removeRange(headHide, tailHide) {
                elements.contains(it)
            }
        }

        override fun retainAll(elements: Collection<E>): Boolean = execute(true) {
            proxy.removeRange(headHide, tailHide) {
                !elements.contains(it)
            }
        }

        override fun set(index: Int, element: E): E = execute {
            if (index < 0 || index >= proxy.size - headHide - tailHide) {
                throw IndexOutOfBoundsException()
            }
            proxy.set(index + headHide, element)
        }

        override fun iterator(): MutableIterator<E> =
            this.listIterator(0)

        override fun listIterator(): MutableListIterator<E> = this.listIterator(0)

        override fun listIterator(index: Int): MutableListIterator<E> = execute {
            proxy.Itr(headHide, tailHide, index) {
                modCount = it
            }
        }

        override fun subList(fromIndex: Int, toIndex: Int): MutableList<E> = execute {
            val size = proxy.size - headHide - tailHide
            if (fromIndex > toIndex) {
                throw IllegalArgumentException()
            }
            if (fromIndex < 0 || toIndex > size) {
                throw IndexOutOfBoundsException()
            }
            SubList(
                proxy,
                headHide + fromIndex,
                tailHide + size - toIndex
            )
        }

        private inline fun <T> execute(forUpdate: Boolean = false, block: () -> T): T {
            if (modCount != proxy.modCount) {
                throw ConcurrentModificationException()
            }
            return if (forUpdate) {
                val result = block()
                modCount = proxy.modCount
                result
            } else {
                block()
            }
        }
    }

    private data class Cursor(
        val next: Boolean,
        val pos: Int
    ) {
        fun <E> recreate(list: MutableList<E>): MutableListIterator<E> =
            if (next) {
                list.listIterator(pos).also {
                    it.next()
                }
            } else {
                list.listIterator(pos + 1).also {
                    it.previous()
                }
            }
    }
}

internal interface ListElementHandler<E> {
    fun input(element: E): Unit
    fun output(element: E): E
    fun resolve(element: E): E
    fun changed(a: E, b: E): Boolean
}