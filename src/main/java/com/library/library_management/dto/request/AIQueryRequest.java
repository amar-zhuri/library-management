package com.library.library_management.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
//@data
//@allargsconstractor
public class AIQueryRequest {

    @NotBlank(message = "Question is required")
    @Size(min = 3, max = 500, message = "Question must be between 3 and 500 characters")
    private String question;

    // Optional: force LLM usage even if rule-based can handle it
    private Boolean useLLM = false;
}