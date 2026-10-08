package com.jobcommandcenter.profile.api;

import com.jobcommandcenter.profile.application.ProfileService;
import com.jobcommandcenter.security.jwt.SecurityUser;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile(@AuthenticationPrincipal SecurityUser securityUser) {
        ProfileResponse response = profileService.getProfileByUserId(securityUser.getId());
        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<ProfileResponse> updateProfile(@AuthenticationPrincipal SecurityUser securityUser,
                                                         @Valid @RequestBody UpdateProfileRequest request) {
        ProfileResponse response = profileService.updateProfile(securityUser.getId(), request);
        return ResponseEntity.ok(response);
    }
}
