package project.airbnb.clone.common.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import project.airbnb.clone.dto.AccommodationProcessorDto;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccommodationWriter implements ItemWriter<AccommodationProcessorDto> {

    @Override
    @Transactional
    public void write(Chunk<? extends AccommodationProcessorDto> chunk) throws Exception {
        log.debug("AccommodationWriter.write");
        List<? extends AccommodationProcessorDto> items = chunk.getItems();

        log.info("items.size() = {}", items.size());
        for (AccommodationProcessorDto item : items) {
            System.out.println("item = " + item);
        }
    }
}
