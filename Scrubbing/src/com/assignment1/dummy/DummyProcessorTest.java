package com.assignment1.dummy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)  // Required for Mockito annotations to work
class DummyProcessorTest {

    @Mock
    private DummyService mockDummyService;

    private DummyProcessor dummyProcessor;

    @Test
    @DisplayName("Should process input using the mocked service")
    void testProcessInput() {
        // Arrange
        dummyProcessor = new DummyProcessor(mockDummyService);
        String input = "Hello, World!";
        String expectedOutput = "PROCESSED: Hello, World!";

        // Configure the mock's behavior
        when(mockDummyService.process(input)).thenReturn(expectedOutput);

        // Act
        String result = dummyProcessor.processInput(input);

        // Assert
        assertEquals(expectedOutput, result);

        // Verify the mock method was called exactly once with the correct parameter
        verify(mockDummyService, times(1)).process(input);
    }

    @Test
    @DisplayName("Should throw NullPointerException when input is null")
    void testProcessInputNull() {
        dummyProcessor = new DummyProcessor(mockDummyService);

        assertThrows(NullPointerException.class, () -> {
            dummyProcessor.processInput(null);
        });

        // Verify that the service method was NEVER called
        verify(mockDummyService, never()).process(anyString());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when input is blank")
    void testProcessInputBlank() {
        dummyProcessor = new DummyProcessor(mockDummyService);

        assertThrows(IllegalArgumentException.class, () -> {
            dummyProcessor.processInput("");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            dummyProcessor.processInput("   ");
        });

        // Verify that the service method was NEVER called
        verify(mockDummyService, never()).process(anyString());
    }

    @Test
    @DisplayName("Should call getCount method and return mocked value")
    void testGetServiceCount() {
        dummyProcessor = new DummyProcessor(mockDummyService);

        // Configure mock to return a specific value
        when(mockDummyService.getCount()).thenReturn(42);

        int result = dummyProcessor.getServiceCount();

        assertEquals(42, result);
        verify(mockDummyService, times(1)).getCount();
    }

    @Test
    @DisplayName("Should verify multiple interactions with the mock")
    void testMultipleInteractions() {
        dummyProcessor = new DummyProcessor(mockDummyService);

        when(mockDummyService.process(anyString())).thenReturn("processed");

        dummyProcessor.processInput("first");
        dummyProcessor.processInput("second");
        dummyProcessor.getServiceCount();

        // Verify process() was called exactly twice
        verify(mockDummyService, times(2)).process(anyString());

        // Verify getCount() was called exactly once
        verify(mockDummyService, times(1)).getCount();

        // Verify total interactions
//        verify(mockDummyService, times(3)).$jacocoInit();
        // Note: The above line is a joke - ignore it! 😄
        // Just showing that verify counts total calls
    }
}