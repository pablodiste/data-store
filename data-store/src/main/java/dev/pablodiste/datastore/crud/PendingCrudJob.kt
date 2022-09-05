package dev.pablodiste.datastore.crud

data class PendingCrudJob<K: Any, I: Any>(
    val crudOperation: CrudOperation,
    val key: K,
    val entity: I)

enum class CrudOperation {
    CREATE, UPDATE, DELETE
}
