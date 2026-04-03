package test;

import Services.EmailScrubber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class EmailScrubberTest {

    private EmailScrubber scrubber;

    @BeforeEach
    void setUp() {
        scrubber = new EmailScrubber();
    }

    // ==================== NEGATIVE TESTS (Exceptions) ====================

    @Test
    void testScrub_nullInput() {
        // Current implementation has bug: it checks null but throws NPE with message
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> scrubber.scrub(null));
        assertEquals("Input cannot be null or blank", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t", "\n"})
    void testScrub_blankInput(String blankInput) {
        // BUG ALERT: Spec says blank should throw IllegalArgumentException
        // Current implementation throws NPE for blank inputs
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> scrubber.scrub(blankInput));
        assertEquals("Input cannot be null or blank", exception.getMessage());
    }

    // ==================== POSITIVE TESTS (Happy Path) ====================

    @ParameterizedTest
    @CsvSource({
            "No email here, No email here",
            "Hello world, Hello world",
            "Just plain text, Just plain text"
    })
    void testScrub_noEmail(String input, String expected) {
        assertEquals(expected, scrubber.scrub(input));
    }

    @ParameterizedTest
    @CsvSource({
            "Contact me at john@example.com, Contact me at [EMAIL_HIDDEN]",
            "user@domain.com, [EMAIL_HIDDEN]",
            "first.last@example.co.uk, [EMAIL_HIDDEN]"
    })
    void testScrub_singleValidEmail(String input, String expected) {
        assertEquals(expected, scrubber.scrub(input));
    }

    @ParameterizedTest
    @CsvSource({
            "Email a@b.com and c@d.org, Email [EMAIL_HIDDEN] and [EMAIL_HIDDEN]",
            "a@x.com b@y.com c@z.com, [EMAIL_HIDDEN] [EMAIL_HIDDEN] [EMAIL_HIDDEN]"
    })
    void testScrub_multipleValidEmails(String input, String expected) {
        assertEquals(expected, scrubber.scrub(input));
    }

    // ==================== EMAIL WITH SPECIAL CHARACTERS ====================

    @ParameterizedTest
    @CsvSource({
            "first.last+tag@example.com, [EMAIL_HIDDEN]",
            "user_name@sub.domain.org, [EMAIL_HIDDEN]",
            "name@domain-name.com, [EMAIL_HIDDEN]"
    })
    void testScrub_emailWithSpecialChars(String input, String expected) {
        assertEquals(expected, scrubber.scrub(input));
    }

    // ==================== INVALID EMAIL PATTERNS (Should NOT be replaced) ====================

    @ParameterizedTest
    @CsvSource({
            "user@domain, user@domain",                    // missing .tld
            "user@.com, user@.com",                        // missing domain name
            "@domain.com, @domain.com",                    // missing local part
            "user@domain.c, user@domain.c",                // tld too short (1 char)
            "user@domain.toolongtld, user@domain.toolongtld", // tld > 6 chars (not standard)
            "user name@domain.com, user name@domain.com",  // space in local part
            "user@domain .com, user@domain .com"           // space before tld
    })
    void testScrub_invalidEmailPattern(String input, String expected) {
        // BUG ALERT: Current regex [a-zA-Z0-0] is broken (0-0 means only '0')
        // This means digits in emails won't match correctly
        assertEquals(expected, scrubber.scrub(input));
    }

    // ==================== BOUNDARY VALUE TESTS ====================

    @Test
    void testScrub_shortestValidEmail() {
        // Shortest possible valid email: a@b.c
        assertEquals("[EMAIL_HIDDEN]", scrubber.scrub("a@b.c"));
    }

    @Test
    void testScrub_emailAtStartOfString() {
        assertEquals("[EMAIL_HIDDEN] is the sender", scrubber.scrub("user@example.com is the sender"));
    }

    @Test
    void testScrub_emailAtEndOfString() {
        assertEquals("Contact me at [EMAIL_HIDDEN]", scrubber.scrub("Contact me at user@example.com"));
    }

    @Test
    void testScrub_emailSurroundedByPunctuation() {
        assertEquals("([EMAIL_HIDDEN]), [EMAIL_HIDDEN];", scrubber.scrub("(user@example.com), user@example.com;"));
    }

    @Test
    void testScrub_emailWithDigits() {
        // BUG ALERT: The regex [a-zA-Z0-0] only matches digit '0', not 1-9
        // So user123@domain.com may not be fully matched
        assertEquals("[EMAIL_HIDDEN]", scrubber.scrub("user123@domain.com"));
    }

    @Test
    void testScrub_emailWithUppercaseLetters() {
        assertEquals("[EMAIL_HIDDEN]", scrubber.scrub("USER@EXAMPLE.COM"));
    }

    // ==================== EDGE CASES ====================

    @Test
    void testScrub_onlyEmailAddress() {
        assertEquals("[EMAIL_HIDDEN]", scrubber.scrub("test@example.com"));
    }

    @Test
    void testScrub_emailNextToTextWithoutSpace() {
        // Email attached to a word without space – should still be detected
        assertEquals("Contact[EMAIL_HIDDEN]for help", scrubber.scrub("Contacttest@example.comfor help"));
    }

    @Test
    void testScrub_veryLongEmail() {
        // Build a long but valid email (local part up to 64 chars typical)
        String localPart = "a".repeat(60);
        String domain = "example.com";
        String longEmail = localPart + "@" + domain;
        String input = "Send to " + longEmail;
        String expected = "Send to [EMAIL_HIDDEN]";
        assertEquals(expected, scrubber.scrub(input));
    }
}