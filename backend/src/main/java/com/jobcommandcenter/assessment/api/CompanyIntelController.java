package com.jobcommandcenter.assessment.api;

import com.jobcommandcenter.assessment.api.dto.CompanyDossierResponse;
import com.jobcommandcenter.assessment.api.dto.GenerateCompanyDossierRequest;
import com.jobcommandcenter.assessment.application.CompanyDossierService;
import com.jobcommandcenter.security.jwt.SecurityUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/intel")
public class CompanyIntelController {

    private final CompanyDossierService dossierService;

    public CompanyIntelController(CompanyDossierService dossierService) {
        this.dossierService = dossierService;
    }

    @GetMapping("/jobs/{jobId}/company-dossier")
    public ResponseEntity<CompanyDossierResponse> getDossier(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID jobId
    ) {
        CompanyDossierResponse response = dossierService.getDossierForJob(securityUser.getId(), jobId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/jobs/{jobId}/company-dossier")
    public ResponseEntity<CompanyDossierResponse> generateOrRefreshDossier(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID jobId,
        @RequestBody(required = false) GenerateCompanyDossierRequest request
    ) {
        CompanyDossierResponse response = dossierService.generateOrRefreshDossier(securityUser.getId(), jobId, request);
        return ResponseEntity.ok(response);
    }
}
