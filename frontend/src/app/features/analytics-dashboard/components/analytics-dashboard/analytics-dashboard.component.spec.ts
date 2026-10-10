import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { of, throwError } from 'rxjs';
import { AnalyticsDashboardComponent } from './analytics-dashboard.component';
import { AnalyticsService } from '../../services/analytics.service';
import {
  AnalyticsOverview,
  FunnelMetrics,
  SourceEffectiveness,
  SkillGapMetric,
  AIAnalyticsAdvisorResponse
} from '../../models/analytics.model';

describe('AnalyticsDashboardComponent', () => {
  let component: AnalyticsDashboardComponent;
  let fixture: ComponentFixture<AnalyticsDashboardComponent>;
  let analyticsService: jasmine.SpyObj<AnalyticsService>;

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
    stageConversions: []
  };

  const mockSources: SourceEffectiveness[] = [
    {
      source: 'LINKEDIN',
      totalApplications: 10,
      screeningsReached: 4,
      interviewsReached: 3,
      offersReceived: 1,
      screeningRatePercent: 40.0,
      interviewRatePercent: 30.0,
      offerRatePercent: 10.0
    }
  ];

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
    },
    {
      skillId: 's2',
      skillName: 'Java',
      category: 'LANGUAGE',
      requiredJobCount: 5,
      totalTargetJobs: 5,
      marketDemandPercent: 100.0,
      candidateVerified: true,
      candidateProficiency: 'EXPERT'
    }
  ];

  beforeEach(async () => {
    const spy = jasmine.createSpyObj('AnalyticsService', [
      'getOverview',
      'getFunnel',
      'getSources',
      'getSkills',
      'generateInsights'
    ]);

    await TestBed.configureTestingModule({
      imports: [AnalyticsDashboardComponent, HttpClientTestingModule],
      providers: [{ provide: AnalyticsService, useValue: spy }]
    }).compileComponents();

    analyticsService = TestBed.inject(AnalyticsService) as jasmine.SpyObj<AnalyticsService>;
  });

  beforeEach(() => {
    analyticsService.getOverview.and.returnValue(of(mockOverview));
    analyticsService.getFunnel.and.returnValue(of(mockFunnel));
    analyticsService.getSources.and.returnValue(of(mockSources));
    analyticsService.getSkills.and.returnValue(of(mockSkills));

    fixture = TestBed.createComponent(AnalyticsDashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create and load all analytics on init', () => {
    expect(component).toBeTruthy();
    expect(component.overview).toEqual(mockOverview);
    expect(component.funnel).toEqual(mockFunnel);
    expect(component.sources.length).toBe(1);
    expect(component.skills.length).toBe(2);
    expect(component.loadingData).toBeFalse();
  });

  it('should filter skills by verification status', () => {
    component.skillFilter = 'UNVERIFIED';
    expect(component.filteredSkills.length).toBe(1);
    expect(component.filteredSkills[0].skillName).toBe('Kubernetes');

    component.skillFilter = 'VERIFIED';
    expect(component.filteredSkills.length).toBe(1);
    expect(component.filteredSkills[0].skillName).toBe('Java');

    component.skillFilter = 'ALL';
    expect(component.filteredSkills.length).toBe(2);
  });

  it('should trigger and display AI career strategy insights', () => {
    const mockAdvisor: AIAnalyticsAdvisorResponse = {
      summary: 'Strong technical momentum detected.',
      bottlenecks: [],
      recommendations: [],
      strengths: ['Expert Java proficiency'],
      dataLimitations: [],
      confidenceScore: 0.92,
      generatedAt: '2026-10-10T10:00:00Z'
    };
    analyticsService.generateInsights.and.returnValue(of(mockAdvisor));

    component.generateCareerInsights();
    expect(component.insights).toEqual(mockAdvisor);
    expect(component.activeSection).toBe('insights');
    expect(component.loadingInsights).toBeFalse();
  });

  it('should prevent duplicate submission while insights are loading', () => {
    component.loadingInsights = true;
    component.generateCareerInsights();
    expect(analyticsService.generateInsights).not.toHaveBeenCalled();
  });

  it('should isolate section failure without hiding valid data', () => {
    analyticsService.getSkills.and.returnValue(throwError(() => new Error('Skills service down')));
    component.loadAllData();

    expect(component.skillsError).toContain('Skills service down');
    // Other sections remain loaded
    expect(component.overview).toEqual(mockOverview);
    expect(component.funnel).toEqual(mockFunnel);
    expect(component.sources).toEqual(mockSources);
  });
});
