
# DataStore  
DataStore is an implementation of the Repository pattern in Android. This library allows async loading and caching data from different sources (API, databases).

## Basics

A Store is composed by two main objects, a fetcher, and a cache.
* **Fetcher**: In charge of calling the API and getting the result in the form of a `FetcherResult<Entity>`.
* **Cache**: In charge of storing the result locally and making queries, usually backed by a local database.

## Preview Usage
After you have configured your Store, you will be able to do from your ViewModel:
```kotlin
val peopleStore = PeopleStore(viewModelScope)
viewModelScope.launch {
    peopleStore.stream(refresh = true).collect { result ->
        Log.d(TAG, "Hello ${result.value.name}")
        uiState.value = result.value
  }
}
```
This example will fetch a People list from an API, then store it in a local database and listen reactively for updates on that list.

## Sample Application
You can see the features working cloning this repository and running the app project.

## Features

 - Handles caching of fetched data automatically.
 - Allow listening for changes on the cache and fetching single values.
 - It is built on coroutines.
 - Allows the integration of different fetcher sources and libraries.
 - Supports caches based on Room and Realm and allows any other caches.
 - Allows configuration of custom database queries (like DAOs)
 - Allows per-request cache expiration configuration.
 - Implements a way to limit multiple repeated calls to the same API.
 - Implements throttling on API errors. You can be notified if throttling is activated and show proper error messaging in the UI.
 - Implements CRUDStores with a very simple interface to make create, read, update and delete operations over APIs and reflect the state in the local cache.

# Using DataStore
## Installation

This software is still in beta and it is not available in any repository yet.

## 1. Defining your data classes

The Store will be in charge of fetching data from the API and caching the data on a local repository (database). You can define data classes for holding the information parsed from the API, for example with GSon or Moshi libraries, and also you can define data classes for the data which is going to be stored in the database.

You can either define different data classes for API and Database or use the same definition, depending of your needs. In the following examples we are going to use the same entity class for simplicity.

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
## 2. Creating a Store

We have few base classes you can use for creating an Store:

 - `StoreImpl<K: Any, I: Any, T: Any>` is the main implementation, it requires you to provide K a key for the request, I the class to be used by the fetcher and T the class to be used by the cache.
 - `SimpleStoreImpl` is a helper class where I = T, we are going to be using the same data class for fetching and storing.
 - `NoKeySimpleStore` is a `SimpleStoreImpl` where the Key is NoKey. Please read the Key section for more information on Keys.

For example:
```kotlin
class PeopleStore: NoKeySimpleStore<List<People>>(
    fetcher = PeopleFetcher(),
    cache = PeopleCache()) {
    ...
}
```
Defines a store which will fetch using a fetcher based on the entity People, and it will cache it in a database using the same Entity.
As an alternative we also provide a functional builder for creating an store:
```kotlin
fun providePersonStore(): Store<Key, People> {
    return SimpleStoreBuilder.from(
        fetcher = LimitedFetcher.of({ key ->
            FetcherResult.Data(provideStarWarsService().getPerson(key.id))
        }),
        cache = SampleApplication.roomDb.personCache()
    ).build()
}
```

## 3. Define a Key.
A `Key` is a parametric data class used to identify the data to be stored. This `Key` can be any class with a `toString` implementation, and unique per API request. Its fields are used to provide parameters to the API and it is also used as a unique identifier for each API Request, so we can avoid multiple repeated calls.

In case our endpoint requires a parameter id, we can do:
```kotlin
    data class Key(val id: String)
```

In case you need to fetch a list of entities without any parameters, or there is no field which could identify the request, you can use an existing `NoKey` implementation instead of creating your own.
This key should be referenced in the generic parameter K of the store definition. For example: `SimpleStoreImpl<PeopleStore.Key, People>`.

## 4. Implement the Fetcher

The fetcher fetches data from an API and returns a FetcherResults.
```kotlin
LimitedFetcher.of({ key ->
  FetcherResult.Data(provideStarWarsService().getPerson(key.id).apply { parseId() })
})
```
In this example `provideStarWarsService()` provides the retrofit service already configured.

