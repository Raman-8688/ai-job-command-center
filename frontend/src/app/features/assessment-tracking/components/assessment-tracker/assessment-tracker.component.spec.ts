import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormsModule } from '@angular/forms';
import { of } from 'rxjs';
import { AssessmentTrackerComponent } from './assessment-tracker.component';
import { AssessmentService } from '../../services/assessment.service';
import {
  AssessmentDashboardSummary,
  OnlineAssessment,
  OnlineAssessmentSummary
} from '../../models/assessment.model';

describe('AssessmentTrackerComponent', () => {
  let component: AssessmentTrackerComponent;
  let fixture: ComponentFixture<AssessmentTrackerComponent>;
  let assessmentService: jasmine.SpyObj<AssessmentService>;

  const mockSummary: AssessmentDashboardSummary = {
    totalAssessments: 5,
    invited: 2,
    inProgress: 1,
    submitted: 2,
    expired: 0,
    abandoned: 0,
    dueSoon: 1,
    overdue: 0
  };

  const mockAssessments: OnlineAssessmentSummary[] = [
    {
      id: 'oa-1',
      jobId: 'job-1',
      title: 'Meta Production Engineer OA',
      companyName: 'Meta',
      platform: 'HACKERRANK',
      status: 'INVITED',
      result: 'PENDING',
      durationMinutes: 90,
      checklistCompletionPercentage: 50,
      totalChecklistItems: 4,
      version: 1,
      createdAt: '2026-10-10T00:00:00Z',
      updatedAt: '2026-10-10T00:00:00Z'
    }
  ];

  beforeEach(async () => {
    const spy = jasmine.createSpyObj('AssessmentService', [
      'getDashboardSummary',
      'listAssessments',
      'getAssessment',
      'getEvents',
      'createAssessment',
      'startAssessment',
      'submitAssessment',
      'recordResult',
      'extendDeadline',
      'abandonAssessment',
      'expireAssessment',
      'toggleChecklistItem',
      'generateBriefing'
    ]);

    spy.getDashboardSummary.and.returnValue(of(mockSummary));
    spy.listAssessments.and.returnValue(of(mockAssessments));

    await TestBed.configureTestingModule({
      imports: [AssessmentTrackerComponent, HttpClientTestingModule, FormsModule],
      providers: [{ provide: AssessmentService, useValue: spy }]
    }).compileComponents();

    assessmentService = TestBed.inject(AssessmentService) as jasmine.SpyObj<AssessmentService>;
    fixture = TestBed.createComponent(AssessmentTrackerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create and load dashboard summary and assessment list', () => {
    expect(component).toBeTruthy();
    expect(component.summary?.totalAssessments).toBe(5);
    expect(component.assessments.length).toBe(1);
    expect(component.filteredAssessments[0].title).toBe('Meta Production Engineer OA');
  });

  it('should filter assessments by search query', () => {
    component.searchQuery = 'Meta';
    component.applyFilter();
    expect(component.filteredAssessments.length).toBe(1);

    component.searchQuery = 'Google';
    component.applyFilter();
    expect(component.filteredAssessments.length).toBe(0);
  });

  it('should open and populate start modal for INVITED assessment', () => {
    const assessment = mockAssessments[0];
    component.openStartModal(assessment);
    expect(component.showStartModal).toBeTrue();
    expect(component.activeAssessmentForAction?.id).toBe(assessment.id);
  });

  it('should emit openCompanyIntel when navigating to dossier', () => {
    spyOn(component.openCompanyIntel, 'emit');
    component.goToCompanyIntel('job-123');
    expect(component.openCompanyIntel.emit).toHaveBeenCalledWith('job-123');
  });
});
