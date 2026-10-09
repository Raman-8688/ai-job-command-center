import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TailoredResumeWorkbenchComponent } from './features/resume-tailoring/components/tailored-resume-workbench/tailored-resume-workbench.component';
import { ApplicationTrackerComponent } from './features/application-tracking/components/application-tracker/application-tracker.component';
import { InterviewCockpitComponent } from './features/interview-management/components/interview-cockpit/interview-cockpit.component';
import { InterviewPrepWorkspaceComponent } from './features/interview-management/components/interview-prep-workspace/interview-prep-workspace.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    TailoredResumeWorkbenchComponent,
    ApplicationTrackerComponent,
    InterviewCockpitComponent,
    InterviewPrepWorkspaceComponent
  ],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'AI Job Command Center';
  status = 'Phase 9 — Interview Management, AI Preparation & Scheduling Active';
  activeTab: 'applications' | 'tailoring' | 'interviews' | 'prep' = 'interviews';
  selectedInterviewIdForPrep: string | null = null;

  navigateToPrep(interviewId: string): void {
    this.selectedInterviewIdForPrep = interviewId;
    this.activeTab = 'prep';
  }

  returnToCockpit(): void {
    this.activeTab = 'interviews';
  }
}
