package dev.pablodiste.datastore.adapters.realm

import io.realm.Realm

typealias UseRealm = (realm: Realm) -> Unit

fun useRealm(use: UseRealm) {
    Realm.getDefaultInstance().run {
        use(this)
        close()
    }
}

fun executeRealmTransaction(use: UseRealm) {
    Realm.getDefaultInstance().run {
        executeTransaction {
            use(it)
        }
        close()
    }
}

fun executeRealmAsyncTransaction(use: UseRealm) {
    Realm.getDefaultInstance().run {
        executeTransactionAsync({
            use(it)
        }, {
            close()
        }, {
            close()
        })
    }
}