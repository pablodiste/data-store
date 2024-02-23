package dev.pablodiste.datastore.sample.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.pablodiste.datastore.CrudFetcher
import dev.pablodiste.datastore.Fetcher
import dev.pablodiste.datastore.FetcherResult
import dev.pablodiste.datastore.Store
import dev.pablodiste.datastore.adapters.ktor.KtorFetcher
import dev.pablodiste.datastore.adapters.retrofit.RetrofitFetcher
import dev.pablodiste.datastore.adapters.retrofit.RetrofitSender
import dev.pablodiste.datastore.crud.SimpleCrudStoreBuilder
import dev.pablodiste.datastore.crud.SimpleCrudStoreImpl
import dev.pablodiste.datastore.fetchers.limit
import dev.pablodiste.datastore.impl.NoKey
import dev.pablodiste.datastore.impl.SimpleStoreBuilder
import dev.pablodiste.datastore.impl.SimpleStoreImpl
import dev.pablodiste.datastore.impl.StoreBuilder
import dev.pablodiste.datastore.ratelimiter.RateLimitPolicy
import dev.pablodiste.datastore.sample.SampleApplication
import dev.pablodiste.datastore.sample.database.AppDatabase
import dev.pablodiste.datastore.sample.models.room.DummyPost
import dev.pablodiste.datastore.sample.models.room.People
import dev.pablodiste.datastore.sample.models.room.Post
import dev.pablodiste.datastore.sample.models.room.Starship
import dev.pablodiste.datastore.sample.models.room.StarshipMapper
import dev.pablodiste.datastore.sample.network.DummyJsonService
import dev.pablodiste.datastore.sample.network.JsonPlaceholderService
import dev.pablodiste.datastore.sample.network.KtorStarWarsService
import dev.pablodiste.datastore.sample.network.RoomStarWarsService
import dev.pablodiste.datastore.sample.repositories.store.room.fetchers.PeopleFetcher
import dev.pablodiste.datastore.sample.repositories.store.room.fetchers.PersonFetcher
import dev.pablodiste.datastore.sample.repositories.store.room.fetchers.PersonFetcherWithError
import dev.pablodiste.datastore.sample.repositories.store.room.RoomPeopleStore
import dev.pablodiste.datastore.sample.repositories.store.room.RoomPersonStore
import dev.pablodiste.datastore.sample.repositories.store.room.RoomPersonStoreWithError
import dev.pablodiste.datastore.sample.repositories.store.room.dao.DummyPostId
import dev.pablodiste.datastore.sample.repositories.store.room.dao.PostKey
import kotlin.time.Duration.Companion.seconds

@Module
@InstallIn(SingletonComponent::class)
class RoomStoreModule {

    @Provides
    fun provideDummyPostsStore(apiService: DummyJsonService, roomDatabase: AppDatabase): SimpleStoreImpl<NoKey, List<DummyPost>> {
        return SimpleStoreBuilder.from(
            fetcher = RetrofitFetcher.of(apiService) { _, service: DummyJsonService -> service.getPosts().posts },
            sourceOfTruth = roomDatabase.dummyPostsSourceOfTruth()
        ).build()
    }

    @Provides
    fun provideDummyPostStore(apiService: DummyJsonService, roomDatabase: AppDatabase): SimpleCrudStoreImpl<DummyPostId, DummyPost> {
        return SimpleCrudStoreBuilder.from(
            crudFetcher = CrudFetcher(
                readFetcher = RetrofitFetcher.of(apiService) { key: DummyPostId, service: DummyJsonService -> service.getPost(key.id) },
                createSender = RetrofitSender.of(apiService) { key: DummyPostId, entity: DummyPost, s: DummyJsonService -> s.createPost(entity) },
                updateSender = RetrofitSender.of(apiService) { key: DummyPostId, entity: DummyPost, s: DummyJsonService -> s.updatePost(key.id, entity) },
                deleteSender = RetrofitSender.noResult(apiService) { key: DummyPostId, entity: DummyPost, s: DummyJsonService -> s.deletePost(key.id) }
            ),
            sourceOfTruth = roomDatabase.dummyPostSourceOfTruth(),
            keyBuilder = { post -> DummyPostId(post.id) }
        ).build()
    }

