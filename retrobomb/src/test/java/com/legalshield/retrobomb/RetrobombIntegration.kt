package com.legalshield.retrobomb

import com.google.re2j.Pattern
import com.greghaskins.spectrum.Spectrum
import com.greghaskins.spectrum.Spectrum.*
import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldBeFalse
import org.amshove.kluent.shouldBeTrue
import org.amshove.kluent.shouldContain

import org.junit.runner.RunWith
import retrofit2.http.*
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

                    beforeEach {
                        retrobomb = Retrobomb(FakeRepository::class.java)
                        results = retrobomb.generateMapping()
                    }

                    it("produces a map with a key for GET") {
                        results.shouldContain(RouteStatusKey(Pattern.compile("${beginning}v1/something$terminal"), Retrobomb.HttpMethod.GET, 500) to FakeUnknownError::class)
                    }

                    it("produces a map with a key for POST") {
                        results.shouldContain(RouteStatusKey(Pattern.compile("${beginning}v1/something$terminal"), Retrobomb.HttpMethod.POST, 401) to FakeAuthError::class)
                    }

                    it("produces a map with a key for PATCH") {
                        results.shouldContain(RouteStatusKey(Pattern.compile("${beginning}v1/something$terminal"), Retrobomb.HttpMethod.PATCH, 500) to FakeUnknownError::class)
                    }

                    it("produces a map with a key for PUT") {
                        results.shouldContain(RouteStatusKey(Pattern.compile("${beginning}v1/something$terminal"), Retrobomb.HttpMethod.PUT, 401) to FakeAuthError::class)
                    }

                    it("produces a map with a key for DELETE") {
                        results.shouldContain(RouteStatusKey(Pattern.compile("${beginning}v1/something$terminal"), Retrobomb.HttpMethod.DELETE, 500) to FakeUnknownError::class)
                    }

                    it("produces a map with a key for HEAD") {
                        results.shouldContain(RouteStatusKey(Pattern.compile("${beginning}v1/something$terminal"), Retrobomb.HttpMethod.HEAD, 401) to FakeAuthError::class)
                    }

                    it("produces a map with a key for OPTIONS") {
                        results.shouldContain(RouteStatusKey(Pattern.compile("${beginning}v1/something$terminal"), Retrobomb.HttpMethod.OPTIONS, 500) to FakeUnknownError::class)
                    }
                }

                describe("when there are routes present with path variables") {
                    lateinit var retrobomb: Retrobomb<FakePathVariableRepository>

                    beforeEach {
                        retrobomb = Retrobomb(FakePathVariableRepository::class.java)
                        results = retrobomb.generateMapping()
                    }

                    it("produces a map with a path regex in the key") {
                        results.shouldContain(RouteStatusKey(Pattern.compile("${beginning}v1/widgets/$pathVar/cogs/$pathVar$terminal"), Retrobomb.HttpMethod.GET, 500) to FakeUnknownError::class)
                    }
                }

                describe("generated regexes only match the intended routes") {
                    it("should match the intended route without host information") {
                        Pattern.compile("${beginning}something$terminal").matcher("something").matches().shouldBeTrue()
                    }

                    describe("route with no parameters") {
                        it("should match the intended route") {
                            Pattern.compile("${beginning}v1/something$terminal").matcher("https://www.something.com/v1/something").matches().shouldBeTrue()
                        }

                        it("should match the intended route ending with a /") {
                            Pattern.compile("${beginning}v1/something$terminal").matcher("https://www.something.com/v1/something/").matches().shouldBeTrue()
                        }

                        it("should match when there are query parameters") {
                            Pattern.compile("${beginning}v1/something$terminal").matcher("https://www.something.com/v1/something?query=something").matches().shouldBeTrue()
                        }

                        it("should not match a longer route") {
                            Pattern.compile("${beginning}v1/something$terminal").matcher("https://www.something.com/v1/something/hello").matches().shouldBeFalse()
                            Pattern.compile("${beginning}v1/something$terminal").matcher("https://www.something.com/v1/something/25/hellos").matches().shouldBeFalse()
                        }
                    }

                    describe("route with path variable") {
                        it("should match the intended route with substitutions") {
                            Pattern.compile("${beginning}v1/widgets/$pathVar$terminal").matcher("https://www.something.com/v1/widgets/25").matches().shouldBeTrue()
                        }

                        it("should match the intended route with query parameters") {
                            Pattern.compile("${beginning}v1/widgets/$pathVar$terminal").matcher("https://www.something.com/v1/widgets/25?query=something").matches().shouldBeTrue()
                        }

                        it("should match the intended route ending with a /") {
                            Pattern.compile("${beginning}v1/widgets/$pathVar$terminal").matcher("https://www.something.com/v1/widgets/25/").matches().shouldBeTrue()
                        }

                        it("should not match a longer route") {
                            Pattern.compile("${beginning}v1/widgets/$pathVar$terminal").matcher("https://www.something.com/v1/widgets/25/cogs").matches().shouldBeFalse()
                            Pattern.compile("${beginning}v1/widgets/$pathVar$terminal").matcher("https://www.something.com/v1/widgets/25/cogs/2").matches().shouldBeFalse()
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

    data class FakeUnknownError(val message: String)
    data class FakeAuthError(val message: String)

    companion object {
        const val beginning = ".*?"
        const val terminal = "((\\?.*)|/)?"
        const val pathVar = "[^/]+"
    }
}