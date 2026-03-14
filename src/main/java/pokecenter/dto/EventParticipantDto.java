package pokecenter.dto;

import pokecenter.model.*;

import pokecenter.model.Trainer;

public class EventParticipantDto {

    private int registrationNumber;
    private long id;
    private String name;
    private String surname;
    private String email;

    public EventParticipantDto(int registrationNumber, Number id, String name, String surname, String email) {
        this.registrationNumber = registrationNumber;
        this.id = (id == null) ? 0L : id.longValue();
        this.name = name;
        this.surname = surname;
        this.email = email;
    }

    public static EventParticipantDto from(int registrationNumber, Trainer t) {
        return new EventParticipantDto(registrationNumber, t.getId(), t.getName(), t.getSurname(), t.getEmail());
    }

    public int getRegistrationNumber() { return registrationNumber; }
    public long getId() { return id; }
    public String getName() { return name; }
    public String getSurname() { return surname; }
    public String getEmail() { return email; }
}