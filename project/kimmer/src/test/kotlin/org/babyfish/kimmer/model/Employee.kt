package org.babyfish.kimmer.model

import org.babyfish.kimmer.Immutable

interface Employee: Immutable {
    val supervisor: Employee?
}