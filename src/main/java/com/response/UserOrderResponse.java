package com.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserOrderResponse<T> extends PageResponse<T> {
    private Map<String, Long> counts;

    public UserOrderResponse(List<T> content, int page, int size, long totalElements, int totalPages, Map<String, Long> counts) {
        super(content, page, size, totalElements, totalPages);
        this.counts = counts;
    }
}
