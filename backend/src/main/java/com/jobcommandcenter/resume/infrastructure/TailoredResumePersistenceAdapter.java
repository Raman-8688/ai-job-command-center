package com.jobcommandcenter.resume.infrastructure;

import com.jobcommandcenter.resume.domain.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class TailoredResumePersistenceAdapter implements TailoredResumeRepository {

    private final SpringDataTailoredResumeRepository repository;

    public TailoredResumePersistenceAdapter(SpringDataTailoredResumeRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TailoredResume> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TailoredResume> findByIdAndUserId(UUID id, UUID userId) {
        return repository.findByIdAndUserId(id, userId).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TailoredResume> findByUserIdAndSourceResumeId(UUID userId, UUID sourceResumeId) {
        return repository.findByUserIdAndSourceResumeIdOrderByVersionDesc(userId, sourceResumeId)
            .stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TailoredResume> findByUserIdAndTargetJobId(UUID userId, UUID targetJobId) {
        return repository.findByUserIdAndTargetJobIdOrderByVersionDesc(userId, targetJobId)
            .stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public int findMaxVersion(UUID sourceResumeId, UUID targetJobId) {
        return repository.findMaxVersion(sourceResumeId, targetJobId);
    }

    @Override
    @Transactional
    public TailoredResume save(TailoredResume tailoredResume) {
        TailoredResumeJpaEntity entity = toEntity(tailoredResume);
        TailoredResumeJpaEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    private TailoredResume toDomain(TailoredResumeJpaEntity entity) {
        List<TailoredResumeSuggestion> suggestions = entity.getSuggestions().stream()
            .map(s -> new TailoredResumeSuggestion(
                s.getId(),
                s.getTailoredResumeId(),
                s.getSectionType(),
                s.getTargetItemTitle(),
                s.getOriginalContent(),
                s.getSuggestedContent(),
                s.getRationale(),
                s.getEvidence(),
                s.getVerificationStatus(),
                s.isApplied(),
                s.getDisplayOrder(),
                s.getCreatedAt()
            ))
            .collect(Collectors.toList());

        return new TailoredResume(
            entity.getId(),
            entity.getUserId(),
            entity.getSourceResumeId(),
            entity.getTargetJobId(),
            entity.getVersion(),
            entity.getStatus(),
            entity.getTailoredTitle(),
            entity.getTailoredSummary(),
            entity.getKeywordCoverageScore(),
            deserializeCommaSeparated(entity.getMatchedKeywords()),
            deserializeCommaSeparated(entity.getMissingKeywords()),
            suggestions,
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    private TailoredResumeJpaEntity toEntity(TailoredResume domain) {
        TailoredResumeJpaEntity entity = new TailoredResumeJpaEntity();
        entity.setId(domain.getId());
        entity.setUserId(domain.getUserId());
        entity.setSourceResumeId(domain.getSourceResumeId());
        entity.setTargetJobId(domain.getTargetJobId());
        entity.setVersion(domain.getVersion());
        entity.setStatus(domain.getStatus());
        entity.setTailoredTitle(domain.getTailoredTitle());
        entity.setTailoredSummary(domain.getTailoredSummary());
        entity.setKeywordCoverageScore(domain.getKeywordCoverageScore());
        entity.setMatchedKeywords(serializeCommaSeparated(domain.getMatchedKeywords()));
        entity.setMissingKeywords(serializeCommaSeparated(domain.getMissingKeywords()));
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());

        List<TailoredResumeSuggestionJpaEntity> suggestions = domain.getSuggestions().stream()
            .map(s -> {
                TailoredResumeSuggestionJpaEntity sj = new TailoredResumeSuggestionJpaEntity();
                sj.setId(s.getId());
                sj.setTailoredResumeId(domain.getId());
                sj.setSectionType(s.getSectionType());
                sj.setTargetItemTitle(s.getTargetItemTitle());
                sj.setOriginalContent(s.getOriginalContent());
                sj.setSuggestedContent(s.getSuggestedContent());
                sj.setRationale(s.getRationale());
                sj.setEvidence(s.getEvidence());
                sj.setVerificationStatus(s.getVerificationStatus());
                sj.setApplied(s.isApplied());
                sj.setDisplayOrder(s.getDisplayOrder());
                sj.setCreatedAt(s.getCreatedAt());
                return sj;
            })
            .collect(Collectors.toList());

        entity.setSuggestions(suggestions);
        return entity;
    }

    private String serializeCommaSeparated(List<String> items) {
        if (items == null || items.isEmpty()) return null;
        return String.join(",", items);
    }

    private List<String> deserializeCommaSeparated(String value) {
        if (value == null || value.isBlank()) return new ArrayList<>();
        return Arrays.stream(value.split(","))
            .map(String::trim)
            .filter(s -> !s.isBlank())
            .collect(Collectors.toList());
    }
}
