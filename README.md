# retrobomb
[ ![Codeship Status for LegalShield/retrobomb](https://app.codeship.com/projects/e2669770-62cc-0136-4a2b-16beb3710b43/status?branch=master)](https://app.codeship.com/projects/296894)

Error mapping library for Retrofit and OkHttp. Provides an interceptor that allows you to continue working with existing call adapter factories.

## Setup
To begin mapping, add the interceptor to the OkHttpClient and use that to build your Retrofit
```java
OkHttpClient client = new OkHttpClient.Builder()
  .addInterceptor(new RetrobombInterceptor(YourRepositoryInterface.class)) /* Must be first interceptor */
  /* Continue configuration */
  .build();

Retrofit retrofit = new Retrofit.Builder()
  .client(client)
  /* Continue configuration */
  .build();
```

## Usage
By default, retrobomb will assume the response is a String, but can be configured to use any class Gson can map to by annotating the Retrofit interface like so.
```java
public interface YourRepository {
  @RetrobombMappings(
    @ErrorMapping(code = 404, errorType = NotFound.class)
    @ErrorMapping(code = 401, errorType = Unauthorized.class)
    @ErrorMapping(code = 400, errorType = UpdateWidgetValidationError.class)
  )
  @PATCH("/v1/widgets/{id}")
  Single<Widget> updateWidget(@Path("id") String id, @Body Widget widget);
}
```

Depending on the converter factory you use for Retrofit, you can use the converted data in multiple ways. If you're using retrofit synchronously, simply catch the RetrobombException. If you're using an asynchronous library like RxJava, there are some helper functions for you.

All of these snippets assume the use of RxJava.

For Java:
```java
/* Make call for observable */
  .subscribe(data -> {
    /* success stuff */
  }, throwable -> {
    if (RetrobombHandler.handleErrorData(throwable, ValidationError.class, this::handleValidationError)) return;
    /* reaching this point means you have a RetrobombMappingException or IOException due to connectivity etc. */
  });
```

For Kotlin when you have only one expected error type:
```kotlin
/* Make call for observable */
  .subscribe({ data ->
    /* success stuff */
  }, { throwable ->
    if (throwable.handleRetrobombErrorData<String> { data ->
      /* Handle string data */
    }) return
    /* reaching this point means you have a RetrobombMappingException or IOException due to connectivity etc. */
  }
```

For Kotlin when you have multiple expected error types:
```kotlin
/* Make call for observable */
  .subscribe({ data ->
    /* success stuff */
  }, { throwable ->
    if (throwable.unwrapRetrobombException {
      when(it) {
        is ValidationError -> /* handle validation error */
        is String -> /* handle generic error */
      }
    }) return
    /* reaching this point means you have a RetrobombMappingException or IOException due to connectivity etc. */
  }
```
