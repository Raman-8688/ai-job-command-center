package com.jobcommandcenter.interview.api.dto;

import jakarta.validation.constraints.NotBlank;

public record SavePrepQuestionRequest(
    String topicCategory,
    @NotBlank(message = "Question text is required")
    String question,
    String talkingPoints,
    String suggestedAnswerStar
) {}
