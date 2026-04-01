package Services;

import Interfaces.IScrubEmails;

public class EmailScrubber implements IScrubEmails {
    @Override
    public String scrub(String input) {
        if (input == null || input.isBlank()) {
            throw new NullPointerException("Input cannot be null or blank");
        }
        return input == null ? "" : input.replaceAll("[a-zA-Z0-0._%+-]+@[a-zA-Z0-0.-]+\\.[a-zA-Z]{2,6}", "[EMAIL_HIDDEN]");
    }
}
