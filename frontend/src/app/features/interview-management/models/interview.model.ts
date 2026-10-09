export type InterviewRound =
  | 'INITIAL_SCREEN'
  | 'TECHNICAL_SCREEN'
  | 'SYSTEM_DESIGN'
  | 'BEHAVIORAL_CULTURE'
  | 'HIRING_MANAGER'
  | 'FINAL_ROUND'
  | 'OTHER';

export type InterviewFormat =
  | 'VIDEO_CALL'
  | 'PHONE_SCREEN'
  | 'ON_SITE'
  | 'ONLINE_ASSESSMENT'
  | 'TAKE_HOME_REVIEW'
  | 'PANEL';

export type InterviewStatus =
  | 'SCHEDULED'
  | 'RESCHEDULED'
  | 'COMPLETED'
  | 'CANCELLED'
  | 'NO_SHOW';

export type InterviewOutcome =
  | 'PENDING'
  | 'PASSED'
  | 'REJECTED'
  | 'STRONG_HIRE'
  | 'HIRE'
  | 'NO_DECISION';

export type InterviewEventType =
  | 'SCHEDULED'
  | 'RESCHEDULED'
  | 'COMPLETED'
  | 'CANCELLED'
  | 'STATUS_CHANGED'
  | 'OUTCOME_UPDATED'
  | 'PREPARATION_GENERATED'
  | 'NOTE_ADDED';

export interface InterviewEvent {
  id: string;
  interviewId: string;
  previousStatus?: InterviewStatus;
  newStatus: InterviewStatus;
  eventType: InterviewEventType;
  notes?: string;
  source: string;
  occurredAt: string;
}

export interface InterviewPreparation {
  id: string;
  interviewId: string;
  topicCategory: 'TECH' | 'BEHAVIORAL' | 'SYSTEM_DESIGN' | 'LEADERSHIP' | string;
  question: string;
  talkingPoints?: string;
  suggestedAnswerStar?: string;
  userAnswerNotes?: string;
  confidenceScore?: number;
  isReviewed: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface Interview {
  id: string;
  userId: string;
  applicationId?: string;
  jobId: string;
  jobTitle: string;
  companyName: string;
  jobLocation?: string;
  round: InterviewRound;
  roundNumber: number;
  format: InterviewFormat;
  status: InterviewStatus;
  outcome: InterviewOutcome;
  scheduledStartTime: string;
  scheduledEndTime: string;
  timeZone: string;
  meetingLink?: string;
  location?: string;
  interviewerNames?: string;
  interviewerRoles?: string;
  notes?: string;
  candidateFeedback?: string;
  createdAt: string;
  updatedAt: string;
  version: number;
  events: InterviewEvent[];
  preparations: InterviewPreparation[];
}

export interface InterviewSummary {
  id: string;
  applicationId?: string;
  jobId: string;
  jobTitle: string;
  companyName: string;
  jobLocation?: string;
  round: InterviewRound;
  roundNumber: number;
  format: InterviewFormat;
  status: InterviewStatus;
  outcome: InterviewOutcome;
  scheduledStartTime: string;
  scheduledEndTime: string;
  timeZone: string;
  meetingLink?: string;
  location?: string;
  interviewerNames?: string;
  preparationCount: number;
}

export interface InterviewDashboardSummary {
  totalInterviews: number;
  upcomingInterviews: number;
  completedInterviews: number;
  countByRound: Record<InterviewRound, number>;
  countByStatus: Record<InterviewStatus, number>;
  nextUpcomingInterview?: InterviewSummary;
  overallReadinessScore: number;
}

export interface InterviewPrepBundle {
  interviewId: string;
  jobTitle: string;
  companyName: string;
  round: InterviewRound;
  readinessScore: number;
  strategySummary: string;
  questions: InterviewPreparation[];
}

export interface ScheduleInterviewRequest {
  jobId: string;
  applicationId?: string;
  round: InterviewRound;
  roundNumber?: number;
  format: InterviewFormat;
  scheduledStartTime: string;
  scheduledEndTime: string;
  timeZone?: string;
  meetingLink?: string;
  location?: string;
  interviewerNames?: string;
  interviewerRoles?: string;
  notes?: string;
}

export interface RescheduleInterviewRequest {
  scheduledStartTime: string;
  scheduledEndTime: string;
  timeZone?: string;
  reason?: string;
}

export interface UpdateInterviewStatusRequest {
  status: InterviewStatus;
  outcome?: InterviewOutcome;
  feedback?: string;
  notes?: string;
  source?: string;
}

export interface UpdatePrepNotesRequest {
  userAnswerNotes?: string;
  isReviewed?: boolean;
}

export interface SavePrepQuestionRequest {
  topicCategory: string;
  question: string;
  talkingPoints?: string;
  suggestedAnswerStar?: string;
}
