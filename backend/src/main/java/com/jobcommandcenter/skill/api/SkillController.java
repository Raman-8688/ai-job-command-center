package com.jobcommandcenter.skill.api;

import com.jobcommandcenter.skill.application.SkillCatalogService;
import com.jobcommandcenter.skill.domain.SkillCategory;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/skills")
public class SkillController {

    private final SkillCatalogService skillCatalogService;

    public SkillController(SkillCatalogService skillCatalogService) {
        this.skillCatalogService = skillCatalogService;
    }

    @GetMapping
    public ResponseEntity<List<SkillResponse>> listSkills(
        @RequestParam(required = false) String query,
        @RequestParam(required = false) SkillCategory category
    ) {
        List<SkillResponse> skills = skillCatalogService.searchSkills(query, category);
        return ResponseEntity.ok(skills);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SkillResponse> getSkill(@PathVariable UUID id) {
        SkillResponse skill = skillCatalogService.getSkillById(id);
        return ResponseEntity.ok(skill);
    }

    @PostMapping
    public ResponseEntity<SkillResponse> createSkill(@Valid @RequestBody CreateSkillRequest request) {
        SkillResponse created = skillCatalogService.createSkill(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
