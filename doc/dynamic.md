# [kimmer](https://github.com/babyfish-ct/kimmer)/[documentation](./README.md)/Dynamics & unloaded properties

Kimmer is designed for server-side development, so dynamics is important.

1. Take GraphQL as example, its data shape is inherently dynamic.
2. Take ORM as example, not all properties(especially associated properties) always need to be queried.

To support this dynamism, kimmer introduced the concept of "unload property", e.g.
```kt
interface TreeNode: Immutable {
    val name: String
    val childNodes: List<TreeNode>
}
val treeNode = new(TreeNode::class).by {
    name = "RootNode"
}
```
Here
1. The user assigned value to name, so name is a loaded field
2. The user did not assign any value to childNodes, so childNodes is an unloaded field

### 1. UnloadedException

Direct access to unloaded properties causes org.babyfish.kimmer.UnloadedException

```kt
val treeNode = new(TreeNode::class).by {
    name = "RootNode"
}
println(treeNode.name) // OK
println(treeNode.childNodes) // Error
```
The output is
```
Exception in thread "main" org.babyfish.kimmer.UnloadedException: The field 'val example.TreeNode.childNodes: kotlin.collections.List<example.TreeNode>' is unloaded
	at org.babyfish.kimmer.runtime.asm.TreeNode{Implementation}-232685612.getChildNodes(Unknown Source)
	at example.AppKt.main(App.kt:29)
```

### 2. Json serialization for unload properties

Unlike direct access, in JSON serialization, unloaded properties do not cause an exception, but are automatically ignored.

JSON serialization is discussed in other documents, here we simply print the object, because the toString function is also implemented through JSON serialization。

```kt
val treeNode = new(TreeNode::class).by {
    name = "RootNode"
}
println(treeNode)
```
The output is
```
{"name":"RootNode"}
```

### 3. Check if properties are loaded
```kt
val treeNode = new(TreeNode::class).by {
    name = "RootNode"
}
println(Immutable.isLoaded(treeNode, TreeNode::name))
println(Immutable.isLoaded(treeNode, TreeNode::childNodes))
```
The output is
```
true
false
```

### 4. Unload properties manually

What we saw earlier is that when an object is created, all properties are initially unloaded, and properties automatically become loaded when they are assigned.

However, we can do the reverse manually, using function "Draft.unload" to turn a loaded property back into an unloaded property.

```kt
val treeNode = new(TreeNode::class).by {
    name = "RootNode"
    childNodes = emptyList()
}
val treeNode2 = new(TreeNode::class).by(treeNode) {
    Draft.unload(this, TreeNode::childNodes)
}
println(Immutable.isLoaded(treeNode, TreeNode::name))
println(Immutable.isLoaded(treeNode, TreeNode::childNodes))
println(treeNode)
println(Immutable.isLoaded(treeNode2, TreeNode::name))
println(Immutable.isLoaded(treeNode2, TreeNode::childNodes))
println(treeNode2)
```

The output is
```
true
true
{"name":"RootNode",childNodes:[]}
true
false
{"name":"RootNode"}
```

---------------

[<Previous: Get started](./get-started.md) | [Back to document](./README.md) | [Next: Draft property vs Draft function >](propfun.md)
