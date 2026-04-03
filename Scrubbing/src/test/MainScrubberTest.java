package test;

import Interfaces.*;
import Models.ScrubMode;
import Services.MainScrubber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MainScrubberTest {

    @Mock
    private IScrubDigits mockDigits;

    @Mock
    private IScrubEmails mockEmails;

    private MainScrubber mainScrubber;

    @BeforeEach
    void setUp() {
        mainScrubber = new MainScrubber(mockDigits, mockEmails);
    }

    // ==================== CONSTRUCTOR NEGATIVE TESTS ====================
    // These tests reveal defects: the constructor does NOT throw NPE for null dependencies.

    @Test
    @DisplayName("Constructor with null digits scrubber should throw NPE")
    void constructorWithNullDigits() {
        assertThrows(NullPointerException.class,
                () -> new MainScrubber(null, mockEmails));
    }

    @Test
    @DisplayName("Constructor with null emails scrubber should throw NPE")
    void constructorWithNullEmails() {
        assertThrows(NullPointerException.class,
                () -> new MainScrubber(mockDigits, null));
    }

    @Test
    @DisplayName("Constructor with both dependencies null should throw NPE")
    void constructorWithBothNull() {
        assertThrows(NullPointerException.class,
                () -> new MainScrubber(null, null));
    }

    // ==================== BASE CHOICE (HAPPY PATH) ====================
    // Order: digits first, then emails (as per actual implementation)

    @Test
    @DisplayName("FULL_SCRUBBING with digits and emails calls digits then emails")
    void fullScrubbingWithBothCallsDigitsThenEmails() {
        String input = "Email me at a@b.com and call 123";
        String afterDigits = "Email me at a@b.com and call XXX";
        String afterBoth = "Email me at [EMAIL_HIDDEN] and call XXX";

        when(mockDigits.scrub(input)).thenReturn(afterDigits);
        when(mockEmails.scrub(afterDigits)).thenReturn(afterBoth);

        String result = mainScrubber.scrub(input, ScrubMode.FULL_SCRUBBING);

        assertEquals(afterBoth, result);
        verify(mockDigits, times(1)).scrub(input);
        verify(mockEmails, times(1)).scrub(afterDigits);
    }

    // ==================== MODE TESTS ====================

    @Test
    @DisplayName("ONLY_DIGITS mode calls only digits scrubber")
    void onlyDigitsCallsOnlyDigitsScrubber() {
        String input = "Email me at a@b.com and call 123";
        String afterDigits = "Email me at a@b.com and call XXX";

        when(mockDigits.scrub(input)).thenReturn(afterDigits);

        String result = mainScrubber.scrub(input, ScrubMode.ONLY_DIGITS);

        assertEquals(afterDigits, result);
        verify(mockDigits, times(1)).scrub(input);
        verify(mockEmails, never()).scrub(anyString());
    }

    @Test
    @DisplayName("ONLY_EMAILS mode calls only emails scrubber")
    void onlyEmailsCallsOnlyEmailsScrubber() {
        String input = "Email me at a@b.com and call 123";
        String afterEmails = "Email me at [EMAIL_HIDDEN] and call 123";

        when(mockEmails.scrub(input)).thenReturn(afterEmails);

        String result = mainScrubber.scrub(input, ScrubMode.ONLY_EMAILS);

        assertEquals(afterEmails, result);
        verify(mockEmails, times(1)).scrub(input);
        verify(mockDigits, never()).scrub(anyString());
    }

    // ==================== EDGE CASES - NO DIGITS OR NO EMAILS ====================

    @Test
    @DisplayName("FULL_SCRUBBING with no digits still calls both scrubbers")
    void fullScrubbingWithNoDigitsCallsBoth() {
        String input = "Email me at a@b.com please";
        when(mockDigits.scrub(input)).thenReturn(input);
        when(mockEmails.scrub(input)).thenReturn("Email me at [EMAIL_HIDDEN] please");

        String result = mainScrubber.scrub(input, ScrubMode.FULL_SCRUBBING);

        assertEquals("Email me at [EMAIL_HIDDEN] please", result);
        verify(mockDigits, times(1)).scrub(input);
        verify(mockEmails, times(1)).scrub(input);
    }

    @Test
    @DisplayName("FULL_SCRUBBING with no emails still calls both scrubbers")
    void fullScrubbingWithNoEmailsCallsBoth() {
        String input = "Call me at 123 please";
        String afterDigits = "Call me at XXX please";
        when(mockDigits.scrub(input)).thenReturn(afterDigits);
        when(mockEmails.scrub(afterDigits)).thenReturn(afterDigits);

        String result = mainScrubber.scrub(input, ScrubMode.FULL_SCRUBBING);

        assertEquals(afterDigits, result);
        verify(mockDigits, times(1)).scrub(input);
        verify(mockEmails, times(1)).scrub(afterDigits);
    }

    // ==================== NULL & BLANK INPUT TESTS (Defects) ====================
    // These tests expect exceptions, but the current implementation does NOT throw.
    // They will fail – document as defects.

    @Test
    @DisplayName("Null input should throw NullPointerException for any mode")
    void nullInputThrowsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> mainScrubber.scrub(null, ScrubMode.FULL_SCRUBBING));
        assertThrows(NullPointerException.class,
                () -> mainScrubber.scrub(null, ScrubMode.ONLY_DIGITS));
        assertThrows(NullPointerException.class,
                () -> mainScrubber.scrub(null, ScrubMode.ONLY_EMAILS));

        verifyNoInteractions(mockDigits, mockEmails);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t", "\n"})
    @DisplayName("Blank input should throw IllegalArgumentException for any mode")
    void blankInputThrowsIllegalArgumentException(String blankInput) {
        assertThrows(IllegalArgumentException.class,
                () -> mainScrubber.scrub(blankInput, ScrubMode.FULL_SCRUBBING));
        assertThrows(IllegalArgumentException.class,
                () -> mainScrubber.scrub(blankInput, ScrubMode.ONLY_DIGITS));
        assertThrows(IllegalArgumentException.class,
                () -> mainScrubber.scrub(blankInput, ScrubMode.ONLY_EMAILS));

        verifyNoInteractions(mockDigits, mockEmails);
    }

    // ==================== ADDITIONAL VERIFICATION TESTS ====================

    @Test
    @DisplayName("ONLY_DIGITS still calls digits scrubber even when no digits present")
    void onlyDigitsCallsScrubberEvenWithNoDigits() {
        String input = "user@example.com";
        when(mockDigits.scrub(input)).thenReturn(input);

        mainScrubber.scrub(input, ScrubMode.ONLY_DIGITS);

        verify(mockDigits, times(1)).scrub(input);
        verify(mockEmails, never()).scrub(anyString());
    }

    @Test
    @DisplayName("ONLY_EMAILS still calls emails scrubber even when no emails present")
    void onlyEmailsCallsScrubberEvenWithNoEmails() {
        String input = "12345";
        when(mockEmails.scrub(input)).thenReturn(input);

        mainScrubber.scrub(input, ScrubMode.ONLY_EMAILS);

        verify(mockEmails, times(1)).scrub(input);
        verify(mockDigits, never()).scrub(anyString());
    }

    @Test
    @DisplayName("FULL_SCRUBBING respects order: digits first then emails")
    void fullScrubbingRespectsDigitsFirstOrder() {
        String input = "test@x.com 123";
        String afterDigits = "test@x.com XXX";
        String afterBoth = "[EMAIL_HIDDEN] XXX";

        when(mockDigits.scrub(input)).thenReturn(afterDigits);
        when(mockEmails.scrub(afterDigits)).thenReturn(afterBoth);

        String result = mainScrubber.scrub(input, ScrubMode.FULL_SCRUBBING);

        assertEquals(afterBoth, result);
        verify(mockDigits, times(1)).scrub(input);
        verify(mockEmails, times(1)).scrub(afterDigits);
    }
}