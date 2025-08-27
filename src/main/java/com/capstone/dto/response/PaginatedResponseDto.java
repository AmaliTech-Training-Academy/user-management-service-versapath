package com.capstone.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.capstone.model.PaginationMetadata;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaginatedResponseDto<T> {
    private List<T> content;
    private PaginationMetadata pagination;
}
