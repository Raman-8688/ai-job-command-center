import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ResumeTailoringService } from '../../services/resume-tailoring.service';
import { TailoredResume, TailoredResumeStatus, TailoredResumeSuggestion, TailoredResumeSummary } from '../../models/tailored-resume.model';

@Component({
  selector: 'app-tailored-resume-workbench',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './tailored-resume-workbench.component.html',
  styleUrls: ['./tailored-resume-workbench.component.css']
})
export class TailoredResumeWorkbenchComponent implements OnInit {

  activeDraft: TailoredResume | null = null;
  versions: TailoredResumeSummary[] = [];
  selectedFilter: string = 'ALL';

  targetResumeId: string = 'a0000000-0000-0000-0000-000000000001';
  targetJobId: string = 'b0000000-0000-0000-0000-000000000001';

  editedTitle: string = '';
  editedSummary: string = '';

  loading: boolean = false;
  statusMessage: string = '';
  isError: boolean = false;

  constructor(private tailoringService: ResumeTailoringService) {}

  ngOnInit(): void {
    // Provide sample initialized draft for immediate workbench exploration
    this.initSampleDraft();
  }

  initSampleDraft(): void {
    this.activeDraft = {
      id: 'd1111111-1111-1111-1111-111111111111',
      userId: 'u1111111-1111-1111-1111-111111111111',
      sourceResumeId: this.targetResumeId,
      targetJobId: this.targetJobId,
      version: 1,
      status: 'DRAFT',
      tailoredTitle: 'Senior Full Stack & Cloud Architect',
      tailoredSummary: 'Senior Software Engineer with 7+ years of experience specializing in Java, Spring Boot, and PostgreSQL. Tailored for Cloud Dynamics.',
      keywordCoverageScore: 80.0,
      matchedKeywords: ['Java', 'Spring Boot', 'PostgreSQL', 'Docker'],
      missingKeywords: ['Kafka'],
      suggestions: [
        {
          id: 's1',
          sectionType: 'SUMMARY',
          targetItemTitle: 'Executive Summary',
          originalContent: 'General software developer building web applications.',
          suggestedContent: 'Senior Software Engineer specializing in high-throughput Java, Spring Boot, and PostgreSQL architecture for cloud environments.',
          rationale: 'Directly aligns executive summary with target job requirements.',
          evidence: 'Verified Candidate Skills: Java, Spring Boot, PostgreSQL',
          verificationStatus: 'VERIFIED',
          applied: false,
          displayOrder: 1,
          createdAt: new Date().toISOString()
        },
        {
          id: 's2',
          sectionType: 'SKILLS',
          targetItemTitle: 'Technical Skills Hierarchy',
          originalContent: 'Java, Python, HTML, CSS, JavaScript, Spring Boot',
          suggestedContent: 'Prioritize Java, Spring Boot, and PostgreSQL in the primary skills section.',
          rationale: 'Moves mission-critical requirements to top visibility.',
          evidence: 'Verified Candidate Skills catalog',
          verificationStatus: 'VERIFIED',
          applied: false,
          displayOrder: 2,
          createdAt: new Date().toISOString()
        },
        {
          id: 's3',
          sectionType: 'SKILLS',
          targetItemTitle: 'Job Requirement Gap: Kafka',
          originalContent: 'Not present on resume',
          suggestedContent: '[NOT_ENOUGH_EVIDENCE] Job requires Kafka. Candidate lacks verified proof; do NOT fabricate experience. Flagged as self-study topic.',
          rationale: 'Anti-hallucination guard preventing false claims.',
          evidence: 'None (Unverified in profile)',
          verificationStatus: 'NOT_ENOUGH_EVIDENCE',
          applied: false,
          displayOrder: 3,
          createdAt: new Date().toISOString()
        },
        {
          id: 's4',
          sectionType: 'EXPERIENCE',
          targetItemTitle: 'Lead Developer at Enterprise Tech',
          originalContent: 'Developed backend microservices and handled database queries.',
          suggestedContent: 'Emphasize PostgreSQL query optimization, connection pooling, and sub-50ms API latency outcomes.',
          rationale: 'Demonstrates tangible production impact matching target position.',
          evidence: 'Work history: Enterprise Tech',
          verificationStatus: 'VERIFIED',
          applied: false,
          displayOrder: 4,
          createdAt: new Date().toISOString()
        }
      ],
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    };

    this.editedTitle = this.activeDraft.tailoredTitle || '';
    this.editedSummary = this.activeDraft.tailoredSummary || '';
  }

