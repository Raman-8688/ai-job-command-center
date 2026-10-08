package com.jobcommandcenter.ai.api;

import com.jobcommandcenter.ai.api.dto.JobAiAnalysisResponseDto;
import com.jobcommandcenter.ai.api.dto.JobAiFitResponseDto;
import com.jobcommandcenter.ai.api.dto.TriggerAiAnalysisRequestDto;
import com.jobcommandcenter.ai.application.JobAiAnalysisService;
import com.jobcommandcenter.ai.application.JobAiFitService;
import com.jobcommandcenter.ai.domain.JobAiAnalysis;
import com.jobcommandcenter.ai.domain.JobAiFitEvaluation;
import com.jobcommandcenter.security.jwt.SecurityUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for AI Job Analysis and explainable fit evaluation.
 */
@RestController
@RequestMapping("/api/jobs/{id}")
public class JobAiController {

    private final JobAiAnalysisService jobAiAnalysisService;
    private final JobAiFitService jobAiFitService;

    public JobAiController(JobAiAnalysisService jobAiAnalysisService,
                           JobAiFitService jobAiFitService) {
        this.jobAiAnalysisService = jobAiAnalysisService;
        this.jobAiFitService = jobAiFitService;
    }

    @PostMapping("/ai-analysis")
    public ResponseEntity<JobAiAnalysisResponseDto> analyzeJob(
        @PathVariable UUID id,
        @RequestBody(required = false) TriggerAiAnalysisRequestDto request
    ) {
        String provider = request != null ? request.provider() : null;
        JobAiAnalysis analysis = jobAiAnalysisService.analyzeJob(id, provider);
        return ResponseEntity.status(HttpStatus.CREATED).body(JobAiAnalysisResponseDto.fromDomain(analysis));
    }

    @GetMapping("/ai-analysis")
    public ResponseEntity<JobAiAnalysisResponseDto> getLatestAnalysis(@PathVariable UUID id) {
        JobAiAnalysis analysis = jobAiAnalysisService.getLatestAnalysis(id);
        return ResponseEntity.ok(JobAiAnalysisResponseDto.fromDomain(analysis));
    }

    @GetMapping("/ai-analysis/history")
    public ResponseEntity<List<JobAiAnalysisResponseDto>> getAnalysisHistory(@PathVariable UUID id) {
        List<JobAiAnalysis> history = jobAiAnalysisService.getAnalysisHistory(id);
        List<JobAiAnalysisResponseDto> response = history.stream()
            .map(JobAiAnalysisResponseDto::fromDomain)
            .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ai-fit")
    public ResponseEntity<JobAiFitResponseDto> getAiFit(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID id
    ) {
        JobAiFitEvaluation evaluation = jobAiFitService.evaluateFit(securityUser.getId(), id);
        return ResponseEntity.ok(JobAiFitResponseDto.fromDomain(evaluation));
    }
}
