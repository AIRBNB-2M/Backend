package project.airbnb.clone.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class PageResponseDto<E> {

    private final List<E> contents;
    private final boolean hasPrev, hasNext;
    private final int totalCount, prevPage, nextPage, totalPage, current, size;

    @Builder
    public PageResponseDto(List<E> contents, int pageSize, int pageNumber, long total) {
        this.contents = contents;
        this.totalCount = (int) total;
        this.size = pageSize;
        this.current = pageNumber;

        this.totalPage = (int) (Math.ceil(totalCount / (double) size));

        this.hasPrev = pageNumber > 0;
        this.hasNext = pageNumber < (totalPage - 1);

        this.prevPage = hasPrev ? current - 1 : -1;
        this.nextPage = hasNext ? current + 1 : -1;
    }
}
