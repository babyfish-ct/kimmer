[kimmer](https://github.com/babyfish-ct/kimmer/)/[document](./README.md)/Jackson

> Notes
> 
> Unloaded properties will be ignored in JSON serialization, which has been discussed in Dynamics & unloaded properties, and will not be repeated in this article.

### 1. Implicit serialization/Deserialization

```
val book = new(Book::class).by(oldBook) {...omit...}
val json = book.toString()
val deserializedBook = Immutable.fromString(json, Book::class)
println(book === deserializedBook)
println(book == deserializedBook)
```

The output is

```
false
true
```

### 2. Explict Serialization/Deserialization

> If you want serialize/deserialize explicitly, please add fastxml/jackson to your project by your self.

Kimmer uses "org.babyfish.kimmer.immutableObjectMapper()" to create ObjectMapper for immutable objects.

```
val book = new(Book::class).by(oldBook) {...omit...}

val mapper = immutableObjectMapper()
val json = mapper.writeValueAsString(book)
val deserializedBook = mapper.readValue(json, Book::class.java)
println(book === deserializedBook)
println(book == deserializedBook)
```

The output is

```
false
true
```

### 3. Spring-Boot

If kimmer is added into spring boot application, the default object mapper created by spring boot can handle immutable objects, you don't need to do anything.

### 4. Polymorphic serialization/deserialization

Although kimmer customizes the JSON mapping mechanism of immutable objects, annotations related to polymorphism can also be used.

```kt
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName

import org.babyfish.kimmer.Immutable

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "__typename")
@JsonSubTypes(
    JsonSubTypes.Type(value = Tiger::class),
    JsonSubTypes.Type(value = Otter::class)
)
interface Animal: Immutable {
    val zoo: Zoo
}

@JsonTypeName("Tiger")
interface Tiger: Animal {
    val weight: Int
}

@JsonTypeName("Otter")
interface Otter: Animal {
    val length: Int
}
```

-------------------

[< Previous: Draft property vs Draft function](propfun.md) | [Back to document](./README.md) | [Next: Abstract.md >](./abstract.md)
