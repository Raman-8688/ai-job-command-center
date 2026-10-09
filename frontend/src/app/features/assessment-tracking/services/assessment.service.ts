import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  AbandonAssessmentRequest,
  AssessmentBriefing,
  AssessmentChecklistBundle,
  AssessmentDashboardSummary,
  AssessmentPlatform,
  AssessmentStatus,
  CreateAssessmentRequest,
  ExpireAssessmentRequest,
  ExtendAssessmentDeadlineRequest,
  GenerateBriefingRequest,
  OnlineAssessment,
  OnlineAssessmentEvent,
  OnlineAssessmentSummary,
  RecordAssessmentResultRequest,
  StartAssessmentRequest,
  SubmitAssessmentRequest,
  ToggleChecklistItemRequest,
  UpdateAssessmentRequest
} from '../models/assessment.model';

@Injectable({
  providedIn: 'root'
})
export class AssessmentService {

  private readonly baseUrl = '/api/assessments';

  constructor(private http: HttpClient) {}

  createAssessment(request: CreateAssessmentRequest): Observable<OnlineAssessment> {
    return this.http.post<OnlineAssessment>(this.baseUrl, request);
  }

  listAssessments(filters?: {
    status?: AssessmentStatus;
    platform?: AssessmentPlatform;
    jobId?: string;
    applicationId?: string;
    expiringWithinHours?: number;
  }): Observable<OnlineAssessmentSummary[]> {
    let params = new HttpParams();
    if (filters?.status) {
      params = params.set('status', filters.status);
    }
    if (filters?.platform) {
      params = params.set('platform', filters.platform);
    }
    if (filters?.jobId) {
      params = params.set('jobId', filters.jobId);
    }
    if (filters?.applicationId) {
      params = params.set('applicationId', filters.applicationId);
    }
    if (filters?.expiringWithinHours !== undefined && filters.expiringWithinHours !== null) {
      params = params.set('expiringWithinHours', filters.expiringWithinHours.toString());
    }

    return this.http.get<OnlineAssessmentSummary[]>(this.baseUrl, { params });
  }

  getDashboardSummary(): Observable<AssessmentDashboardSummary> {
    return this.http.get<AssessmentDashboardSummary>(`${this.baseUrl}/dashboard-summary`);
  }

  getAssessment(id: string): Observable<OnlineAssessment> {
    return this.http.get<OnlineAssessment>(`${this.baseUrl}/${id}`);
  }

  updateAssessment(id: string, request: UpdateAssessmentRequest): Observable<OnlineAssessment> {
    return this.http.patch<OnlineAssessment>(`${this.baseUrl}/${id}`, request);
  }

  startAssessment(id: string, request?: StartAssessmentRequest): Observable<OnlineAssessment> {
    return this.http.post<OnlineAssessment>(`${this.baseUrl}/${id}/start`, request || {});
  }

  submitAssessment(id: string, request: SubmitAssessmentRequest): Observable<OnlineAssessment> {
    return this.http.post<OnlineAssessment>(`${this.baseUrl}/${id}/submit`, request);
  }

  recordResult(id: string, request: RecordAssessmentResultRequest): Observable<OnlineAssessment> {
    return this.http.post<OnlineAssessment>(`${this.baseUrl}/${id}/result`, request);
  }

  extendDeadline(id: string, request: ExtendAssessmentDeadlineRequest): Observable<OnlineAssessment> {
    return this.http.post<OnlineAssessment>(`${this.baseUrl}/${id}/extend-deadline`, request);
  }

  abandonAssessment(id: string, request?: AbandonAssessmentRequest): Observable<OnlineAssessment> {
    return this.http.post<OnlineAssessment>(`${this.baseUrl}/${id}/abandon`, request || {});
  }

  expireAssessment(id: string, request?: ExpireAssessmentRequest): Observable<OnlineAssessment> {
    return this.http.post<OnlineAssessment>(`${this.baseUrl}/${id}/expire`, request || {});
  }

  getEvents(id: string): Observable<OnlineAssessmentEvent[]> {
    return this.http.get<OnlineAssessmentEvent[]>(`${this.baseUrl}/${id}/events`);
  }

  getChecklist(id: string): Observable<AssessmentChecklistBundle> {
    return this.http.get<AssessmentChecklistBundle>(`${this.baseUrl}/${id}/checklist`);
  }

  toggleChecklistItem(id: string, itemId: string, request?: ToggleChecklistItemRequest): Observable<AssessmentChecklistBundle> {
    return this.http.patch<AssessmentChecklistBundle>(`${this.baseUrl}/${id}/checklist/${itemId}`, request || {});
  }

  generateBriefing(id: string, request?: GenerateBriefingRequest): Observable<AssessmentBriefing> {
    return this.http.post<AssessmentBriefing>(`${this.baseUrl}/${id}/briefing`, request || {});
  }
}
