package interestingideas.brainchatserver.mail;

public interface EmailSender {
    void send(String sender, String to, String email);
}
