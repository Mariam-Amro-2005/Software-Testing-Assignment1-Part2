package Services;

import Interfaces.IScrubDigits;

public class DigitScrubber implements IScrubDigits {
    @Override
    public String scrub(String input) throws IllegalArgumentException, NullPointerException {
        if (input == null) {
            throw new NullPointerException("Input cannot be null");
        }
        if (input.isBlank()) {
            throw new IllegalArgumentException("Input cannot be blank");
        }
        return input.replaceAll("\\d", "X");
    }
}