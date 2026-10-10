import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AnalyticsService } from '../../services/analytics.service';
import {
  AnalyticsOverview,
  FunnelMetrics,
  SourceEffectiveness,
  SkillGapMetric,
  AIAnalyticsAdvisorResponse,
  ApplicationSource
} from '../../models/analytics.model';

@Component({
  selector: 'app-analytics-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './analytics-dashboard.component.html',
  styleUrls: ['./analytics-dashboard.component.css']
})
export class AnalyticsDashboardComponent implements OnInit {
  overview: AnalyticsOverview | null = null;
  funnel: FunnelMetrics | null = null;
  sources: SourceEffectiveness[] = [];
  skills: SkillGapMetric[] = [];
  insights: AIAnalyticsAdvisorResponse | null = null;

  // Global & Per-Section Loading States
  loadingData = false;
  overviewLoading = false;
  funnelLoading = false;
  sourcesLoading = false;
  skillsLoading = false;
  loadingInsights = false;

  // Global & Per-Section Error States
  globalError: string | null = null;
  overviewError: string | null = null;
  funnelError: string | null = null;
  sourcesError: string | null = null;
  skillsError: string | null = null;
  insightsError: string | null = null;

  skillFilter: 'ALL' | 'UNVERIFIED' | 'VERIFIED' = 'ALL';
  activeSection: 'overview' | 'funnel' | 'sources' | 'skills' | 'insights' = 'overview';

  constructor(private analyticsService: AnalyticsService) {}

  ngOnInit(): void {
    this.loadAllData();
  }

  /**
   * Loads all four deterministic analytical datasets concurrently with isolated error boundaries.
   * A failure in one section does not conceal or invalidate data in other sections.
   */
  loadAllData(): void {
    this.loadingData = true;
    this.overviewLoading = true;
    this.funnelLoading = true;
    this.sourcesLoading = true;
    this.skillsLoading = true;

    this.globalError = null;
    this.overviewError = null;
    this.funnelError = null;
    this.sourcesError = null;
    this.skillsError = null;

    forkJoin({
      overview: this.analyticsService.getOverview().pipe(
        catchError((err) => {
          this.overviewError = err?.error?.detail || err?.message || 'Failed to load overview KPIs.';
          return of(null);
        })
      ),
      funnel: this.analyticsService.getFunnel().pipe(
        catchError((err) => {
          this.funnelError = err?.error?.detail || err?.message || 'Failed to load conversion funnel.';
          return of(null);
        })
      ),
      sources: this.analyticsService.getSources().pipe(
        catchError((err) => {
          this.sourcesError = err?.error?.detail || err?.message || 'Failed to load source effectiveness.';
          return of([] as SourceEffectiveness[]);
        })
      ),
      skills: this.analyticsService.getSkills().pipe(
        catchError((err) => {
          this.skillsError = err?.error?.detail || err?.message || 'Failed to load skill demands.';
          return of([] as SkillGapMetric[]);
        })
      )
    }).subscribe({
      next: (results) => {
        if (results.overview) this.overview = results.overview;
        if (results.funnel) this.funnel = results.funnel;
        if (results.sources) this.sources = results.sources;
        if (results.skills) this.skills = results.skills;

        // If all 4 sections failed simultaneously, surface global connection alert
        if (this.overviewError && this.funnelError && this.sourcesError && this.skillsError) {
          this.globalError = 'Failed to load career search analytics. Please verify server connection and retry.';
        }

        this.loadingData = false;
        this.overviewLoading = false;
        this.funnelLoading = false;
        this.sourcesLoading = false;
        this.skillsLoading = false;
      },
      error: (err) => {
        this.loadingData = false;
        this.overviewLoading = false;
        this.funnelLoading = false;
        this.sourcesLoading = false;
        this.skillsLoading = false;
        this.globalError = err?.error?.detail || err?.message || 'Unexpected failure loading analytics.';
      }
    });
  }

