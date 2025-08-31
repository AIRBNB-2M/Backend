package project.airbnb.clone;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import project.airbnb.clone.common.clients.TourApiClient;
import project.airbnb.clone.dto.TourApiResponse;
import project.airbnb.clone.entity.AreaCode;
import project.airbnb.clone.entity.SigunguCode;
import project.airbnb.clone.repository.AreaCodeRepository;
import project.airbnb.clone.repository.SigunguCodeRepository;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class AppRun implements ApplicationListener<ContextRefreshedEvent> {

    private final TourApiClient tourApiClient;
    private final AreaCodeRepository areaCodeRepository;
    private final SigunguCodeRepository sigunguCodeRepository;
    private final EntityManager em;

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {

        JsonNode response = tourApiClient.areaCode(1, 17, "");
        TourApiResponse result = new TourApiResponse(response);

        sigunguCodeRepository.deleteAllInBatch();
        areaCodeRepository.deleteAllInBatch();

        for (Map<String, String> item : result.getItems()) {
            String parentCode = item.get("code");
            String parentName = item.get("name");

            AreaCode areaCode = new AreaCode(parentCode, parentName);
            em.persist(areaCode);

            JsonNode sigungu = tourApiClient.areaCode(1, 31, parentCode);

            result = new TourApiResponse(sigungu);

            for (Map<String, String> c : result.getItems()) {
                String childCode = c.get("code");
                String childName = c.get("name");

                em.persist(new SigunguCode(parentCode + "-" + childCode, childName, areaCode));
            }

            em.flush();
            em.clear();
        }
    }
}
