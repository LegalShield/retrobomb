package com.legalshield.retrobomb

import com.greghaskins.spectrum.Spectrum
import com.greghaskins.spectrum.Spectrum.*
import com.legalshield.retrobomb.extensions.handleRetrobombErrorData
import com.legalshield.retrobomb.extensions.unwrapRetrobombException
import org.amshove.kluent.shouldBeFalse
import org.amshove.kluent.shouldBeTrue
import org.amshove.kluent.shouldEqual
import org.junit.runner.RunWith

@RunWith(Spectrum::class)
class ThrowableExtensionSpec {
    init {
        describe("Throwable Extension Spec") {
            describe("#handleRetrobombErrorData") {
                describe("when the throwable is not a RetrobombException") {
                    lateinit var throwable: Throwable

                    beforeEach { throwable = RuntimeException() }

                    it("should not invoke the handler") {
                        throwable.handleRetrobombErrorData<Unit> { true.shouldBeFalse() }
                    }

                    it("should return false") {
                        throwable.handleRetrobombErrorData<Unit> { }.shouldBeFalse()
                    }
                }

                describe("when the throwable is a RetrobombException") {
                    lateinit var throwable: Throwable

                    describe("when the data type matches") {
                        lateinit var fakeData: FakeData
                        beforeEach {
                            fakeData = FakeData("")
                            throwable = RetrobombException(fakeData)
                        }

                        it("should invoke the handler with the correct data") {
                            lateinit var handlerData: FakeData

                            throwable.handleRetrobombErrorData<FakeData> { handlerData = it }

                            handlerData shouldEqual fakeData
                        }

                        it("should return true") {
                            throwable.handleRetrobombErrorData<FakeData> { }.shouldBeTrue()
                        }
                    }

                    describe("when the data type does not match") {
                        lateinit var fakeData: String
                        beforeEach {
                            fakeData = ""
                            throwable = RetrobombException(fakeData)
                        }

                        it("should not invoke the handler") {
                            throwable.handleRetrobombErrorData<FakeData> { true.shouldBeFalse() }
                        }

                        it("should return false") {
                            throwable.handleRetrobombErrorData<FakeData> { }.shouldBeFalse()
                        }
                    }
                }
            }

            describe("#unwrapRetrobombException") {
                lateinit var throwable: Throwable

                describe("when the throwable is not a RetrobombException") {
                    beforeEach { throwable = Throwable() }

                    it("should not invoke the handler") {
                        throwable.unwrapRetrobombException { true.shouldBeFalse() }
                    }

                    it("should return false") {
                        throwable.unwrapRetrobombException { }.shouldBeFalse()
                    }
                }

                describe("when the throwable is a RetrobombException") {
                    lateinit var retrobombException: RetrobombException

                    beforeEach {
                        retrobombException = RetrobombException("")
                        throwable = retrobombException
                    }

                    it("should invoke the handler with the exception data") {
                        lateinit var unwrapResult: Any

                        throwable.unwrapRetrobombException { unwrapResult = it }

                        unwrapResult shouldEqual retrobombException.data
                    }

                    it("should return true") {
                        throwable.unwrapRetrobombException { }.shouldBeTrue()
                    }
                }
            }
        }
    }

    data class FakeData(val id: String)
}