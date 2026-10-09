import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { InterviewService } from './interview.service';
import { ScheduleInterviewRequest } from '../models/interview.model';

describe('InterviewService', () => {
  let service: InterviewService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        InterviewService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(InterviewService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should schedule an interview via POST', () => {
    const dummyReq: ScheduleInterviewRequest = {
      jobId: '12345678-1234-1234-1234-123456789012',
      round: 'TECHNICAL_SCREEN',
      format: 'VIDEO_CALL',
      scheduledStartTime: '2026-10-15T10:00:00Z',
      scheduledEndTime: '2026-10-15T11:00:00Z'
    };

    service.scheduleInterview(dummyReq).subscribe(res => {
      expect(res.round).toBe('TECHNICAL_SCREEN');
    });

    const req = httpMock.expectOne('/api/interviews');
    expect(req.request.method).toBe('POST');
    req.flush({ id: 'mock-id', ...dummyReq });
  });

  it('should get dashboard summary via GET', () => {
    service.getDashboardSummary().subscribe(summary => {
      expect(summary.totalInterviews).toBe(3);
    });

    const req = httpMock.expectOne('/api/interviews/dashboard-summary');
    expect(req.request.method).toBe('GET');
    req.flush({
      totalInterviews: 3,
      upcomingInterviews: 2,
      completedInterviews: 1,
      countByRound: {},
      countByStatus: {},
      overallReadinessScore: 88
    });
  });
});