There is also a useful subclass if you want to build Retrofit services using a `RetrofitServiceProvider` (here `RetrofitManager`).
```kotlin
class PeopleFetcher: RetrofitFetcher<NoKey, List<People>, StarWarsService>(StarWarsService::class.java, RetrofitManager) {
    override suspend fun fetch(key: NoKey, service: StarWarsService): FetcherResult<List<People>> {
        val people = service.getPeople()
        // Make any changes to the entities before caching them
        return FetcherResult.Data(people.results)
    }
}
```
You can also use other libraries to fetch data, you just need to make the call in the fetch function or override and return the `FetcherResult`.
 - `RetrofitManager` is a `RetrofitServiceProvider` implementation that manages the creation of the retrofit service.
 - `StarWarsService` is a Retrofit service definition class.

## 5. Implement the Cache

### Using Room
We have provided base DAOs for using with Room:

 - `RoomListCache` and `SimpleRoomListCache`, stores a list of entities.
 - `RoomCache` and `SimpleRoomCache` stores individual objects.

For example if we want to store a list of People we can do:
```kotlin
@Dao
abstract class PeopleCache: SimpleRoomListCache<NoKey, People>("people", SampleApplication.roomDb) {
    override fun query(key: NoKey): String = ""
}
```
The `query` is the filter to be used to retrieve the cached data which has been stored after the API call. It generally matches the parameters sent to the API.
For example if we are fetching an entity by id, our Cache class will look like this one:
```kotlin
@Dao
abstract class PersonCache: SimpleRoomCache<Key, People>("people", SampleApplication.roomDb) {
    override fun query(key: Key): String = "id = ${key.id}"
}
```
All created DAOs automatically generate the required methods for making it work with the Store, but you can also add your own methods for using the cache directly as a regular Room DAO.

The created DAOs should be connected with your Room Database implementation, for example:
```kotlin
@Database(entities = [People::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun peopleCache(): RoomPeopleStore.PeopleCache
    abstract fun personCache(): RoomPersonStore.PersonCache
}
```
and you need to provide the DAO as the cache in the Store:
```kotlin
class RoomPersonStore: SimpleStoreImpl<RoomPersonStore.Key, People>(
    fetcher = PersonFetcher(),
    cache = SampleApplication.roomDb.personCache()
)
```
Please refer to the Room implementation for more details on the Database definition.

### Using Realm database
In case we are using `Realm` we can use:
```kotlin
class PeopleCache: SimpleRealmListCache<NoKey, People>(People::class.java) {
    override fun query(key: NoKey): (query: RealmQuery<People>) -> Unit = { }
}
```
The `query` is the filter to be used to retrieve the cached data which has been stored after the API call. It generally matches the parameters sent to the API.
For example if we are fetching an entity by id, our Cache class will look like this one:
```kotlin
class PersonCache: SimpleRealmCache<Key, People>(People::class.java) {
    override fun query(key: Key): (query: RealmQuery<People>) -> Unit = {
 it.equalTo("id", key.id)
    }
}
```
Optionally you can provide a `storeInRealm` which implements a custom method to persist to the Realm cache.

```kotlin
class PersonCache: SimpleRealmCache<Key, People>(People::class.java) {
    override fun query(key: Key): (query: RealmQuery<People>) -> Unit = { it.equalTo("id", key.id) }
  override fun storeInRealm(key: Key, bgRealm: Realm, entity: People) { bgRealm.copyToRealmOrUpdate(entity) // Custom code }
}
```

## 6. Using the Store

These are the main methods for using the Store we just created:

### Instancing your Store
```kotlin
val peopleStore = PeopleStore() // or injected using DI
```
All operation with the store are usually tied to a scope, we can use the viewModelScope for example to launch a coroutine that listens for the incoming data.
```kotlin
viewModelScope.launch {
  val response = personStore.fetch(RealmPersonStore.Key("1"))
  ...
}
```
With some databases like Realm we should close the opened resources when we are done with using the data. We have provided some extension functions for closing automatically the Realm instances.
```kotlin
viewModelScope.launch(personStore) {
  val response = personStore.fetch(RealmPersonStore.Key("1"))
  ...
}
```
Here in the launch we provide the store instance and it will close itself once the coroutine job is cancelled (finished).
As an alternative, we have an additional method to close many stores.
```kotlin
viewModelScope.launch {
	... (use peopleStore and planetStore)
}.autoClose(peopleStore, planetStore)
```
### Responses
All responses coming from the store are based on StoreResponse classes. The subclasses are:
| StoreResponse | Description |
|---------------|-------------|
| Data          | When the request was successful it returns the parsed objects |
| Error         | It is returned when there was a network or parsing error, includes the exception that generated it. Please note Store will not throw the exception |
| NoData        | When there is no data returned |


### Stream data

