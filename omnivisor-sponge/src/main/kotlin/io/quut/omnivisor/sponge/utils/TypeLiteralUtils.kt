package io.quut.omnivisor.sponge.utils

import com.google.inject.TypeLiteral

inline fun <reified T> typeLiteral(): TypeLiteral<T> = object : TypeLiteral<T>() { }
