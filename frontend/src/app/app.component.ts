import { Component } from '@angular/core';
import { TailoredResumeWorkbenchComponent } from './features/resume-tailoring/components/tailored-resume-workbench/tailored-resume-workbench.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [TailoredResumeWorkbenchComponent],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'AI Job Command Center';
  status = 'Phase 6 — Resume Tailoring & Versioning Engine Active';
}
