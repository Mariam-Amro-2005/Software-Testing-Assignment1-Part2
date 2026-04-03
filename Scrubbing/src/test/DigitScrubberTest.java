package test;

import Services.DigitScrubber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class DigitScrubberTest {

    private DigitScrubber scrubber;

    @BeforeEach
    void setUp() {
        scrubber = new DigitScrubber();
    }

    // ==================== NEGATIVE TESTS (Exceptions) ====================

    @Test
    void testScrub_nullInput() {
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> scrubber.scrub(null));
        assertEquals("Input cannot be null", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t", "\n"})
    void testScrub_blankInput(String blankInput) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> scrubber.scrub(blankInput));
        assertEquals("Input cannot be blank", exception.getMessage());
    }

    // ==================== POSITIVE TESTS (Happy Path) ====================

    @ParameterizedTest
    @CsvSource({
            "abc, abc",               // no digits → unchanged
            "123, XXX",               // only digits
            "abc123def, abcXXXdef",   // digits in middle
            "123abc, XXXabc",         // digits at start
            "abc123, abcXXX",         // digits at end
            "a1b2c3, aXbXcX",         // interleaved digits
            "Hello World 456, Hello World XXX"  // digits with spaces
    })
    void testScrub_digitsWithoutPrice(String input, String expected) {
        assertEquals(expected, scrubber.scrub(input));
    }

    // ==================== BOUNDARY VALUE TESTS ====================

    @Test
    void testScrub_singleDigit() {
        assertEquals("X", scrubber.scrub("5"));
    }

    @Test
    void testScrub_digitAtVeryStart() {
        assertEquals("Xabc", scrubber.scrub("5abc"));
    }

    @Test
    void testScrub_digitAtVeryEnd() {
        assertEquals("abcX", scrubber.scrub("abc5"));
    }

    @Test
    void testScrub_onlyDigitAndLetter() {
        assertEquals("X X", scrubber.scrub("5 5"));
    }

    // ==================== PRICE PRESERVATION TESTS (Spec Requirement) ====================
    // According to the spec: digits followed by '$' should NOT be scrubbed.
    // The current implementation does NOT satisfy this. These tests will FAIL.
    // They are included to reveal the defect.

    @Test
    void testScrub_priceWithSingleDigit() {
        // Expected: "$" after digit protects the digit
        assertEquals("9$", scrubber.scrub("9$"));
        // Current buggy implementation returns "X$" → test will fail
    }

    @Test
    void testScrub_priceWithMultipleDigits() {
        assertEquals("99$", scrubber.scrub("99$"));
    }

    @Test
    void testScrub_mixedDigitsAndPrice() {
        // "Order 123 for 99$" → digits "123" become "XXX", "99" in price remain "99"
        assertEquals("Order XXX for 99$", scrubber.scrub("Order 123 for 99$"));
    }

    @Test
    void testScrub_priceAtStartOfString() {
        assertEquals("99$ is the price", scrubber.scrub("99$ is the price"));
    }

    @Test
    void testScrub_priceAtEndOfString() {
        assertEquals("Total is 99$", scrubber.scrub("Total is 99$"));
    }

    @Test
    void testScrub_multiplePrices() {
        assertEquals("10$ and 20$", scrubber.scrub("10$ and 20$"));
    }

    @Test
    void testScrub_dollarSignNotImmediatelyAfterDigit() {
        // "99 $" has space before $ → not a price, so digits should be scrubbed
        assertEquals("XX $", scrubber.scrub("99 $"));
    }

    @Test
    void testScrub_dollarBeforeDigits() {
        // "$99" → dollar before digits, not a price per spec → scrub digits
        assertEquals("$XX", scrubber.scrub("$99"));
    }
}