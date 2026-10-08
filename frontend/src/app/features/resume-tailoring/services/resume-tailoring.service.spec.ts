import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { ResumeTailoringService } from './resume-tailoring.service';

describe('ResumeTailoringService', () => {
  let service: ResumeTailoringService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ResumeTailoringService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(ResumeTailoringService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should request creation of tailored draft via POST', () => {
    const resumeId = '11111111-1111-1111-1111-111111111111';
    const jobId = '22222222-2222-2222-2222-222222222222';

    service.createTailoredDraft(resumeId, jobId).subscribe(response => {
      expect(response).toBeTruthy();
    });

    const req = httpMock.expectOne(`/api/resumes/${resumeId}/tailor/${jobId}`);
    expect(req.request.method).toBe('POST');
    req.flush({ id: 'draft-1', version: 1, status: 'DRAFT' });
  });

  it('should fetch tailored draft details via GET', () => {
    const draftId = '33333333-3333-3333-3333-333333333333';

    service.getTailoredResume(draftId).subscribe(draft => {
      expect(draft.id).toBe(draftId);
    });

    const req = httpMock.expectOne(`/api/tailored-resumes/${draftId}`);
    expect(req.request.method).toBe('GET');
    req.flush({ id: draftId, version: 1, status: 'DRAFT', keywordCoverageScore: 85.0 });
  });
});
