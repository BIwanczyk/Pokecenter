package pokecenter.repository;

import pokecenter.model.*;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PublicEventRepository extends JpaRepository<PublicEvent, Long> {}