package com.jobcommandcenter.skill.api;

import com.jobcommandcenter.security.jwt.SecurityUser;
import com.jobcommandcenter.skill.application.UserSkillService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/profile/skills")
public class UserSkillController {

    private final UserSkillService userSkillService;

    public UserSkillController(UserSkillService userSkillService) {
        this.userSkillService = userSkillService;
    }

    @GetMapping
    public ResponseEntity<List<UserSkillResponse>> getUserSkills(@AuthenticationPrincipal SecurityUser securityUser) {
        List<UserSkillResponse> skills = userSkillService.getUserSkills(securityUser.getId());
        return ResponseEntity.ok(skills);
    }

    @PostMapping
    public ResponseEntity<UserSkillResponse> addUserSkill(
        @AuthenticationPrincipal SecurityUser securityUser,
        @Valid @RequestBody AddUserSkillRequest request
    ) {
        UserSkillResponse created = userSkillService.addUserSkill(securityUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserSkillResponse> updateUserSkill(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID id,
        @Valid @RequestBody UpdateUserSkillRequest request
    ) {
        UserSkillResponse updated = userSkillService.updateUserSkill(securityUser.getId(), id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserSkill(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID id
    ) {
        userSkillService.deleteUserSkill(securityUser.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
