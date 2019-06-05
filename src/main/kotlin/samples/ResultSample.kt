package samples

import Result

internal fun sum(result1: Result<Int>, result2: Result<Int>): Int = try {
    result1.get() + result2.get()
} catch (e: Exception) {
    0
}

internal class IsNotStringException : Exception()
internal class IsNotIntegerException : Exception()

internal fun errorResultWith(exception: Exception): Result<String> = Result.error<String>(exception)
    .composeError(::handleIsNotStringCase)
    .composeError(::handleIsNotIntegerCase)

internal fun handleIsNotStringCase(exception: IsNotStringException) {
    println("Is not a String, sorry =(")
}

internal fun handleIsNotIntegerCase(exception: IsNotIntegerException) {
    println("Is not an Integer, sorry =(")
}