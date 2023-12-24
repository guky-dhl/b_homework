### Notes:
* The Datastore runs in memory for the sake of simplicity. The Repository pattern was used due to the use of in-memory storage. For some real-life applications, I would use a type-safe SQL builder like QueryDSL or Kotlin Exposed. Thread safety is achieved by using ConcurrentHashMap or a synchronized monitor.

* Introduced the Transaction class for use in balances because of the pattern: every change in user balance should be backed by a transaction entry, and the user's balance is the sum of all entries. These entries are also a convenient way to generate ledger books for accounting purposes and state reporting. While these entries could be replaced by ledger books, I think it is overkill for homework (Keep It Simple, Stupid - [KISS](https://en.wikipedia.org/wiki/KISS_principle)).

* The Command layer doesn't have validation for different cases (such as checking if there is enough balance) since the domain layer protects itself from entering an illegal state and throws exceptions. These exceptions are then handled by the rest layer, transforming them into error messages.

### To Run:
The App class's main method starts the application, or the Gradle task "run" from the application plugin can be used.

By default, the application state is empty, but integration tests can be used as a showcase to fill the state with users.
