package com.legalshield.retrobomb.interop

interface Handler<T> {
  fun handle(data: T)
}