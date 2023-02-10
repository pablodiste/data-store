# ChangeLog

v0.1.0
First release
- Handles caching of fetched data automatically.
- Integrations with Room, Realm and Retrofit.
- Rate Limiter, join requests to active API calls.
- Basic throttling implementation. 
- Allows CRUD implementation using the same data stores.
- Sample project
- Release configuration for Maven Central

v0.1.1
- Memory source of truth
- Ktor integration
- Bugfixes
- Adding some tests to repositories

v0.1.2
- Fix bug for in memory store
- Adding StoreRequest with additional configurations to main store implementation
- Initial implementation of WritableStore
    - Basic pending operation worker job for sending changes.
    - Connecting stores. Reapplying changes when fetching data from other stores.
    - Tests for basic operations, create, update, delete

v0.1.3
- Improved rate limiter. Now it supports number of calls and the possibility of adding more implementations.

v0.1.4
- Added more detail in fetcher results.
- Supporting Retries.
- Bugfix in in-memory store.

v0.1.5
- Refactor for fetchers, convert features to operators.
- Adding tests to fetchers.
- Bugfix in joinInProgressCall behavior.

v0.1.6
- Refactors for CRUD, using Senders

