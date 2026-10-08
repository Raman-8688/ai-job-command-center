package com.jobcommandcenter.job.domain;

/**
 * Defines whether a required skill is mandatory or nice-to-have.
 */
public enum SkillRequirementType {
    /**
     * Non-negotiable core qualification.
     */
    REQUIRED,

    /**
     * Preferred bonus skill or nice-to-have qualification.
     */
    PREFERRED
}
