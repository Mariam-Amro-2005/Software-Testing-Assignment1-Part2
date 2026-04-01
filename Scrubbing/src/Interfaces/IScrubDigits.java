package Interfaces;
public interface IScrubDigits {
    // Method to scrub digits from the input string
    // replaces digits with "X"
    String scrub(String input)  throws IllegalArgumentException, NullPointerException;
}