
# DataStore
[![GitHub release](https://img.shields.io/maven-central/v/dev.pablodiste.datastore/datastore)](https://search.maven.org/search?q=g:dev.pablodiste.datastore)

DataStore is an implementation of the Repository pattern in Android. This library allows async loading and caching data from different sources (API, databases).

## Basics

A Store is composed of two main objects, a fetcher, and a source of truth.

* **Fetcher**: In charge of calling the API and getting the result in the form of a `FetcherResult<Entity>`.
* **Source of Truth**: In charge of storing the result locally and making queries, usually backed by a local database.

## Example of Usage

```kotlin
// Repository code
fun providePeopleStore(): Store<NoKey, List<People>> =
    SimpleStoreBuilder.from(
        fetcher = LimitedFetcher.of { key -> FetcherResult.Data(provideStarWarsService().getPeople().results) },
        cache = SampleApplication.roomDb.peopleCache()
    ).build()

// In the Viewmodel
val peopleStore = providePeopleStore()
viewModelScope.launch {
    peopleStore.stream(refresh = true).collect { result ->
        Log.d(TAG, "Hello ${result.value.name}")
        uiState.value = result.value
    }
}
```
This example will fetch a People list from an API, then store it in a local database and listen reactively for updates on that list.

Please see the documentation below about how to provide a fetcher and a source of truth using your preferred libraries.

## Sample Application
You can see the features working by cloning this repository and running the `app` module.

## Features

- Handles caching of fetched data automatically.
- Allows listening to changes on the source of truth and making queries.
- It is built on coroutines.
- Allows the integration of different fetcher sources and libraries (Retrofit, Ktor).
- Includes support for source of truth based on Room and Realm libraries but you can plug in other ones too.
- Allows configuration of custom database queries (like DAOs)
- Allows per-request source of truth expiration configuration.
- Implements a way to limit multiple repeated calls to the same API.
- Supports retries using exponential backoff on fetcher errors.
- Implements throttling on API errors. You can be notified if throttling is activated and show proper error messaging in the UI.
- Implements CRUDStores, a very simple interface to make Create, Read, Update and Delete operations over APIs and reflect the state updates automatically in the source of truth.
- (WIP) Implements writable stores.

## Using DataStore
### Installation

Include the library in the dependencies section of your module configuration file. For example using Kotlin:

```kotlin
implementation("dev.pablodiste.datastore:datastore:0.1.2")
```

Plugins for integrating DataStore with common libraries, you only need to include the ones you need

```kotlin
implementation("dev.pablodiste.datastore:datastore-room:0.1.1")
implementation("dev.pablodiste.datastore:datastore-realm:0.1.1")
implementation("dev.pablodiste.datastore:datastore-retrofit:0.1.2")
implementation("dev.pablodiste.datastore:datastore-ktor:0.1.1")
```

### 1. Defining your data classes

The Store will be in charge of fetching data from the API and caching the data on a local repository (database). You can define data classes for holding the information parsed from the API, for example with GSon or Moshi libraries, and also you can define data classes for the data which is going to be stored in the database.

You can either define different data classes for API and Database or use the same definition, depending on your needs. In the following examples we are going to use the same entity class for simplicity.

Here is a Room entity.
```kotlin
@Entity(tableName = "people")
data class People(
    @PrimaryKey var id: String = "",
    @ColumnInfo(name = "name") var name: String? = null,
    @ColumnInfo(name = "height") var height: String? = null,
    @ColumnInfo(name = "mass") var mass: String? = null,
    @ColumnInfo(name = "gender") var gender: String? = null,
    @ColumnInfo(name = "url") var url: String? = null,
)
```
### 2. Creating a Store

We have few base classes you can use for creating an Store:

- `StoreImpl<K: Any, I: Any, T: Any>` is the main implementation, it requires you to provide K a key for the request, I the class to be used by the fetcher and T the class to be used by the source of truth.
- `SimpleStoreImpl` is a helper class where I = T, we are going to be using the same data class for fetching and storing.
- `NoKeySimpleStore` is a `SimpleStoreImpl` where the Key is NoKey. Please read the Key section for more information on Keys.

For example:
```kotlin
class PeopleStore: NoKeySimpleStore<List<People>>(
    fetcher = PeopleFetcher(),
    sourceOfTruth = PeopleSourceOfTruth()) {
    ...
}
```
Defines a store which will fetch using a fetcher based on the entity People, and it will store it in a source of truth database using the same Entity.

As an alternative we also provide a functional builder for creating the store:
```kotlin
fun providePersonStore(): Store<RoomPersonStore.Key, People> =
    SimpleStoreBuilder.from(
        fetcher = LimitedFetcher.of { key -> FetcherResult.Data(provideStarWarsService().getPerson(key.id)) },
        sourceOfTruth = SampleApplication.roomDb.personSourceOfTruth()
    ).build()
```

### 3. Define a Key.
A `Key` is a parametric data class used to identify the API request operation. This `Key` can be any class with a `toString` implementation. Its fields are used to provide parameters to the API and it is also used as a unique identifier for each API Request, so we can avoid multiple repeated calls.

In case our endpoint requires a parameter id, we can do:
```kotlin
	data class Key(val id: String)
```

In case you need to fetch a list of entities without any parameters, or there is no field which could identify the request, you can use an existing `NoKey` implementation instead of creating your own.
This key should be referenced in the generic parameter K of the store definition. For example: `SimpleStoreImpl<PeopleStore.Key, People>`.

### 4. Implement the Fetcher

The fetcher fetches data from an API and returns a FetcherResults. You can provide a fetcher to the store this way:
```kotlin
LimitedFetcher.of { key -> FetcherResult.Data(provideStarWarsService().getPerson(key.id)) }
```
In this example `provideStarWarsService()` provides the API service which makes the call to the server.

We have available a couple of helper integrations to most common libraries too.
The advantage of using these helpers is not only less boilerplate code but also they handle automatically the specific library errors.

#### Retrofit Fetcher

Including the retrofit integration, you can use the following code to create a Retrofit fetcher.

```kotlin
RetrofitFetcher.of(RetrofitManager) { key, service: RoomStarWarsService -> service.getStarships().results }
```

Here `RetrofitManager` implements `FetcherServiceProvider` interface including a method used to create a Retrofit service. You can find more details in the sample application source code.

```kotlin
object RetrofitManager : FetcherServiceProvider {

    private val retrofit: Retrofit
    // Setup retrofit instance...
    
    override fun <T> createService(service: Class<T>): T {
        retrofit.create(service)
    }
}
```

There is also a helper class you can use if you do not want to build Retrofit services using the functional approach.

```kotlin
class PeopleFetcher: RetrofitFetcher<NoKey, List<People>, StarWarsService>(StarWarsService::class.java, RetrofitManager) {
    override suspend fun fetch(key: NoKey, service: StarWarsService): List<People> {
        val people = service.getPeople()
        return people.results
    }
}
```

- As we mentioned `RetrofitManager` is a `FetcherServiceProvider` implementation that manages the creation of the retrofit service.
- `StarWarsService` is a Retrofit service definition interface.

#### Ktor Fetcher

Similar to the Retrofit integration you can use Ktor as HTTP client.

```kotlin
KtorFetcher.of(KtorManager) { key, service: KtorStarWarsService -> service.getStarships().results }
```

It works the same way as Retrofit, please check the sample project for the implementation details.

#### Other fetcher libraries

You can also use other libraries or provide your own code to fetch data, you just need to make the call in the fetch function and return a `FetcherResult`.

```kotlin
LimitedFetcher.of { key -> FetcherResult.Data(provideAPIService().getPerson(key.id)) }
```

### 5. Implement the Source of Truth

#### Using Room

We have provided base DAOs for using with Room:

- `RoomSourceOfTruth` stores individual objects. This type of source of truth is used, for example, when fetching data from APIs which return a single entity in the response.
- `RoomListSourceOfTruth` stores a list of entities. This type of source of truth is used, for example, when fetching data from APIs which return a list of entities as a response.

For example if we want to store a list of People we can do:
```kotlin
@Dao
abstract class PeopleSourceOfTruth: RoomListSourceOfTruth<NoKey, People>("people", SampleApplication.roomDb) {
    override fun query(key: NoKey): String = ""
}
```
The `query` is the filter to be used to retrieve the stored data which has been stored after the API call. It generally matches the parameters sent to the API.
For example if we are fetching an entity by id, our SourceOfTruth class will look like this one:
```kotlin
@Dao
abstract class PersonSourceOfTruth: RoomSourceOfTruth<Key, People>("people", SampleApplication.roomDb) {
    override fun query(key: Key): String = "id = ${key.id}"
}
```
All created DAOs automatically generate the required methods for making it work with the Store, but you can also add your own methods for using the source of truth directly as a regular Room DAO.

The created DAOs should be connected with your Room Database implementation, for example:
```kotlin
@Database(entities = [People::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun peopleSourceOfTruth(): RoomPeopleStore.PeopleSourceOfTruth
    abstract fun personSourceOfTruth(): RoomPersonStore.PersonSourceOfTruth
}
```
and you need to provide the DAO as the source of truth in the Store:
```kotlin 
class RoomPersonStore: SimpleStoreImpl<RoomPersonStore.Key, People>(
	fetcher = PersonFetcher(),
	sourceOfTruth = SampleApplication.roomDb.personSourceOfTruth()
)
```
Please refer to the Room implementation for more details on the Database definition.

#### Using Realm database
In case we are using `Realm` we can use:
```kotlin
class PeopleSourceOfTruth: RealmListSourceOfTruth<NoKey, People>(People::class.java) {
    override fun query(key: NoKey): (query: RealmQuery<People>) -> Unit = { }
}
```
The `query` is the filter to be used to retrieve the stored data which has been stored after the API call. It generally matches the parameters sent to the API.
For example if we are fetching an entity by id, our SourceOfTruth class will look like this one:
```kotlin
class PersonSourceOfTruth: RealmSourceOfTruth<Key, People>(People::class.java) {
    override fun query(key: Key): (query: RealmQuery<People>) -> Unit = {
        it.equalTo("id", key.id)
    }
}
```
Optionally you can provide a `storeInRealm` which implements a custom method to persist to the Realm source of truth.

```kotlin
class PersonCache: RealmSourceOfTruth<Key, People>(People::class.java) {
    override fun query(key: Key): (query: RealmQuery<People>) -> Unit = { it.equalTo("id", key.id) }
    override fun storeInRealm(key: Key, bgRealm: Realm, entity: People) { bgRealm.copyToRealmOrUpdate(entity) } // Custom code
}
```

#### In-memory source of truth

It is available an implementation of the source of truth which stores the data in-memory. The data is not persisted to disk, it will not be available after restarting the app. It is useful for storing temporary or short lived information.

```kotlin
data class Key(val cid: Int)
data class Entity(val id: Int, val cid: Int, val name: String)

class InMemoryEntityListSOT: InMemoryListSourceOfTruth<Key, Entity>() {
    override fun predicate(key: Key): (key: Key, value: Entity) -> Boolean = { key, value -> value.cid == key.cid }
}
```

This example shows a version storing a list of fetcher results. You can also use `InMemorySourceOfTruth` for storing single values.
The predicate is a function you provide to filter out the data in relation to key, similar to the query in the other source of truth implementations. When requesting data with `get` or `stream` only the items matching the provided predicate are returned. 

### 6. Using the Store

These are the main methods for using the Store we just created:

#### Instancing your Store

```kotlin
val peopleStore = PeopleStore() // or injected using DI
```

All operations using the store are usually tied to a scope, we can use the viewModelScope for example to launch a coroutine that listens for the incoming data.

```kotlin
viewModelScope.launch {
    val response = personStore.fetch(RealmPersonStore.Key("1"))
    // ...
}
```

With some databases like Realm we should close the opened resources when we are done with using the data. We have provided some extension functions for closing the Realm instances automatically.

```kotlin
viewModelScope.launch(personStore) {
    val response = personStore.fetch(RealmPersonStore.Key("1"))
    // ...
}
```

Here in the launch we provide the store instance and it will close itself once the coroutine job is canceled (finished).
As an alternative, we have an additional method to close many stores.

```kotlin
viewModelScope.launch {
    // Use peopleStore and planetStore
}.autoClose(peopleStore, planetStore)
```

#### Responses

All responses coming from the store are based on StoreResponse classes. The subclasses are:

| StoreResponse | Description |
|---------------|-------------|
| Data		  | When the request was successful it returns the parsed objects |
| Error		 | This is returned when there was a network or parsing error. It includes the exception that generated it. Please note Store will not throw the exception |
| NoData		| This is returned when there is no data to return, for example when the fetch operation has not been executed. Please note NoData is not returned when the API returns an empty result set.  |


#### Stream data

The `stream` method does the following:
1. Checks the source of truth looking for any stored data, if there is any, it is emitted immediately.
2. If the `refresh` parameter is `true`, it calls the Fetcher for new data from the API and stores them. Then it will emit the new data. If stored data existed before the fetch, you will receive **two emissions**: one for the previously stored data and another one for the new data after the fetch has completed.
3. If initially there was no data on the source of truth, it performs the API call. Then its result gets stored in the source of truth and emitted to the client.
4. Stream keeps listening and emitting any update in the stored data.

For example, from your ViewModel
```kotlin
viewModelScope.launch {
    peopleStore.stream(refresh = true).collect { result ->
        when (result) {
            is StoreResponse.Data -> refreshUI() // Here you send it to the UI
            is StoreResponse.Error -> showError() // You can show an error 
            else -> handleNoData() // You can also handle the NoData showing an error 
        }
    }
}
```
`stream` returns a `Flow`, and it can be combined and processed like any other `Flow`.

You can also add `.map { it.requireData() }` in case you want the errors to be thrown and handled in a different way.

#### Making an API call directly

The `fetch` method makes an API call, stores the result and then it returns the new data. It does not check the source of truth for any previously existing data.

```kotlin
viewModelScope.launch {
    val response = personStore.fetch(PersonStore.Key("1"))
    Log.d(TAG, "Fetch response: ${response.value}")
}
```

#### Getting the data from source of truth (no API)

The `get` method gets the data from the source of truth, usually used in places when you know the data should be already stored. If there is no data in the source of truth, it automatically tries to fetch the data from API. `get` is a suspend function and it does not keep listening for changes, it returns the current status of the stored data.

```kotlin
viewModelScope.launch {
    val response = personStore.get(RoomPersonStore.Key("1"))
}
```

#### Summary of main methods

| method | feature |
|--------|---------|
| stream | Gets the data first from the source of truth, then from the API and listens for source of truth updates. It usually emits twice or more times (old and new data) |
| fetch | Gets the data from the API and stores it. |
| get | Gets the data from the source of truth. It does not listen for updates. |

#### Error handling
You can detect errors in different ways:
```kotlin
viewModelScope.launch {
    val result = personStore.fetch(RoomPersonStore.Key("1"))
    when (result) {
        is StoreResponse.Data -> // UI work
        is StoreResponse.Error -> // Handle the error here
        else -> {}
    }
}
```
Another alternative is using the catch method after a `requireData()` call.
```kotlin
viewModelScope.launch {
    personStore.stream(RoomPersonStore.Key("1"), refresh = true)
        .map { it.requireData() }
        .catch { /* Error handling */ }
        .collect { result -> /* UI work */ }
}
```

### Staleness Settings

When we are calling the API to fetch a list of resources, we usually perform a GET call and the returned items are stored in the source of truth. If there are entities that were deleted in the backend, the API response will not contain them anymore, and the source of truth will still have them stored -and they will appear in local queries-. In order to sync the data and delete the source of truth items properly we have different strategies.

The staleness strategy is configured in the SourceOfTruth. For example using Room:
```kotlin
@Dao
abstract class PeopleSourceOfTruth: RoomListSourceOfTruth<NoKey, People>("people", SampleApplication.roomDb,
    stalenessPolicy = DeleteAllNotInFetchStalenessPolicy { people -> people.id } // Example of staleness settings.
) {
    override fun query(key: NoKey): String = ""
}
```

| stalenessPolicy					| description |
|------------------------------------|-------------|
| `DoNotExpireStalenessPolicy`		 | Avoids calling any DELETE on the database when new data arrives. No data is deleted, only updates are performed. |
| `DeleteAllStalenessPolicy`		   | Calls DELETE doing a query using the query method of the SourceOfTruth. |
| `DeleteAllNotInFetchStalenessPolicy` | Calls DELETE on all database rows which are not in the API response. You should provide a function which indicates a way to compare the old and new items, usually a primary key. | 

### Avoiding multiple repeated calls

It is very common in big applications to request the same information from many different locations. In order to avoid doing repeated API calls the `Store` implements a `RateLimiter`.

The limiter will allow the first call, and then any subsequent call inside a time span provided will not be executed. If the second call happens before the completion of the first call, that second call will wait for the result of the first one and it will return the same value for both. If the second call happens after the first call has arrived, `NoData` is returned instead until the time provided in the limiter has elapsed. Once the time has elapsed the store is able to call the API again.

It is available a `LimitedFetcher` and provides a way to define a `rateLimitPolicy`:

```kotlin
fun providePostsStore(): SimpleStoreImpl<NoKey, List<Post>> {
    return SimpleStoreBuilder.from(
        fetcher = LimitedFetcher.of(
            fetch = { FetcherResult.Data(provideService().getPosts()) },
            rateLimitPolicy = RateLimitPolicy.FixedWindowPolicy(duration = 10.seconds)
        ),
        sourceOfTruth = SampleApplication.roomDb.postsSourceOfTruth()
    ).build() as SimpleStoreImpl
}
```
Similarly, if you are inheriting from `RetrofitFetcher` you can provide the limiter settings in the constructor.

```kotlin
	class PlayerFetcher: RetrofitFetcher<NoKey, List<Player>, RetrofitTeamService>(
    serviceClass = RetrofitPlayerService::class.java,
    rateLimitPolicy = RateLimitPolicy.FixedWindowPolicy(duration = 1.minutes)) {
```

The default implementation uses `RateLimitPolicy.FixedWindowPolicy(duration = 5.seconds)`

| rateLimitPolicy					| description                                                                                                                                                                                                                                                                                                                                                                                  |
|---------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `FixedWindowPolicy`	| It lets you define a number of calls and a time window. The first N times you make an API call it will proceed, if you make a subsequent call inside the time window provided, it will NOT make an API call. Once the time has passed the timeout threshold, the next call will go to the API again with the same logic. Example: `RateLimitPolicy.FixedWindowPolicy(duration = 10.seconds)` |
| `FetchOnlyOnce`		| It calls the API only once in the app lifetime. Please note if you restart the app, the store will fetch again.                                                                                                                                                                                                                                                                              |
| `FetchAlways` | It does not limit the API calls in any way.                                                                                                                                                                                                                                                                                                                                                  |

#### Forcing a fetch ignoring the rate limiter

If you need to force a fetch to the API ignoring the rate limiter, you should send a parameter to the `fetch` call the following way:

```kotlin
val result = personStore.fetch(RoomPersonStore.Key("1"), forced = true)
```

#### Disabling the Rate Limiter

The Rate limiter is active by default, but you can disable it with the code:

```kotlin
StoreConfig.isRateLimiterEnabled = {
    // You can use a feature flag or a remote config here to return a Boolean 
}
```
If you want to disable it for a specific call, you can set `rateLimitPolicy = FetchAlways`.

### Retries

You can configure retries for a fetcher call this way:

```kotlin
fun providePostsStore(): SimpleStoreImpl<NoKey, List<Post>> {
  return SimpleStoreBuilder.from(
    fetcher = LimitedFetcher.of(
      fetch = { FetcherResult.Data(provideService().getPosts()) },
      rateLimitPolicy = RateLimitPolicy.FixedWindowPolicy(duration = 10.seconds),
      retryPolicy = RetryPolicy.ExponentialBackoff(3)
    ),
    sourceOfTruth = SampleApplication.roomDb.postsSourceOfTruth()
  ).build()
}
```

The retryPolicy is supported in all fetcher implementations (Retrofit, Ktor, custom). The alternatives are:

| retryPolicy          | description                                                                                                                                                                              |
|----------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `DoNotRetry`         | It does not retry the fetcher call on failures. This is the default.                                                                                                                     |
| `ExponentialBackoff` | When the fetcher returns a `FetcherResult.Error`, the fetch is retried until `maxRetries`. The time between retries can be controlled by the `initialBackoff` and `backoffRate` params. |

#### Configurable Retry

You can configure `ExponentialBackoff` policy so the retry only happens when receiving certain HTTP response codes. For example:

```kotlin
RetryPolicy.ExponentialBackoff(maxRetries = 3, retryOnErrorCodes = listOf(500, 501, 502))
```

This config will retry only when the server returns 500, 501 or 502 error codes.
You can also configure a custom retryOn function like the following example. The fetcher will retry only if `retryOn` function returns `true`.

```kotlin
RetryPolicy.ExponentialBackoff(maxRetries = 3, retryOn = { error -> ... })
```

### Throttling

You can enable throttling of service calls in case of continuously failing requests. Sometimes backends and servers are not able to process the requests fast enough, or they are down, or they experience temporary issues. In that case, the Store clients are able to wait some time before making the next call.
It works the following way: If there are more than a configurable amount of errors in a row, the next service calls during a timeframe will result in an immediate local exception and they will not be executed.
You can also configure a base timeframe until a next call is allowed. If the time has passed and there is another error this timeframe grows exponentially to avoid flooding the server with retries or repeated calls.

> The throttled requests are not retried automatically nor queued
> (yet), they result in a throttling error. You can retry them from the client
> code.

#### Throttling configuration

You can configure the throttling setting `StoreConfig.throttlingConfiguration`.

|Property  | Description  |
|--|--|
|errorCountThreshold  |  Amount of errors in a row since the throttling is activated. |
|throttleInitialTimeout  | Duration of the throttling period in milliseconds. |
|errorDurationThreshold  | The timeout period will grow exponentially if the API errors continue happening, until reaching this duration in milliseconds. |


#### Disabling throttling

The throttling is active by default, but you can disable it for debugging purposes this way:
```kotlin
StoreConfig.isThrottlingEnabled = {
    // You can use a feature flag or a remote config here to return a Boolean
}
```

#### Showing API errors on the screen

`ThrottlingFetcherController` exposes a `throttlingState` state flow you can use in the UI. The state includes `isThrottling` boolean indicating if throttling is currently active, and `timestampUntilNextCall` the unix timestamp to the date-time in which you can resume making calls or retry them.

```kotlin
// In the ViewModel
val throttlingState = StoreConfig.throttlingController.throttlingState

// In the View / Compose
val throttlingState = viewModel.throttlingState.collectAsState()
// Then you can use throttlingState.value.isThrottling to know if it is throttling.
```

### CRUD Stores

A CrudStore is a Store which also implements create, update and delete methods.
These operations are reflected in API calls (POST, PUT and DELETE REST calls for example), and the new/updated/deleted entities are also created/updated/deleted from the source of truth when the API call succeeds.
This is a very simple implementation and it does not consider making changes on a working thread.

Example of definition:

```kotlin
data class PostKey(val id: Int)

fun providePostsCRUDStore(): SimpleCrudStoreImpl<PostKey, Post> {
    return SimpleCrudStoreBuilder.from(
        fetcher = LimitedCrudFetcher.of(
            fetch = { post -> provideService().getPost(post.id) },
            create = { key, post -> FetcherResult.Data(provideService().createPost(post)) },
            update = { key, post -> FetcherResult.Data(provideService().updatePost(key.id, post)) },
            delete = { key, post -> provideService().deletePost(key.id); true },
        ),
        sourceOfTruth = SampleApplication.roomDb.postSourceOfTruth(),
        keyBuilder = { entity -> PostKey(entity.id) }
    ).build() as SimpleCrudStoreImpl
}

private fun provideService() = RetrofitManager.createService(JsonPlaceholderService::class.java)

@Dao
abstract class PostCache: RoomCache<PostKey, Post>("posts", SampleApplication.roomDb) {
    override fun query(key: PostKey): String = "id = ${key.id}"
}
```
Some details:

- `PostKey` is the key used to identify each request, in this example the id is used to distinguish between different entities.
- `providePostsCRUDStore` creates the CRUD Store, we provide one method for each CRUD operation: `create`, `update`, `delete`, and `fetch`. We also need to provide a `keyBuilder` function used to generate a new key for the new stored data.
- The source of truth in this case is a Room Dao with a query using the key provided.

The usage is simple:

```kotlin
private val postsStore = providePostsCRUDStore()
//...
fun create() {
  viewModelScope.launch {
    postsStore.create(PostKey(id = 1), Post(title = "Title", body = "Body", userId = 1))
    uiState.value = "Created" // Here you can show a success message
  }
}
```
And similar for `update` and `delete`. The source of truth is updated as soon the response is received.
In general this CRUD requests assumes an API expecting POST/PUT/PATCH operations returning the created/updated object with the generated/edited id as a result.

### Error handling
The CRUD operations returns a `StoreResponse` object which can be an `Error`. None of the operations are throwing exceptions.
```kotlin
val response = postsStore.create(PostKey(id = 1), Post(title = "Title", body = "Body", userId = 1))
when (response) {
  is StoreResponse.Data -> uiState.value = "Created"
  is StoreResponse.Error -> uiState.value = "Error in create"
}
```

## Contributions

Any suggestion and bug report is welcome, you can create issues in the github page.
Feel free to fork it and/or send a pull request in case you want to make fixes or add any additional feature or change. Please create an issue in github so we can discuss the idea and collaborate.

## Roadmap

- Document fetcher errors
- Improve throttling code
- Add additional testing coverage.
- Support of Pagination, integration with Pager3 or custom implementation.
- Support parsing of error results.
- Retries: Support Retry-After header
- Work in progress: Writeable Store
    - Sender Controller and library helpers    
    - Error handling / Undo
    - On delete operation remove pending updates for that id
    - Provide a way for clients to store pending updates
    - Provide a way for clients to enable and disable worker, for example for not sending when not logged in to API.
    - Detect changes on same entity id and process only one
    - More Testing
    - Documentation
- Analyze making it available for KMM.
- Add an optional memory cache.
