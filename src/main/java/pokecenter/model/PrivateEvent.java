package pokecenter.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

import java.time.LocalDate;

@Entity
public class PrivateEvent extends Event {

    private int maxParticipants = 20;

    public PrivateEvent() {}

    public PrivateEvent(String eventName, LocalDate date, int maxParticipants) {
        super(eventName, date);
        this.maxParticipants = maxParticipants;
    }

    public int getMaxParticipants() { return maxParticipants; }

    public void setMaxParticipants(int maxParticipants) {
        if (maxParticipants <= 0) {
            throw new IllegalArgumentException("maxParticipants must be > 0");
        }
        int current = getParticipantsByRegistrationNumber().size();
        if (current > maxParticipants) {
            throw new IllegalStateException(
                    "Cannot set maxParticipants to " + maxParticipants + " because " + current + " participant(s) are already registered."
            );
        }
        this.maxParticipants = maxParticipants;
    }

    @Override
    @Transient
    public String getEventType() { return "Private Event"; }
}
