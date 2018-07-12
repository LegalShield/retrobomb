package com.legalshield.retrobomb;

import com.greghaskins.spectrum.Spectrum;
import com.legalshield.retrobomb.interop.RetrobombHandler;
import com.legalshield.retrobomb.utility.MutableLocalReference;
import org.junit.runner.RunWith;

import static com.greghaskins.spectrum.Spectrum.describe;
import static com.greghaskins.spectrum.Spectrum.it;
import static org.junit.Assert.*;

@SuppressWarnings("ThrowableNotThrown")
@RunWith(Spectrum.class)
public class RetrobombHandlerSpec {
    {
        describe("RetrobombHandler Spec", () -> {
            describe("#handleErrorData", () -> {
                describe("when the throwable is not a RetrobombException", () -> {
                    final Throwable throwable = new RuntimeException();

                    it("should not invoke the handler", () ->
                        RetrobombHandler.handleErrorData(throwable, String.class, data -> fail())
                    );

                    it("should return false", () ->
                        assertFalse(RetrobombHandler.handleErrorData(throwable, String.class, data -> {}))
                    );
                });

                describe("when the throwable is a RetrobombException", () -> {
                    describe("when the data type matches", () -> {
                        final FakeData fakeData = new FakeData();
                        final Throwable throwable = new RetrobombException(fakeData);

                        it("should invoke the handler with the correct data", () -> {
                            MutableLocalReference<FakeData> handledData = new MutableLocalReference<>();

                            RetrobombHandler.handleErrorData(throwable, FakeData.class, handledData::set);

                            assertEquals(handledData.get(), fakeData);
                        });

                        it("should return true", () ->
                            assertTrue(RetrobombHandler.handleErrorData(throwable, FakeData.class, data -> {}))
                        );
                    });

                    describe("when the data type does not match", () -> {
                        final String fakeData = "";
                        final Throwable throwable = new RetrobombException(fakeData);

                        it("should not invoke the handler", () ->
                            RetrobombHandler.handleErrorData(throwable, FakeData.class, data -> fail())
                        );

                        it("should return false", () ->
                            assertFalse(RetrobombHandler.handleErrorData(throwable, FakeData.class, data -> {}))
                        );
                    });
                });
            });
        });
    }

    private class FakeData { }
}
