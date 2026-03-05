package dev.fishies.ranim2.core

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.createCoroutine
import kotlin.coroutines.resume

class AnimatedScene : Scene(), Animated {
    internal lateinit var continuation: Continuation<Unit>
    override var isFinished = false

    override fun tick() {
        continuation.resume(Unit)
    }

    /**
     * Waits for one frame.
     */
    suspend fun yield() = suspendCancellableCoroutine { continuation = it }
}

/**
 * Runs [animations] in parallel. If any of them inherit from [Element], they are automatically added as children to
 * be drawn, and automatically removed once they finish.
 */
suspend fun AnimatedScene.yield(vararg animations: Animated) {
    val elements = animations.filterIsInstance<Element>()
    val finishedAnimations = mutableSetOf<Animated>()
    children += elements
    while (finishedAnimations.size < animations.size) {
        for (animation in animations.filter { it !in finishedAnimations }) {
            animation.tick()
            if (animation.isFinished) {
                finishedAnimations.add(animation)

                if (animation !is Element) continue
                children -= animation
            }
        }
        yield()
    }
}

suspend fun AnimatedScene.yield(frames: Frames) {
    for (frame in 1..frames) {
        yield()
    }
}

fun animation(block: suspend AnimatedScene.() -> Unit) = AnimatedScene().apply {
    continuation = block.createCoroutine(
        receiver = this,
        completion = Continuation(EmptyCoroutineContext) { result ->
            if (result.isFailure) {
                println("Animation cancelled with exception: $result")
                println("Backtrace: ${result.exceptionOrNull()?.stackTraceToString()}")
            }
            isFinished = true
        }
    )
}
