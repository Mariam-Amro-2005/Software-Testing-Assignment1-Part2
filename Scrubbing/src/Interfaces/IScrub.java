package Interfaces;

import Models.ScrubMode;


public interface IScrub {
    // Method to scrub input string based on the specified ScrubMode
    // ScrubMode determines whether to scrub digits, emails, or both
    String scrub(String input, ScrubMode mode) throws IllegalArgumentException, NullPointerException;
}