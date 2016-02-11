# README #

This is a Java 7 compatible promise implementation aiming to conform to the [Promises/A+](https://promisesaplus.com/) specification to as much extent as possible, with the constraints given by the Java 7 language.

The implemenation is heavily influenced by Kris Kowal's [Q](https://github.com/kriskowal/q) JavaScript library.

### What is this repository for? ###

* Promises enables you to easily adopt a clean and simple asynchronous programming style in an event driven environment.
* This library has built-in support for Android and offers a cleaner approach to asynchronous programming than for example `AsyncTask`, thanks to the chaining capabilities of promises.
* The Promise object in this library can be used to wrap other well-known asynchronous mechanisms such as Java 8 `CompletionStage`, Koush Dutta's AndroidAsync/Ion `Future` implementation or standard Java5 `Future`.


### How do I get set up? ###

* Download the latest JAR [here](todo) or use Gradle to include the library in your project.
* The library expects a Config object to be passed at startup. The Config object serves as an integration layer to enable using the library in custom environments. It supports Android's event model by default, but may be tailored to your needs. The library itself has no dependencies except for standard Java7.

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
