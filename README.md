# Result
A way to modeling success/error values from operations following a pure unwrapping way of success values and a linear and decoupled error handling way.

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
    when(responseResult.errorData) {
       is NetworkException -> {}
       is XException -> {}
       is YException -> {}
    }
  } if(responseResult2 == Result.Error) {
    when(responseResult2.errorData) {
       is NetworkException -> {}
       is XException -> {}
       is YException -> {}
    }
  }
}
```

The need to check states let it [not extensible but modifiable](https://en.wikipedia.org/wiki/Open%E2%80%93closed_principle)

# Solution
To avoid results with custom states, this implementation was created: A way to get operation results with its pure values and consider its own values a state.

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

To avoid exceptions checking and turns error handling more linear and decoupled we start to handle it with [composition](https://en.wikipedia.org/wiki/Function_composition_(computer_science)), let's create an example of error handling:

```kotlin
class ExceptionOne : Exception()
class ExceptionTwo : Exception()
class ExceptionThree : Exception()

private fun createResultWith(number: Int): Result<Int> {
  return if(number == 1) Result.error(ExceptionOne())
  else if(number == 2) Result.error(ExceptionTwo())
  else if(number == 3) Result.error(ExceptionThree())
  else Result.ok(number)
}

private fun handleExceptionOne(exception: ExceptionOne) = println("Your number cannot be 1")
private fun handleExceptionTwo(exception: ExceptionTwo) = println("Your number cannot be 2")
private fun handleExceptionThree(exception: ExceptionThree) = println("Your number cannot be 3")

fun printSumOf(n: Int, n2: Int) = try {
  val left = createResultWith(n)
      .composeError(::handleExceptionOne)
      .composeError(::handleExceptionTwo)
      .composeError(::handleExceptionThree)
      
  val right = createResultWith(n2)
      .composeError(::handleExceptionOne)
      .composeError(::handleExceptionTwo)
      .composeError(::handleExceptionThree)
      
  println(left.get() + right.get())
} catch(e: Exception) {}
```

This is basically an example that ask for two numbers, transform each of them in results and sum them, but the rule is: the number cannot be 1, 2 or 3:

```kotlin
printSumOf(4, 1) // Your number cannot be 1
printSumOf(2, 1) // Your number cannot be 2
printSumOf(5, 3) // Your number cannot be 3
printSumOf(5, 5) // 10
```

Here we're introducing `composeError<MyException> { exception -> }` function that keeps your handling and just execute it if the exception that was throwed is the exception that you specified in `composeError` method, example:

###### Original method syntax
```kotlin
createResultWith(n)
  .composeError<ExceptionOne> { handleExceptionOne(it) } //will be executed if the throwed exception is ExceptionOne
  .composeError<ExceptionTwo> { handleExceptionTwo(it) } //will be executed if the throwed exception is ExceptionTwo
  .composeError<ExceptionThree> { handleExceptionThree(it) } //will be executed if the throwed exception is ExceptionThree
```

###### Kotlin alternative syntax
```kotlin
createResultWith(n)
   .composeError(::handleExceptionOne) //will be executed if the throwed exception is ExceptionOne
   .composeError(::handleExceptionTwo) //will be executed if the throwed exception is ExceptionTwo
   .composeError(::handleExceptionThree) //will be executed if the throwed exception is ExceptionThree
```

## Operators
`Result` also implements concepts like `map` and `flatmap`:

```kotlin
fun getResult(): Result<Int> = Result.ok("1").flatMap(::sum)

private fun sum(number: String): Result<Int> = Result.ok("2").map { it.toInt() + number.toInt() }

println(getResult().get()) // 3
```

# Import

###### Gradle
```groovy
implementation 'bloder.com:result:0.0.1'
```

###### Maven
```xml
<dependency>
	<groupId>bloder.com</groupId>
	<artifactId>result</artifactId>
	<version>0.0.1</version>
	<type>pom</type>
</dependency>
```

# License

```
MIT License

Copyright (c) 2019 Bloder

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