  loadOverview(): void {
    this.overviewLoading = true;
    this.overviewError = null;
    this.analyticsService.getOverview().subscribe({
      next: (data) => {
        this.overview = data;
        this.overviewLoading = false;
      },
      error: (err) => {
        this.overviewLoading = false;
        this.overviewError = err?.error?.detail || err?.message || 'Failed to load overview KPIs.';
      }
    });
  }

  loadFunnel(): void {
    this.funnelLoading = true;
    this.funnelError = null;
    this.analyticsService.getFunnel().subscribe({
      next: (data) => {
        this.funnel = data;
        this.funnelLoading = false;
      },
      error: (err) => {
        this.funnelLoading = false;
        this.funnelError = err?.error?.detail || err?.message || 'Failed to load conversion funnel.';
      }
    });
  }

  loadSources(): void {
    this.sourcesLoading = true;
    this.sourcesError = null;
    this.analyticsService.getSources().subscribe({
      next: (data) => {
        this.sources = data;
        this.sourcesLoading = false;
      },
      error: (err) => {
        this.sourcesLoading = false;
        this.sourcesError = err?.error?.detail || err?.message || 'Failed to load source effectiveness.';
      }
    });
  }

  loadSkills(): void {
    this.skillsLoading = true;
    this.skillsError = null;
    this.analyticsService.getSkills().subscribe({
      next: (data) => {
        this.skills = data;
        this.skillsLoading = false;
      },
      error: (err) => {
        this.skillsLoading = false;
        this.skillsError = err?.error?.detail || err?.message || 'Failed to load skill demands.';
      }
    });
  }

  generateCareerInsights(): void {
    if (this.loadingInsights) {
      return;
    }
    this.loadingInsights = true;
    this.insightsError = null;

    this.analyticsService.generateInsights().subscribe({
      next: (response) => {
        this.insights = response;
        this.loadingInsights = false;
        this.activeSection = 'insights';
      },
      error: (err) => {
        this.loadingInsights = false;
        this.insightsError =
          err?.error?.detail ||
          err?.message ||
          'Failed to generate career strategy insights. Please retry.';
      }
    });
  }

  get filteredSkills(): SkillGapMetric[] {
    if (this.skillFilter === 'UNVERIFIED') {
      return this.skills.filter((s) => !s.candidateVerified);
    }
    if (this.skillFilter === 'VERIFIED') {
      return this.skills.filter((s) => s.candidateVerified);
    }
    return this.skills;
  }

  formatPercent(val: number | null | undefined): string {
    if (val === null || val === undefined || isNaN(val)) {
      return '0.0%';
    }
    return `${val.toFixed(1)}%`;
  }

  getSourceDisplayName(source: ApplicationSource | string | null | undefined): string {
    if (!source) {
      return 'Direct / Unspecified';
    }
    switch (source) {
      case 'LINKEDIN':
        return 'LinkedIn';
      case 'INDEED':
        return 'Indeed';
      case 'COMPANY_WEBSITE':
        return 'Company Careers Portal';
      case 'EMAIL':
        return 'Direct Email Submission';
      case 'REFERRAL':
        return 'Personal / Employee Referral';
      case 'RECRUITER':
        return 'Recruiter / Agency Outreach';
      case 'MANUAL':
        return 'Manual Direct Entry';
      case 'OTHER':
        return 'Other Channels';
      default:
        return source;
    }
  }

  getSeverityBadgeClass(severity: string): string {
    switch (severity?.toUpperCase()) {
      case 'HIGH':
        return 'badge-danger';
      case 'MEDIUM':
        return 'badge-warning';
      default:
        return 'badge-info';
    }
  }

  getPriorityBadgeClass(priority: string): string {
    switch (priority?.toUpperCase()) {
      case 'HIGH':
        return 'badge-danger';
      case 'MEDIUM':
        return 'badge-warning';
      default:
        return 'badge-info';
    }
  }

  formatDate(dateStr: string | null | undefined): string {
    if (!dateStr) return 'N/A';
    try {
      const d = new Date(dateStr);
      return d.toLocaleDateString(undefined, {
        month: 'short',
        day: 'numeric',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    } catch {
      return dateStr;
    }
  }
}
