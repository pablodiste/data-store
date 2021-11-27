package com.pablodiste.android.datastore.adapters.realm

import io.realm.Realm

typealias UseRealm = (realm: Realm) -> Unit

//Don't like using any but until we move all repos to inheriting the same thing we will keep like this
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