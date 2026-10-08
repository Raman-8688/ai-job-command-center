package com.jobcommandcenter.job.domain;

public record JobSearchCriteria(
    String query,
    JobStatus status,
    JobSource source,
    WorkMode workMode,
    String location,
    String companyName
) {
    public static JobSearchCriteria empty() {
        return new JobSearchCriteria(null, null, null, null, null, null);
    }
}
