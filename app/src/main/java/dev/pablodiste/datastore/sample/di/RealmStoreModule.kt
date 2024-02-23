package dev.pablodiste.datastore.sample.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.pablodiste.datastore.sample.network.StarWarsService
import dev.pablodiste.datastore.sample.repositories.store.realm.RealmPeopleStore
import dev.pablodiste.datastore.sample.repositories.store.realm.RealmPersonStore
import dev.pablodiste.datastore.sample.repositories.store.realm.RealmPlanetsStore
import dev.pablodiste.datastore.sample.repositories.store.realm.dao.PeopleSourceOfTruth
import dev.pablodiste.datastore.sample.repositories.store.realm.dao.PersonSourceOfTruth
import dev.pablodiste.datastore.sample.repositories.store.realm.dao.PlanetSourceOfTruth
import dev.pablodiste.datastore.sample.repositories.store.realm.fetchers.PeopleFetcher
import dev.pablodiste.datastore.sample.repositories.store.realm.fetchers.PersonFetcher
import dev.pablodiste.datastore.sample.repositories.store.realm.fetchers.PlanetsFetcher

@Module
@InstallIn(SingletonComponent::class)
class RealmStoreModule {

    @Provides
    fun providePeopleSourceOfTruth() = PeopleSourceOfTruth()

    @Provides
    fun providePeopleFetcher(starWarsService: StarWarsService) = PeopleFetcher(starWarsService)

    @Provides
    fun provideRealmPeopleStore(fetcher: PeopleFetcher, peopleSourceOfTruth: PeopleSourceOfTruth) =
        RealmPeopleStore(fetcher, peopleSourceOfTruth)

    @Provides
    fun providePersonSourceOfTruth() = PersonSourceOfTruth()

    @Provides
    fun providePersonFetcher(starWarsService: StarWarsService) = PersonFetcher(starWarsService)

    @Provides
    fun provideRealmPersonStore(fetcher: PersonFetcher, personSourceOfTruth: PersonSourceOfTruth) =
        RealmPersonStore(fetcher, personSourceOfTruth)

    @Provides
    fun providePlanetSourceOfTruth() = PlanetSourceOfTruth()

    @Provides
    fun providePlanetFetcher(starWarsService: StarWarsService) = PlanetsFetcher(starWarsService)

    @Provides
    fun provideRealmPlanetStore(fetcher: PlanetsFetcher, planetSourceOfTruth: PlanetSourceOfTruth) =
        RealmPlanetsStore(fetcher, planetSourceOfTruth)
}