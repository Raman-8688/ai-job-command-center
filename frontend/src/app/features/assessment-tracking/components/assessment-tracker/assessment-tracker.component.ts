import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AssessmentService } from '../../services/assessment.service';
import {
  AssessmentBriefing,
  AssessmentChecklistItem,
  AssessmentDashboardSummary,
  AssessmentEventType,
  AssessmentPlatform,
  AssessmentResult,
  AssessmentStatus,
  CreateAssessmentRequest,
  OnlineAssessment,
  OnlineAssessmentEvent,
  OnlineAssessmentSummary
} from '../../models/assessment.model';

@Component({
  selector: 'app-assessment-tracker',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './assessment-tracker.component.html',
  styleUrls: ['./assessment-tracker.component.css']
})
export class AssessmentTrackerComponent implements OnInit {

  @Output() openCompanyIntel = new EventEmitter<string>();

  assessments: OnlineAssessmentSummary[] = [];
  filteredAssessments: OnlineAssessmentSummary[] = [];
  summary: AssessmentDashboardSummary | null = null;

  // Selected assessment for drawer/workspace
  selectedAssessment: OnlineAssessment | null = null;
  selectedAssessmentEvents: OnlineAssessmentEvent[] = [];
  selectedAssessmentChecklist: AssessmentChecklistItem[] = [];
  activeDetailTab: 'briefing' | 'checklist' | 'events' | 'details' = 'briefing';

  // State flags
  loadingList = false;
  loadingDetail = false;
  generatingBriefing = false;
  errorMessage = '';
  successMessage = '';
  togglingItem: Record<string, boolean> = {};

  // Filters
  selectedStatus: string = 'ALL';
  selectedPlatform: string = 'ALL';
  expiringHoursFilter: number | null = null;
  searchQuery: string = '';
  viewMode: 'cards' | 'table' = 'cards';
  selectedCategory: string = 'ALL';

  // Platforms definition
  platforms: { value: AssessmentPlatform; label: string }[] = [
    { value: 'HACKERRANK', label: 'HackerRank' },
    { value: 'CODE_SIGNAL', label: 'CodeSignal' },
    { value: 'LEETCODE', label: 'LeetCode' },
    { value: 'BYTEBOARD', label: 'Byteboard' },
    { value: 'KARAT', label: 'Karat' },
    { value: 'CODERPAD', label: 'CoderPad' },
    { value: 'TALENT_ASSESSMENT', label: 'Talent Assessment' },
    { value: 'CUSTOM', label: 'Custom Platform' }
  ];

  // Lifecycle Modals
  showCreateModal = false;
  showStartModal = false;
  showSubmitModal = false;
  showResultModal = false;
  showExtendModal = false;
  showAbandonModal = false;
  showExpireModal = false;
  activeAssessmentForAction: { id: string; version: number } | null = null;

  // Forms
  createForm = {
    jobId: '',
    applicationId: '',
    platform: 'HACKERRANK' as AssessmentPlatform,
    title: '',
    durationMinutes: 90,
    invitedDate: '',
    invitedTime: '09:00',
    expiresDate: '',
    expiresTime: '23:59',
    scheduledStartDate: '',
    scheduledStartTime: '10:00',
    assessmentUrl: '',
    accessCode: '',
    submissionNotes: ''
  };

  startForm = {
    startedDate: '',
    startedTime: '',
    notes: ''
  };

  submitForm = {
    submissionRepoUrl: '',
    notes: ''
  };

  resultForm = {
    result: 'PASSED' as AssessmentResult,
    score: null as number | null,
    maxScore: 100,
    notes: ''
  };

  extendForm = {
    newExpiresDate: '',
    newExpiresTime: '23:59',
    reason: ''
  };

  abandonForm = {
    reason: ''
  };

  expireForm = {
    notes: ''
  };

  constructor(private assessmentService: AssessmentService) {}

  ngOnInit(): void {
    this.loadAllData();
  }

