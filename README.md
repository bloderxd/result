# Result
This is a way to modeling success/error values from operations following a pure unwrapping way of success values and a linear and decoupled error handling way.

# Problem
`Result` is a name of a struct that helps to get operation values, for example when we need to do a request we can have a success response from the request or an error if something goes wrong. Most of `Result` implementations returns states to check if the operation was success or error like this:

```kotlin
fun getResponse(): Result<Response, Error> {
  return if(isSuccess()) Result.success(Response) else Result.error(Error())
}

fun handleResponse() {
  val responseResult = getResponse()
  if(responseResult == Result.Success) {
    // Do something with success data
  } else {
    // Do something with error
  }
}
```

But this start to be a bad idea when we try to implement basic stuffs around it like error handling or some kind of comparation of success value:

```kotlin
fun getResponsesAndHandle() {
  val responseResult = getResponse()
  val responseResult2 = getResponse2()
  if(responseResult == Result.Success && responseResult2 == Result.Success) {
    println(responseResult.data + responseResult2.data)
  } if(responseResult == Result.Error) {
    handleResponseError(responseResult.errorData)
  } if(responseResult2 == Result.Error) {
    handleResponse2Error(responseResult.errorData)
  }
}
```

I didn't even put the error state checking there like ```when(responseResult.errorData) {}``` to check what kind of error each result had and it's already really ugly.

# Solution
To avoid results with states, this implementation was created: A way to get operation results with its pure values.

## Result struct

To demonstrate this `Result` features, let's create an example:

Here we want to get some pre defined data, then let's create a function that will return a `Result` that wraps a `Data` type:

```kotlin

private fun fetchData(): Result<Data> {
  val data = repository.getData() // fun getData(): Data?
  return if(data != null) Result.ok(data) else Result.error(NullDataException())
}
```

Ok, here we have a function that get and checks if the data is not null and if it wasn't, returns a `Result` with its data, if is, it returns a `Result` with an `Exception`, and yes: you don't need to specify in `Result` return assignment which `Exception` you want to return like `fun fetchData(): Result<Data, NullDataException>` just declare as `Result<Data>`, return a `Result.error()` with wathever `Exception` you want and you're good to go.

Let's now unwrap this `Data` value from our `Result` to use in our application:

```kotlin
// fetchData method
...

private fun handleDataFromRequest() = try {
  showData(fetchData().get())
} catch(e: Exception) {
  showError()
}

private fun showData(data: Data) {
  // Handle Data value
}

private fun showError() {
  // Handle error case 
}
```

The `get()` method will return for us the pure value (`Data` type in this case) then we don't actually need to check states to see if that result was a success or not, `get()` will decide it and if is not a success it will throw the `Exception` that we tell the result before (`Result.error(NullDataException())`). 

Let's create again our first example but using our implementation of `Result` to show how easy is compare two or more result values:

```kotlin
data class Response(val number: Int)

fun getResponsesAndHandle() = try {
  val response = getResponse().get() // fun getResponse(): Result<Response>
  val response2 = getResponse2().get() // fun getResponse2(): Result<Response>
  val response3 = getResponse3().get() // fun getResponse3(): Result<Response>
  val response4 = getResponse4().get() // fun getResponse4(): Result<Response>
  println(response.number + response2.number + response3.number + response4.number)
} catch(e: Exception) {
  handleGenericError()
}
```

Here we have a comparation with 4 values and if something goes wrong `handleGenericError()` is called, but sometimes we don't want to handle just a generic error but specifc errors and following this mindset probably the only way to handle that is in `catch()` making a `when(exception) {}` and checking every single `Exception` like `is NullDataException` for example, right? Wrong! Let's compose our errors.

## Error cases with composition
