package pokecenter.model;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.*;

@MappedSuperclass
public abstract class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @NotBlank
    @Size(min = 2, max = 50)
    protected String name;

    @NotBlank
    @Size(min = 2, max = 50)
    protected String surname;

    @Min(1)
    @Max(120)
    protected int age;

    @Pattern(regexp = "^\\d{3}[-]?\\d{3}[-]?\\d{3}$", message = "Invalid phone number format.")
    protected String phoneNumber;

    public Person() {}

    public Person(String name, String surname, int age, String phoneNumber) {
        this.name = name;
        this.surname = surname;
        this.age = age;
        this.phoneNumber = phoneNumber;
    }

    public abstract double calculateMonthlyCost();
    public abstract String getRoleDescription();

    public String getFullName() {
        return name + " " + surname;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getSurname() { return surname; }
    public int getAge() { return age; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setAge(int age) {
        if (age < 1 || age > 120) {
            throw new IllegalArgumentException("Age must be between 1 and 120.");
        }
        this.age = age;
    }

    @Override
    public String toString() {
        return "ID: " + id + ", Name: " + getFullName() + ", Age: " + age +
                (phoneNumber != null ? ", Phone: " + phoneNumber : ", Phone: N/A");
    }
}