  loadAllData(): void {
    this.loadDashboardSummary();
    this.loadAssessments();
  }

  loadDashboardSummary(): void {
    this.assessmentService.getDashboardSummary().subscribe({
      next: (sum) => this.summary = sum,
      error: (err) => console.error('Failed to load assessment summary', err)
    });
  }

  loadAssessments(): void {
    this.loadingList = true;
    this.errorMessage = '';
    const filters: {
      status?: AssessmentStatus;
      platform?: AssessmentPlatform;
      expiringWithinHours?: number;
    } = {};

    if (this.selectedStatus !== 'ALL') {
      filters.status = this.selectedStatus as AssessmentStatus;
    }
    if (this.selectedPlatform !== 'ALL') {
      filters.platform = this.selectedPlatform as AssessmentPlatform;
    }
    if (this.expiringHoursFilter) {
      filters.expiringWithinHours = this.expiringHoursFilter;
    }

    this.assessmentService.listAssessments(filters).subscribe({
      next: (list) => {
        this.assessments = list;
        this.applyFilter();
        this.loadingList = false;
      },
      error: (err) => {
        this.errorMessage = 'Failed to load assessments. Please try again.';
        this.loadingList = false;
        console.error('Error loading assessments:', err);
      }
    });
  }

  applyFilter(): void {
    let result = [...this.assessments];
    if (this.searchQuery.trim()) {
      const q = this.searchQuery.toLowerCase();
      result = result.filter(a =>
        a.title.toLowerCase().includes(q) ||
        (a.companyName && a.companyName.toLowerCase().includes(q)) ||
        (a.jobTitle && a.jobTitle.toLowerCase().includes(q)) ||
        a.platform.toLowerCase().includes(q)
      );
    }
    this.filteredAssessments = result;
  }

  selectAssessment(assessmentId: string): void {
    this.loadingDetail = true;
    this.errorMessage = '';
    this.assessmentService.getAssessment(assessmentId).subscribe({
      next: (oa) => {
        this.selectedAssessment = oa;
        this.selectedAssessmentChecklist = oa.checklist || [];
        this.selectedAssessmentEvents = oa.recentEvents || [];
        this.loadingDetail = false;
        this.loadEvents(assessmentId);
      },
      error: (err) => {
        this.errorMessage = 'Failed to load assessment details.';
        this.loadingDetail = false;
        console.error(err);
      }
    });
  }

  loadEvents(assessmentId: string): void {
    this.assessmentService.getEvents(assessmentId).subscribe({
      next: (events) => this.selectedAssessmentEvents = events,
      error: (err) => console.error('Failed to load events', err)
    });
  }

  closeDetail(): void {
    this.selectedAssessment = null;
  }

  // --- Study Checklist Management ---
  get checklistCategories(): string[] {
    const cats = new Set<string>();
    this.selectedAssessmentChecklist.forEach(item => {
      if (item.topicCategory) cats.add(item.topicCategory);
    });
    return ['ALL', ...Array.from(cats)];
  }

  get filteredChecklistItems(): AssessmentChecklistItem[] {
    if (this.selectedCategory === 'ALL') {
      return this.selectedAssessmentChecklist;
    }
    return this.selectedAssessmentChecklist.filter(i => i.topicCategory === this.selectedCategory);
  }

  toggleChecklist(item: AssessmentChecklistItem): void {
    if (!this.selectedAssessment || this.togglingItem[item.id]) return;

    this.togglingItem[item.id] = true;
    const newCompleted = !item.isCompleted;

    this.assessmentService.toggleChecklistItem(this.selectedAssessment.id, item.id, {
      isCompleted: newCompleted
    }).subscribe({
      next: (bundle) => {
        this.selectedAssessmentChecklist = bundle.items;
        if (this.selectedAssessment) {
          this.selectedAssessment.checklistCompletionPercentage = bundle.completionPercentage;
          this.selectedAssessment.checklistCompletedCount = bundle.completedItems;
          this.selectedAssessment.checklistTotalCount = bundle.totalItems;
        }
        this.togglingItem[item.id] = false;
        this.loadDashboardSummary();
        this.loadAssessments();
      },
      error: (err) => {
        this.togglingItem[item.id] = false;
        this.errorMessage = 'Could not update checklist item. Please retry.';
        console.error(err);
      }
    });
  }

