import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { CompanyIntelService } from './company-intel.service';
import { CompanyDossier } from '../models/company-intel.model';

describe('CompanyIntelService', () => {
  let service: CompanyIntelService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [CompanyIntelService]
    });
    service = TestBed.inject(CompanyIntelService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch dossier via GET /api/intel/jobs/{jobId}/company-dossier', () => {
    const mockDossier: CompanyDossier = {
      id: 'dossier-1',
      userId: 'user-1',
      jobId: 'job-1',
      companyName: 'Stripe',
      companyTier: 'Tier 1 Big Tech',
      overview: 'Fintech infrastructure',
      engineeringScale: 'Global high availability',
      coreTechStack: 'Java, Go, Ruby',
      engineeringCulture: 'Excellence in API design',
      architectureFocus: 'Distributed consensus & idempotency',
      tailoredTalkingPoints: 'Emphasize high reliability and idempotency',
      interviewerQuestions: 'How is state replicated across regions?',
      provenanceSummary: 'Grounded in job description and user skills',
      confidenceScore: 0.92,
      createdAt: '2026-10-10T00:00:00Z',
      updatedAt: '2026-10-10T00:00:00Z'
    };

    service.getCompanyDossier('job-1').subscribe(dossier => {
      expect(dossier.companyName).toBe('Stripe');
      expect(dossier.companyTier).toBe('Tier 1 Big Tech');
    });

    const req = httpMock.expectOne('/api/intel/jobs/job-1/company-dossier');
    expect(req.request.method).toBe('GET');
    req.flush(mockDossier);
  });

  it('should trigger AI generation via POST /api/intel/jobs/{jobId}/company-dossier', () => {
    service.generateCompanyDossier('job-1', { rawCompanyResearch: 'Public tech blog info' }).subscribe();

    const req = httpMock.expectOne('/api/intel/jobs/job-1/company-dossier');
    expect(req.request.method).toBe('POST');
    expect(req.request.body.rawCompanyResearch).toBe('Public tech blog info');
    req.flush({ id: 'dossier-1', companyName: 'Stripe' });
  });
});
