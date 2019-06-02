inline class Result<out T>(private val value: Any?) {

    companion object {
        inline fun <T> ok(value: T) : Result<T> = Result(value)
        inline fun <T> error(exception: Exception) : Result<T> = Result(Failure(exception))
    }

    fun get() : Any? = value

    @Suppress("UNCHECKED_CAST")
    fun getOrThrow() : T = when(value) {
        is Failure -> throw value.dispatchHandlers()
        else -> value as T
    }

    open class Failure(val exception: Exception) {
        @PublishedApi internal var handler: (Exception) -> Exception = {it}

        inline fun <reified T : Exception> handleExceptionWith(crossinline handler: (T) -> Unit) {
            this.handler = compose({ e -> if(e is T) handler(e); e }, this.handler)
        }

        inline fun <A, B, C> compose(crossinline f: (B) -> C, crossinline g: (A) -> B): (A) -> C {
            return { x -> f(g(x)) }
        }

        fun dispatchHandlers() : Exception = exception.also { this.handler(exception) }
    }
}

inline fun <T, R> Result<T>.map(transform: (T) -> R) : Result<R> = when(val value = get()) {
    is Result.Failure -> Result(value)
    else -> Result.ok(transform(value as T))
}

inline fun <T, R> Result<T>.flatMap(transform: (T) -> Result<R>) : Result<R> = when(val value = get()) {
    is Result.Failure -> Result(value)
    else -> transform(value as T)
}

inline fun <reified T, reified R : Exception> Result<T>.composeError(crossinline function: (R) -> Unit) : Result<T> = when(val value = this.get()) {
    is Result.Failure -> this.also { value.handleExceptionWith(function) }
    else -> this
}