  // --- AI Briefing Generation ---
  triggerBriefing(): void {
    if (!this.selectedAssessment || this.generatingBriefing) return;

    this.generatingBriefing = true;
    this.errorMessage = '';

    this.assessmentService.generateBriefing(this.selectedAssessment.id, {
      candidateNotes: 'Candidate requested briefing generation'
    }).subscribe({
      next: (briefing: AssessmentBriefing) => {
        this.generatingBriefing = false;
        this.showToast('Grounded assessment briefing generated successfully!');
        if (this.selectedAssessment) {
          this.refreshSelectedAssessment(this.selectedAssessment.id);
        }
      },
      error: (err) => {
        this.generatingBriefing = false;
        this.errorMessage = 'Failed to generate AI briefing. Please check job requirements.';
        console.error(err);
      }
    });
  }

  refreshSelectedAssessment(id: string): void {
    this.assessmentService.getAssessment(id).subscribe({
      next: (oa) => {
        this.selectedAssessment = oa;
        this.selectedAssessmentChecklist = oa.checklist || [];
        this.loadDashboardSummary();
        this.loadAssessments();
      }
    });
  }

  // --- Lifecycle Actions ---
  openCreateModal(): void {
    const now = new Date();
    const in3Days = new Date(now.getTime() + 3 * 24 * 60 * 60 * 1000);

    this.createForm = {
      jobId: '',
      applicationId: '',
      platform: 'HACKERRANK',
      title: '',
      durationMinutes: 90,
      invitedDate: now.toISOString().split('T')[0],
      invitedTime: '09:00',
      expiresDate: in3Days.toISOString().split('T')[0],
      expiresTime: '23:59',
      scheduledStartDate: '',
      scheduledStartTime: '10:00',
      assessmentUrl: '',
      accessCode: '',
      submissionNotes: ''
    };
    this.showCreateModal = true;
  }

  submitCreate(): void {
    if (!this.createForm.jobId.trim() || !this.createForm.title.trim()) {
      this.errorMessage = 'Job ID and Title are required.';
      return;
    }

    const invitedAt = this.combineDateTime(this.createForm.invitedDate, this.createForm.invitedTime);
    const expiresAt = this.combineDateTime(this.createForm.expiresDate, this.createForm.expiresTime);
    const scheduledStartTime = this.combineDateTime(this.createForm.scheduledStartDate, this.createForm.scheduledStartTime);

    const req: CreateAssessmentRequest = {
      jobId: this.createForm.jobId.trim(),
      applicationId: this.createForm.applicationId.trim() || undefined,
      platform: this.createForm.platform,
      title: this.createForm.title.trim(),
      durationMinutes: this.createForm.durationMinutes ? Number(this.createForm.durationMinutes) : undefined,
      invitedAt: invitedAt || undefined,
      expiresAt: expiresAt || undefined,
      scheduledStartTime: scheduledStartTime || undefined,
      assessmentUrl: this.createForm.assessmentUrl.trim() || undefined,
      accessCode: this.createForm.accessCode.trim() || undefined,
      submissionNotes: this.createForm.submissionNotes.trim() || undefined
    };

    this.assessmentService.createAssessment(req).subscribe({
      next: (created) => {
        this.showCreateModal = false;
        this.showToast('Online Assessment created successfully!');
        this.loadAllData();
        this.selectAssessment(created.id);
      },
      error: (err) => {
        this.errorMessage = 'Failed to create assessment. Verify Job ID UUID.';
        console.error(err);
      }
    });
  }

