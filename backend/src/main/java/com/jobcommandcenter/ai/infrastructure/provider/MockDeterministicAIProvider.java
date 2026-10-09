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

    @Override
    public AIApplicationGuidanceResponse generateApplicationGuidance(AIApplicationGuidanceRequest request) {
        String company = request.companyName() != null ? request.companyName() : "the hiring team";
        String title = request.jobTitle() != null ? request.jobTitle() : "the position";
        String status = request.currentStatus() != null ? request.currentStatus().toUpperCase(Locale.ROOT) : "APPLIED";
        int days = request.daysSinceApplied();

        return switch (status) {
            case "APPLIED" -> {
                if (days >= 7) {
                    yield new AIApplicationGuidanceResponse(
                        "Send courteous status inquiry to talent acquisition team",
                        "Over " + days + " days have elapsed since submission without a formal response.",
                        "Dear " + company + " Recruiting Team,\n\nI hope this email finds you well. I submitted my application for the " +
                            title + " role at " + company + " " + days + " days ago. I remain very interested in the opportunity and would appreciate any updates on the search timeline.\n\nThank you,\nCandidate"
                    );
                } else {
                    yield new AIApplicationGuidanceResponse(
                        "Allow standard review window (5-7 business days)",
                        "Application was recently submitted (" + days + " days ago). Most enterprise ATS queues review within 1-2 weeks.",
                        ""
                    );
                }
            }
            case "ASSESSMENT" -> new AIApplicationGuidanceResponse(
                "Complete coding challenge within prescribed window and test edge cases",
                "Technical online assessment round active. Ensure prompt submission before stated deadline.",
                ""
            );
            case "INTERVIEW" -> new AIApplicationGuidanceResponse(
                "Prepare STAR behavioral outlines, architectural trade-offs, and send post-interview thank you",
                "Active interview evaluation stage.",
                "Dear Interview Team at " + company + ",\n\nThank you for the insightful conversation regarding the " + title + " role today. I enjoyed learning more about the team's engineering roadmap and look forward to the next steps.\n\nBest regards,\nCandidate"
            );
            case "OFFER" -> new AIApplicationGuidanceResponse(
                "Review compensation package, equity vesting, benefits, and decision deadline",
                "Offer phase reached. Analyze market alignment before final response.",
                ""
            );
            case "REJECTED" -> new AIApplicationGuidanceResponse(
                "Archive application and conduct skills gap review against canonical job description",
                "Requisition closed for this cycle.",
                ""
            );
            default -> new AIApplicationGuidanceResponse(
                "Review application notes and upcoming milestone dates",
                "Application is currently in " + status + " status.",
                ""
            );
        };
    }

    @Override
    public AIInterviewPrepResponse generateInterviewPrep(AIInterviewPrepRequest request) {
        String jobTitle = (request.jobTitle() != null && !request.jobTitle().isBlank()) ? request.jobTitle() : "Software Engineer";
        String company = (request.companyName() != null && !request.companyName().isBlank()) ? request.companyName() : "Target Company";
        String round = (request.round() != null && !request.round().isBlank()) ? request.round() : "TECHNICAL_SCREEN";

        List<String> skills = (request.candidateVerifiedSkills() != null && !request.candidateVerifiedSkills().isEmpty())
            ? request.candidateVerifiedSkills()
            : List.of("Java", "Spring Boot", "PostgreSQL", "System Architecture");

        String primarySkill = skills.get(0);
        String secondarySkill = skills.size() > 1 ? skills.get(1) : "Relational Databases";

        int readinessScore = Math.min(95, 80 + Math.min(15, skills.size() * 3));
        String strategy = String.format(
            "Targeting %s for %s position at %s. Focus on deep %s patterns, data consistency guarantees, and business impact.",
            round, jobTitle, company, primarySkill
        );

        List<AIPracticeQuestion> questions = new ArrayList<>();

        // 1. Technical Deep-Dive
        questions.add(new AIPracticeQuestion(
            "TECH",
            String.format("How do you design and optimize high-throughput services using %s and %s in a production environment?", primarySkill, secondarySkill),
            String.format("Discuss concurrency models, connection pooling, memory profiling, and query indexing strategies in %s.", primarySkill),
            String.format("Situation: Faced with high latency spikes under peak load.\nTask: Re-architect critical execution paths using %s.\nAction: Optimized queries, applied batching, and introduced cache layers.\nResult: Reduced p99 response times by 45%% while maintaining 99.99%% availability.", primarySkill),
            new BigDecimal("0.95")
        ));

        // 2. System Design & Scalability
        questions.add(new AIPracticeQuestion(
            "SYSTEM_DESIGN",
            String.format("How would you design a fault-tolerant notification and event processing pipeline for %s scale?", company),
            "Address event delivery semantics (at-least-once, idempotent consumers), message partitioning, backpressure handling, and dead-letter queue strategies.",
            String.format("Situation: Asynchronous ingestion service required scalable event fanout.\nTask: Design decoupled pipeline capable of 10,000 events/sec.\nAction: Implemented event streaming with durable message queues and idempotent consumer workers in %s.\nResult: Zero data loss during regional outages and linear scaling.", primarySkill),
            new BigDecimal("0.90")
        ));

        // 3. Behavioral & Culture
        questions.add(new AIPracticeQuestion(
            "BEHAVIORAL",
            String.format("Tell me about a time you encountered a critical production incident at work. How did you lead the triage and resolve it?"),
            "Demonstrate psychological safety, blameless post-mortem culture, structured root-cause analysis, and preventative monitoring guardrails.",
            String.format("Situation: Unexpected memory leak caused rolling restarts during business hours.\nTask: Triage issue swiftly to restore customer service SLA.\nAction: Isolated failing replicas, collected heap dumps, identified unclosed stream leak, and rolled back safely.\nResult: Restored stability in 18 minutes; introduced static analysis rules to prevent re-occurrence."),
            new BigDecimal("0.92")
        ));

        // 4. Leadership & Cross-Functional Collaboration
        questions.add(new AIPracticeQuestion(
            "LEADERSHIP",
            String.format("How do you handle disagreements on technical trade-offs with senior engineering peers or product stakeholders?"),
            "Show objective decision matrix evaluation, RFC documentation, listening with empathy, and committing once a direction is decided.",
            "Situation: Conflicting proposals between relational schema vs document store for a new service.\nTask: Drive consensus across 4 senior engineers without delaying sprint goals.\nAction: Built a comparative benchmark matrix measuring read/write patterns and hosted a focused RFC review.\nResult: Achieved unanimous alignment on PostgreSQL with JSONB; delivered on schedule.",
            new BigDecimal("0.88")
        ));

        return new AIInterviewPrepResponse(readinessScore, strategy, questions);
    }

    @Override
    public AIAssessmentBriefingResponse generateAssessmentBriefing(AIAssessmentBriefingRequest request) {
        String platform = (request.platform() != null && !request.platform().isBlank())
            ? request.platform().trim().toUpperCase(Locale.ROOT)
            : "OTHER";

        String platformGuidance = switch (platform) {
            case "HACKERRANK" -> "HackerRank assessment: Emphasizes standard stream input/output (BufferedReader / System.out), strict execution timeout limits (typically 2-4 seconds per test batch), and memory limits. Dry-run large scale inputs against off-by-one errors and integer overflow.";
            case "LEETCODE" -> "LeetCode assessment: Function signature-based evaluation with automated harness. Focus on optimal algorithmic Big-O boundaries, constraints up to 10^5, hash table lookups, and two-pointer or sliding window paradigms.";
            case "CODESIGNAL" -> "CodeSignal assessment: Structured 4-question format (Q1-Q2 warm-up, Q3 matrix/simulation, Q4 algorithmic optimization). Speed and clean first-time submissions are rewarded; penalty applies for failed test suite submissions.";
            case "KARAT" -> "Karat technical interview: Hybrid session combining rapid-fire architecture/debugging questions followed by 2 algorithmic problems. Prioritize running partial solutions and communicating edge-case assumptions aloud.";
            case "CODERPAD" -> "CoderPad collaborative environment: Live execution with full standard library support. Write modular methods, print debug statements cleanly, and communicate your thought process proactively.";
            case "TAKE_HOME" -> "Take-Home assignment: Production-grade architecture expectations. Implement modular layered separation, clean domain modeling, robust unit and integration tests, and include an architectural decision README.";
            case "TALENTLMS" -> "TalentLMS assessment: Multiple-choice technical fundamentals, syntax nuances, framework conventions, and systems concepts. Read questions carefully and verify edge-case behavioral semantics.";
            default -> "Standard technical assessment: Write clean, readable code with defensible time and space complexity. Validate inputs against boundary edge cases and handle null/empty states gracefully.";
        };

        int duration = (request.durationMinutes() != null && request.durationMinutes() > 0) ? request.durationMinutes() : 60;
        int planMins = Math.max(5, (int) Math.round(duration * 0.15));
        int implMins = (int) Math.round(duration * 0.65);
        int verifyMins = Math.max(5, duration - planMins - implMins);

        String timeManagementAdvice = String.format(
            "Pacing strategy for %d-minute assessment: Allocate %d minutes for reading requirements, analyzing constraints, and identifying edge cases. Dedicate %d minutes to modular implementation. Reserve final %d minutes strictly for dry-running hidden test cases, memory profiling, and complexity verification.",
            duration, planMins, implMins, verifyMins
        );

        List<String> prioritizedTopics = new ArrayList<>();
        prioritizedTopics.add("Algorithmic Problem Solving (Two Pointers, Sliding Window, DFS/BFS)");
        prioritizedTopics.add("Boundary and Edge Case Validation");
        if (request.candidateVerifiedSkills() != null && !request.candidateVerifiedSkills().isEmpty()) {
            prioritizedTopics.add("Standard Collections and Data Structures in " + request.candidateVerifiedSkills().get(0));
        } else {
            prioritizedTopics.add("Core Data Structures (Hash Maps, Heaps, Balanced Trees)");
        }
        if (request.canonicalJobRequirements() != null && !request.canonicalJobRequirements().isEmpty()) {
            prioritizedTopics.add("Role Requirements Alignment: " + request.canonicalJobRequirements().get(0));
        }

        List<AIAssessmentChecklistItem> checklistItems = new ArrayList<>();
        checklistItems.add(new AIAssessmentChecklistItem(
            "ENVIRONMENT",
            "Validate Platform Workspace and Keyboard Shortcuts",
            "Ensure browser compatibility, test terminal I/O, disable interfering extensions, and familiarize with available standard library versions.",
            1
        ));
        checklistItems.add(new AIAssessmentChecklistItem(
            "ALGORITHMS",
            "Review Core Algorithmic Paradigms",
            "Brush up on hash map lookup, sorting variants, binary search on answer spaces, and graph traversal algorithms.",
            2
        ));
        checklistItems.add(new AIAssessmentChecklistItem(
            "EDGE_CASES",
            "Construct Boundary Test Cases",
            "Prepare tests for zero, negative, empty string/array, integer overflow, and maximum constraint inputs before submitting.",
            3
        ));
        checklistItems.add(new AIAssessmentChecklistItem(
            "TIME_MANAGEMENT",
            "Establish Milestone Time Checks",
            String.format("Set a milestone checkpoint at %d minutes to ensure core logic is operational before attempting micro-optimizations.", implMins),
            4
        ));

        String langTopic = (request.candidateVerifiedSkills() != null && !request.candidateVerifiedSkills().isEmpty())
            ? "Review " + request.candidateVerifiedSkills().get(0) + " Standard Library APIs and Collections"
            : "Review Language Standard Library APIs and Built-in Collections";
        checklistItems.add(new AIAssessmentChecklistItem(
            "LANGUAGE_FUNDAMENTALS",
            langTopic,
            "Ensure fluency with built-in sorting comparators, deque/queue implementations, and string manipulation idioms.",
            5
        ));

        checklistItems.add(new AIAssessmentChecklistItem(
            "SUBMISSION",
            "Final Code Review and Solution Dry-Run",
            "Walk through code line-by-line against example 1 and example 2 test inputs before pressing final submit.",
            6
        ));

        BigDecimal confidence = new BigDecimal("0.92");
        return new AIAssessmentBriefingResponse(
            platformGuidance,
            timeManagementAdvice,
            prioritizedTopics,
            checklistItems,
            confidence
        );
    }

    @Override
    public AICompanyDossierResponse generateCompanyDossier(AICompanyDossierRequest request) {
        String company = (request.companyName() != null && !request.companyName().isBlank())
            ? request.companyName().trim()
            : "Target Enterprise";
        String role = (request.jobTitle() != null && !request.jobTitle().isBlank())
            ? request.jobTitle().trim()
            : "Software Engineer";

        String companyLower = company.toLowerCase(Locale.ROOT);
        String tier = "ENTERPRISE";
        if (companyLower.contains("google") || companyLower.contains("amazon") || companyLower.contains("meta")
            || companyLower.contains("apple") || companyLower.contains("netflix") || companyLower.contains("microsoft")) {
            tier = "TIER_1_TECH";
        } else if (companyLower.contains("startup") || companyLower.contains("labs") || companyLower.contains("ai")) {
            tier = "GROWTH_STARTUP";
        } else if (companyLower.contains("fintech") || companyLower.contains("bank") || companyLower.contains("capital")) {
            tier = "FINANCIAL_TECH";
        }

        String coreTechStack;
        if (request.canonicalJobRequirements() != null && !request.canonicalJobRequirements().isEmpty()) {
            coreTechStack = String.join(", ", request.canonicalJobRequirements());
        } else if (request.jobDescription() != null && !request.jobDescription().isBlank()) {
            coreTechStack = "Extracted from job posting: Enterprise backend and systems software stack";
        } else {
            coreTechStack = "No canonical technical requirements specified in the job posting.";
        }

        boolean hasResearch = request.rawCompanyResearch() != null && !request.rawCompanyResearch().isBlank();
        String overview;
        if (hasResearch) {
            overview = request.rawCompanyResearch().trim();
        } else {
            overview = String.format(
                "%s is recruiting for the %s position. General industry intelligence indicates an engineering organization focused on reliable, customer-facing software products. (Note: Company-specific internal telemetry was not supplied; overview is derived from public market profiles).",
                company, role
            );
        }

        String engineeringScale = String.format(
            "Expected engineering profile for %s (%s tier): Distributed multi-service architecture supporting resilient operations, automated CI/CD pipelines, and cloud-native infrastructure.",
            company, tier
        );

        String architectureFocus = String.format(
            "Primary architectural themes relevant to %s: Service boundary decoupling, data consistency guarantees across persistence tiers, and horizontal scalability under high concurrency.",
            role
        );

        String engineeringCulture = "Collaborative engineering with emphasis on automated test coverage, code reviews, observability, and iterative feature delivery.";

        // Ground tailored talking points strictly in verified skills and experiences
        StringBuilder talkingPoints = new StringBuilder();
        List<String> skills = request.candidateVerifiedSkills();
        List<String> experiences = request.candidateExperiences();
        if (skills != null && !skills.isEmpty()) {
            talkingPoints.append("Candidate demonstrates verified proficiency in ").append(String.join(", ", skills)).append(". ");
            talkingPoints.append("Highlight real-world engineering contributions leveraging ").append(skills.get(0))
                .append(" to solve production scalability and reliability challenges directly aligned with the ").append(role).append(" opening.");
            if (experiences != null && !experiences.isEmpty()) {
                talkingPoints.append(" Direct evidence from career experience: ").append(String.join("; ", experiences)).append(".");
            }
        } else if (experiences != null && !experiences.isEmpty()) {
            talkingPoints.append("Highlight career experience evidence: ").append(String.join("; ", experiences)).append(". ");
            talkingPoints.append("Frame engineering decisions in terms of business impact, team collaboration, and architectural trade-offs.");
        } else {
            talkingPoints.append("No candidate verified skills or prior experience entries provided. Formulate talking points highlighting core computer science fundamentals, design patterns, and independent software engineering projects.");
        }

        String interviewerQuestions =
            "1. What are the highest-priority architectural milestones scheduled for the " + role + " team over the next two quarters?\n" +
            "2. How does the engineering team balance technical debt remediation with product feature roadmap velocity?\n" +
            "3. What does the production release and deployment workflow look like, and how is on-call rotation structured?\n" +
            "4. What observability tooling and SLI/SLO metrics does the team rely on for monitoring service health?";

        int reqCount = request.canonicalJobRequirements() != null ? request.canonicalJobRequirements().size() : 0;
        int skillCount = skills != null ? skills.size() : 0;
        int expCount = experiences != null ? experiences.size() : 0;

        String provenanceSummary = String.format(
            "Grounded in canonical job posting requirements (%d specified) and candidate profile (%d verified skills, %d experiences). %s",
            reqCount,
            skillCount,
            expCount,
            hasResearch
                ? "Company context supplemented by verified source research."
                : "Company internal architecture reflects general industry tier patterns as proprietary company research was not supplied."
        );

        BigDecimal confidence = (reqCount > 0 && skillCount > 0)
            ? new BigDecimal("0.92")
            : new BigDecimal("0.80");

        return new AICompanyDossierResponse(
            company,
            tier,
            overview,
            engineeringScale,
            coreTechStack,
            engineeringCulture,
            architectureFocus,
            talkingPoints.toString(),
            interviewerQuestions,
            provenanceSummary,
            confidence
        );
    }
}


