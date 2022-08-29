package dev.pablodiste.datastore.sample.repositories.store.room

import androidx.room.Dao
import dev.pablodiste.datastore.FetcherResult
import dev.pablodiste.datastore.Store
import dev.pablodiste.datastore.adapters.ktor.KtorFetcher
import dev.pablodiste.datastore.adapters.room.RoomListSourceOfTruth
import dev.pablodiste.datastore.impl.LimitedFetcher
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

fun provideStarshipStore(): Store<NoKey, List<Starship>> =
    StoreBuilder.from(
        fetcher = LimitedFetcher.of { key -> FetcherResult.Data(provideStarWarsService().getStarships().results) },
        sourceOfTruth = SampleApplication.roomDb.starshipSourceOfTruth(),
        mapper = StarshipMapper()
    ).build()

private fun provideStarWarsService() = RetrofitManager.createService(RoomStarWarsService::class.java)

fun provideStarshipStoreKtor(): Store<NoKey, List<Starship>> =
    StoreBuilder.from(
        fetcher = KtorFetcher.of(KtorManager) { key, service: KtorStarWarsService -> FetcherResult.Data(service.getStarships().results) },
        sourceOfTruth = SampleApplication.roomDb.starshipSourceOfTruth(),
        mapper = StarshipMapper()
    ).build()