  openStartModal(assessment: { id: string; version: number }): void {
    this.activeAssessmentForAction = assessment;
    this.startForm = { startedDate: '', startedTime: '', notes: '' };
    this.showStartModal = true;
  }

  submitStart(): void {
    if (!this.activeAssessmentForAction) return;

    const startedAt = this.combineDateTime(this.startForm.startedDate, this.startForm.startedTime);
    this.assessmentService.startAssessment(this.activeAssessmentForAction.id, {
      startTime: startedAt || undefined,
      notes: this.startForm.notes.trim() || undefined
    }).subscribe({
      next: (updated) => {
        this.showStartModal = false;
        this.showToast('Assessment marked as In Progress!');
        this.loadAllData();
        if (this.selectedAssessment?.id === updated.id) {
          this.selectAssessment(updated.id);
        }
      },
      error: (err) => {
        this.errorMessage = 'Failed to start assessment: ' + (err.error?.detail || 'Conflict');
        console.error(err);
      }
    });
  }

  openSubmitModal(assessment: { id: string; version: number }): void {
    this.activeAssessmentForAction = assessment;
    this.submitForm = { submissionRepoUrl: '', notes: '' };
    this.showSubmitModal = true;
  }

  submitSubmission(): void {
    if (!this.activeAssessmentForAction) return;

    this.assessmentService.submitAssessment(this.activeAssessmentForAction.id, {
      submissionRepoUrl: this.submitForm.submissionRepoUrl.trim() || undefined,
      submissionNotes: this.submitForm.notes.trim() || undefined,
      completedAt: new Date().toISOString()
    }).subscribe({
      next: (updated) => {
        this.showSubmitModal = false;
        this.showToast('Assessment successfully submitted!');
        this.loadAllData();
        if (this.selectedAssessment?.id === updated.id) {
          this.selectAssessment(updated.id);
        }
      },
      error: (err) => {
        this.errorMessage = 'Failed to record submission: ' + (err.error?.detail || 'Conflict');
        console.error(err);
      }
    });
  }

  openResultModal(assessment: { id: string; version: number }): void {
    this.activeAssessmentForAction = assessment;
    this.resultForm = { result: 'PASSED', score: null, maxScore: 100, notes: '' };
    this.showResultModal = true;
  }

  submitResult(): void {
    if (!this.activeAssessmentForAction) return;

    this.assessmentService.recordResult(this.activeAssessmentForAction.id, {
      result: this.resultForm.result,
      score: this.resultForm.score !== null ? Number(this.resultForm.score) : undefined,
      maxScore: this.resultForm.maxScore ? Number(this.resultForm.maxScore) : undefined,
      notes: this.resultForm.notes.trim() || undefined
    }).subscribe({
      next: (updated) => {
        this.showResultModal = false;
        this.showToast('Assessment outcome recorded!');
        this.loadAllData();
        if (this.selectedAssessment?.id === updated.id) {
          this.selectAssessment(updated.id);
        }
      },
      error: (err) => {
        this.errorMessage = 'Failed to record outcome: ' + (err.error?.detail || 'Validation error');
        console.error(err);
      }
    });
  }

  openExtendModal(assessment: { id: string; version: number }): void {
    this.activeAssessmentForAction = assessment;
    const tomorrow = new Date(Date.now() + 24 * 60 * 60 * 1000);
    this.extendForm = {
      newExpiresDate: tomorrow.toISOString().split('T')[0],
      newExpiresTime: '23:59',
      reason: ''
    };
    this.showExtendModal = true;
  }

  submitExtend(): void {
    if (!this.activeAssessmentForAction) return;

    const newExpiresAt = this.combineDateTime(this.extendForm.newExpiresDate, this.extendForm.newExpiresTime);
    if (!newExpiresAt) {
      this.errorMessage = 'New expiration date is required.';
      return;
    }

    this.assessmentService.extendDeadline(this.activeAssessmentForAction.id, {
      newExpiresAt: newExpiresAt,
      reason: this.extendForm.reason.trim() || undefined
    }).subscribe({
      next: (updated) => {
        this.showExtendModal = false;
        this.showToast('Assessment deadline extended!');
        this.loadAllData();
        if (this.selectedAssessment?.id === updated.id) {
          this.selectAssessment(updated.id);
        }
      },
      error: (err) => {
        this.errorMessage = 'Failed to extend deadline: ' + (err.error?.detail || 'Error');
        console.error(err);
      }
    });
  }

