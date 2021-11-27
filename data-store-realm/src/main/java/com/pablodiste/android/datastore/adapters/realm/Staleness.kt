package com.pablodiste.android.datastore.adapters.realm

import com.pablodiste.android.datastore.HasKey
import io.realm.Realm
import io.realm.RealmObject

interface StalenessPolicy<K: Any, T: RealmObject> {
    fun removeStaleEntityInRealm(bgRealm: Realm, cache: RealmCache<K, T>, key: K, entity: T)
    fun removeStaleListInRealm(bgRealm: Realm, cache: RealmListCache<K, T>, key: K, entity: List<T>)
}

/**
 * This staleness policy does not delete any entity.
 */
class DoNotExpireStalenessPolicy<K: Any, T: RealmObject>: StalenessPolicy<K, T> {
    override fun removeStaleEntityInRealm(bgRealm: Realm, cache: RealmCache<K, T>, key: K, entity: T) {
    }

    override fun removeStaleListInRealm(bgRealm: Realm, cache: RealmListCache<K, T>, key: K, entity: List<T>) {
    }
}

/**
 * This staleness policy executes the repository query and deletes all entities that matched.
 */
class DeleteAllStalenessPolicy<K: Any, T: RealmObject>: StalenessPolicy<K, T> {

    override fun removeStaleListInRealm(bgRealm: Realm, cache: RealmListCache<K, T>, key: K, entity: List<T>) {
        val where = bgRealm.where(cache.klass)
        cache.query(key).invoke(where)
        where.findAll().deleteAllFromRealm()
    }

    override fun removeStaleEntityInRealm(bgRealm: Realm, cache: RealmCache<K, T>, key: K, entity: T) {
        val where = bgRealm.where(cache.klass)
        cache.query(key).invoke(where)
        where.findFirst()?.deleteFromRealm()
    }
}

/**
 * This staleness policy removes all stored entities which are not included in the API response
 * Requires the entities to implement HasKey interface to compare by the keys
 */
class DeleteAllNotInFetchStalenessPolicy<K: Any, T: RealmObject>: StalenessPolicy<K, T> {

    override fun removeStaleListInRealm(bgRealm: Realm, cache: RealmListCache<K, T>, key: K, entity: List<T>) {
        // Collects all keys from API call
        val inputKeys = entity.mapNotNullTo(HashSet()) { i -> if (i is HasKey) (i as HasKey).getKey() else null }
        val where = bgRealm.where(cache.klass)
        cache.query(key).invoke(where)
        val results = where.findAll()
        results.forEach { storedEntity ->
            if (storedEntity is HasKey) {
                // if the store contains an entity not in the API response, it gets deleted
                if (!inputKeys.contains((storedEntity as HasKey).getKey())) {
                    storedEntity.deleteFromRealm()
                }
            }
        }
    }

    override fun removeStaleEntityInRealm(bgRealm: Realm, cache: RealmCache<K, T>, key: K, entity: T) {
        val where = bgRealm.where(cache.klass)
        cache.query(key).invoke(where)
        where.findFirst()?.deleteFromRealm()
    }
}
