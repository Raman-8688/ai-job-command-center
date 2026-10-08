export type TailoredResumeStatus = 'DRAFT' | 'UNDER_REVIEW' | 'APPROVED' | 'REJECTED';

export type SectionType = 'SUMMARY' | 'EXPERIENCE' | 'PROJECT' | 'SKILLS';

export interface TailoredResumeSuggestion {
  id: string;
  sectionType: SectionType;
  targetItemTitle?: string;
  originalContent?: string;
  suggestedContent: string;
  rationale: string;
  evidence?: string;
  verificationStatus: string;
  applied: boolean;
  displayOrder: number;
  createdAt: string;
}

export interface TailoredResume {
  id: string;
  userId: string;
  sourceResumeId: string;
  targetJobId: string;
  version: number;
  status: TailoredResumeStatus;
  tailoredTitle?: string;
  tailoredSummary?: string;
  keywordCoverageScore: number;
  matchedKeywords: string[];
  missingKeywords: string[];
  suggestions: TailoredResumeSuggestion[];
  createdAt: string;
  updatedAt: string;
}

export interface TailoredResumeSummary {
  id: string;
  sourceResumeId: string;
  targetJobId: string;
  version: number;
  status: TailoredResumeStatus;
  tailoredTitle?: string;
  keywordCoverageScore: number;
  createdAt: string;
  updatedAt: string;
}
