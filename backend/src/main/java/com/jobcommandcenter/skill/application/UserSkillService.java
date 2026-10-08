package com.jobcommandcenter.skill.application;

import com.jobcommandcenter.common.error.ConflictException;
import com.jobcommandcenter.common.error.ResourceNotFoundException;
import com.jobcommandcenter.skill.api.AddUserSkillRequest;
import com.jobcommandcenter.skill.api.UpdateUserSkillRequest;
import com.jobcommandcenter.skill.api.UserSkillResponse;
import com.jobcommandcenter.skill.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class UserSkillService {

    private final UserSkillRepository userSkillRepository;
    private final SkillRepository skillRepository;

    public UserSkillService(UserSkillRepository userSkillRepository,
                            SkillRepository skillRepository) {
        this.userSkillRepository = userSkillRepository;
        this.skillRepository = skillRepository;
    }

    public List<UserSkillResponse> getUserSkills(UUID userId) {
        List<UserSkill> userSkills = userSkillRepository.findByUserId(userId);
        if (userSkills.isEmpty()) {
            return List.of();
        }

        Map<UUID, Skill> skillMap = skillRepository.findAll().stream()
            .collect(Collectors.toMap(Skill::getId, Function.identity(), (a, b) -> a));

        return userSkills.stream()
            .map(us -> UserSkillResponse.of(us, skillMap.get(us.getSkillId())))
            .collect(Collectors.toList());
    }

    @Transactional
    public UserSkillResponse addUserSkill(UUID userId, AddUserSkillRequest request) {
        Skill skill = skillRepository.findById(request.skillId())
            .orElseThrow(() -> new ResourceNotFoundException("Skill not found in catalog with ID: " + request.skillId()));

        if (userSkillRepository.existsByUserIdAndSkillId(userId, skill.getId())) {
            throw new ConflictException("Skill '" + skill.getName() + "' is already in candidate profile");
        }

        VerificationSource source = request.verificationSource() != null
            ? request.verificationSource()
            : VerificationSource.USER_EXPLICIT;

        boolean verified = Boolean.TRUE.equals(request.verified());
        if (source == VerificationSource.AI_SUGGESTED) {
            // Anti-hallucination principle: AI suggestions can never be auto-verified
            verified = false;
        }

        UserSkill userSkill = UserSkill.createNew(
            userId,
            skill.getId(),
            request.proficiency(),
            request.yearsExperience(),
            verified,
            source,
            request.notes()
        );

        UserSkill saved = userSkillRepository.save(userSkill);
        return UserSkillResponse.of(saved, skill);
    }

    @Transactional
    public UserSkillResponse updateUserSkill(UUID userId, UUID userSkillId, UpdateUserSkillRequest request) {
        UserSkill userSkill = userSkillRepository.findById(userSkillId)
            .orElseThrow(() -> new ResourceNotFoundException("User skill not found with ID: " + userSkillId));

        if (!userSkill.getUserId().equals(userId)) {
            // Tenant/owner isolation
            throw new ResourceNotFoundException("User skill not found with ID: " + userSkillId);
        }

        userSkill.updateDetails(
            request.proficiency(),
            request.yearsExperience(),
            request.verified(),
            request.verificationSource(),
            request.notes()
        );

        UserSkill saved = userSkillRepository.save(userSkill);
        Skill skill = skillRepository.findById(saved.getSkillId()).orElse(null);
        return UserSkillResponse.of(saved, skill);
    }

    @Transactional
    public void deleteUserSkill(UUID userId, UUID userSkillId) {
        UserSkill userSkill = userSkillRepository.findById(userSkillId)
            .orElseThrow(() -> new ResourceNotFoundException("User skill not found with ID: " + userSkillId));

        if (!userSkill.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("User skill not found with ID: " + userSkillId);
        }

        userSkillRepository.deleteById(userSkillId);
    }
}
