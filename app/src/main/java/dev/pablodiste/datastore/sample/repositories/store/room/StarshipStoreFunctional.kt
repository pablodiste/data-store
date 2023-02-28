package dev.pablodiste.datastore.sample.repositories.store.room

import androidx.room.Dao
import dev.pablodiste.datastore.Store
import dev.pablodiste.datastore.adapters.ktor.KtorFetcher
import dev.pablodiste.datastore.adapters.retrofit.RetrofitFetcher
import dev.pablodiste.datastore.adapters.room.RoomListSourceOfTruth
import dev.pablodiste.datastore.impl.NoKey
import dev.pablodiste.datastore.impl.StoreBuilder
import dev.pablodiste.datastore.sample.SampleApplication
import dev.pablodiste.datastore.sample.models.room.Starship
import dev.pablodiste.datastore.sample.models.room.StarshipMapper
import dev.pablodiste.datastore.sample.network.KtorManager
import dev.pablodiste.datastore.sample.network.KtorStarWarsService
import dev.pablodiste.datastore.sample.network.RetrofitManager
import dev.pablodiste.datastore.sample.network.RoomStarWarsService

@Dao
abstract class StarshipSourceOfTruth: RoomListSourceOfTruth<NoKey, Starship>("starships", SampleApplication.roomDb) {
    override fun query(key: NoKey): String = ""
}

private fun provideService(): RoomStarWarsService = RetrofitManager.roomStarWarsService

private fun provideKtorService(): KtorStarWarsService = KtorManager.ktorStarWarsService

fun provideStarshipStore(): Store<NoKey, List<Starship>> =
    StoreBuilder.from(
        fetcher = RetrofitFetcher.of(provideService()) { key, service: RoomStarWarsService -> service.getStarships().results },
        sourceOfTruth = SampleApplication.roomDb.starshipSourceOfTruth(),
        mapper = StarshipMapper()
    ).build()

fun provideStarshipStoreKtor(): Store<NoKey, List<Starship>> =
    StoreBuilder.from(
        fetcher = KtorFetcher.of(provideKtorService()) { key, service: KtorStarWarsService -> service.getStarships().results },
        sourceOfTruth = SampleApplication.roomDb.starshipSourceOfTruth(),
        mapper = StarshipMapper()
    ).build()
