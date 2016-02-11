# README #

This is a Java 7 compatible promise implementation aiming to conform to the [Promises/A+](https://promisesaplus.com/) specification to as much extent as possible, with the constraints given by the Java 7 language.

The implemenation is heavily influenced by Kris Kowal's [Q](https://github.com/kriskowal/q) JavaScript library.

[Download](https://github.com/code77se/jq#getting-jq)

### What is this repository for? ###

Promises enables you to easily adopt a clean and simple asynchronous programming style in an event driven environment. Like Futures, Promises represent the pending result of a task. When the task completes or fails, the result or error can be retrieved. However, unlike standard Java Futures, which only offers blocking methods to retrieve the result of the task, Promises may be observed using callbacks that are invoked asynchronously when the task is complete. 

A promise is said to be resolved or fulfilled when the task associated with the promise is complete, and rejected if the task fails. To observe the resolution or rejection of a promise, the `then` method (and/or any of its variants) is used. Since `then` returns a new promise, which will be resolved or rejected depending on the outcome of the next task initiated inside the callback for the previous task, promises can effectively be chained - the outcome of a promise is either directly passed to the appropriate callback, or if no such callback is registered, propagated to the next promise in the chain. This means that you can for example place one single rejection handler at the end of a promise chain, which will catch any error occurring throughout the entire chain of tasks, similar to a try/catch block in synchronous code. It also means you can get rid of excessive indentation caused by callbacks registering callbacks registering callbacks.

Promises are ideal for code that may be broken down into multiple, shorter tasks that depend on each other. Each step of the task is then typically performed in the fulfillment callback for the previous task. This is very flexible because each step may return either a direct value, such as a `String` (which must then be wrapped in a `Value`), or a promise associated with a new task. In case a direct value is returned or an exception is thrown, it will be used to directly resolve/reject the next promise in the chain. On the other hand, if a new promise is returned, the next promise in the chain will be resolved/rejected with the value of the new promise once it's done.

A caller receiving a promise may only observe its state and attach callbacks to it, never modify its state. Only the task that created the promise may modify it. To accomplish this, promises can only be created by calling any of the `defer` methods on the `JQ` class. These methods give the task access to a deferred object which may be used to modify the state of a promise (however, note that the state of a promise may be modified only once). There are also convenience methods for creating promises for tasks handled with standard Java concurrent classes such as `Callable` and `Executor`.

The `JQ` class also contains several useful utilities. For example, multiple parallel asynchronous tasks can be coordinated using the `all`, `any` or `race` methods.

The library may be used in basically any Java environment using an event loop. It offers a `Config` class used to integrate event dispatching into the host environment, and has built-in support for Android, relying on `Looper` and `Handler`. The library offers utilities for wrapping other well-known asynchronous mechanisms such as Java 8 `CompletionStage`, Koushik Dutta's AndroidAsync/Ion `Future` implementation or standard Java5 `Future` into promises.



### Getting JQ ###

##### Jars 

[jq.jar](http://TODO)

##### Maven
```xml
<dependency>
   <groupId>se.code77.jq</groupId>
   <artifactId>jq</artifactId>
   <version>[1.0.0,)</version>
</dependency>
```

##### Gradle
```groovy
dependencies {
    compile 'se.code77.jq:jq:1.0.+'
}
````

### Configuration

The library expects a Config object to be passed at startup. The Config object serves as an integration layer to enable using the library in custom environments. It supports Android's event model by default, but may be tailored to your needs. The library itself has no dependencies except for standard Java7. For Android, a default config is used automatically if no config is supplied.

### API documentation ###

[Javadoc](https://code77se.github.io/jq/)

### Examples ###

```

// Creating a promise for a blocking background task and resolving it upon completion
private Promise<Integer> someOtherMethod(final String x) {
  return JQ.defer(deferred -> {
    new Thread() {
      public void run() {
        try {
          int res = waitForSomething(x);
          deferred.resolve(res);
        } catch (InterruptedException e) {
          deferred.reject(e);
        }
      }
    }.start();
  });
}

// Calling a method returning a promise and chaining the result to a set of 
// other asynchronous actions and an error handler
someMethod().then(x -> {
  return someOtherMethod(x);
}).then(y -> {
  if (y > 0) {
    return Value.wrap(y * 2);
  } else {
    throw new IllegalStateException("Something");
  }
}).then(z -> {
  return Value.wrap("Foobar");
}).fail(err -> {
  // Exception was thrown  
}).done();


// Wrapping Ion Future
return JQ.wrap(Ion.with(mContext).load("http://foobar.com").
               asJsonObject()).then(json -> {
  // Note that Future:s may be returned without wrapping
  return Ion.with(mContext).load(json.get("url")).asJsonObject();	      
}).then(data -> {
  return parseJson(json);
}).then(data -> {
  // Do something with parsed data
}).fail(err -> {  
  // Error handling
}).done();




```


### Who do I talk to? ###

* henrik@code77.se
