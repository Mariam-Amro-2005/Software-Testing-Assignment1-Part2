package com.assignment1.dummy;

public class DummyProcessor {

    private final DummyService dummyService;

    // Constructor injection (similar to your IScrub implementation)
    public DummyProcessor(DummyService dummyService) {
        this.dummyService = dummyService;
    }

    public String processInput(String input) {
        if (input == null) {
            throw new NullPointerException("Input cannot be null");
        }
        if (input.trim().isEmpty()) {
            throw new IllegalArgumentException("Input cannot be blank");
        }
        return dummyService.process(input);
    }

    public int getServiceCount() {
        return dummyService.getCount();
    }
}