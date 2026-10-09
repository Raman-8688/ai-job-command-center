package com.jobcommandcenter.ai.infrastructure.provider;

import com.jobcommandcenter.ai.domain.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Deterministic, offline-safe AI provider implementation.
 * Performs NLP and rule-based semantic extraction without external network calls or API keys.
 */
@Component("mockAIProvider")
public class MockDeterministicAIProvider implements AIProvider {

    private static final String PROVIDER_NAME = "MOCK";
    private static final String MODEL_NAME = "deterministic-rule-v1";
    private static final String PROMPT_VERSION = "v1.0";

    // Known technology catalog for extraction
    private static final Map<String, TechnologyCategory> KNOWN_TECH = new LinkedHashMap<>();

    static {
        // Languages
        KNOWN_TECH.put("Java", TechnologyCategory.LANGUAGE);
        KNOWN_TECH.put("Python", TechnologyCategory.LANGUAGE);
        KNOWN_TECH.put("TypeScript", TechnologyCategory.LANGUAGE);
        KNOWN_TECH.put("JavaScript", TechnologyCategory.LANGUAGE);
        KNOWN_TECH.put("Go", TechnologyCategory.LANGUAGE);
        KNOWN_TECH.put("Kotlin", TechnologyCategory.LANGUAGE);
        KNOWN_TECH.put("C++", TechnologyCategory.LANGUAGE);
        KNOWN_TECH.put("C#", TechnologyCategory.LANGUAGE);
        KNOWN_TECH.put("Rust", TechnologyCategory.LANGUAGE);
        KNOWN_TECH.put("SQL", TechnologyCategory.LANGUAGE);

        // Frameworks
        KNOWN_TECH.put("Spring Boot", TechnologyCategory.FRAMEWORK);
        KNOWN_TECH.put("Spring", TechnologyCategory.FRAMEWORK);
        KNOWN_TECH.put("React", TechnologyCategory.FRAMEWORK);
        KNOWN_TECH.put("Angular", TechnologyCategory.FRAMEWORK);
        KNOWN_TECH.put("Vue", TechnologyCategory.FRAMEWORK);
        KNOWN_TECH.put("Node.js", TechnologyCategory.FRAMEWORK);
        KNOWN_TECH.put("Express", TechnologyCategory.FRAMEWORK);
        KNOWN_TECH.put("Django", TechnologyCategory.FRAMEWORK);

        // Databases
        KNOWN_TECH.put("PostgreSQL", TechnologyCategory.DATABASE);
        KNOWN_TECH.put("MySQL", TechnologyCategory.DATABASE);
        KNOWN_TECH.put("MongoDB", TechnologyCategory.DATABASE);
        KNOWN_TECH.put("Redis", TechnologyCategory.DATABASE);
        KNOWN_TECH.put("Elasticsearch", TechnologyCategory.DATABASE);
        KNOWN_TECH.put("Oracle", TechnologyCategory.DATABASE);

        // Cloud & DevOps
        KNOWN_TECH.put("AWS", TechnologyCategory.CLOUD);
        KNOWN_TECH.put("Azure", TechnologyCategory.CLOUD);
        KNOWN_TECH.put("GCP", TechnologyCategory.CLOUD);
        KNOWN_TECH.put("Docker", TechnologyCategory.DEVOPS);
        KNOWN_TECH.put("Kubernetes", TechnologyCategory.DEVOPS);
        KNOWN_TECH.put("Terraform", TechnologyCategory.DEVOPS);
        KNOWN_TECH.put("CI/CD", TechnologyCategory.DEVOPS);
        KNOWN_TECH.put("Git", TechnologyCategory.DEVOPS);
        KNOWN_TECH.put("Linux", TechnologyCategory.DEVOPS);

        // Messaging
        KNOWN_TECH.put("Kafka", TechnologyCategory.MESSAGING);
        KNOWN_TECH.put("RabbitMQ", TechnologyCategory.MESSAGING);

        // Testing
        KNOWN_TECH.put("JUnit", TechnologyCategory.TESTING);
        KNOWN_TECH.put("Mockito", TechnologyCategory.TESTING);
        KNOWN_TECH.put("Jest", TechnologyCategory.TESTING);
        KNOWN_TECH.put("Cypress", TechnologyCategory.TESTING);
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public String getDefaultModel() {
        return MODEL_NAME;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public AIResumeAnalysisResponse analyzeResumeFit(AIResumeAnalysisRequest request) {
        String jobTitle = request.jobTitle() != null ? request.jobTitle() : "Software Engineer";
        String company = request.jobCompany() != null ? request.jobCompany() : "Target Company";
        String resumeTitle = request.resumeTitle() != null ? request.resumeTitle() : "Candidate";

        StringBuilder assessment = new StringBuilder();
        assessment.append("Resume '").append(resumeTitle)
                  .append("' demonstrates relevant background for the role of ")
                  .append(jobTitle).append(" at ").append(company).append(". ");

        List<String> resumeSkillsLower = request.resumeSkills().stream()
            .map(s -> s.toLowerCase(Locale.ROOT))
            .toList();

        List<String> candidateVerifiedLower = request.candidateVerifiedSkills().stream()
            .map(s -> s.toLowerCase(Locale.ROOT))
            .toList();

        List<String> suggestions = new ArrayList<>();
        List<String> alignment = new ArrayList<>();

        if (!request.resumeExperienceSummaries().isEmpty()) {
            alignment.add("Work history demonstrates direct production experience relevant to core engineering duties.");
        }
        if (!request.resumeProjectSummaries().isEmpty()) {
            alignment.add("Highlighted projects provide hands-on implementation evidence for system architecture.");
        }

        // Anti-hallucination check on missing skills
        for (String reqSkill : request.jobRequiredSkills()) {
            String reqLower = reqSkill.toLowerCase(Locale.ROOT);
            boolean onResume = resumeSkillsLower.stream().anyMatch(s -> s.contains(reqLower) || reqLower.contains(s));
            if (!onResume) {
                boolean isVerified = candidateVerifiedLower.stream().anyMatch(s -> s.contains(reqLower) || reqLower.contains(s));
                if (isVerified) {
                    suggestions.add("Candidate has verified expertise in '" + reqSkill +
                        "' which is required by " + company + " but currently omitted from this resume version. Recommend highlighting it.");
                } else {
                    suggestions.add("Required technology '" + reqSkill +
                        "' is not represented on the resume. [NOT_ENOUGH_EVIDENCE] Candidate has no verified proof in skill profile; do not fabricate experience.");
                }
            }
        }

        if (suggestions.isEmpty()) {
            suggestions.add("Resume covers all required technical competencies. Focus on quantifying business impact metrics in bullet points.");
        }

        return new AIResumeAnalysisResponse(
            assessment.toString().trim(),
            alignment,
            suggestions,
            new BigDecimal("0.92")
        );
    }

    @Override
    public AITailoringResponse generateTailoringSuggestions(AITailoringRequest request) {
        String jobTitle = request.jobTitle() != null && !request.jobTitle().isBlank() ? request.jobTitle() : "Software Engineer";
        String company = request.jobCompanyName() != null && !request.jobCompanyName().isBlank() ? request.jobCompanyName() : "Target Company";
        String resumeTitle = request.resumeTitle() != null && !request.resumeTitle().isBlank() ? request.resumeTitle() : "Software Engineer";
        String tailoredTitle = jobTitle;

        List<String> verifiedLower = request.candidateVerifiedSkills() != null
            ? request.candidateVerifiedSkills().stream().map(s -> s.toLowerCase(Locale.ROOT)).toList()
            : List.of();

        List<String> matchedVerifiedSkills = new ArrayList<>();
        List<String> missingUnverifiedSkills = new ArrayList<>();

        if (request.requiredJobSkills() != null) {
            for (String req : request.requiredJobSkills()) {
                String reqLower = req.toLowerCase(Locale.ROOT);
                if (verifiedLower.stream().anyMatch(v -> v.contains(reqLower) || reqLower.contains(v))) {
                    matchedVerifiedSkills.add(req);
                } else {
                    missingUnverifiedSkills.add(req);
                }
            }
        }

        // 1. Build Tailored Summary
        StringBuilder summaryBuilder = new StringBuilder();
        summaryBuilder.append(jobTitle).append(" with proven background in building scalable distributed systems");
        if (!matchedVerifiedSkills.isEmpty()) {
            summaryBuilder.append(" specializing in ").append(String.join(", ", matchedVerifiedSkills));
        }
        summaryBuilder.append(". Tailored for ").append(company).append(".");
        String tailoredSummary = summaryBuilder.toString();

        List<AISuggestionItem> suggestions = new ArrayList<>();

        // Summary Suggestion
        suggestions.add(new AISuggestionItem(
            "SUMMARY",
            "Professional Summary",
            request.resumeSummary() != null ? request.resumeSummary() : "",
            tailoredSummary,
            "Aligns executive summary directly with target position of " + jobTitle + " at " + company + " highlighting core verified competencies.",
            matchedVerifiedSkills.isEmpty() ? "Candidate verified background" : "Verified skills: " + String.join(", ", matchedVerifiedSkills),
            "VERIFIED"
        ));

        // Skills Suggestions
        if (!matchedVerifiedSkills.isEmpty()) {
            suggestions.add(new AISuggestionItem(
                "SKILLS",
                "Technical Skills Alignment",
                "Existing skills order",
                "Prioritize " + String.join(", ", matchedVerifiedSkills) + " in primary skills section.",
                "Target job explicitly requires these competencies and candidate has proven verified proficiency.",
                "Verified Candidate Skills: " + String.join(", ", matchedVerifiedSkills),
                "VERIFIED"
            ));
        }

        // Anti-hallucination warning for unverified skills
        for (String unverified : missingUnverifiedSkills) {
            suggestions.add(new AISuggestionItem(
                "SKILLS",
                "Job Requirement Gap: " + unverified,
                "Not present",
                "[NOT_ENOUGH_EVIDENCE] Job requires '" + unverified + "'. Candidate lacks verified evidence; do NOT fabricate experience. Prepare as an interview discussion topic or self-study.",
                "Target job lists '" + unverified + "' but candidate has no verified proof.",
                "None (Unverified)",
                "NOT_ENOUGH_EVIDENCE"
            ));
        }

        // Experience Suggestions
        if (request.experiences() != null && !request.experiences().isEmpty()) {
            String firstExp = request.experiences().get(0);
            suggestions.add(new AISuggestionItem(
                "EXPERIENCE",
                "Recent Professional Experience",
                firstExp,
                "Emphasize scalable architecture and production outcomes aligned with " + company + "'s technical stack.",
                "Highlights high-impact engineering accomplishments relevant to target role.",
                "Work history in candidate master resume",
                "VERIFIED"
            ));
        }

        // Project Suggestions
        if (request.projects() != null && !request.projects().isEmpty()) {
            String firstProj = request.projects().get(0);
            suggestions.add(new AISuggestionItem(
                "PROJECT",
                "Key Engineering Projects",
                firstProj,
                "Highlight architectural decisions, concurrency handling, and technical trade-offs relevant to " + jobTitle + ".",
                "Demonstrates practical hands-on system building capability.",
                "Project records in candidate master resume",
                "VERIFIED"
            ));
        }

        return new AITailoringResponse(
            tailoredTitle,
            tailoredSummary,
            suggestions,
            new BigDecimal("0.95")
        );
    }

    @Override
    public AIJobAnalysisResponse analyzeJob(AIJobAnalysisRequest request) {
        String description = request.rawDescription() != null ? request.rawDescription() : "";
        String title = request.title() != null ? request.title() : "";
        String descLower = description.toLowerCase(Locale.ROOT);
        String titleLower = title.toLowerCase(Locale.ROOT);

        // 1. Normalized Title
        String normalizedTitle = normalizeTitle(title);

        // 2. Seniority Level
        String seniority = detectSeniority(titleLower, descLower);

        // 3. Technologies
        List<AnalyzedTechnology> technologies = extractTechnologies(description);

        // 4. Core & Inferred Responsibilities
        List<String> coreResponsibilities = extractCoreResponsibilities(description, title);
        List<String> inferredResponsibilities = List.of(
            "Collaborate cross-functionally with product managers and engineers",
            "Participate in code reviews and architectural planning sessions",
            "Maintain high test coverage and production observability"
        );

        // 5. Requirements (Education & Certifications)
        List<String> education = extractEducation(description);
        List<String> certifications = extractCertifications(description);

        // 6. Experience Expectations
        String experienceExpectations = extractExperience(description, seniority);

        // 7. Potential Red Flags
        List<String> redFlags = extractRedFlags(descLower);

        // 8. Confidence
        BigDecimal confidence = technologies.isEmpty() ? new BigDecimal("0.70") : new BigDecimal("0.92");

        String promptVer = (request.promptVersion() != null && !request.promptVersion().isBlank())
            ? request.promptVersion() : PROMPT_VERSION;

        return new AIJobAnalysisResponse(
            normalizedTitle,
            seniority,
            coreResponsibilities,
            inferredResponsibilities,
            technologies,
            education,
            certifications,
            experienceExpectations,
            redFlags,
            confidence,
            PROVIDER_NAME,
            MODEL_NAME,
            promptVer
        );
    }

    private String normalizeTitle(String rawTitle) {
        if (rawTitle == null || rawTitle.isBlank()) return "Software Engineer";
        return rawTitle.replaceAll("(?i)\\b(sr\\.?|senior)\\b", "Senior")
                       .replaceAll("(?i)\\b(jr\\.?|junior)\\b", "Junior")
                       .replaceAll("(?i)\\b(lead)\\b", "Lead")
                       .replaceAll("(?i)\\b(dev|coder)\\b", "Developer")
                       .trim();
    }

    private String detectSeniority(String titleLower, String descLower) {
        if (titleLower.contains("lead") || descLower.contains("technical lead")) return "LEAD";
        if (titleLower.contains("principal") || descLower.contains("principal engineer")) return "PRINCIPAL";
        if (titleLower.contains("staff") || descLower.contains("staff engineer")) return "STAFF";
        if (titleLower.contains("senior") || titleLower.contains("sr.") || descLower.contains("senior engineer")) return "SENIOR";
        if (titleLower.contains("junior") || titleLower.contains("jr.") || titleLower.contains("entry") || titleLower.contains("associate")) return "JUNIOR";
        if (titleLower.contains("intern") || descLower.contains("internship")) return "INTERN";
        return "MID";
    }

    private List<AnalyzedTechnology> extractTechnologies(String description) {
        Map<String, AnalyzedTechnology> found = new LinkedHashMap<>();
        String descLower = description.toLowerCase(Locale.ROOT);

        for (Map.Entry<String, TechnologyCategory> entry : KNOWN_TECH.entrySet()) {
            String tech = entry.getKey();
            Pattern p = Pattern.compile("\\b" + Pattern.quote(tech.toLowerCase(Locale.ROOT)) + "\\b");
            if (p.matcher(descLower).find()) {
                boolean isReq = isRequiredContext(descLower, tech.toLowerCase(Locale.ROOT));
                found.putIfAbsent(tech.toLowerCase(Locale.ROOT), new AnalyzedTechnology(tech, entry.getValue(), isReq));
            }
        }

        return new ArrayList<>(found.values());
    }

    private boolean isRequiredContext(String text, String term) {
        int idx = text.indexOf(term);
        if (idx == -1) return true;
        int start = Math.max(0, idx - 80);
        int end = Math.min(text.length(), idx + term.length() + 80);
        String context = text.substring(start, end);
        if (context.contains("preferred") || context.contains("nice to have") || context.contains("bonus") || context.contains("plus")) {
            return false;
        }
        return true;
    }

    private List<String> extractCoreResponsibilities(String description, String title) {
        List<String> responsibilities = new ArrayList<>();
        Pattern bulletPattern = Pattern.compile("(?m)^[\\s*•\\-]+\\s*([A-Z][^\\n\\.]{15,180}\\.?)$");
        Matcher matcher = bulletPattern.matcher(description);
        while (matcher.find() && responsibilities.size() < 6) {
            responsibilities.add(matcher.group(1).trim());
        }

        if (responsibilities.isEmpty()) {
            responsibilities.add("Design, develop, and maintain robust backend microservices and APIs");
            responsibilities.add("Write clean, well-tested, and maintainable production code");
            responsibilities.add("Optimize system performance, reliability, and scalability");
        }
        return responsibilities;
    }

    private List<String> extractEducation(String description) {
        List<String> education = new ArrayList<>();
        String descLower = description.toLowerCase(Locale.ROOT);
        if (descLower.contains("bachelor") || descLower.contains("b.s.") || descLower.contains("degree in computer science")) {
            education.add("Bachelor's degree in Computer Science, Engineering, or related technical field (or equivalent practical experience)");
        }
        if (descLower.contains("master") || descLower.contains("m.s.")) {
            education.add("Master's degree in Computer Science or related field preferred");
        }
        return education;
    }

    private List<String> extractCertifications(String description) {
        List<String> certs = new ArrayList<>();
        String descLower = description.toLowerCase(Locale.ROOT);
        if (descLower.contains("aws certified")) certs.add("AWS Certified Solutions Architect or Developer");
        if (descLower.contains("ckad") || descLower.contains("cka")) certs.add("Certified Kubernetes Administrator / Application Developer");
        return certs;
    }

    private String extractExperience(String description, String seniority) {
        Pattern expPattern = Pattern.compile("(\\d+\\+?\\s*(?:to|-)?\\s*\\d*\\s*(?:years|yrs))", Pattern.CASE_INSENSITIVE);
        Matcher m = expPattern.matcher(description);
        if (m.find()) {
            return m.group(1) + " of software engineering experience";
        }
        return switch (seniority) {
            case "LEAD", "PRINCIPAL", "STAFF" -> "7+ years of experience";
            case "SENIOR" -> "5+ years of experience";
            case "JUNIOR", "INTERN" -> "0-2 years of experience";
            default -> "3-5 years of experience";
        };
    }

    private List<String> extractRedFlags(String descLower) {
        List<String> flags = new ArrayList<>();
        if (descLower.contains("rockstar") || descLower.contains("ninja") || descLower.contains("guru")) {
            flags.add("Unrealistic cultural buzzwords ('rockstar' / 'ninja') detected");
        }
        if (descLower.contains("fast-paced high stress") || descLower.contains("wear many hats") || descLower.contains("unlimited overtime")) {
            flags.add("Potential work-life balance risk: excessive workload implied");
        }
        if (descLower.contains("competitive salary") && !descLower.contains("$") && !descLower.contains("usd") && !descLower.contains("eur") && !descLower.contains("inr")) {
            flags.add("Salary transparency missing: 'competitive salary' without compensation range");
        }
        return flags;
    }

    @Override
    public AIEmailClassificationResponse classifyEmail(AIEmailClassificationRequest request) {
        String subject = request.subject() != null ? request.subject().toLowerCase(Locale.ROOT) : "";
        String body = request.bodyPlain() != null ? request.bodyPlain().toLowerCase(Locale.ROOT) : "";
        String text = subject + " " + body;

        if (text.contains("interview") || text.contains("technical screen") || text.contains("round 1") || text.contains("phone screen") || text.contains("chat with our team")) {
            return new AIEmailClassificationResponse("INTERVIEW_INVITATION", new BigDecimal("0.950"), "Matched direct interview scheduling invitation in communication.");
        }
        if (text.contains("online assessment") || text.contains("hackerrank") || text.contains("codility") || text.contains("codesignal") || text.contains("coding challenge") || text.contains("technical assessment")) {
            return new AIEmailClassificationResponse("ASSESSMENT", new BigDecimal("0.950"), "Matched online technical coding challenge or assessment request.");
        }
        if (text.contains("thank you for applying") || text.contains("application received") || text.contains("we received your application") || text.contains("application confirmation")) {
            return new AIEmailClassificationResponse("APPLICATION_CONFIRMATION", new BigDecimal("0.920"), "Matched official job application receipt confirmation.");
        }
        if (text.contains("offer letter") || text.contains("pleased to offer") || text.contains("formal offer") || text.contains("congratulations on your offer")) {
            return new AIEmailClassificationResponse("OFFER", new BigDecimal("0.980"), "Matched formal job offer letter / package proposal.");
        }
        if (text.contains("unfortunately") || text.contains("not moving forward") || text.contains("pursue other candidates") || text.contains("decided to move forward with other")) {
            return new AIEmailClassificationResponse("REJECTION", new BigDecimal("0.940"), "Matched candidate notification of non-selection.");
        }
        if (text.contains("reaching out") || text.contains("saw your profile") || text.contains("talent acquisition") || text.contains("recruiter at") || text.contains("exciting opportunity")) {
            return new AIEmailClassificationResponse("NETWORKING_OUTREACH", new BigDecimal("0.880"), "Matched direct recruiter or sourcer outbound inquiry.");
        }
        if (text.contains("status update") || text.contains("update on your application") || text.contains("application status")) {
            return new AIEmailClassificationResponse("STATUS_UPDATE", new BigDecimal("0.850"), "Matched application progress or status update.");
        }
        if (text.contains("unsubscribe") || text.contains("newsletter") || text.contains("promotional") || text.contains("sale") || text.contains("webinar")) {
            return new AIEmailClassificationResponse("SPAM_OR_IRRELEVANT", new BigDecimal("0.910"), "Detected non-job promotional or marketing communication.");
        }

        return new AIEmailClassificationResponse("OTHER", new BigDecimal("0.500"), "General career or corporate correspondence.");
    }

    @Override
    public AIEmailJobExtractionResponse extractJobFromEmail(AIEmailJobExtractionRequest request) {
        String subject = request.subject() != null ? request.subject() : "";
        String body = request.bodyPlain() != null ? request.bodyPlain() : "";
        String sender = request.sender() != null ? request.sender() : "";

        // Extract company
        String company = "Unknown Company";
        Matcher atMatcher = Pattern.compile("(?:at|with)\\s+([A-Z][a-zA-Z0-9&\\s]{2,30})", Pattern.CASE_INSENSITIVE).matcher(subject + " " + body);
        if (atMatcher.find()) {
            company = atMatcher.group(1).trim();
        } else if (sender.contains("@")) {
            String domain = sender.substring(sender.indexOf('@') + 1);
            if (domain.contains(".")) {
                String domainBase = domain.substring(0, domain.indexOf('.'));
                if (!domainBase.equalsIgnoreCase("gmail") && !domainBase.equalsIgnoreCase("yahoo") && !domainBase.equalsIgnoreCase("outlook")) {
                    company = Character.toUpperCase(domainBase.charAt(0)) + domainBase.substring(1).toLowerCase(Locale.ROOT);
                }
            }
        }

        // Extract job title
        String jobTitle = "Software Engineer";
        Pattern titlePattern = Pattern.compile("((?:Senior|Lead|Staff|Principal|Junior)?\\s*(?:Software Engineer|Backend Engineer|Frontend Engineer|Full Stack Engineer|DevOps Engineer|Data Engineer|Cloud Architect|Product Manager))", Pattern.CASE_INSENSITIVE);
        Matcher titleMatcher = titlePattern.matcher(subject + " " + body);
        if (titleMatcher.find()) {
            jobTitle = titleMatcher.group(1).trim();
        }

        // Extract requisition / external ID
        String externalJobId = null;
        Matcher reqMatcher = Pattern.compile("(?:Req(?:uisition)?|Job ID|Ref(?:erence)?)\\s*[:#]?\\s*([A-Z0-9-]{4,20})", Pattern.CASE_INSENSITIVE).matcher(subject + " " + body);
        if (reqMatcher.find()) {
            externalJobId = reqMatcher.group(1).trim();
        }

        String nextSteps = "Follow up with recruiter / Review application timeline.";
        String lowerSubject = subject.toLowerCase(Locale.ROOT);
        if (lowerSubject.contains("interview")) {
            nextSteps = "Prepare system design, algorithmic problems, and review project contributions.";
        } else if (lowerSubject.contains("assessment") || lowerSubject.contains("hackerrank")) {
            nextSteps = "Complete coding assessment within prescribed timeline window.";
        }

        String notes = "Extracted from email: \"" + subject + "\" from " + sender;

        return new AIEmailJobExtractionResponse(company, jobTitle, externalJobId, nextSteps, notes);
    }
}

