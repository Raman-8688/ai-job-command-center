import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, forkJoin } from 'rxjs';
import {
  AnalyticsOverview,
  FunnelMetrics,
  SourceEffectiveness,
  SkillGapMetric,
  AIAnalyticsAdvisorResponse
} from '../models/analytics.model';

@Injectable({
  providedIn: 'root'
})
export class AnalyticsService {
  private readonly baseUrl = '/api/analytics';

  constructor(private http: HttpClient) {}

  getOverview(): Observable<AnalyticsOverview> {
    return this.http.get<AnalyticsOverview>(`${this.baseUrl}/overview`);
  }

  getFunnel(): Observable<FunnelMetrics> {
    return this.http.get<FunnelMetrics>(`${this.baseUrl}/funnel`);
  }

  getSources(): Observable<SourceEffectiveness[]> {
    return this.http.get<SourceEffectiveness[]>(`${this.baseUrl}/sources`);
  }

  getSkills(): Observable<SkillGapMetric[]> {
    return this.http.get<SkillGapMetric[]>(`${this.baseUrl}/skills`);
  }

  generateInsights(): Observable<AIAnalyticsAdvisorResponse> {
    return this.http.post<AIAnalyticsAdvisorResponse>(`${this.baseUrl}/insights`, {});
  }

  /**
   * Loads all four deterministic analytical datasets in parallel.
   */
  getAllAnalytics(): Observable<{
    overview: AnalyticsOverview;
    funnel: FunnelMetrics;
    sources: SourceEffectiveness[];
    skills: SkillGapMetric[];
  }> {
    return forkJoin({
      overview: this.getOverview(),
      funnel: this.getFunnel(),
      sources: this.getSources(),
      skills: this.getSkills()
    });
  }
}
