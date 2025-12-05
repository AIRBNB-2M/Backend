package project.airbnb.clone.service.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final VectorStore vectorStore;

    public void embedAccommodations() {
        //숙소 - 제목, 설명, 위치, 최대인원, 주소, 편의시설, 가격
        //도전과제 - 사진 텍스트 설명 추출
    }
}
