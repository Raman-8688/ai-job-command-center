import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TailoredResumeWorkbenchComponent } from './features/resume-tailoring/components/tailored-resume-workbench/tailored-resume-workbench.component';
import { ApplicationTrackerComponent } from './features/application-tracking/components/application-tracker/application-tracker.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, TailoredResumeWorkbenchComponent, ApplicationTrackerComponent],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'AI Job Command Center';
  status = 'Phase 8 — Application Tracking & Lifecycle Engine Active';
  activeTab: 'applications' | 'tailoring' = 'applications';
}
