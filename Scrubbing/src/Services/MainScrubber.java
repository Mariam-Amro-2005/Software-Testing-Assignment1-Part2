package Services;

import Interfaces.*;
import Models.ScrubMode;

public class MainScrubber implements IScrub {
    private final IScrubDigits digitScrubber;
    private final IScrubEmails emailScrubber;

    public MainScrubber(IScrubDigits digitScrubber, IScrubEmails emailScrubber) {
        this.digitScrubber = digitScrubber;
        this.emailScrubber = emailScrubber;
    }

    @Override
    public String scrub(String input, ScrubMode mode) {
        try{
            return switch (mode) {
                case ONLY_DIGITS -> digitScrubber.scrub(input);
                case ONLY_EMAILS -> emailScrubber.scrub(input);
                case FULL_SCRUBBING -> emailScrubber.scrub(digitScrubber.scrub(input));
            };
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }
}
