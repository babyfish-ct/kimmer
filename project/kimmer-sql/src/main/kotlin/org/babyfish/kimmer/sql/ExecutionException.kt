package org.babyfish.kimmer.sql

open class ExecutionException(message: String, cause: Throwable? = null):
    RuntimeException(message, cause)