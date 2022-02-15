# Why do you need a kimmer?

In practical projects, objects of different types are often related. 

Here, in order to simplify the discussion, an tree-like relationship between objects of the same type is discussed.

## 1. Use copy function of data class

```kt
data class Node(
    val name: String,
    val childNodes: List<Node>
)

val node = ...blabla...
```

1. Change the name of the current object to "Hello"
```kt
val newNode = node.copy(name = "Hello")
```

2. Change the name of a first-level object to "Hello"
   
   Breadcrumbs condition as follows
   1. first-level object position: pos1
   
```kt
val newNode = node.copy(
    childNodes = node
        .childNodes
        .toMutableList().apply {
            this[pos1] = this[pos1].copy(name = "X")
        }
)
```

3. Change the name of a second-level object to "Hello"

   Breadcrumbs condition as follows
   1. first-level object position: pos1
   2. second-level object position: pos2

```kt
val newNode = node.copy(
    childNodes = node
        .childNodes
        .toMutableList().apply {
            this[pos1] = this[pos1].copy(
                childNodes = this[pos1]
                    .childNodes
                    .toMutableList()
                    .apply {
                        this[pos2] = this[pos2].copy(name = "Hello")
                    }
            )
        }
)
```

4. Change the name of a third-level object to "Hello"

   Breadcrumbs condition as follows
   1. first-level object position: pos1
   2. second-level object position: pos2
   3. second-level object position: pos3
```kt
val newNode = node.copy(
    childNodes = node
        .childNodes
        .toMutableList().apply {
            this[pos1] = this[pos1].copy(
                childNodes = this[pos1]
                    .childNodes
                    .toMutableList()
                    .apply {
                        this[pos2] = this[pos2].copy(
                            childNodes = this[pos2]
                                .childNodes
                                .toMutableList()
                                .apply {
                                    this[pos3] = this[pos3].copy(name = "Hello")
                                }
                        )
                    }
            )
        }
    )
```

## 2.Why are immutable objects so hard to "modify"?

If the subordinate object changes, the superior object must change too. 

The change is propagated from bottom to top until the root object.

## 3. How do kimmer solve this problem?

```kt
interface Node: Immutable {
    val name: String,
    val childNodes: List<Node>
}

val node = ...blabla...

val newNode = new(Node::class).by(node) {
    childNodes()[pos1]
        .childNodes()[pos2]
        .childNodes()[pos3]
        .name = "Hello"
}
```
-----

[Back to home](https://github.com/babyfish-ct/kimmer)
