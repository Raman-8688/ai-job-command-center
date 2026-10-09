import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AssessmentService } from './assessment.service';
import {
  AssessmentChecklistBundle,
  AssessmentDashboardSummary,
  CreateAssessmentRequest,
  OnlineAssessment,
  OnlineAssessmentSummary
} from '../models/assessment.model';

describe('AssessmentService', () => {
  let service: AssessmentService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AssessmentService]
    });
    service = TestBed.inject(AssessmentService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should post create assessment to /api/assessments', () => {
    const mockRequest: CreateAssessmentRequest = {
      jobId: '11111111-1111-1111-1111-111111111111',
      platform: 'HACKERRANK',
      title: 'HackerRank Algorithm OA'
    };

    const mockResponse = { id: 'test-oa-id', title: 'HackerRank Algorithm OA' } as OnlineAssessment;

    service.createAssessment(mockRequest).subscribe(res => {
      expect(res.id).toBe('test-oa-id');
    });

    const req = httpMock.expectOne('/api/assessments');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(mockRequest);
    req.flush(mockResponse);
  });

  it('should fetch assessments with query params', () => {
    service.listAssessments({ status: 'INVITED', platform: 'CODE_SIGNAL' }).subscribe(list => {
      expect(list.length).toBe(1);
    });

    const req = httpMock.expectOne(r =>
      r.url === '/api/assessments' &&
      r.params.get('status') === 'INVITED' &&
      r.params.get('platform') === 'CODE_SIGNAL'
    );
    expect(req.request.method).toBe('GET');
    req.flush([{ id: 'oa-1', title: 'CodeSignal' } as OnlineAssessmentSummary]);
  });

  it('should fetch dashboard summary from /api/assessments/dashboard-summary', () => {
    const mockSummary: AssessmentDashboardSummary = {
      totalAssessments: 10,
      invited: 3,
      inProgress: 2,
      submitted: 4,
      expired: 1,
      abandoned: 0,
      dueSoon: 2,
      overdue: 1
    };

    service.getDashboardSummary().subscribe(summary => {
      expect(summary.totalAssessments).toBe(10);
      expect(summary.dueSoon).toBe(2);
    });

    const req = httpMock.expectOne('/api/assessments/dashboard-summary');
    expect(req.request.method).toBe('GET');
    req.flush(mockSummary);
  });

  it('should trigger start lifecycle transition', () => {
    service.startAssessment('oa-123', { notes: 'Starting now' }).subscribe();

    const req = httpMock.expectOne('/api/assessments/oa-123/start');
    expect(req.request.method).toBe('POST');
    expect(req.request.body.notes).toBe('Starting now');
    req.flush({ id: 'oa-123', status: 'IN_PROGRESS' });
  });

  it('should extend deadline via POST /api/assessments/{id}/extend-deadline', () => {
    service.extendDeadline('oa-123', { newExpiresAt: '2026-10-15T00:00:00Z', reason: 'Extension granted' }).subscribe();

    const req = httpMock.expectOne('/api/assessments/oa-123/extend-deadline');
    expect(req.request.method).toBe('POST');
    expect(req.request.body.reason).toBe('Extension granted');
    req.flush({ id: 'oa-123', status: 'INVITED' });
  });

  it('should toggle checklist item via PATCH returning bundle', () => {
    const mockBundle: AssessmentChecklistBundle = {
      assessmentId: 'oa-123',
      totalItems: 4,
      completedItems: 1,
      completionPercentage: 25,
      items: [{ id: 'item-456', assessmentId: 'oa-123', topicCategory: 'ALGO', title: 'Trees', description: '', isCompleted: true, sortOrder: 0 }]
    };

    service.toggleChecklistItem('oa-123', 'item-456', { isCompleted: true }).subscribe(res => {
      expect(res.completedItems).toBe(1);
      expect(res.completionPercentage).toBe(25);
    });

    const req = httpMock.expectOne('/api/assessments/oa-123/checklist/item-456');
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body.isCompleted).toBe(true);
    req.flush(mockBundle);
  });

  it('should generate AI briefing via POST /api/assessments/{id}/briefing', () => {
    service.generateBriefing('oa-123', { candidateNotes: 'Focus on graphs' }).subscribe();

    const req = httpMock.expectOne('/api/assessments/oa-123/briefing');
    expect(req.request.method).toBe('POST');
    expect(req.request.body.candidateNotes).toBe('Focus on graphs');
    req.flush({ assessmentId: 'oa-123', platformGuidance: 'Focus on time limits' });
  });
});
