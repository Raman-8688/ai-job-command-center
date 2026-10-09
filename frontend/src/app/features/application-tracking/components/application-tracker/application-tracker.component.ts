import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApplicationTrackingService } from '../../services/application-tracking.service';
import {
  ApplicationDashboardSummary,
  ApplicationGuidance,
  ApplicationStatus,
  JobApplication,
  JobApplicationEvent,
  JobApplicationSummary
} from '../../models/job-application.model';

@Component({
  selector: 'app-application-tracker',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './application-tracker.component.html',
  styleUrls: ['./application-tracker.component.css']
})
export class ApplicationTrackerComponent implements OnInit {

  // State
  applications: JobApplicationSummary[] = [];
  dashboardSummary: ApplicationDashboardSummary | null = null;
  selectedApplication: JobApplication | null = null;
  selectedTimeline: JobApplicationEvent[] = [];
  selectedGuidance: ApplicationGuidance | null = null;

  // Filters & Pagination
  selectedStatus: ApplicationStatus | '' = '';
  searchQuery = '';
  page = 0;
  size = 20;
  totalElements = 0;
  totalPages = 0;

  // Modals & UI states
  isLoading = false;
  isDetailLoading = false;
  isGuidanceLoading = false;
  errorMessage = '';
  successMessage = '';

  // Transition form
  transitionTargetStatus: ApplicationStatus = 'APPLIED';
  transitionNotes = '';

  // Status definitions
  readonly statuses: ApplicationStatus[] = [
    'DRAFT',
    'APPLIED',
    'SCREENING',
    'ASSESSMENT',
    'INTERVIEW',
    'OFFER',
    'ACCEPTED',
    'REJECTED',
    'WITHDRAWN',
    'ARCHIVED'
  ];

  constructor(private trackingService: ApplicationTrackingService) {}

  ngOnInit(): void {
    this.loadDashboardSummary();
    this.loadApplications();
  }

  loadDashboardSummary(): void {
    this.trackingService.getDashboardSummary().subscribe({
      next: (summary) => (this.dashboardSummary = summary),
      error: (err) => console.error('Failed to load dashboard summary', err)
    });
  }

  loadApplications(): void {
    this.isLoading = true;
    this.errorMessage = '';
    const statusParam = this.selectedStatus ? (this.selectedStatus as ApplicationStatus) : undefined;

    this.trackingService.listApplications(statusParam, this.searchQuery, this.page, this.size).subscribe({
      next: (res) => {
        this.applications = res.content;
        this.totalElements = res.totalElements;
        this.totalPages = res.totalPages;
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'Failed to load applications';
        this.isLoading = false;
        console.error(err);
      }
    });
  }

  onFilterChange(): void {
    this.page = 0;
    this.loadApplications();
  }

  selectApplication(id: string): void {
    this.isDetailLoading = true;
    this.selectedGuidance = null;
    this.trackingService.getApplication(id).subscribe({
      next: (app) => {
        this.selectedApplication = app;
        this.selectedTimeline = app.events || [];
        this.isDetailLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'Failed to load application details';
        this.isDetailLoading = false;
        console.error(err);
      }
    });
  }

  closeDetail(): void {
    this.selectedApplication = null;
    this.selectedTimeline = [];
    this.selectedGuidance = null;
  }

  performTransition(): void {
    if (!this.selectedApplication) return;

    this.trackingService.transitionStatus(this.selectedApplication.id, {
      targetStatus: this.transitionTargetStatus,
      notes: this.transitionNotes,
      eventSource: 'USER'
    }).subscribe({
      next: (updated) => {
        this.selectedApplication = updated;
        this.selectedTimeline = updated.events || [];
        this.successMessage = `Status updated to ${updated.status}`;
        this.transitionNotes = '';
        this.loadDashboardSummary();
        this.loadApplications();
        setTimeout(() => (this.successMessage = ''), 4000);
      },
      error: (err) => {
        this.errorMessage = err?.error?.detail || 'Failed to update status transition';
        setTimeout(() => (this.errorMessage = ''), 5000);
      }
    });
  }

  fetchGuidance(): void {
    if (!this.selectedApplication) return;
    this.isGuidanceLoading = true;
    this.trackingService.getGuidance(this.selectedApplication.id).subscribe({
      next: (guidance) => {
        this.selectedGuidance = guidance;
        this.isGuidanceLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'Failed to retrieve AI guidance';
        this.isGuidanceLoading = false;
        console.error(err);
      }
    });
  }

  copyDraft(): void {
    if (this.selectedGuidance?.draftedFollowUpMessage) {
      navigator.clipboard.writeText(this.selectedGuidance.draftedFollowUpMessage);
      this.successMessage = 'Draft message copied to clipboard!';
      setTimeout(() => (this.successMessage = ''), 3000);
    }
  }

  getStatusClass(status: ApplicationStatus): string {
    switch (status) {
      case 'DRAFT': return 'badge-draft';
      case 'APPLIED': return 'badge-applied';
      case 'SCREENING':
      case 'ASSESSMENT': return 'badge-progress';
      case 'INTERVIEW': return 'badge-interview';
      case 'OFFER':
      case 'ACCEPTED': return 'badge-success';
      case 'REJECTED': return 'badge-rejected';
      case 'WITHDRAWN':
      case 'ARCHIVED': return 'badge-muted';
      default: return 'badge-default';
    }
  }
}
