import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { InterviewPrepWorkspaceComponent } from './interview-prep-workspace.component';
import { InterviewService } from '../../services/interview.service';
import { InterviewPrepBundle, InterviewPreparation } from '../../models/interview.model';

describe('InterviewPrepWorkspaceComponent', () => {
  let component: InterviewPrepWorkspaceComponent;
  let fixture: ComponentFixture<InterviewPrepWorkspaceComponent>;
  let mockInterviewService: jasmine.SpyObj<InterviewService>;

  const mockQuestions: InterviewPreparation[] = [
    {
      id: 'q1',
      interviewId: 'int-1',
      topicCategory: 'TECH',
      question: 'Java concurrency model',
      talkingPoints: 'Threads, locks, executors',
      suggestedAnswerStar: 'STAR 1',
      userAnswerNotes: 'My notes',
      confidenceScore: 0.9,
      isReviewed: true,
      createdAt: '2026-10-15T10:00:00Z',
      updatedAt: '2026-10-15T10:00:00Z'
    },
    {
      id: 'q2',
      interviewId: 'int-1',
      topicCategory: 'TECH',
      question: 'Spring Boot internals',
      talkingPoints: 'Auto-configuration',
      suggestedAnswerStar: 'STAR 2',
      userAnswerNotes: '',
      confidenceScore: 0.85,
      isReviewed: false,
      createdAt: '2026-10-15T10:00:00Z',
      updatedAt: '2026-10-15T10:00:00Z'
    },
    {
      id: 'q3',
      interviewId: 'int-1',
      topicCategory: 'SYSTEM_DESIGN',
      question: 'Distributed caching',
      talkingPoints: 'Redis, invalidation',
      suggestedAnswerStar: 'STAR 3',
      userAnswerNotes: '',
      confidenceScore: 0.95,
      isReviewed: true,
      createdAt: '2026-10-15T10:00:00Z',
      updatedAt: '2026-10-15T10:00:00Z'
    },
    {
      id: 'q4',
      interviewId: 'int-1',
      topicCategory: 'BEHAVIORAL',
      question: 'Conflict resolution',
      talkingPoints: 'Empathy, outcome',
      suggestedAnswerStar: 'STAR 4',
      userAnswerNotes: '',
      confidenceScore: 0.88,
      isReviewed: false,
      createdAt: '2026-10-15T10:00:00Z',
      updatedAt: '2026-10-15T10:00:00Z'
    }
    // Note: LEADERSHIP has 0 questions
  ];

  const mockBundle: InterviewPrepBundle = {
    interviewId: 'int-1',
    jobTitle: 'Senior Systems Engineer',
    companyName: 'Stripe',
    round: 'TECHNICAL_SCREEN',
    readinessScore: 75,
    strategySummary: 'Focus on distributed systems and concurrency',
    questions: mockQuestions
  };

  beforeEach(async () => {
    mockInterviewService = jasmine.createSpyObj('InterviewService', [
      'getPrepBundle',
      'generateAiPrep',
      'updatePrepNotes',
      'addCustomPrepQuestion'
    ]);

    mockInterviewService.getPrepBundle.and.returnValue(of(mockBundle));
    mockInterviewService.updatePrepNotes.and.returnValue(of(mockQuestions[0]));

    await TestBed.configureTestingModule({
      imports: [InterviewPrepWorkspaceComponent],
      providers: [
        { provide: InterviewService, useValue: mockInterviewService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(InterviewPrepWorkspaceComponent);
    component = fixture.componentInstance;
  });

  it('should initialize with 0% across all categories when no bundle is loaded', () => {
    component.ngOnInit();
    expect(component.competencies.length).toBe(4);
    for (const comp of component.competencies) {
      expect(comp.percentage).toBe(0);
      expect(comp.total).toBe(0);
      expect(comp.reviewed).toBe(0);
    }
  });

  it('should dynamically calculate competency percentages grouped by category', () => {
    component.bundle = JSON.parse(JSON.stringify(mockBundle));
    component.computeCompetencies();

    const techComp = component.competencies.find(c => c.category === 'TECH');
    expect(techComp).toBeDefined();
    // 2 TECH questions, 1 reviewed => 50%
    expect(techComp!.total).toBe(2);
    expect(techComp!.reviewed).toBe(1);
    expect(techComp!.percentage).toBe(50);

    const sdComp = component.competencies.find(c => c.category === 'SYSTEM_DESIGN');
    expect(sdComp).toBeDefined();
    // 1 SYSTEM_DESIGN question, 1 reviewed => 100%
    expect(sdComp!.total).toBe(1);
    expect(sdComp!.reviewed).toBe(1);
    expect(sdComp!.percentage).toBe(100);

    const behComp = component.competencies.find(c => c.category === 'BEHAVIORAL');
    expect(behComp).toBeDefined();
    // 1 BEHAVIORAL question, 0 reviewed => 0%
    expect(behComp!.total).toBe(1);
    expect(behComp!.reviewed).toBe(0);
    expect(behComp!.percentage).toBe(0);

    const leadComp = component.competencies.find(c => c.category === 'LEADERSHIP');
    expect(leadComp).toBeDefined();
    // 0 LEADERSHIP questions => safe 0% (no NaN)
    expect(leadComp!.total).toBe(0);
    expect(leadComp!.reviewed).toBe(0);
    expect(leadComp!.percentage).toBe(0);
  });

  it('should update competency percentages dynamically when question is reviewed', () => {
    component.bundle = JSON.parse(JSON.stringify(mockBundle));
    component.interviewId = 'int-1';
    component.computeCompetencies();

    // Toggle TECH question 2 from false to true
    const q2 = component.bundle!.questions.find(q => q.id === 'q2')!;
    component.toggleReviewed(q2);

    expect(q2.isReviewed).toBe(true);

    const techComp = component.competencies.find(c => c.category === 'TECH');
    // Now 2 of 2 reviewed => 100%
    expect(techComp!.percentage).toBe(100);
  });
});
