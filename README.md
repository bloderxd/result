# Result
This is a way to modeling success/error values from operations following a pure unwrapping way of success values and a linear and decoupled error handling way.

# What that means?
`Result` is a name of a struct that helps to get operation values, for example when we need to do a request we can have a response from the request or an error if something goes wrong. Most of `Result` implementations returns states to check if the operation was success or error like this:

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
