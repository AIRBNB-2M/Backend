package project.airbnb.clone.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.airbnb.clone.entity.SigunguCode;

public interface SigunguCodeRepository extends JpaRepository<SigunguCode, String> {
}