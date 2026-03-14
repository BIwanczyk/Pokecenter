package pokecenter.model;

public interface ITrainer {


    Boolean getHasInsurance();
    void setHasInsurance(Boolean hasInsurance);


    String getEmail();
    void setEmail(String email);


    Integer getBadgeCount();
    void setBadgeCount(Integer badgeCount);
}
