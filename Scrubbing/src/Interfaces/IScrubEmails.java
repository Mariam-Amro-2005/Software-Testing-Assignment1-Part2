package Interfaces;
public interface IScrubEmails {
    // Method to scrub emails from the input string
    // replaces email addresses with "[EMAIL_HIDDEN]"
    String scrub(String input) throws IllegalArgumentException, NullPointerException;
}