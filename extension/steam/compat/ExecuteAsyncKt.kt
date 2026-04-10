/**
 * Compatibility shim for okhttp3.coroutines.executeAsync.
 *
 * The okhttp-coroutines artifact was compiled against Kotlin 2.x / coroutines 1.9+
 * which introduced a new CancellableContinuation.resume(value, onCancellation: Function3)
 * overload. The base APK ships an older coroutines that only has single-arg resume,
 * so calling the 2-arg form throws NoSuchMethodError at runtime.
 *
 * This shim provides the same suspend fun Call.executeAsync() but uses
 * Continuation.resumeWith(Result) from kotlin-stdlib (always present) so it
 * works with any coroutines version in the host APK.
 *
 * okhttp-coroutines.jar must be excluded from the runtime DEX bundle; this
 * class (compiled into the Kotlin Steam DEX) is the only provider at runtime.
 */
package okhttp3.coroutines

import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

suspend fun Call.executeAsync(): Response = suspendCancellableCoroutine { cont ->
    enqueue(object : Callback {
        override fun onResponse(call: Call, response: Response) {
            cont.resumeWith(Result.success(response))
        }
        override fun onFailure(call: Call, e: IOException) {
            cont.resumeWith(Result.failure(e))
        }
    })
    cont.invokeOnCancellation { cancel() }
}
