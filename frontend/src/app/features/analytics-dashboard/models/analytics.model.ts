export type ApplicationSource =
  | 'COMPANY_WEBSITE'
  | 'LINKEDIN'
  | 'INDEED'
  | 'EMAIL'
  | 'REFERRAL'
  | 'RECRUITER'
  | 'MANUAL'
  | 'OTHER';

export interface AnalyticsOverview {
  totalApplications: number;
  activePipelines: number;
  interviewsCount: number;
  assessmentsCount: number;
  activeOffers: number;
  rejectionsCount: number;
  interviewConversionRatePercent: number;
  assessmentPassRatePercent: number;
  offerRatePercent: number;
}

export interface StageConversionRate {
  fromStage: string;
  toStage: string;
  enteredCount: number;
  progressedCount: number;
  conversionRatePercent: number;
  medianDaysInStage: number | null;
}

export interface FunnelMetrics {
  totalApplications: number;
  activeApplications: number;
  draftsCount: number;
  appliedCount: number;
  screeningCount: number;
  assessmentCount: number;
  interviewCount: number;
  offerCount: number;
  acceptedCount: number;
  rejectedCount: number;
  withdrawnCount: number;
  archivedCount: number;
  screeningConversionRate: number;
  assessmentConversionRate: number;
  interviewConversionRate: number;
  offerConversionRate: number;
  overallAcceptanceRate: number;
  stageConversions: StageConversionRate[];
}

export interface SourceEffectiveness {
  source: ApplicationSource;
  totalApplications: number;
  screeningsReached: number;
  interviewsReached: number;
  offersReceived: number;
  screeningRatePercent: number;
  interviewRatePercent: number;
  offerRatePercent: number;
}

export interface SkillGapMetric {
  skillId: string;
  skillName: string;
  category: string;
  requiredJobCount: number;
  totalTargetJobs: number;
  marketDemandPercent: number;
  candidateVerified: boolean;
  candidateProficiency: string | null;
}

export interface AdvisorBottleneck {
  category: string;
  title: string;
  description: string;
  supportingMetrics: Record<string, string>;
  severity: 'HIGH' | 'MEDIUM' | 'LOW' | string;
  recommendedActions: string[];
}

export interface AdvisorRecommendation {
  action: string;
  rationale: string;
  priority: 'HIGH' | 'MEDIUM' | 'LOW' | string;
  metricEvidence: string;
}

export interface AIAnalyticsAdvisorResponse {
  summary: string;
  bottlenecks: AdvisorBottleneck[];
  recommendations: AdvisorRecommendation[];
  strengths: string[];
  dataLimitations: string[];
  confidenceScore: number | null;
  generatedAt: string;
}

export interface AnalyticsDashboardState {
  overview: AnalyticsOverview | null;
  funnel: FunnelMetrics | null;
  sources: SourceEffectiveness[];
  skills: SkillGapMetric[];
  insights: AIAnalyticsAdvisorResponse | null;
  loadingData: boolean;
  loadingInsights: boolean;
  dataError: string | null;
  insightsError: string | null;
}
