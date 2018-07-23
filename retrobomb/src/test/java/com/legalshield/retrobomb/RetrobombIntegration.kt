package com.legalshield.retrobomb

import com.google.re2j.Pattern
import com.greghaskins.spectrum.Spectrum
import com.greghaskins.spectrum.Spectrum.*
import org.amshove.kluent.*

import org.junit.runner.RunWith
import retrofit2.http.*
import java.util.*
import kotlin.reflect.KClass

@RunWith(Spectrum::class)
class RetrobombIntegration {
    init {
        describe("Retrobomb integration tests") {
            describe("#generateMapping") {
                lateinit var results: Map<RouteStatusKey, KClass<*>>

                describe("when there are no routes to map") {
                    lateinit var retrobomb: Retrobomb<FakeEmptyRepository>

                    beforeEach {
                        retrobomb = Retrobomb(FakeEmptyRepository::class.java)
                        results = retrobomb.generateMapping()
                    }

                    it("produces an empty map") {
                        results.shouldBeEmpty()
                    }
                }

                describe("when there are routes present without path variables") {
                    lateinit var retrobomb: Retrobomb<FakeRepository>
                    val expectedPattern = Pattern.compile("${beginning}v1/something$terminal")

                    beforeEach {
                        retrobomb = Retrobomb(FakeRepository::class.java)
                        results = retrobomb.generateMapping()
                    }

                    it("produces a map with a key for GET") {
                        results.shouldContain(RouteStatusKey(expectedPattern, Retrobomb.HttpMethod.GET, 500) to FakeUnknownError::class)
                    }

                    it("produces a map with a key for POST") {
                        results.shouldContain(RouteStatusKey(expectedPattern, Retrobomb.HttpMethod.POST, 401) to FakeAuthError::class)
                    }

                    it("produces a map with a key for PATCH") {
                        results.shouldContain(RouteStatusKey(expectedPattern, Retrobomb.HttpMethod.PATCH, 500) to FakeUnknownError::class)
                    }

                    it("produces a map with a key for PUT") {
                        results.shouldContain(RouteStatusKey(expectedPattern, Retrobomb.HttpMethod.PUT, 401) to FakeAuthError::class)
                    }

                    it("produces a map with a key for DELETE") {
                        results.shouldContain(RouteStatusKey(expectedPattern, Retrobomb.HttpMethod.DELETE, 500) to FakeUnknownError::class)
                    }

                    it("produces a map with a key for HEAD") {
                        results.shouldContain(RouteStatusKey(expectedPattern, Retrobomb.HttpMethod.HEAD, 401) to FakeAuthError::class)
                    }

                    it("produces a map with a key for OPTIONS") {
                        results.shouldContain(RouteStatusKey(expectedPattern, Retrobomb.HttpMethod.OPTIONS, 500) to FakeUnknownError::class)
                    }

                    it("should produce only 7 results") {
                        results.size shouldEqual 7
                    }

                    describe("generated regular expression spec") {
                        lateinit var expression: Pattern

                        beforeEach {
                            expression = expectedPattern
                        }

                        it("should match the intended route") {
                            expression.matcher("https://www.something.com/v1/something")
                                .matches().shouldBeTrue()
                        }

                        it("should match the intended route ending with a /") {
                            expression.matcher("https://www.something.com/v1/something/")
                                .matches().shouldBeTrue()
                        }

                        it("should not match when there are query parameters") {
                            expression.matcher("https://www.something.com/v1/something?query=something")
                                .matches().shouldBeFalse()
                        }

                        it("should not match when a longer route") {
                            expression.matcher("https://www.something.com/v1/something/hello")
                                .matches().shouldBeFalse()
                            expression.matcher("https://www.something.com/v1/something/25")
                                .matches().shouldBeFalse()
                        }
                    }
                }

                describe("when there are routes present with path variables") {
                    lateinit var retrobomb: Retrobomb<FakePathVariableRepository>

                    val expectedPathVariableKey = RouteStatusKey(
                        Pattern.compile("${beginning}v1/widgets/$pathVar/cogs/$pathVar$terminal"),
                        Retrobomb.HttpMethod.GET,
                        500
                    )

                    beforeEach {
                        retrobomb = Retrobomb(FakePathVariableRepository::class.java)
                        results = retrobomb.generateMapping()
                    }

                    it("produces a map with the appropriate regex in the key") {
                        results.shouldContain(expectedPathVariableKey to FakeUnknownError::class)
                    }

                    it("should produce a single result") {
                        results.size shouldEqual 1
                    }

                    describe("generated regular expression spec") {
                        lateinit var expression: Pattern

                        beforeEach {
                            expression = expectedPathVariableKey.route
                        }

                        it("should match the intended route with substitutions") {
                            expression.matcher("https://www.something.com/v1/widgets/25/cogs/1")
                                .matches().shouldBeTrue()
                        }

                        it("should not match the route when there are query parameters") {
                            expression.matcher("https://www.something.com/v1/widgets/25/cogs/1?query=something")
                                .matches().shouldBeFalse()
                        }

                        it("should match the intended route ending with a /") {
                            expression.matcher("https://www.something.com/v1/widgets/25/cogs/25")
                                .matches().shouldBeTrue()
                        }

                        it("should not match when there are additional variables") {
                            expression.matcher("https://www.something.com/v1/widgets/25/cogs/25/bits")
                                .matches().shouldBeFalse()
                            expression.matcher("https://www.something.com/v1/widgets/25/cogs/25/bits/2")
                                .matches().shouldBeFalse()
                        }

                        it("should not match when there are missing variables") {
                            expression.matcher("https://www.something.com/v1/widgets/25")
                                .matches().shouldBeFalse()
                        }

                        it("should not match when there are other variables") {
                            expression.matcher("https://www.something.com/v1/widgets/25/notCogs/1")
                                .matches().shouldBeFalse()
                        }

                        it("should not match when a variable has an empty value") {
                            expression.matcher("https://www.something.com/v1/widgets//notCogs/1")
                                .matches().shouldBeFalse()
                        }

                        it("should not match when the route ends with a missing value") {
                            expression.matcher("https://www.something.com/v1/widgets/25/cogs//")
                                .matches().shouldBeFalse()
                        }
                    }
                }

                describe("when there are routes present with query parameters") {
                    lateinit var retrobomb: Retrobomb<FakeQueryParamRepository>

                    val expectedSingleParamKey = RouteStatusKey(
                        Pattern.compile("${beginning}v1/widgets$terminal${beginQuery}widgetType=$queryParam"),
                        Retrobomb.HttpMethod.GET,
                        401
                    )

                    val expectedDoubleParamKey = RouteStatusKey(
                        Pattern.compile("${beginning}v1/widgets$terminal${beginQuery}widgetType=$queryParam&createdBefore=$queryParam"),
                        Retrobomb.HttpMethod.GET,
                        500
                    )

                    beforeEach {
                        retrobomb = Retrobomb(FakeQueryParamRepository::class.java)
                        results = retrobomb.generateMapping()
                    }

                    it("produces a map with the appropriate expressions in the key") {
                        results.shouldContain(expectedSingleParamKey to FakeAuthError::class)
                        results.shouldContain(expectedDoubleParamKey to FakeUnknownError::class)
                    }

                    it("should produce two results") {
                        results.size shouldEqual 2
                    }

                    describe("generated regular expression spec") {
                        describe("when a single query parameter is present") {
                            lateinit var expression: Pattern

                            beforeEach {
                                expression = expectedSingleParamKey.route
                            }

                            it("should match the intended route with query param substituted") {
                                expression.matcher("https://www.something.com/v1/widgets?widgetType=broken")
                                    .matches().shouldBeTrue()
                            }

                            it("should match the intended route ending with a / before the query") {
                                expression.matcher("https://www.something.com/v1/widgets/?widgetType=broken")
                                    .matches().shouldBeTrue()
                            }

                            it("should match a query parameter with an empty value") {
                                expression.matcher("https://www.something.com/v1/widgets?widgetType=")
                                    .matches().shouldBeTrue()
                            }

                            it("should not match when there are extra parameters") {
                                expression.matcher("https://www.something.com/v1/widgets?widgetType=fixed&widgetLength=short")
                                    .matches().shouldBeFalse()
                            }

                            it("should not match when there are missing parameters") {
                                expression.matcher("https://www.something.com/v1/widgets")
                                    .matches().shouldBeFalse()
                            }

                            it("should not match when there are other query parameters") {
                                expression.matcher("https://www.something.com/v1/widgets?widgetType=fixed&widgetLength=short")
                                    .matches().shouldBeFalse()
                            }
                        }

                        describe("when multiple query parameters are present") {
                            lateinit var expression: Pattern

                            beforeEach {
                                expression = expectedDoubleParamKey.route
                            }

                            it("should match the intended route with query param substitutions") {
                                expression.matcher("https://www.something.com/v1/widgets?widgetType=fixed&createdBefore=100")
                                    .matches().shouldBeTrue()
                            }

                            it("should match the intended route ending with a / before the query") {
                                expression.matcher("https://www.something.com/v1/widgets/?widgetType=fixed&createdBefore=300")
                                    .matches().shouldBeTrue()
                            }

                            it("should match parameters with empty values") {
                                expression.matcher("https://www.something.com/v1/widgets?widgetType=&createdBefore=")
                                    .matches().shouldBeTrue()
                            }

                            it("should not match when there are extra parameters") {
                                expression.matcher("https://www.something.com/v1/widgets?widgetType=fixed&createdBefore=yesterday&widgetSize=nvm")
                                    .matches().shouldBeFalse()
                            }

                            it("should not match when there are missing parameters") {
                                expression.matcher("https://www.something.com/v1/widgets?widgetType=fixed")
                                    .matches().shouldBeFalse()
                            }

                            it("should not match when there are other query parameters") {
                                expression.matcher("https://www.something.com/v1/widgets?widgetType=fixed&widgetLength=short")
                                    .matches().shouldBeFalse()
                            }
                        }
                    }
                }

                describe("when there are routes present with path and query parameters") {
                    lateinit var retrobomb: Retrobomb<FakeComplexRepository>
                    val expectedComplexKey = RouteStatusKey(
                        Pattern.compile("${beginning}v1/widgets/$pathVar/cogs/$pathVar$terminal" +
                            "${beginQuery}widgetType=$queryParam&createdBefore=$queryParam"),
                        Retrobomb.HttpMethod.GET,
                        500
                    )

                    beforeEach {
                        retrobomb = Retrobomb(FakeComplexRepository::class.java)
                        results = retrobomb.generateMapping()
                    }

                    it("produces a map with the appropriate regex as the key") {
                        results.shouldContain(expectedComplexKey to FakeUnknownError::class)
                    }

                    describe("generated regular expression spec") {
                        lateinit var expression: Pattern

                        beforeEach {
                            expression = expectedComplexKey.route
                        }

                        it("should match the intended route with substitutions") {
                            expression.matcher("https://www.something.com/v1/widgets/10/cogs/25?widgetType=fixed&createdBefore=100")
                                .matches().shouldBeTrue()
                        }

                        it("should match the intended route ending with a / before the query") {
                            expression.matcher("https://www.something.com/v1/widgets/10/cogs/25/?widgetType=fixed&createdBefore=300")
                                .matches().shouldBeTrue()
                        }

                        it("should match when parameters have empty values") {
                            expression.matcher("https://www.something.com/v1/widgets/10/cogs/25?widgetType=&createdBefore=")
                                .matches().shouldBeTrue()
                        }

                        it("should not match when path variables have empty values") {
                            expression.matcher("https://www.something.com/v1/widgets//cogs/25?widgetType=fixed&createdBefore=100")
                                .matches().shouldBeFalse()
                        }

                        it("should not match when there are extra parameters") {
                            expression.matcher("https://www.something.com/v1/widgets/10/cogs/25?widgetType=fixed&createdBefore=yesterday&widgetSize=nvm")
                                .matches().shouldBeFalse()
                        }

                        it("should not match when there are missing parameters") {
                            expression.matcher("https://www.something.com/v1/widgets/10/cogs/25?widgetType=fixed")
                                .matches().shouldBeFalse()
                        }

                        it("should not match when there are other query parameters") {
                            expression.matcher("https://www.something.com/v1/widgets/10/cogs/25?widgetType=fixed&widgetLength=short")
                                .matches().shouldBeFalse()
                        }

                        it("should not match when there are extra variables") {
                            expression.matcher("https://www.something.com/v1/widgets/10/cogs/25/extras/30?widgetType=fixed&createdBefore=yesterday")
                                .matches().shouldBeFalse()
                        }

                        it("should not match when there are missing variables") {
                            expression.matcher("https://www.something.com/v1/widgets/10?widgetType=fixed&createdBefore=yesterday")
                                .matches().shouldBeFalse()
                        }

                        it("should not match when there are other variables") {
                            expression.matcher("https://www.something.com/v1/widgets/10/notCogs/25?widgetType=fixed&createdBefore=yesterday")
                                .matches().shouldBeFalse()
                        }
                    }
                }
            }
        }
    }