  get filteredSuggestions(): TailoredResumeSuggestion[] {
    if (!this.activeDraft) return [];
    if (this.selectedFilter === 'ALL') return this.activeDraft.suggestions;
    return this.activeDraft.suggestions.filter(s => s.sectionType === this.selectedFilter);
  }

  createTailoringDraft(): void {
    if (!this.targetResumeId || !this.targetJobId) {
      this.showMessage('Please provide valid Resume ID and Job ID', true);
      return;
    }
    this.loading = true;
    this.tailoringService.createTailoredDraft(this.targetResumeId, this.targetJobId).subscribe({
      next: (draft) => {
        this.activeDraft = draft;
        this.editedTitle = draft.tailoredTitle || '';
        this.editedSummary = draft.tailoredSummary || '';
        this.loading = false;
        this.showMessage(`Created tailored draft v${draft.version} successfully!`, false);
        this.loadVersionHistory();
      },
      error: (err) => {
        this.loading = false;
        this.showMessage(`API call failed: ${err.message || 'Check backend status'}`, true);
      }
    });
  }

  loadVersionHistory(): void {
    if (!this.activeDraft) return;
    this.tailoringService.getTailoredResumesForResume(this.activeDraft.sourceResumeId).subscribe({
      next: (versions) => this.versions = versions,
      error: () => {}
    });
  }

  applySuggestion(suggestion: TailoredResumeSuggestion): void {
    if (!this.activeDraft) return;
    this.loading = true;
    this.tailoringService.applySuggestion(this.activeDraft.id, suggestion.id).subscribe({
      next: (updated) => {
        this.activeDraft = updated;
        this.editedSummary = updated.tailoredSummary || '';
        this.loading = false;
        this.showMessage(`Applied suggestion: ${suggestion.targetItemTitle}`, false);
      },
      error: () => {
        // Fallback local update
        suggestion.applied = true;
        if (suggestion.sectionType === 'SUMMARY') {
          this.editedSummary = suggestion.suggestedContent;
          this.activeDraft!.tailoredSummary = suggestion.suggestedContent;
        }
        this.loading = false;
        this.showMessage(`Applied suggestion locally: ${suggestion.targetItemTitle}`, false);
      }
    });
  }

  saveContentChanges(): void {
    if (!this.activeDraft) return;
    this.loading = true;
    this.tailoringService.updateContent(this.activeDraft.id, this.editedTitle, this.editedSummary).subscribe({
      next: (updated) => {
        this.activeDraft = updated;
        this.loading = false;
        this.showMessage('Draft content saved successfully.', false);
      },
      error: () => {
        this.activeDraft!.tailoredTitle = this.editedTitle;
        this.activeDraft!.tailoredSummary = this.editedSummary;
        this.loading = false;
        this.showMessage('Saved changes locally.', false);
      }
    });
  }

  changeStatus(newStatus: TailoredResumeStatus): void {
    if (!this.activeDraft) return;
    this.loading = true;
    this.tailoringService.updateStatus(this.activeDraft.id, newStatus).subscribe({
      next: (updated) => {
        this.activeDraft = updated;
        this.loading = false;
        this.showMessage(`Status transitioned to: ${newStatus}`, false);
      },
      error: () => {
        this.activeDraft!.status = newStatus;
        this.loading = false;
        this.showMessage(`Status updated locally to: ${newStatus}`, false);
      }
    });
  }

  private showMessage(msg: string, isErr: boolean): void {
    this.statusMessage = msg;
    this.isError = isErr;
    setTimeout(() => {
      this.statusMessage = '';
    }, 4500);
  }
}
