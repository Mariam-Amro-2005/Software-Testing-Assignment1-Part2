package Tests;

import Services.DigitScrubber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DigitScrubber Tests")
class DigitScrubberTest {

    private DigitScrubber scrubber;

    @BeforeEach
    void setUp() {
        scrubber = new DigitScrubber();
    }

    // ==================== NEGATIVE TESTS (Exceptions) ====================

    @Test
    @DisplayName("Null input throws NullPointerException")
    void testScrubNullInput() {
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> scrubber.scrub(null));
        assertEquals("Input cannot be null", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t", "\n"})
    @DisplayName("Blank input throws IllegalArgumentException")
    void testScrubBlankInput(String blankInput) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> scrubber.scrub(blankInput));
        assertEquals("Input cannot be blank", exception.getMessage());
    }

    // ==================== POSITIVE TESTS (Happy Path) ====================

    @ParameterizedTest
    @CsvSource({
            "abc, abc",
            "123, XXX",
            "abc123def, abcXXXdef",
            "123abc, XXXabc",
            "abc123, abcXXX",
            "a1b2c3, aXbXcX",
            "Hello World 456, Hello World XXX"
    })
    @DisplayName("Digits without price are scrubbed correctly")
    void testScrubDigitsWithoutPrice(String input, String expected) {
        assertEquals(expected, scrubber.scrub(input));
    }

    // ==================== BOUNDARY VALUE TESTS ====================

    @Test
    @DisplayName("Single digit becomes X")
    void testScrubSingleDigit() {
        assertEquals("X", scrubber.scrub("5"));
    }

    @Test
    @DisplayName("Digit at very start of string")
    void testScrubDigitAtVeryStart() {
        assertEquals("Xabc", scrubber.scrub("5abc"));
    }

    @Test
    @DisplayName("Digit at very end of string")
    void testScrubDigitAtVeryEnd() {
        assertEquals("abcX", scrubber.scrub("abc5"));
    }

    @Test
    @DisplayName("Only digits and spaces")
    void testScrubOnlyDigitAndLetter() {
        assertEquals("X X", scrubber.scrub("5 5"));
    }

    // ==================== PRICE PRESERVATION TESTS (Spec Requirement) ====================
    // These tests reveal the defect: digits followed by '$' are not protected.

    @Test
    @DisplayName("Price with single digit should not be scrubbed (defect)")
    void testScrubPriceWithSingleDigit() {
        assertEquals("9$", scrubber.scrub("9$"));
    }

    @Test
    @DisplayName("Price with multiple digits should not be scrubbed (defect)")
    void testScrubPriceWithMultipleDigits() {
        assertEquals("99$", scrubber.scrub("99$"));
    }

    @Test
    @DisplayName("Mixed digits and price: only non-price digits scrubbed (defect)")
    void testScrubMixedDigitsAndPrice() {
        assertEquals("Order XXX for 99$", scrubber.scrub("Order 123 for 99$"));
    }

    @Test
    @DisplayName("Price at start of string (defect)")
    void testScrubPriceAtStartOfString() {
        assertEquals("99$ is the price", scrubber.scrub("99$ is the price"));
    }

    @Test
    @DisplayName("Price at end of string (defect)")
    void testScrubPriceAtEndOfString() {
        assertEquals("Total is 99$", scrubber.scrub("Total is 99$"));
    }

    @Test
    @DisplayName("Multiple prices (defect)")
    void testScrubMultiplePrices() {
        assertEquals("10$ and 20$", scrubber.scrub("10$ and 20$"));
    }

    @Test
    @DisplayName("Dollar sign not immediately after digit → digits scrubbed")
    void testScrubDollarSignNotImmediatelyAfterDigit() {
        assertEquals("XX $", scrubber.scrub("99 $"));
    }

    @Test
    @DisplayName("Dollar before digits → digits scrubbed")
    void testScrubDollarBeforeDigits() {
        assertEquals("$XX", scrubber.scrub("$99"));
    }
}