    @Provides
    fun providePostsCRUDStore(apiService: JsonPlaceholderService, roomDatabase: AppDatabase): SimpleCrudStoreImpl<PostKey, Post> {
        return SimpleCrudStoreBuilder.from(
            crudFetcher = CrudFetcher(
                readFetcher = { post -> FetcherResult.Data(apiService.getPost(post.id)) },
                createSender = { key, post -> FetcherResult.Data(apiService.createPost(post)) },
                updateSender = { key, post -> FetcherResult.Data(apiService.updatePost(key.id, post)) },
                deleteSender = { key, post -> apiService.deletePost(key.id); FetcherResult.Success(true) },
            ),
            sourceOfTruth = roomDatabase.postSourceOfTruth(),
            keyBuilder = { entity -> PostKey(entity.id) }
        ).build()
    }

    @Provides
    fun providePersonStore(apiService: RoomStarWarsService, roomDatabase: AppDatabase): Store<RoomPersonStore.Key, People> =
        SimpleStoreBuilder.from(
            fetcher = { key -> FetcherResult.Data(apiService.getPerson(key.id).apply { parseId() }) },
            sourceOfTruth = roomDatabase.personSourceOfTruth()
        ).build()

    @Provides
    fun providePeopleStore(apiService: RoomStarWarsService, roomDatabase: AppDatabase): Store<NoKey, List<People>> =
        SimpleStoreBuilder.from(
            fetcher = { key -> FetcherResult.Data(apiService.getPeople().results) },
            sourceOfTruth = roomDatabase.peopleSourceOfTruth()
        ).build()

    @Provides
    fun providePostsStore(apiService: JsonPlaceholderService, roomDatabase: AppDatabase): SimpleStoreImpl<NoKey, List<Post>> {
        return SimpleStoreBuilder.from(
            fetcher = Fetcher<NoKey, List<Post>> { FetcherResult.Data(apiService.getPosts()) }
                .limit(rateLimitPolicy = RateLimitPolicy.FixedWindowPolicy(duration = 2.seconds)),
            sourceOfTruth = roomDatabase.postsSourceOfTruth()
        ).build()
    }

    /*
    @Provides
    fun provideStarshipStore(apiService: RoomStarWarsService, roomDatabase: AppDatabase): Store<NoKey, List<Starship>> =
        StoreBuilder.from(
            fetcher = RetrofitFetcher.of(apiService) { key, service: RoomStarWarsService -> service.getStarships().results },
            sourceOfTruth = roomDatabase.starshipSourceOfTruth(),
            mapper = StarshipMapper()
        ).build()
     */

    @Provides
    fun provideStarshipStoreKtor(apiService: KtorStarWarsService, roomDatabase: AppDatabase): Store<NoKey, List<Starship>> =
        StoreBuilder.from(
            fetcher = KtorFetcher.of(apiService) { key, service: KtorStarWarsService -> service.getStarships().results },
            sourceOfTruth = roomDatabase.starshipSourceOfTruth(),
            mapper = StarshipMapper()
        ).build()

    @Provides
    fun provideRoomPeopleStore(fetcher: PeopleFetcher, roomDatabase: AppDatabase) =
        RoomPeopleStore(fetcher, roomDatabase.peopleSourceOfTruth())

    @Provides
    fun providePeopleFetcher(roomStarWarsService: RoomStarWarsService) = PeopleFetcher(roomStarWarsService)

    @Provides
    fun provideRoomPersonStore(fetcher: PersonFetcher, roomDatabase: AppDatabase) =
        RoomPersonStore(fetcher, roomDatabase.personSourceOfTruth())

    @Provides
    fun providePersonFetcher(roomStarWarsService: RoomStarWarsService) = PersonFetcher(roomStarWarsService)

    @Provides
    fun provideRoomPersonStoreWithError(fetcher: PersonFetcherWithError, roomDatabase: AppDatabase) =
        RoomPersonStoreWithError(fetcher, roomDatabase.personSourceOfTruth())

    @Provides
    fun providePersonFetcherWithError(roomStarWarsService: RoomStarWarsService) = PersonFetcherWithError(roomStarWarsService)

}