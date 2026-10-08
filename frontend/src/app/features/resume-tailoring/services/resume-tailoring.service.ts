import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TailoredResume, TailoredResumeStatus, TailoredResumeSummary } from '../models/tailored-resume.model';

@Injectable({
  providedIn: 'root'
})
export class ResumeTailoringService {

  private readonly baseUrl = '/api';

  constructor(private http: HttpClient) {}

  createTailoredDraft(resumeId: string, jobId: string): Observable<TailoredResume> {
    return this.http.post<TailoredResume>(`${this.baseUrl}/resumes/${resumeId}/tailor/${jobId}`, {});
  }

  getTailoredResume(id: string): Observable<TailoredResume> {
    return this.http.get<TailoredResume>(`${this.baseUrl}/tailored-resumes/${id}`);
  }

  getTailoredResumesForResume(resumeId: string): Observable<TailoredResumeSummary[]> {
    return this.http.get<TailoredResumeSummary[]>(`${this.baseUrl}/resumes/${resumeId}/tailored`);
  }

  getTailoredResumesForJob(jobId: string): Observable<TailoredResumeSummary[]> {
    return this.http.get<TailoredResumeSummary[]>(`${this.baseUrl}/jobs/${jobId}/tailored-resumes`);
  }

  updateContent(id: string, tailoredTitle: string, tailoredSummary: string): Observable<TailoredResume> {
    return this.http.put<TailoredResume>(`${this.baseUrl}/tailored-resumes/${id}`, {
      tailoredTitle,
      tailoredSummary
    });
  }

  updateStatus(id: string, status: TailoredResumeStatus): Observable<TailoredResume> {
    return this.http.patch<TailoredResume>(`${this.baseUrl}/tailored-resumes/${id}/status`, {
      status
    });
  }

  applySuggestion(id: string, suggestionId: string): Observable<TailoredResume> {
    return this.http.post<TailoredResume>(`${this.baseUrl}/tailored-resumes/${id}/suggestions/${suggestionId}/apply`, {});
  }

  deleteTailoredResume(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/tailored-resumes/${id}`);
  }
}
