package project.airbnb.clone.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import project.airbnb.clone.entity.area.AreaCode;

public interface AreaCodeRepository extends JpaRepository<AreaCode, String> {
}