The `stream` method does the following:
1. Checks the cache for any cached data, if they exist it returns them.
2. If the `refresh` parameter is `true`, if calls the Fetcher for new data from the API and caches them. Then it will emit the new data. If old data existed before the fetch, you will receive **two emissions**: one for the old data and another one for the new data after the fetch has completed.
3. If initially there was no data on the cache, it performs the fetch from the API. Then it gets stored in the cache and emitted to the consumer.

For example, from your ViewModel
```kotlin
viewModelScope.launch {
   peopleStore.stream(refresh = true).collect { result ->
      when (result) {
          is StoreResponse.Data -> // Here you send it to the UI
          is StoreResponse.Error -> // You can show an error
          else -> // You can also handle the NoData showing an error
      }
   }
}
```
`stream` returns a `Flow`, and it can be combined and processed like any other `Flow`.

You can also add `.map { it.requireData() }` in case you want the errors to be thrown and handled in a different way.

### Making an API call directly

The `fetch` method makes an API call, caches the result and then it returns the new data. It does not check the cache for any previously existing data.

```kotlin
viewModelScope.launch {
  val response = personStore.fetch(PersonStore.Key("1"))
  Log.d(TAG, "Fetch response: ${response.value}")
}
```

### Getting the data from cache (no API)

The `get` method gets the data from the cache, usually used in places when you know the data should be already cached. If there is no data in the cache, it automatically tries to fetch the data from API. `get` is a suspend function and it does not keep listening for changes, it returns the current status of cached data.

```kotlin
viewModelScope.launch {
  val response = personStore.get(RoomPersonStore.Key("1"))
}
```

### Summary
| method | feature |
|--------|---------|
| stream | Gets the data first from the cache, then from the API and listens for cache updates. It usually emits twice or more times (old and new data) |
| fetch | Gets the data from the API and caches it. |
| get | Gets the data from the cache. It does not listen for updates. |

### Error handling
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

## Staleness Settings

When the data is returned from the API, it gets stored in the cache. The methods Cache.store or RealmCache.storeInRealm are used to store all entities returned from the API. For example in Realm we use `bgRealm.copyToRealmOrUpdate(newData)` in the Cache object. The `copytoRealmOrUpdate` will be in charge of creating new rows in the database or updating them if they already exist. If there are entities that were deleted in the backend, the API response will not contain them, and the cache will still have them stored -and they will appear in queries-. In order to delete the cache properly we have some strategies.

The staleness strategy is configured in the Cache:

```kotlin
    class PlayerCache: SimpleRealmListCache<Key, Player>(
        klass = Player::class.java,
        stalenessPolicy = DeleteAllNotInFetchStalenessPolicy()) // You can set the staleness policy here
```

| stalenessPolicy                    | description |
|------------------------------------|-------------|
| DoNotExpireStalenessPolicy         | Avoids calling any DELETE on the database when new data arrives. No data is deleted. |
| DeleteAllStalenessPolicy           | Calls DELETE doing a query using the query method of the Cache. |
| DeleteAllNotInFetchStalenessPolicy | Calls DELETE on all database rows which are not in the API response. For knowing how to identify each row you should implement `HasKey` interface in the object used for the entity. |

Example for the `HasKey` interface

```kotlin
class Player : RealmObject(), HasKey {
    val id: String? = "" // Usually a hash string coming from the API
    val teamId: String? = ""

    override fun getKey(): Any? = id
}
```

## Avoiding multiple repeated calls

It is very common in a big application to request the same information from different locations. In order to avoid doing repeated API calls the `Store` implements a `RateLimiter`. The limiter will allow the first call, and then any subsequent call inside a time span provided will not be executed and a cached result will be returned instead until the time has elapsed.

It is available a `LimitedFetcher` and provides a way to define a `rateLimitPolicy`:

```kotlin
fun providePostsStore(): SimpleStoreImpl<NoKey, List<Post>> {
    return SimpleStoreBuilder.from(
        fetcher = LimitedFetcher.of(
           fetch = { FetcherResult.Data(provideService().getPosts()) },
           rateLimitPolicy = RateLimitPolicy(10, TimeUnit.SECONDS)
        ),
        cache = SampleApplication.roomDb.postsCache()
    ).build() as SimpleStoreImpl
}
```
Similarly, if you are inheriting from `RetrofitFetcher` you can provide the limiter settings in the constructor.

```kotlin
    class PlayerFetcher: RetrofitFetcher<NoKey, List<Player>, RetrofitTeamService>(
        serviceClass = RetrofitPlayerService::class.java,
        rateLimitPolicy = RateLimitPolicy(1, TimeUnit.MINUTES)) {
```

