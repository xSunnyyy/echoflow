package com.plexglassplayer.data.api.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PlexTv

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PlexTvOkHttp

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PlexTvLogging

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PlexTvJson
