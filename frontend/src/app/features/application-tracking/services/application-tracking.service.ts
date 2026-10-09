import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  ApplicationDashboardSummary,
  ApplicationGuidance,
  ApplicationPageResponse,
  ApplicationStatus,
  CreateApplicationRequest,
  JobApplication,
  JobApplicationEvent,
  LinkResumeRequest,
  TransitionStatusRequest
} from '../models/job-application.model';

@Injectable({
  providedIn: 'root'
})
export class ApplicationTrackingService {

  private readonly baseUrl = '/api/applications';

  constructor(private http: HttpClient) {}

  createApplication(request: CreateApplicationRequest): Observable<JobApplication> {
    return this.http.post<JobApplication>(this.baseUrl, request);
  }

  listApplications(status?: ApplicationStatus, search?: string, page = 0, size = 20): Observable<ApplicationPageResponse> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (status) {
      params = params.set('status', status);
    }
    if (search && search.trim()) {
      params = params.set('search', search.trim());
    }

    return this.http.get<ApplicationPageResponse>(this.baseUrl, { params });
  }

  getDashboardSummary(): Observable<ApplicationDashboardSummary> {
    return this.http.get<ApplicationDashboardSummary>(`${this.baseUrl}/dashboard-summary`);
  }

  getApplication(id: string): Observable<JobApplication> {
    return this.http.get<JobApplication>(`${this.baseUrl}/${id}`);
  }

  updateApplication(id: string, updateReq: Partial<CreateApplicationRequest>): Observable<JobApplication> {
    return this.http.put<JobApplication>(`${this.baseUrl}/${id}`, updateReq);
  }

  transitionStatus(id: string, transitionReq: TransitionStatusRequest): Observable<JobApplication> {
    return this.http.post<JobApplication>(`${this.baseUrl}/${id}/transition`, transitionReq);
  }

  linkResume(id: string, linkReq: LinkResumeRequest): Observable<JobApplication> {
    return this.http.post<JobApplication>(`${this.baseUrl}/${id}/link-resume`, linkReq);
  }

  getTimeline(id: string): Observable<JobApplicationEvent[]> {
    return this.http.get<JobApplicationEvent[]>(`${this.baseUrl}/${id}/events`);
  }

  getGuidance(id: string): Observable<ApplicationGuidance> {
    return this.http.get<ApplicationGuidance>(`${this.baseUrl}/${id}/guidance`);
  }

  deleteApplication(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
