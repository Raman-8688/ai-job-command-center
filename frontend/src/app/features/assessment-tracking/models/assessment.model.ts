export type AssessmentPlatform =
  | 'HACKERRANK'
  | 'CODE_SIGNAL'
  | 'LEETCODE'
  | 'BYTEBOARD'
  | 'KARAT'
  | 'CODERPAD'
  | 'TALENT_ASSESSMENT'
  | 'CUSTOM';

export type AssessmentStatus =
  | 'INVITED'
  | 'IN_PROGRESS'
  | 'SUBMITTED'
  | 'EXPIRED'
  | 'ABANDONED';

export type AssessmentResult =
  | 'PENDING'
  | 'PASSED'
  | 'FAILED'
  | 'UNKNOWN';

export type AssessmentEventType =
  | 'INVITED'
  | 'STARTED'
  | 'SUBMITTED'
  | 'RESULT_RECORDED'
  | 'DEADLINE_EXTENDED'
  | 'EXPIRED'
  | 'ABANDONED'
  | 'NOTE_ADDED';

export interface AssessmentChecklistItem {
  id: string;
  assessmentId: string;
  topicCategory: string;
  title: string;
  description: string;
  isCompleted: boolean;
  sortOrder: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface OnlineAssessmentEvent {
  id: string;
  assessmentId: string;
  previousStatus?: AssessmentStatus;
  newStatus?: AssessmentStatus;
  eventType: AssessmentEventType;
  notes?: string;
  source?: string;
  occurredAt: string;
}

export interface OnlineAssessmentSummary {
  id: string;
  jobId: string;
  jobTitle?: string;
  companyName?: string;
  applicationId?: string;
  interviewId?: string;
  platform: AssessmentPlatform;
  title: string;
  status: AssessmentStatus;
  result: AssessmentResult;
  durationMinutes?: number;
  invitedAt?: string;
  expiresAt?: string;
  scheduledStartTime?: string;
  completedAt?: string;
  checklistCompletionPercentage: number;
  totalChecklistItems: number;
  version: number;
  createdAt: string;
  updatedAt: string;
}

export interface OnlineAssessment {
  id: string;
  userId: string;
  jobId: string;
  jobTitle?: string;
  companyName?: string;
  jobLocation?: string;
  applicationId?: string;
  interviewId?: string;
  platform: AssessmentPlatform;
  title: string;
  status: AssessmentStatus;
  result: AssessmentResult;
  durationMinutes?: number;
  invitedAt?: string;
  expiresAt?: string;
  scheduledStartTime?: string;
  completedAt?: string;
  score?: number;
  maxScore?: number;
  assessmentUrl?: string;
  accessCode?: string;
  submissionNotes?: string;
  submissionRepoUrl?: string;
  version: number;
  checklistCompletionPercentage: number;
  checklistTotalCount: number;
  checklistCompletedCount: number;
  checklist: AssessmentChecklistItem[];
  recentEvents: OnlineAssessmentEvent[];
  createdAt: string;
  updatedAt: string;
}

export interface AssessmentDashboardSummary {
  totalAssessments: number;
  invited: number;
  inProgress: number;
  submitted: number;
  expired: number;
  abandoned: number;
  dueSoon: number;
  overdue: number;
}

export interface AssessmentChecklistBundle {
  assessmentId: string;
  totalItems: number;
  completedItems: number;
  completionPercentage: number;
  items: AssessmentChecklistItem[];
}

export interface AssessmentBriefing {
  assessmentId: string;
  platformGuidance: string;
  timeManagementAdvice: string;
  prioritizedTopics: string[];
  totalChecklistItems: number;
  completedChecklistItems: number;
  completionPercentage: number;
  checklist: AssessmentChecklistItem[];
  confidenceScore?: number;
}

export interface CreateAssessmentRequest {
  jobId: string;
  applicationId?: string;
  interviewId?: string;
  platform: AssessmentPlatform;
  title: string;
  durationMinutes?: number;
  invitedAt?: string;
  expiresAt?: string;
  scheduledStartTime?: string;
  assessmentUrl?: string;
  accessCode?: string;
  submissionNotes?: string;
}

export interface UpdateAssessmentRequest {
  platform?: AssessmentPlatform;
  title?: string;
  assessmentUrl?: string;
  accessCode?: string;
  expiresAt?: string;
  scheduledStartTime?: string;
  durationMinutes?: number;
  submissionNotes?: string;
  submissionRepoUrl?: string;
  version?: number;
}

export interface StartAssessmentRequest {
  startTime?: string;
  notes?: string;
}

export interface SubmitAssessmentRequest {
  score?: number;
  maxScore?: number;
  submissionNotes?: string;
  submissionRepoUrl?: string;
  completedAt?: string;
}

export interface RecordAssessmentResultRequest {
  result: AssessmentResult;
  score?: number;
  maxScore?: number;
  notes?: string;
}

export interface ExtendAssessmentDeadlineRequest {
  newExpiresAt: string;
  reason?: string;
}

export interface AbandonAssessmentRequest {
  reason?: string;
}

export interface ExpireAssessmentRequest {
  reason?: string;
}

export interface ToggleChecklistItemRequest {
  isCompleted?: boolean;
}

export interface GenerateBriefingRequest {
  candidateNotes?: string;
}
