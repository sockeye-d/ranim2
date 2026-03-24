package dev.fishies.sailfish

interface Animated {
    val isFinished: Boolean

    /**
     * Runs one tick of the animation.
     * @return True if the animation is finished
     */
    fun tick()
}
