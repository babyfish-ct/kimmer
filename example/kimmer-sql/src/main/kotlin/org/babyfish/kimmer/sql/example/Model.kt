import org.babyfish.kimmer.sql.Entity
import java.util.*

interface BookStore: Entity<UUID> {
    val name: String
    val books: List<Book>
}

interface Book: Entity<UUID> {
    val name: String
    val store: BookStore
    val authors: List<Author>
}

interface Author: Entity<String> {
    val name: String
    val books: List<Book>
}