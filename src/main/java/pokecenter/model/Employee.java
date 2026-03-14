package pokecenter.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "employee_role", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("EMPLOYEE")
public class Employee extends Person {

    @ManyToOne
    @JoinColumn(name = "pokecenter_id", nullable = false)
    @JsonBackReference("pokecenter-employees")
    private Pokecenter pokecenter;

    public Employee() {}

    public Employee(String name, String surname, int age, String phoneNumber, Pokecenter pokecenter) {
        super(name, surname, age, phoneNumber);
        this.pokecenter = pokecenter;
    }

    protected Employee(Employee old) {
        super(old.getName(), old.getSurname(), old.getAge(), old.getPhoneNumber());
        this.pokecenter = old.getPokecenter();
    }

    public Pokecenter getPokecenter() {
        return pokecenter;
    }

    public void setPokecenter(Pokecenter pokecenter) {
        this.pokecenter = pokecenter;
    }

    public void setName(String name) { this.name = name; }
    public void setSurname(String surname) { this.surname = surname; }
    public void setAge(int age) { this.age = age; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getName() { return name; }
    public String getSurname() { return surname; }
    public int getAge() { return age; }
    public String getPhoneNumber() { return phoneNumber; }

    @Override
    public double calculateMonthlyCost() {
        double baseSalary = 3000;
        double seniorBonus = (age > 50) ? 200 : 0;
        return baseSalary + seniorBonus;
    }

    @Override
    public String getRoleDescription() {
        return "Employee at Pokecenter: " +
                (pokecenter != null ? pokecenter.getLocation() : "No Pokecenter assigned");
    }

    @Override
    public String toString() {
        return super.toString() + ", Role: " + getRoleDescription();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Employee)) return false;
        Employee other = (Employee) o;
        return id != null && id.equals(other.id);
    }

    //stały hashCode id jest nadawane po zapisie do bazy

    @Override
    public int hashCode() {
        return Employee.class.hashCode();
    }

}
