export type ApplicationStatus =
  | 'DRAFT'
  | 'APPLIED'
  | 'SCREENING'
  | 'ASSESSMENT'
  | 'INTERVIEW'
  | 'OFFER'
  | 'ACCEPTED'
  | 'REJECTED'
  | 'WITHDRAWN'
  | 'ARCHIVED';

export type ApplicationSource =
  | 'COMPANY_WEBSITE'
  | 'LINKEDIN'
  | 'INDEED'
  | 'EMAIL'
  | 'REFERRAL'
  | 'RECRUITER'
  | 'MANUAL'
  | 'OTHER';

export type ApplicationEventType =
  | 'CREATED'
  | 'STATUS_CHANGED'
  | 'NOTE_ADDED'
  | 'RESUME_LINKED'
  | 'EMAIL_ASSOCIATED'
  | 'FOLLOW_UP_SCHEDULED'
  | 'REOPENED';

export type EventSource = 'USER' | 'SYSTEM' | 'AI' | 'EMAIL_SYNC';

export interface JobApplicationEvent {
  id: string;
  applicationId: string;
  previousStatus?: ApplicationStatus;
  newStatus: ApplicationStatus;
  eventType: ApplicationEventType;
  notes?: string;
  source: EventSource;
  occurredAt: string;
}

export interface JobApplication {
  id: string;
  userId: string;
  jobId: string;
  jobTitle?: string;
  companyName?: string;
  jobLocation?: string;
  resumeId?: string;
  tailoredResumeId?: string;
  status: ApplicationStatus;
  appliedAt?: string;
  submissionSource: ApplicationSource;
  externalReference?: string;
  nextFollowUpDate?: string;
  notes?: string;
  createdAt: string;
  updatedAt: string;
  version: number;
  events: JobApplicationEvent[];
}

export interface JobApplicationSummary {
  id: string;
  jobId: string;
  jobTitle: string;
  companyName: string;
  jobLocation?: string;
  status: ApplicationStatus;
  submissionSource: ApplicationSource;
  appliedAt?: string;
  nextFollowUpDate?: string;
  updatedAt: string;
  hasResumeLinked: boolean;
  hasTailoredResumeLinked: boolean;
}

export interface ApplicationPageResponse {
  content: JobApplicationSummary[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface ApplicationDashboardSummary {
  countsByStatus: Record<ApplicationStatus, number>;
  activeApplicationsCount: number;
  followUpsDueCount: number;
  totalApplicationsCount: number;
}

export interface ApplicationGuidance {
  recommendedAction: string;
  rationale: string;
  draftedFollowUpMessage: string;
  modelUsed: string;
  generatedAt: string;
}

export interface CreateApplicationRequest {
  jobId: string;
  status?: ApplicationStatus;
  submissionSource?: ApplicationSource;
  appliedAt?: string;
  externalReference?: string;
  notes?: string;
  nextFollowUpDate?: string;
  resumeId?: string;
  tailoredResumeId?: string;
}

export interface TransitionStatusRequest {
  targetStatus: ApplicationStatus;
  notes?: string;
  eventSource?: EventSource;
  occurredAt?: string;
}

export interface LinkResumeRequest {
  resumeId?: string;
  tailoredResumeId?: string;
  notes?: string;
}
