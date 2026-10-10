import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AnalyticsService } from './analytics.service';
import {
  AnalyticsOverview,
  FunnelMetrics,
  SourceEffectiveness,
  SkillGapMetric,
  AIAnalyticsAdvisorResponse
} from '../models/analytics.model';

describe('AnalyticsService', () => {
  let service: AnalyticsService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AnalyticsService]
    });
    service = TestBed.inject(AnalyticsService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch analytics overview', () => {
    const mockOverview: AnalyticsOverview = {
      totalApplications: 10,
      activePipelines: 4,
      interviewsCount: 3,
      assessmentsCount: 2,
      activeOffers: 1,
      rejectionsCount: 2,
      interviewConversionRatePercent: 30.0,
      assessmentPassRatePercent: 100.0,
      offerRatePercent: 10.0
    };

    service.getOverview().subscribe((res) => {
      expect(res).toEqual(mockOverview);
      expect(res.totalApplications).toBe(10);
    });

    const req = httpMock.expectOne('/api/analytics/overview');
    expect(req.request.method).toBe('GET');
    req.flush(mockOverview);
  });

  it('should fetch funnel metrics', () => {
    const mockFunnel: FunnelMetrics = {
      totalApplications: 10,
      activeApplications: 4,
      draftsCount: 0,
      appliedCount: 10,
      screeningCount: 4,
      assessmentCount: 2,
      interviewCount: 3,
      offerCount: 1,
      acceptedCount: 0,
      rejectedCount: 2,
      withdrawnCount: 0,
      archivedCount: 0,
      screeningConversionRate: 40.0,
      assessmentConversionRate: 20.0,
      interviewConversionRate: 30.0,
      offerConversionRate: 10.0,
      overallAcceptanceRate: 0.0,
      stageConversions: [
        {
          fromStage: 'APPLIED',
          toStage: 'SCREENING',
          enteredCount: 10,
          progressedCount: 4,
          conversionRatePercent: 40.0,
          medianDaysInStage: 3.5
        }
      ]
    };

    service.getFunnel().subscribe((res) => {
      expect(res.stageConversions.length).toBe(1);
      expect(res.stageConversions[0].fromStage).toBe('APPLIED');
    });

    const req = httpMock.expectOne('/api/analytics/funnel');
    expect(req.request.method).toBe('GET');
    req.flush(mockFunnel);
  });

  it('should fetch source effectiveness', () => {
    const mockSources: SourceEffectiveness[] = [
      {
        source: 'LINKEDIN',
        totalApplications: 5,
        screeningsReached: 2,
        interviewsReached: 1,
        offersReceived: 0,
        screeningRatePercent: 40.0,
        interviewRatePercent: 20.0,
        offerRatePercent: 0.0
      }
    ];

    service.getSources().subscribe((res) => {
      expect(res.length).toBe(1);
      expect(res[0].source).toBe('LINKEDIN');
    });

    const req = httpMock.expectOne('/api/analytics/sources');
    expect(req.request.method).toBe('GET');
    req.flush(mockSources);
  });

  it('should fetch skill gap metrics', () => {
    const mockSkills: SkillGapMetric[] = [
      {
        skillId: 's1',
        skillName: 'Kubernetes',
        category: 'DEVOPS',
        requiredJobCount: 4,
        totalTargetJobs: 5,
        marketDemandPercent: 80.0,
        candidateVerified: false,
        candidateProficiency: null
      }
    ];

    service.getSkills().subscribe((res) => {
      expect(res.length).toBe(1);
      expect(res[0].candidateVerified).toBeFalse();
    });

    const req = httpMock.expectOne('/api/analytics/skills');
    expect(req.request.method).toBe('GET');
    req.flush(mockSkills);
  });

  it('should generate grounded AI career insights', () => {
    const mockAdvisor: AIAnalyticsAdvisorResponse = {
      summary: 'Pipeline summary',
      bottlenecks: [],
      recommendations: [],
      strengths: ['Great technical background'],
      dataLimitations: [],
      confidenceScore: 0.95,
      generatedAt: '2026-10-10T10:00:00Z'
    };

    service.generateInsights().subscribe((res) => {
      expect(res.summary).toBe('Pipeline summary');
      expect(res.strengths.length).toBe(1);
    });

    const req = httpMock.expectOne('/api/analytics/insights');
    expect(req.request.method).toBe('POST');
    req.flush(mockAdvisor);
  });
});
