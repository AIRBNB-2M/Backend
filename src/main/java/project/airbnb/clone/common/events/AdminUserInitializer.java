package project.airbnb.clone.common.events;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import project.airbnb.clone.entity.Guest;
import project.airbnb.clone.repository.jpa.GuestRepository;

@Profile("!test")
@Component
@RequiredArgsConstructor
public class AdminUserInitializer implements ApplicationListener<ContextRefreshedEvent> {

    private final GuestRepository guestRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (guestRepository.existsByEmail(adminEmail)) {
            return;
        }

        Guest admin = Guest.createAdmin(adminEmail, passwordEncoder.encode(adminPassword));
        guestRepository.save(admin);
    }
}