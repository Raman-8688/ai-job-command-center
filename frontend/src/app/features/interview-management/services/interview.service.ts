import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  Interview,
  InterviewDashboardSummary,
  InterviewPrepBundle,
  InterviewPreparation,
  InterviewRound,
  InterviewStatus,
  InterviewSummary,
  RescheduleInterviewRequest,
  SavePrepQuestionRequest,
  ScheduleInterviewRequest,
  UpdateInterviewStatusRequest,
  UpdatePrepNotesRequest
} from '../models/interview.model';

@Injectable({
  providedIn: 'root'
})
export class InterviewService {

  private readonly baseUrl = '/api/interviews';

  constructor(private http: HttpClient) {}

  scheduleInterview(request: ScheduleInterviewRequest): Observable<Interview> {
    return this.http.post<Interview>(this.baseUrl, request);
  }

  listInterviews(status?: InterviewStatus, round?: InterviewRound, after?: string, before?: string): Observable<InterviewSummary[]> {
    let params = new HttpParams();
    if (status) params = params.set('status', status);
    if (round) params = params.set('round', round);
    if (after) params = params.set('scheduledAfter', after);
    if (before) params = params.set('scheduledBefore', before);

    return this.http.get<InterviewSummary[]>(this.baseUrl, { params });
  }

  getDashboardSummary(): Observable<InterviewDashboardSummary> {
    return this.http.get<InterviewDashboardSummary>(`${this.baseUrl}/dashboard-summary`);
  }

  getInterview(id: string): Observable<Interview> {
    return this.http.get<Interview>(`${this.baseUrl}/${id}`);
  }

  rescheduleInterview(id: string, request: RescheduleInterviewRequest): Observable<Interview> {
    return this.http.post<Interview>(`${this.baseUrl}/${id}/reschedule`, request);
  }

  updateStatus(id: string, request: UpdateInterviewStatusRequest): Observable<Interview> {
    return this.http.post<Interview>(`${this.baseUrl}/${id}/status`, request);
  }

  deleteInterview(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  generateAiPrep(id: string): Observable<InterviewPrepBundle> {
    return this.http.post<InterviewPrepBundle>(`${this.baseUrl}/${id}/ai-prep`, {});
  }

  getPrepBundle(id: string): Observable<InterviewPrepBundle> {
    return this.http.get<InterviewPrepBundle>(`${this.baseUrl}/${id}/prep`);
  }

  updatePrepNotes(interviewId: string, prepId: string, request: UpdatePrepNotesRequest): Observable<InterviewPreparation> {
    return this.http.put<InterviewPreparation>(`${this.baseUrl}/${interviewId}/prep/${prepId}`, request);
  }

  addCustomPrepQuestion(interviewId: string, request: SavePrepQuestionRequest): Observable<InterviewPreparation> {
    return this.http.post<InterviewPreparation>(`${this.baseUrl}/${interviewId}/prep`, request);
  }
}
