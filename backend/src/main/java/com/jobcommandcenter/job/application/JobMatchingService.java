package com.jobcommandcenter.job.application;

import com.jobcommandcenter.common.error.ResourceNotFoundException;
import com.jobcommandcenter.job.domain.*;
import com.jobcommandcenter.profile.domain.Profile;
import com.jobcommandcenter.profile.domain.ProfileRepository;
import com.jobcommandcenter.profile.domain.WorkPreference;
import com.jobcommandcenter.skill.domain.Skill;
import com.jobcommandcenter.skill.domain.SkillRepository;
import com.jobcommandcenter.skill.domain.UserSkill;
import com.jobcommandcenter.skill.domain.UserSkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class JobMatchingService {

    private final JobRepository jobRepository;
    private final ProfileRepository profileRepository;
    private final UserSkillRepository userSkillRepository;
    private final SkillRepository skillRepository;

    public JobMatchingService(JobRepository jobRepository,
                              ProfileRepository profileRepository,
                              UserSkillRepository userSkillRepository,
                              SkillRepository skillRepository) {
        this.jobRepository = jobRepository;
        this.profileRepository = profileRepository;
        this.userSkillRepository = userSkillRepository;
        this.skillRepository = skillRepository;
    }

    public JobMatchResult calculateMatch(UUID userId, UUID jobId) {
        Job job = jobRepository.findById(jobId)
            .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + jobId));

        Profile profile = profileRepository.findByUserId(userId)
            .orElseGet(() -> Profile.createNew(userId));

        // Anti-hallucination grounding: strictly fetch and filter verified user skills
        List<UserSkill> verifiedUserSkills = userSkillRepository.findByUserId(userId).stream()
            .filter(UserSkill::isVerified)
            .collect(Collectors.toList());

        Set<UUID> verifiedSkillIds = verifiedUserSkills.stream()
            .map(UserSkill::getSkillId)
            .collect(Collectors.toSet());

        // Cache skill names for explainability
        Map<UUID, String> skillNames = skillRepository.findAll().stream()
            .collect(Collectors.toMap(Skill::getId, Skill::getName, (a, b) -> a));

        return computeMatch(job, profile, verifiedSkillIds, skillNames);
    }

    public List<JobMatchResult> calculateMatchesForUser(UUID userId) {
        Profile profile = profileRepository.findByUserId(userId)
            .orElseGet(() -> Profile.createNew(userId));

        List<UserSkill> verifiedUserSkills = userSkillRepository.findByUserId(userId).stream()
            .filter(UserSkill::isVerified)
            .collect(Collectors.toList());

        Set<UUID> verifiedSkillIds = verifiedUserSkills.stream()
            .map(UserSkill::getSkillId)
            .collect(Collectors.toSet());

        Map<UUID, String> skillNames = skillRepository.findAll().stream()
            .collect(Collectors.toMap(Skill::getId, Skill::getName, (a, b) -> a));

        List<Job> activeJobs = jobRepository.findAll().stream()
            .filter(j -> j.getStatus() == JobStatus.ACTIVE)
            .collect(Collectors.toList());

        return activeJobs.stream()
            .map(job -> computeMatch(job, profile, verifiedSkillIds, skillNames))
            .sorted(Comparator.comparingInt(JobMatchResult::overallScore).reversed())
            .collect(Collectors.toList());
    }

    JobMatchResult computeMatch(Job job,
                               Profile profile,
                               Set<UUID> verifiedSkillIds,
                               Map<UUID, String> skillNames) {
        List<String> matchedSkills = new ArrayList<>();
        List<String> missingRequiredSkills = new ArrayList<>();
        List<String> matchedPreferredSkills = new ArrayList<>();
        List<String> missingPreferredSkills = new ArrayList<>();
        List<String> matchReasons = new ArrayList<>();

        // 1. Required Skills Score (35% weight)
        List<JobSkill> requiredJobSkills = job.getJobSkills().stream()
            .filter(JobSkill::isRequired)
            .toList();

        int requiredSkillScore;
        if (requiredJobSkills.isEmpty()) {
            requiredSkillScore = 100;
            matchReasons.add("No specific required skills specified by employer");
        } else {
            int matchedCount = 0;
            for (JobSkill js : requiredJobSkills) {
                String name = skillNames.getOrDefault(js.getSkillId(), "Skill " + js.getSkillId());
                if (verifiedSkillIds.contains(js.getSkillId())) {
                    matchedCount++;
                    matchedSkills.add(name);
                } else {
                    missingRequiredSkills.add(name);
                }
            }
            requiredSkillScore = Math.round(((float) matchedCount / requiredJobSkills.size()) * 100);
            matchReasons.add("Matched " + matchedCount + " of " + requiredJobSkills.size() + " required skills");
        }

        // 2. Preferred Skills Score (15% weight)
        List<JobSkill> preferredJobSkills = job.getJobSkills().stream()
            .filter(JobSkill::isPreferred)
            .toList();

        int preferredSkillScore;
        if (preferredJobSkills.isEmpty()) {
            preferredSkillScore = 100;
        } else {
            int matchedCount = 0;
            for (JobSkill js : preferredJobSkills) {
                String name = skillNames.getOrDefault(js.getSkillId(), "Skill " + js.getSkillId());
                if (verifiedSkillIds.contains(js.getSkillId())) {
                    matchedCount++;
                    matchedPreferredSkills.add(name);
                } else {
                    missingPreferredSkills.add(name);
                }
            }
            preferredSkillScore = Math.round(((float) matchedCount / preferredJobSkills.size()) * 100);
            if (matchedCount > 0) {
                matchReasons.add("Matched " + matchedCount + " of " + preferredJobSkills.size() + " preferred skills");
            }
        }

        // 3. Title / Target Role Relevance (20% weight)
        int titleScore = computeTitleScore(job.getTitle(), profile.getTargetRoles(), matchReasons);

        // 4. Experience Compatibility (15% weight)
        int experienceScore = computeExperienceScore(
            profile.getYearsExperience(),
            job.getExperienceMinYears(),
            job.getExperienceMaxYears(),
            matchReasons
        );

        // 5. Work Mode Compatibility (10% weight)
        int workModeScore = computeWorkModeScore(profile.getWorkPreference(), job.getWorkMode(), matchReasons);

        // 6. Location Compatibility (5% weight)
        int locationScore = computeLocationScore(
            profile.getLocation(),
            profile.getPreferredLocations(),
            job.getLocation(),
            job.getWorkMode(),
            matchReasons
        );

        // Overall Weighted Score
        int overallScore = Math.round(
            (0.35f * requiredSkillScore) +
            (0.15f * preferredSkillScore) +
            (0.20f * titleScore) +
            (0.15f * experienceScore) +
            (0.10f * workModeScore) +
            (0.05f * locationScore)
        );
        overallScore = Math.max(0, Math.min(100, overallScore));

        // Core Anti-Hallucination & Explainability Rule: Missing required skills must be visibly flagged!
        if (!missingRequiredSkills.isEmpty()) {
            matchReasons.add(0, "ATTENTION: Missing " + missingRequiredSkills.size() +
                " required skill(s): [" + String.join(", ", missingRequiredSkills) + "]");
        }

        return new JobMatchResult(
            job.getId(),
            profile.getUserId(),
            overallScore,
            titleScore,
            requiredSkillScore,
            preferredSkillScore,
            experienceScore,
            locationScore,
            workModeScore,
            matchedSkills,
            missingRequiredSkills,
            matchedPreferredSkills,
            missingPreferredSkills,
            matchReasons
        );
    }

    private int computeTitleScore(String jobTitle, List<String> targetRoles, List<String> reasons) {
        if (targetRoles == null || targetRoles.isEmpty()) {
            reasons.add("No target roles specified in profile (baseline title score)");
            return 50;
        }

        String normJobTitle = normalizeText(jobTitle);
        Set<String> jobTokens = Set.of(normJobTitle.split("\\s+"));

        for (String role : targetRoles) {
            String normRole = normalizeText(role);
            if (normJobTitle.contains(normRole) || normRole.contains(normJobTitle)) {
                reasons.add("Target role '" + role + "' matches job title '" + jobTitle + "'");
                return 100;
            }

            Set<String> roleTokens = Set.of(normRole.split("\\s+"));
            long commonTokens = roleTokens.stream().filter(jobTokens::contains).count();
            if (commonTokens >= 2) {
                reasons.add("Target role '" + role + "' closely aligns with job title '" + jobTitle + "'");
                return 85;
            } else if (commonTokens >= 1) {
                reasons.add("Partial keyword overlap between target role '" + role + "' and job title");
                return 70;
            }
        }

        reasons.add("Target roles do not directly match job title '" + jobTitle + "'");
        return 30;
    }

    private int computeExperienceScore(BigDecimal candidateExp,
                                       BigDecimal minExp,
                                       BigDecimal maxExp,
                                       List<String> reasons) {
        BigDecimal exp = candidateExp != null ? candidateExp : BigDecimal.ZERO;

        if (minExp == null && maxExp == null) {
            return 100;
        }

        boolean aboveMin = minExp == null || exp.compareTo(minExp) >= 0;
        boolean belowMax = maxExp == null || exp.compareTo(maxExp) <= 0;

        if (aboveMin && belowMax) {
            reasons.add("Candidate experience (" + exp + " yrs) satisfies required range");
            return 100;
        }

        if (minExp != null && exp.compareTo(minExp) < 0) {
            BigDecimal diff = minExp.subtract(exp);
            if (diff.compareTo(new BigDecimal("1.0")) <= 0) {
                reasons.add("Candidate experience (" + exp + " yrs) is slightly below minimum (" + minExp + " yrs)");
                return 70;
            } else {
                reasons.add("Candidate experience (" + exp + " yrs) is below minimum (" + minExp + " yrs)");
                return 25;
            }
        }

        if (maxExp != null && exp.compareTo(maxExp) > 0) {
            reasons.add("Candidate experience (" + exp + " yrs) exceeds maximum requested (" + maxExp + " yrs)");
            return 85;
        }

        return 50;
    }

    private int computeWorkModeScore(WorkPreference candidatePref, WorkMode jobMode, List<String> reasons) {
        if (candidatePref == WorkPreference.OPEN || jobMode == WorkMode.UNKNOWN) {
            return 100;
        }

        if (jobMode == WorkMode.REMOTE && candidatePref == WorkPreference.REMOTE) {
            reasons.add("Fully aligned remote work preference");
            return 100;
        }
        if (jobMode == WorkMode.HYBRID && candidatePref == WorkPreference.HYBRID) {
            reasons.add("Fully aligned hybrid work preference");
            return 100;
        }
        if (jobMode == WorkMode.ONSITE && candidatePref == WorkPreference.ONSITE) {
            reasons.add("Fully aligned onsite work preference");
            return 100;
        }

        if (jobMode == WorkMode.REMOTE && (candidatePref == WorkPreference.HYBRID || candidatePref == WorkPreference.ONSITE)) {
            reasons.add("Remote job is compatible with candidate preference");
            return 90;
        }

        if (candidatePref == WorkPreference.REMOTE && (jobMode == WorkMode.HYBRID || jobMode == WorkMode.ONSITE)) {
            reasons.add("Work mode mismatch: Candidate prefers REMOTE, but job is " + jobMode);
            return 20;
        }

        return 50;
    }

    private int computeLocationScore(String candidateLocation,
                                     List<String> preferredLocations,
                                     String jobLocation,
                                     WorkMode jobMode,
                                     List<String> reasons) {
        if (jobMode == WorkMode.REMOTE) {
            return 100;
        }

        if (jobLocation == null || jobLocation.isBlank()) {
            return 80;
        }

        String normJobLoc = normalizeText(jobLocation);

        if (candidateLocation != null && !candidateLocation.isBlank()) {
            String normCandLoc = normalizeText(candidateLocation);
            if (normJobLoc.contains(normCandLoc) || normCandLoc.contains(normJobLoc)) {
                reasons.add("Job location matches candidate home location");
                return 100;
            }
        }

        if (preferredLocations != null) {
            for (String pref : preferredLocations) {
                String normPref = normalizeText(pref);
                if (normJobLoc.contains(normPref) || normPref.contains(normJobLoc)) {
                    reasons.add("Job location matches preferred location: " + pref);
                    return 100;
                }
            }
        }

        reasons.add("Location mismatch: Job in '" + jobLocation + "'");
        return 30;
    }

    private String normalizeText(String input) {
        if (input == null) return "";
        return input.trim().toLowerCase().replaceAll("[^a-zA-Z0-9\\s]", " ");
    }
}
