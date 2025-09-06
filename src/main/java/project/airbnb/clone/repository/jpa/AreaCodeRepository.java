package project.airbnb.clone.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import project.airbnb.clone.entity.AreaCode;

public interface AreaCodeRepository extends JpaRepository<AreaCode, String> {
}