package test;

import Services.EmailScrubber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EmailScrubber Tests")
class EmailScrubberTest {

    private EmailScrubber scrubber;

    @BeforeEach
    void setUp() {
        scrubber = new EmailScrubber();
    }

    // ==================== NEGATIVE TESTS (Exceptions) ====================

    @Test
    @DisplayName("Null input throws NullPointerException")
    void testScrubNullInput() {
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> scrubber.scrub(null));
        assertEquals("Input cannot be null or blank", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t", "\n"})
    @DisplayName("Blank input currently throws NPE but should throw IAE (defect)")
    void testScrubBlankInput(String blankInput) {
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
    @DisplayName("Text without email remains unchanged")
    void testScrubNoEmail(String input, String expected) {
        assertEquals(expected, scrubber.scrub(input));
    }

    @ParameterizedTest
    @CsvSource({
            "Contact me at john@example.com, Contact me at [EMAIL_HIDDEN]",
            "user@domain.com, [EMAIL_HIDDEN]",
            "first.last@example.co.uk, [EMAIL_HIDDEN]"
    })
    @DisplayName("Single valid email is replaced")
    void testScrubSingleValidEmail(String input, String expected) {
        assertEquals(expected, scrubber.scrub(input));
    }

    @ParameterizedTest
    @CsvSource({
            "Email a@b.com and c@d.org, Email [EMAIL_HIDDEN] and [EMAIL_HIDDEN]",
            "a@x.com b@y.com c@z.com, [EMAIL_HIDDEN] [EMAIL_HIDDEN] [EMAIL_HIDDEN]"
    })
    @DisplayName("Multiple valid emails are all replaced")
    void testScrubMultipleValidEmails(String input, String expected) {
        assertEquals(expected, scrubber.scrub(input));
    }

    // ==================== EMAIL WITH SPECIAL CHARACTERS ====================

    @ParameterizedTest
    @CsvSource({
            "first.last+tag@example.com, [EMAIL_HIDDEN]",
            "user_name@sub.domain.org, [EMAIL_HIDDEN]",
            "name@domain-name.com, [EMAIL_HIDDEN]"
    })
    @DisplayName("Email with special characters (dots, plus, hyphen) is replaced")
    void testScrubEmailWithSpecialChars(String input, String expected) {
        assertEquals(expected, scrubber.scrub(input));
    }

    // ==================== INVALID EMAIL PATTERNS ====================

    @ParameterizedTest
    @CsvSource({
            "user@domain, user@domain",
            "user@.com, user@.com",
            "@domain.com, @domain.com",
            "user@domain.c, user@domain.c",
            "user@domain.toolongtld, user@domain.toolongtld",
            "user name@domain.com, user name@domain.com",
            "user@domain .com, user@domain .com"
    })
    @DisplayName("Invalid email patterns are not replaced")
    void testScrubInvalidEmailPattern(String input, String expected) {
        assertEquals(expected, scrubber.scrub(input));
    }

    // ==================== BOUNDARY VALUE TESTS ====================

    @Test
    @DisplayName("Shortest valid email (a@b.c) is replaced")
    void testScrubShortestValidEmail() {
        assertEquals("[EMAIL_HIDDEN]", scrubber.scrub("a@b.c"));
    }

    @Test
    @DisplayName("Email at start of string")
    void testScrubEmailAtStartOfString() {
        assertEquals("[EMAIL_HIDDEN] is the sender", scrubber.scrub("user@example.com is the sender"));
    }

    @Test
    @DisplayName("Email at end of string")
    void testScrubEmailAtEndOfString() {
        assertEquals("Contact me at [EMAIL_HIDDEN]", scrubber.scrub("Contact me at user@example.com"));
    }

    @Test
    @DisplayName("Email surrounded by punctuation")
    void testScrubEmailSurroundedByPunctuation() {
        assertEquals("([EMAIL_HIDDEN]), [EMAIL_HIDDEN];", scrubber.scrub("(user@example.com), user@example.com;"));
    }

    @Test
    @DisplayName("Email with digits (defect: regex only matches digit 0)")
    void testScrubEmailWithDigits() {
        assertEquals("[EMAIL_HIDDEN]", scrubber.scrub("user123@domain.com"));
    }

    @Test
    @DisplayName("Email with uppercase letters")
    void testScrubEmailWithUppercaseLetters() {
        assertEquals("[EMAIL_HIDDEN]", scrubber.scrub("USER@EXAMPLE.COM"));
    }

    // ==================== EDGE CASES ====================

    @Test
    @DisplayName("Only email address, no other text")
    void testScrubOnlyEmailAddress() {
        assertEquals("[EMAIL_HIDDEN]", scrubber.scrub("test@example.com"));
    }

    @Test
    @DisplayName("Email attached to text without space")
    void testScrubEmailNextToTextWithoutSpace() {
        assertEquals("Contact[EMAIL_HIDDEN]for help", scrubber.scrub("Contacttest@example.comfor help"));
    }

    @Test
    @DisplayName("Very long valid email (60 char local part)")
    void testScrubVeryLongEmail() {
        String localPart = "a".repeat(60);
        String domain = "example.com";
        String longEmail = localPart + "@" + domain;
        String input = "Send to " + longEmail;
        String expected = "Send to [EMAIL_HIDDEN]";
        assertEquals(expected, scrubber.scrub(input));
    }
}