  openAbandonModal(assessment: { id: string; version: number }): void {
    this.activeAssessmentForAction = assessment;
    this.abandonForm = { reason: '' };
    this.showAbandonModal = true;
  }

  submitAbandon(): void {
    if (!this.activeAssessmentForAction) return;

    this.assessmentService.abandonAssessment(this.activeAssessmentForAction.id, {
      reason: this.abandonForm.reason.trim() || undefined
    }).subscribe({
      next: (updated) => {
        this.showAbandonModal = false;
        this.showToast('Assessment marked as abandoned.');
        this.loadAllData();
        if (this.selectedAssessment?.id === updated.id) {
          this.selectAssessment(updated.id);
        }
      },
      error: (err) => {
        this.errorMessage = 'Failed to abandon assessment: ' + (err.error?.detail || 'Error');
        console.error(err);
      }
    });
  }

  openExpireModal(assessment: { id: string; version: number }): void {
    this.activeAssessmentForAction = assessment;
    this.expireForm = { notes: '' };
    this.showExpireModal = true;
  }

  submitExpire(): void {
    if (!this.activeAssessmentForAction) return;

    this.assessmentService.expireAssessment(this.activeAssessmentForAction.id, {
      reason: this.expireForm.notes.trim() || undefined
    }).subscribe({
      next: (updated) => {
        this.showExpireModal = false;
        this.showToast('Assessment marked as expired.');
        this.loadAllData();
        if (this.selectedAssessment?.id === updated.id) {
          this.selectAssessment(updated.id);
        }
      },
      error: (err) => {
        this.errorMessage = 'Failed to expire assessment: ' + (err.error?.detail || 'Error');
        console.error(err);
      }
    });
  }

  // --- Helper Methods ---
  isOverdue(expiresAt?: string): boolean {
    if (!expiresAt) return false;
    return new Date(expiresAt).getTime() < Date.now();
  }

  isDueSoon(expiresAt?: string): boolean {
    if (!expiresAt) return false;
    const diff = new Date(expiresAt).getTime() - Date.now();
    return diff > 0 && diff < 24 * 60 * 60 * 1000;
  }

  formatDate(dateStr?: string): string {
    if (!dateStr) return 'N/A';
    return new Date(dateStr).toLocaleString(undefined, {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  canStart(status: AssessmentStatus): boolean {
    return status === 'INVITED';
  }

  canSubmit(status: AssessmentStatus): boolean {
    return status === 'IN_PROGRESS';
  }

  canRecordResult(status: AssessmentStatus): boolean {
    return status === 'SUBMITTED' || status === 'IN_PROGRESS';
  }

  canExtend(status: AssessmentStatus): boolean {
    return status === 'INVITED' || status === 'IN_PROGRESS';
  }

  canAbandon(status: AssessmentStatus): boolean {
    return status === 'INVITED' || status === 'IN_PROGRESS';
  }

  canExpire(status: AssessmentStatus): boolean {
    return status === 'INVITED' || status === 'IN_PROGRESS';
  }

  showToast(msg: string): void {
    this.successMessage = msg;
    setTimeout(() => {
      if (this.successMessage === msg) {
        this.successMessage = '';
      }
    }, 4000);
  }

  goToCompanyIntel(jobId: string): void {
    this.openCompanyIntel.emit(jobId);
  }

  private combineDateTime(dateStr?: string, timeStr?: string): string | null {
    if (!dateStr) return null;
    const time = timeStr || '00:00';
    return new Date(`${dateStr}T${time}:00Z`).toISOString();
  }
}
