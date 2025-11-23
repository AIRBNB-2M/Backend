package project.airbnb.clone.repository.jpa;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import project.airbnb.clone.consts.SocialType;
import project.airbnb.clone.entity.Member;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    default Member getMemberByEmail(String email) {
        return findByEmail(email).orElseThrow(() -> new EntityNotFoundException(
                "Cannot found Guest from: " + email
        ));
    }

    default Member getMemberById(Long id) {
        return findById(id).orElseThrow(() -> new EntityNotFoundException(
                "Cannot found Guest from: " + id
        ));
    }

    Optional<Member> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByEmailAndSocialType(String email, SocialType socialType);
}