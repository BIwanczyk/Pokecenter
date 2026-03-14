package pokecenter.dto;

import pokecenter.model.*;

import pokecenter.model.Trainer;
public class TrainerBriefDto {
    private long id;
    private String name;
    private String surname;
    private String email;

    public TrainerBriefDto(Number id, String name, String surname, String email) {
        this.id = (id == null) ? 0L : id.longValue();
        this.name = name;
        this.surname = surname;
        this.email = email;
    }

    public static TrainerBriefDto from(Trainer t){
        return new TrainerBriefDto(t.getId(), t.getName(), t.getSurname(), t.getEmail());
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public String getSurname() { return surname; }
    public String getEmail() { return email; }
}