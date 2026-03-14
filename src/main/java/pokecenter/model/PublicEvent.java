package pokecenter.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

import java.time.LocalDate;

@Entity
public class PublicEvent extends Event {

    private int maxParticipants = 0;
    private int maxAudience = 0;

    public PublicEvent() {}

    public PublicEvent(String eventName, LocalDate date, int maxParticipants, int maxAudience) {
        super(eventName, date);
        this.maxParticipants = maxParticipants;
        this.maxAudience = maxAudience;
    }

    public int getMaxParticipants() { return maxParticipants; }

    public void setMaxParticipants(int maxParticipants) {
        int current = getParticipantsByRegistrationNumber().size();
        if (maxParticipants > 0 && current > maxParticipants) {
            throw new IllegalStateException(
                    "Cannot set maxParticipants to " + maxParticipants + " because " + current + " participant(s) are already registered."
            );
        }
        this.maxParticipants = maxParticipants;
    }

    public int getMaxAudience() { return maxAudience; }

    public void setMaxAudience(int maxAudience) { this.maxAudience = maxAudience; }

    @Override
    @Transient
    public String getEventType() {
        return "Public Event";
    }
}
