import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CompanyDossier, GenerateCompanyDossierRequest } from '../models/company-intel.model';

@Injectable({
  providedIn: 'root'
})
export class CompanyIntelService {

  private readonly baseUrl = '/api/intel';

  constructor(private http: HttpClient) {}

  getCompanyDossier(jobId: string): Observable<CompanyDossier> {
    return this.http.get<CompanyDossier>(`${this.baseUrl}/jobs/${jobId}/company-dossier`);
  }

  generateCompanyDossier(jobId: string, request?: GenerateCompanyDossierRequest): Observable<CompanyDossier> {
    return this.http.post<CompanyDossier>(`${this.baseUrl}/jobs/${jobId}/company-dossier`, request || {});
  }
}
