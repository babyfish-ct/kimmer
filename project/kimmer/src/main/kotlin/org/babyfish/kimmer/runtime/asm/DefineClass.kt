package org.babyfish.kimmer.runtime.asm

import java.lang.invoke.MethodHandles

fun ByteArray.defineClass(): Class<*> =
    MethodHandles.lookup().defineClass(this)