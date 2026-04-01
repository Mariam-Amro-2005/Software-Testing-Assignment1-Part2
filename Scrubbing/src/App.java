import Services.EmailScrubber;
import Interfaces.IScrubEmails;

public class App {
    public static void main(String[] args) throws Exception {
        IScrubEmails emailScrubber = new EmailScrubber();
        System.out.println(emailScrubber.scrub("Contact me at john.doe@example.com"));
    
    }
}
