package test;
import Interfaces.*;
import Models.ScrubMode;
import Services.MainScrubber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;



@ExtendWith(MockitoExtension.class)
class MainScrubberTest {
    @Mock
    private IScrubDigits mockDigits;
    @Mock
    private IScrubEmails mockEmails;
    private MainScrubber mainScrubber;
    @BeforeEach
    void setup() {
        mainScrubber = new MainScrubber(mockDigits, mockEmails);
    }
    @Test
    @DisplayName("T1 base choise")
    void testFullScrubbing() {
        String input = "Email me at a@b.com and call 123";
        String afterDigits = "email me at exambil@email.com and my number is XXX";
        String afterEmail = "email me at [EMAIL_HIDDEN] and my number is XXX";
        String finalResult = "email me at [EMAIL_HIDDEN] and my number is XXX";



      
        when(mockDigits.scrub(input)).thenReturn(afterDigits);
        when(mockEmails.scrub(afterDigits)).thenReturn(afterEmail);
        String result = mainScrubber.scrub(input, ScrubMode.FULL_SCRUBBING);
      
        assertEquals(finalResult, result);
        verify(mockDigits, times(1)).scrub(input);
        verify(mockEmails, times(1)).scrub(afterDigits);
    }

    @Test
    @DisplayName("T2")
    void T2() {
        String input = "Email me at a@b.com and call 123";
        String afterDigits = "email me at exambil@email.com and my number is XXX";
        String finalResult = "email me at exambil@email.com and my number is XXX";


        when(mockDigits.scrub(input)).thenReturn(afterDigits);
        String result = mainScrubber.scrub(input, ScrubMode.ONLY_DIGITS);
        assertEquals(finalResult, result);
        verify(mockDigits, times(1)).scrub(input);
        verify(mockEmails, never()).scrub(any());
    }

    @Test
    @DisplayName("T3")
    void T3() {
        String input = "Email me at a@b.com and call 123";
        String afterEmail = "email me at [EMAIL_HIDDEN] and my number is XXX";
        String finalResult = "email me at [EMAIL_HIDDEN] and my number is XXX";


        when(mockEmails.scrub(input)).thenReturn(afterEmail);
        String result = mainScrubber.scrub(input, ScrubMode.ONLY_EMAILS);
        assertEquals(finalResult, result);
        verify(mockEmails, times(1)).scrub(input);
        verify(mockDigits, never()).scrub(any());

    }

    @Test
    @DisplayName("T4")
    void T4() {
        String input = "";
        String afterDigits = "";
        String afterEmail = "";
        String finalResult = "";


        when(mockDigits.scrub(input)).thenReturn(afterDigits);
        when(mockEmails.scrub(afterDigits)).thenReturn(afterEmail);
        String result = mainScrubber.scrub(input, ScrubMode.FULL_SCRUBBING);
        assertEquals(finalResult, result);
        verify(mockDigits, times(1)).scrub(input);
        verify(mockEmails, times(1)).scrub(afterDigits);

    }

    @Test
    @DisplayName("T5")
    void T5() {
        String input = null;
        String afterDigits = "";
        String afterEmail = "";
        String finalResult = "";


        when(mockDigits.scrub(input)).thenReturn(afterDigits);
        when(mockEmails.scrub(afterDigits)).thenReturn(afterEmail);
        String result = mainScrubber.scrub(input, ScrubMode.FULL_SCRUBBING);
        assertEquals(finalResult, result);
        verify(mockDigits, times(1)).scrub(input);
        verify(mockEmails, times(1)).scrub(afterDigits);

    }
    @Test
    @DisplayName("T6")
    void T6() {
        String input = " ";
        String afterDigits = " ";
        String afterEmail = " ";
        String finalResult = " ";


        when(mockDigits.scrub(input)).thenReturn(afterDigits);
        when(mockEmails.scrub(afterDigits)).thenReturn(afterEmail);
        String result = mainScrubber.scrub(input, ScrubMode.FULL_SCRUBBING);
        assertEquals(finalResult, result);
        verify(mockDigits, times(1)).scrub(input);
        verify(mockEmails, times(1)).scrub(afterDigits);

    }

    @Test
    @DisplayName("T4")
    void T7() {
        String input = "12345";
        String afterDigits = "XXXXX";
        String afterEmail = "XXXXX";
        String finalResult = "XXXXX";


        when(mockDigits.scrub(input)).thenReturn(afterDigits);
        when(mockEmails.scrub(afterDigits)).thenReturn(afterEmail);
        String result = mainScrubber.scrub(input, ScrubMode.FULL_SCRUBBING);
        assertEquals(finalResult, result);
        verify(mockDigits, times(1)).scrub(input);
        verify(mockEmails, times(1)).scrub(afterDigits);

    }
    @Test
    @DisplayName("T4")
    void T8() {
        String input = "exampil.gmail.com";
        String afterDigits = "exampil.gmail.com";
        String afterEmail = "[EMAIL_HIDDEN]";
        String finalResult = "[EMAIL_HIDDEN]";

        when(mockDigits.scrub(input)).thenReturn(afterDigits);
        when(mockEmails.scrub(afterDigits)).thenReturn(afterEmail);
        String result = mainScrubber.scrub(input, ScrubMode.FULL_SCRUBBING);
        assertEquals(finalResult, result);
        verify(mockDigits, times(1)).scrub(input);
        verify(mockEmails, times(1)).scrub(afterDigits);
    }
}