The default implementation is `RateLimitPolicy(5, TimeUnit.SECONDS)`

| rateLimitPolicy                    | description |
|------------------------------------|-------------|
| RateLimitPolicy         | It lets you define a timeout and time unit. The first time you make an API it will proceed, if you make a subsequent call inside this timeout period, it will NOT make an API call and it will try to fetch the data from the cache instead. Once the time has passed the timeout threshold, any subsequent calls will go to the API again with the same logic. Example: `RateLimitPolicy(10, TimeUnit.SECONDS)` |
| FetchOnlyOnce           | It calls the API only once in the app lifetime. Please note if you kill the app, this strategy will fetch again. |
| FetchAlways | It does not limit the API calls in any way. |

### Forcing a fetch

If you need to force a fetch to the API ignoring the rate limiter, you should send a parameter to the `fetch` call the following way:

```kotlin
val result = personStore.fetch(RoomPersonStore.Key("1"), forced = true)
```

### Disabling the Rate Limiter

The Rate limiter is active by default, but you can disable it for debugging purposes with the code:

```kotlin
StoreConfig.isRateLimiterEnabled = { // You can use a feature flag or a remote config here to return a Boolean }
```

## Throttling

When there is an API error -response code 500 or above-, we have implemented a throttling controller for the subsequent service calls. If there were more than 3 errors in a row the next service calls will result in an immediate local error and it will not be executed for an exponential growing amount of time. This allows to not flood the server with retries forcing a waiting times between calls.

Note: The throttled requests are not retried automatically nor queued, they result in an error. You can retry them from the client code.

### Disabling throttling

The throttling is active by default, but you can disable it for debugging purposes this way:
```kotlin
StoreConfig.isThrottlingEnabled = { // You can use a feature flag or a remote config here to return a Boolean }
```

### Showing API errors on the screen

`ThrottlingFetcherController` exposes a `throttlingObservable` which you can use to show a message in the screen with a time remaining for the next time you can make a request.

```kotlin
throttlingFetcherController.throttlingObservable.subscribe({ timeout ->
        val throttleTimeout = timeout - Date().time // Amount of time remaining to enable the calls again (milliseconds)
        if (throttleTimeout > 0) {
            throttleError.value = true // Publish a LiveData error event
        }
    }, { error ->
        SNLog.w(TAG, "Error listening for api throttling observer $error")
    })
```

## CRUD Stores

A CrudStore is a Store which also implements create, update and delete methods.
These operations are reflected in API calls (POST, PUT and DELETE REST calls for example), and the new/updated/deleted entities are also created/updated/deleted from the cache when the API call succeeds.

Example of definition:

```kotlin
data class PostKey(val id: Int)

fun providePostsCRUDStore(): SimpleCrudStoreImpl<PostKey, Post> {
    return SimpleCrudStoreBuilder.from(
        fetcher = LimitedCrudFetcher.of(
            fetch = { post -> FetcherResult.Data(provideService().getPost(post.id)) },
  create = { key, post -> FetcherResult.Data(provideService().createPost(post)) },
  update = { key, post -> FetcherResult.Data(provideService().updatePost(key.id, post)) },
  delete = { key, post -> provideService().deletePost(key.id); true },
  ),
  cache = SampleApplication.roomDb.postCache(),
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

 - `PostKey` is the key used to identify each request, in this example
   the id is used to distinguish between different entities.
 - `providePostsCRUDStore` creates the CRUD Store, we provide one method for each CRUD operation: `create`, `update`, `delete`, and `fetch`. We also need to provide a `keyBuilder` function used to generate a new key for the new stored data.
 - The cache in this case is a Room Dao with a query using the key provided.

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
And similar for `update` and `delete`. The cache is updated as soon the response is received.
In general this CRUD requests assumes an API expecting POST and PUT operations which returns the created/updated object with the generated/edited id as a result.

### Error handling
The CRUD operations returns a `StoreResponse` object which can be an `Error`. None of the operations are throwing exceptions.
```kotlin
val response = postsStore.create(PostKey(id = 1), Post(title = "Title", body = "Body", userId = 1))
when (response) {
    is StoreResponse.Data -> uiState.value = "Created"
    is StoreResponse.Error -> uiState.value = "Error in create"
}
```


## Roadmap

 - Review staleness options
 - Return the same fetcher response when a request is the same as one already sent
 - Add an optional memory cache
 - Publish the library
