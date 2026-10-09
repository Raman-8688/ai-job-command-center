package com.jobcommandcenter.interview.api.dto;

public record UpdatePrepNotesRequest(
    String userAnswerNotes,
    Boolean isReviewed
) {}
