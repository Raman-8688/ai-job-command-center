export interface CompanyDossier {
  id: string;
  userId: string;
  jobId: string;
  companyName: string;
  companyTier: string;
  overview: string;
  engineeringScale: string;
  coreTechStack: string;
  engineeringCulture: string;
  architectureFocus: string;
  tailoredTalkingPoints: string;
  interviewerQuestions: string;
  provenanceSummary: string;
  confidenceScore?: number;
  createdAt: string;
  updatedAt: string;
}

export interface GenerateCompanyDossierRequest {
  rawCompanyResearch?: string;
}
