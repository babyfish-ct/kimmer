package org.babyfish.kimmer.runtime

import org.babyfish.kimmer.Draft
import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.graphql.Connection
import org.babyfish.kimmer.graphql.impl.*
import org.babyfish.kimmer.graphql.impl.AsyncConnectionDraftImpl
import org.babyfish.kimmer.graphql.impl.ConnectionImplementor
import org.babyfish.kimmer.graphql.impl.EdgeImplementor
import org.babyfish.kimmer.graphql.impl.SyncConnectionDraftImpl
import org.babyfish.kimmer.graphql.impl.SyncEdgeDraftImpl
import org.babyfish.kimmer.graphql.meta.ConnectionEdgeType
import org.babyfish.kimmer.graphql.meta.ConnectionType
import org.babyfish.kimmer.meta.ImmutableType
import org.babyfish.kimmer.runtime.list.ListDraft
import org.babyfish.kimmer.runtime.list.LockedListDraft
import org.babyfish.kimmer.runtime.list.SimpleListDraft
import java.util.*
import kotlin.reflect.KClass

internal interface DraftContext {

    fun <T: Immutable> createDraft(type: KClass<T>, base: T?): Draft<T>

    fun <T: Immutable> toDraft(obj: T?): Draft<T>?

    fun <E: Immutable> toDraft(list: List<E>?): MutableList<E>?

    fun <T: Immutable> resolve(obj: T?): T?

    fun <E: Immutable> resolve(list: List<E>?): List<E>?
}

internal abstract class AbstractDraftContext: DraftContext {

    private val objDraftMap = IdentityHashMap<Immutable, Draft<*>>()

    private val listDraftMap = IdentityHashMap<List<*>, ListDraft<*>>()

    override fun <T : Immutable> createDraft(type: KClass<T>, base: T?): Draft<T> {
        val raw = base
            ?: Factory.of(type).create()
        return toDraft(raw)!!
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T: Immutable> toDraft(obj: T?): Draft<T>? {
        if (obj === null) {
            return null
        }
        if (obj is Draft<*>) {
            if ((obj as DraftSpi).`{draftContext}`() !== this) {
                throw IllegalArgumentException("Cannot accept draft object created by another DraftContext")
            }
            return obj as Draft<T>
        }
        return objDraftMap.computeIfAbsent(obj) {
            createObjectDraft(obj)
        } as Draft<T>
    }

    @Suppress("UNCHECKED_CAST")
    override fun <E: Immutable> toDraft(list: List<E>?): MutableList<E>? {
        if (list === null) {
            return null
        }
        if (list is ListDraft<*>) {
            if (list.draftContext !== this) {
                throw IllegalArgumentException("Cannot accept draft list created by another DraftContext")
            }
            return list as MutableList<E>
        }
        return listDraftMap.computeIfAbsent(list) {
            this.createListDraft(list)
        } as MutableList<E>
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Immutable> resolve(obj: T?): T? {
        if (obj === null) {
            return null
        }
        val draft = obj as? Draft<*> ?: objDraftMap[obj]
        if (draft === null) {
            return obj
        }
        val spi = draft as DraftSpi
        if (spi.`{draftContext}`() !== this) {
            throw IllegalArgumentException(
                "Cannot resolve the draft object '${spi}' because it belong to another draft context"
            )
        }
        return spi.`{resolve}`() as T
    }

    @Suppress("UNCHECKED_CAST")
    override fun <E : Immutable> resolve(list: List<E>?): List<E>? {
        if (list === null) {
            return null
        }
        val draft = list as? ListDraft<*> ?: listDraftMap[list]
        return if (draft === null) {
            var newList: MutableList<E>? = null
            list.forEachIndexed { index, obj ->
                val resolved = resolve(obj) as E
                if (resolved !== obj && newList === null) {
                    newList = list.subList(0, index).toMutableList()
                }
                newList?.add(resolved)
            }
            newList ?: list
        } else {
            if (draft.draftContext !== this) {
                throw IllegalArgumentException(
                    "Cannot resolve the draft list '${list}' because it belong to another draft context"
                )
            }
            draft.resolve() as List<E>
        }
    }

    protected abstract fun createObjectDraft(obj: Immutable): Draft<*>

    protected abstract fun createListDraft(list: List<*>): ListDraft<*>
}

internal class SyncDraftContext: AbstractDraftContext() {

    @Suppress("UNCHECKED_CAST")
    override fun createObjectDraft(obj: Immutable): Draft<*> =
        when (Immutable.type(obj).kotlinType) {
            Connection::class ->
                SyncConnectionDraftImpl(
                    this,
                    Immutable.type(obj) as ConnectionType,
                    obj as ConnectionImplementor<*>
                )
            Connection.Edge::class ->
                SyncEdgeDraftImpl(
                    this,
                    Immutable.type(obj) as ConnectionEdgeType,
                    obj as EdgeImplementor<*>
                )
            else ->
                Factory
                    .of(ImmutableType.fromInstance(obj).kotlinType.java as Class<Immutable>)
                    .createDraft(this, obj)
        }

    @Suppress("UNCHECKED_CAST")
    override fun createListDraft(list: List<*>): ListDraft<*> =
        SimpleListDraft(this, list as List<Immutable>)
}

internal class AsyncDraftContext: AbstractDraftContext() {

    @Suppress("UNCHECKED_CAST")
    override fun createObjectDraft(obj: Immutable): Draft<*> =
        when (Immutable.type(obj).kotlinType) {
            Connection::class ->
                AsyncConnectionDraftImpl(
                    this,
                    Immutable.type(obj) as ConnectionType,
                    obj as ConnectionImplementor<*>
                )
            Connection.Edge::class ->
                AsyncEdgeDraftImpl(
                    this,
                    Immutable.type(obj) as ConnectionEdgeType,
                    obj as EdgeImplementor<*>
                )
            else ->
                Factory
                    .of(ImmutableType.fromInstance(obj).kotlinType.java as Class<Immutable>)
                    .createDraft(this, obj)
        }

    @Suppress("UNCHECKED_CAST")
    override fun createListDraft(list: List<*>): ListDraft<*> {
        return LockedListDraft(this, list as List<Immutable>)
    }
}
