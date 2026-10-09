import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormsModule } from '@angular/forms';
import { of } from 'rxjs';
import { CompanyDossierComponent } from './company-dossier.component';
import { CompanyIntelService } from '../../services/company-intel.service';
import { CompanyDossier } from '../../models/company-intel.model';

describe('CompanyDossierComponent', () => {
  let component: CompanyDossierComponent;
  let fixture: ComponentFixture<CompanyDossierComponent>;
  let intelService: jasmine.SpyObj<CompanyIntelService>;

  const mockDossier: CompanyDossier = {
    id: 'dossier-1',
    userId: 'user-1',
    jobId: 'job-1',
    companyName: 'Stripe',
    companyTier: 'Tier 1 Big Tech',
    overview: 'Financial infrastructure platforms',
    engineeringScale: 'Processes billions in transactions',
    coreTechStack: 'Java, Ruby, Go, AWS',
    engineeringCulture: 'Customer obsession, rigorous code reviews',
    architectureFocus: 'Idempotency and distributed consistency',
    tailoredTalkingPoints: 'Highlight experience with transactional consistency',
    interviewerQuestions: 'How is data durability guaranteed across edge nodes?',
    provenanceSummary: 'Grounded in job description and user skill profile',
    confidenceScore: 0.95,
    createdAt: '2026-10-10T00:00:00Z',
    updatedAt: '2026-10-10T00:00:00Z'
  };

  beforeEach(async () => {
    const spy = jasmine.createSpyObj('CompanyIntelService', ['getCompanyDossier', 'generateCompanyDossier']);
    spy.getCompanyDossier.and.returnValue(of(mockDossier));
    spy.generateCompanyDossier.and.returnValue(of(mockDossier));

    await TestBed.configureTestingModule({
      imports: [CompanyDossierComponent, HttpClientTestingModule, FormsModule],
      providers: [{ provide: CompanyIntelService, useValue: spy }]
    }).compileComponents();

    intelService = TestBed.inject(CompanyIntelService) as jasmine.SpyObj<CompanyIntelService>;
    fixture = TestBed.createComponent(CompanyDossierComponent);
    component = fixture.componentInstance;
  });

  it('should create component', () => {
    expect(component).toBeTruthy();
  });

  it('should fetch dossier when initialJobId is supplied', () => {
    component.initialJobId = 'job-1';
    component.ngOnInit();
    expect(intelService.getCompanyDossier).toHaveBeenCalledWith('job-1');
    expect(component.dossier?.companyName).toBe('Stripe');
    expect(component.notFound).toBeFalse();
  });

  it('should trigger AI dossier generation on button click', () => {
    component.targetJobId = 'job-1';
    component.generateDossier();
    expect(intelService.generateCompanyDossier).toHaveBeenCalledWith('job-1', {});
    expect(component.dossier?.companyTier).toBe('Tier 1 Big Tech');
  });

  it('should format paragraphs safely', () => {
    const paras = component.formatParagraphs('Line 1\n\nLine 2');
    expect(paras.length).toBe(2);
    expect(paras[0]).toBe('Line 1');
  });
});