    interface FakeEmptyRepository
    @Suppress("unused")
    interface FakeRepository {
        @RetrobombMappings(ErrorMapping(500, FakeUnknownError::class))
        @GET("v1/something")
        fun getSomething()

        @RetrobombMappings(ErrorMapping(401, FakeAuthError::class))
        @POST("v1/something")
        fun postSomething()

        @RetrobombMappings(ErrorMapping(500, FakeUnknownError::class))
        @PATCH("v1/something")
        fun patchSomething()

        @RetrobombMappings(ErrorMapping(401, FakeAuthError::class))
        @PUT("v1/something")
        fun putSomething()

        @RetrobombMappings(ErrorMapping(500, FakeUnknownError::class))
        @DELETE("v1/something")
        fun deleteSomething()

        @RetrobombMappings(ErrorMapping(401, FakeAuthError::class))
        @HEAD("v1/something")
        fun headSomething()

        @RetrobombMappings(ErrorMapping(500, FakeUnknownError::class))
        @OPTIONS("v1/something")
        fun optionsSomething()
    }

    @Suppress("unused")
    interface FakePathVariableRepository {
        @RetrobombMappings(ErrorMapping(500, FakeUnknownError::class))
        @GET("v1/widgets/{widgetId}/cogs/{cogId}")
        fun getCogById(@Path("widgetId") widgetId: String, @Path("cogId") cogId: Int)
    }

    @Suppress("unused")
    interface FakeQueryParamRepository {
        @RetrobombMappings(ErrorMapping(401, FakeAuthError::class))
        @GET("v1/widgets")
        fun getFilteredWidgets(@Query("widgetType") widgetTypeQueryParam: String)

        @RetrobombMappings(ErrorMapping(500, FakeUnknownError::class))
        @GET("v1/widgets")
        fun getFilteredWidgets(@Query("widgetType") widgetTypeQueryParam: String, @Query("createdBefore") createdBefore: Date)
    }

    @Suppress("unused")
    interface FakeComplexRepository {
        @RetrobombMappings(ErrorMapping(500, FakeUnknownError::class))
        @GET("v1/widgets/{widgetId}/cogs/{cogId}")
        fun getFilteredWidgets(@Query("widgetType") widgetTypeQueryParam: String, @Query("createdBefore") createdBefore: Date)
    }

    data class FakeUnknownError(val message: String)
    data class FakeAuthError(val message: String)

    companion object {
        const val beginning = "^.*?"
        const val terminal = "/?"
        const val queryParam = "[^&]*"
        const val pathVar = "[^/?]+"
        const val beginQuery = "\\?"
    }
}