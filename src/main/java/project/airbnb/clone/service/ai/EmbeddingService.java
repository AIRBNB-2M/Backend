package project.airbnb.clone.service.ai;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.airbnb.clone.config.ai.embed.AccommodationEmbeddingDto;
import project.airbnb.clone.config.ai.embed.AmenitiesDto;
import project.airbnb.clone.consts.DayType;
import project.airbnb.clone.consts.Season;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmbeddingService {

    private final EntityManager em;
    private final VectorStore vectorStore;

    //TODO : 사진 텍스트 설명 추출
    public void embedAccommodations(Pageable pageable) {
        List<Long> ids = getEmbeddingTargetIds(pageable);

        List<AccommodationEmbeddingDto> embeddingDtos = getEmbeddingDtos(ids);
        Map<Long, AccommodationEmbeddingDto> baseInfoMapping = collectBaseInfo(embeddingDtos);

        Map<Long, String> pricesMapping = collectEmbedPrices(embeddingDtos);
        Map<Long, Map<Season, Map<DayType, Integer>>> priceInfo = collectMetadataPrices(embeddingDtos);

        List<AmenitiesDto> amenitiesDtos = getAmenitiesDtos(ids);

        Map<Long, List<String>> amenitiesMapping = collectEmbedAmenities(amenitiesDtos);

        List<Document> documents = new ArrayList<>();
        List<Long> successIds = new ArrayList<>();
        List<Long> failedIds = new ArrayList<>();

        for (Long id : ids) {
            try {
                AccommodationEmbeddingDto dto = baseInfoMapping.get(id);
                if (dto == null) continue;

                String prices = pricesMapping.getOrDefault(id, "");
                List<String> amenities = amenitiesMapping.getOrDefault(id, List.of());

                String content = String.format("""
                        [제목] : %s
                        [설명] : %s
                        [주소] : %s
                        [최대인원] : %d
                        [시기별 가격] :
                        %s
                        [편의시설] : %s
                        """, dto.title(), dto.description(), dto.address(), dto.maxPeople(), prices, String.join(", ", amenities));
                Map<String, Object> metadata = getMetadata(id, priceInfo, dto, amenities);

                documents.add(Document.builder().text(content).metadata(metadata).build());
                successIds.add(id);
            } catch (Exception e) {
                failedIds.add(id);
            }
        }

        try {
            vectorStore.add(documents);
        } catch (Exception e) {
            failedIds.addAll(successIds);
            successIds.clear();
            log.error("오류 발생! 임베딩 전체 실패", e);
        }

        afterProcess(successIds, "숙소 정보 임베딩 성공", true);
        afterProcess(failedIds, "숙소 정보 임베딩 실패", false);
    }

    private List<Long> getEmbeddingTargetIds(Pageable pageable) {
        return em.createQuery("""
                         SELECT acc.id
                         FROM Accommodation AS acc
                         WHERE acc.isEmbedded = false OR acc.isEmbedded IS NULL
                         ORDER BY acc.id
                         """, Long.class)
                 .setFirstResult((int) pageable.getOffset())
                 .setMaxResults(pageable.getPageSize())
                 .getResultList();
    }

    private List<AccommodationEmbeddingDto> getEmbeddingDtos(List<Long> ids) {
        return em.createQuery("""
                         SELECT new project.airbnb.clone.config.ai.embed.AccommodationEmbeddingDto(
                             acc.id,
                             acc.title,
                             acc.description,
                             acc.maxPeople,
                             acc.address,
                             p.season,
                             p.dayType,
                             p.price
                         )
                         FROM Accommodation AS acc
                         JOIN AccommodationPrice AS p ON p.accommodation = acc
                         WHERE acc.id IN :ids
                         """, AccommodationEmbeddingDto.class)
                 .setParameter("ids", ids)
                 .getResultList();
    }

    private Map<Long, AccommodationEmbeddingDto> collectBaseInfo(List<AccommodationEmbeddingDto> embeddingDtos) {
        return embeddingDtos.stream()
                            .collect(Collectors.groupingBy(
                                    AccommodationEmbeddingDto::accommodationId,
                                    Collectors.collectingAndThen(
                                            Collectors.toList(),
                                            list -> list.get(0)
                                    )
                            ));
    }

    private Map<Long, String> collectEmbedPrices(List<AccommodationEmbeddingDto> embeddingDtos) {
        return embeddingDtos.stream()
                            .collect(Collectors.groupingBy(
                                    AccommodationEmbeddingDto::accommodationId,
                                    Collectors.mapping(
                                            dto -> String.format("\t[%s / %s] : %,d원", dto.season(), dto.dayType(), dto.price()),
                                            Collectors.joining("\n")
                                    )
                            ));
    }

    private Map<Long, Map<Season, Map<DayType, Integer>>> collectMetadataPrices(List<AccommodationEmbeddingDto> embeddingDtos) {
        return embeddingDtos.stream()
                            .collect(Collectors.groupingBy(
                                    AccommodationEmbeddingDto::accommodationId,
                                    Collectors.groupingBy(
                                            AccommodationEmbeddingDto::season,
                                            Collectors.toMap(
                                                    AccommodationEmbeddingDto::dayType,
                                                    AccommodationEmbeddingDto::price
                                            )
                                    )
                            ));
    }

    private List<AmenitiesDto> getAmenitiesDtos(List<Long> ids) {
        return em.createQuery("""
                         SELECT new project.airbnb.clone.config.ai.embed.AmenitiesDto(
                             acc.id,
                             am.description
                         )
                         FROM Accommodation AS acc
                         LEFT JOIN AccommodationAmenity AS aa ON aa.accommodation = acc
                         JOIN Amenity AS am ON aa.amenity = am
                         WHERE acc.id IN :ids
                         """, AmenitiesDto.class)
                 .setParameter("ids", ids)
                 .getResultList();
    }

    private Map<Long, List<String>> collectEmbedAmenities(List<AmenitiesDto> amenitiesDtos) {
        return amenitiesDtos.stream()
                            .collect(Collectors.groupingBy(
                                    AmenitiesDto::accommodationId,
                                    Collectors.mapping(
                                            AmenitiesDto::name,
                                            Collectors.toList())
                            ));
    }

    private Map<String, Object> getMetadata(Long id,
                                            Map<Long, Map<Season, Map<DayType, Integer>>> priceInfo,
                                            AccommodationEmbeddingDto dto,
                                            List<String> amenities) {

        Map<Season, Map<DayType, Integer>> pricesMap = priceInfo.get(id);

        List<Integer> allPrices = pricesMap.values()
                                           .stream()
                                           .flatMap(dayMap -> dayMap.values().stream())
                                           .toList();

        Integer minPrice = allPrices.stream().min(Integer::compareTo).orElse(null);
        Integer maxPrice = allPrices.stream().max(Integer::compareTo).orElse(null);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("accId", id);
        metadata.put("title", dto.title());
        metadata.put("maxPeople", dto.maxPeople());
        metadata.put("address", dto.address());
        metadata.put("amenities", amenities);
        metadata.put("minPrice", minPrice);
        metadata.put("maxPrice", maxPrice);

        return metadata;
    }

    private void afterProcess(List<Long> ids, String message, boolean embedded) {
        if (!ids.isEmpty()) {
            log.info("{} : {}", message, ids);

            em.createQuery("""
                      UPDATE Accommodation AS acc
                      SET acc.isEmbedded = :embedded
                      WHERE acc.id IN :ids
                      """)
              .setParameter("ids", ids)
              .setParameter("embedded", embedded)
              .executeUpdate();
        }
    }
}
