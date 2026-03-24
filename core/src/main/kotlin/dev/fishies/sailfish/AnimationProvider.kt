package dev.fishies.sailfish

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class AnimationProvider(val framerate: Int = 60)
