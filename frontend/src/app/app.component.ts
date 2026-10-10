import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TailoredResumeWorkbenchComponent } from './features/resume-tailoring/components/tailored-resume-workbench/tailored-resume-workbench.component';
import { ApplicationTrackerComponent } from './features/application-tracking/components/application-tracker/application-tracker.component';
import { InterviewCockpitComponent } from './features/interview-management/components/interview-cockpit/interview-cockpit.component';
import { InterviewPrepWorkspaceComponent } from './features/interview-management/components/interview-prep-workspace/interview-prep-workspace.component';
import { AssessmentTrackerComponent } from './features/assessment-tracking/components/assessment-tracker/assessment-tracker.component';
import { CompanyDossierComponent } from './features/company-intel/components/company-dossier/company-dossier.component';
import { AnalyticsDashboardComponent } from './features/analytics-dashboard/components/analytics-dashboard/analytics-dashboard.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    AnalyticsDashboardComponent,
    AssessmentTrackerComponent,
    CompanyDossierComponent,
    InterviewCockpitComponent,
    InterviewPrepWorkspaceComponent,
    ApplicationTrackerComponent,
    TailoredResumeWorkbenchComponent
  ],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'AI Job Command Center';
  status = 'Phase 11 — Analytics & Career Strategy Active';
  activeTab: 'analytics' | 'assessments' | 'company-intel' | 'interviews' | 'prep' | 'applications' | 'tailoring' = 'analytics';
  selectedInterviewIdForPrep: string | null = null;
  selectedJobIdForIntel: string | undefined = undefined;

  navigateToPrep(interviewId: string): void {
    this.selectedInterviewIdForPrep = interviewId;
    this.activeTab = 'prep';
  }

  navigateToCompanyIntel(jobId: string): void {
    this.selectedJobIdForIntel = jobId;
    this.activeTab = 'company-intel';
  }

  returnToCockpit(): void {
    this.activeTab = 'interviews';
  }
}
