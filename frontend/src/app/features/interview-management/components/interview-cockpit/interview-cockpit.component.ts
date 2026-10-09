import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { InterviewService } from '../../services/interview.service';
import {
  InterviewDashboardSummary,
  InterviewFormat,
  InterviewOutcome,
  InterviewRound,
  InterviewStatus,
  InterviewSummary,
  RescheduleInterviewRequest,
  ScheduleInterviewRequest
} from '../../models/interview.model';

interface CalendarDay {
  date: Date;
  dayName: string;
  dayNumber: number;
  isToday: boolean;
  interviews: InterviewSummary[];
}

@Component({
  selector: 'app-interview-cockpit',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './interview-cockpit.component.html',
  styleUrls: ['./interview-cockpit.component.css']
})
export class InterviewCockpitComponent implements OnInit {

  @Output() openPrep = new EventEmitter<string>();

  summary: InterviewDashboardSummary | null = null;
  interviews: InterviewSummary[] = [];
  filteredInterviews: InterviewSummary[] = [];

  selectedRoundFilter: string = 'ALL';
  selectedStatusFilter: string = 'ALL';
  viewMode: 'calendar' | 'list' = 'calendar';
  currentWeekStart: Date = new Date();
  calendarDays: CalendarDay[] = [];

  // Modals state
  showScheduleModal: boolean = false;
  showRescheduleModal: boolean = false;
  showCompleteModal: boolean = false;
  selectedInterviewForAction: InterviewSummary | null = null;

  // Schedule Form
  scheduleForm = {
    jobId: '',
    applicationId: '',
    round: 'TECHNICAL_SCREEN' as InterviewRound,
    roundNumber: 1,
    format: 'VIDEO_CALL' as InterviewFormat,
    startDate: '',
    startTime: '10:00',
    durationMinutes: 45,
    timeZone: 'UTC',
    meetingLink: '',
    location: '',
    interviewerNames: '',
    interviewerRoles: '',
    notes: ''
  };

  // Reschedule Form
  rescheduleForm = {
    newDate: '',
    newTime: '11:00',
    durationMinutes: 45,
    timeZone: 'UTC',
    reason: ''
  };

  // Complete Form
  completeForm = {
    outcome: 'PASSED' as InterviewOutcome,
    feedback: '',
    notes: ''
  };

  rounds: { value: InterviewRound; label: string }[] = [
    { value: 'INITIAL_SCREEN', label: 'Initial Screen' },
    { value: 'TECHNICAL_SCREEN', label: 'Technical Screen' },
    { value: 'SYSTEM_DESIGN', label: 'System Design' },
    { value: 'BEHAVIORAL_CULTURE', label: 'Behavioral & Culture' },
    { value: 'HIRING_MANAGER', label: 'Hiring Manager' },
    { value: 'FINAL_ROUND', label: 'Final Round' },
    { value: 'OTHER', label: 'Other Round' }
  ];

  formats: { value: InterviewFormat; label: string }[] = [
    { value: 'VIDEO_CALL', label: 'Video Call (Zoom/Meet)' },
    { value: 'PHONE_SCREEN', label: 'Phone Call' },
    { value: 'ON_SITE', label: 'On-Site' },
    { value: 'ONLINE_ASSESSMENT', label: 'Online Assessment' },
    { value: 'TAKE_HOME_REVIEW', label: 'Take Home Review' },
    { value: 'PANEL', label: 'Panel Interview' }
  ];

  constructor(private interviewService: InterviewService) {
    this.initCurrentWeek();
  }

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.interviewService.getDashboardSummary().subscribe({
      next: (data) => this.summary = data,
      error: (err) => console.error('Error fetching interview summary:', err)
    });

    this.interviewService.listInterviews().subscribe({
      next: (data) => {
        this.interviews = data;
        this.applyFilters();
      },
      error: (err) => console.error('Error listing interviews:', err)
    });
  }

  initCurrentWeek(): void {
    const today = new Date();
    const day = today.getDay();
    const diff = today.getDate() - day + (day === 0 ? -6 : 1); // Monday as start
    this.currentWeekStart = new Date(today.setDate(diff));
    this.currentWeekStart.setHours(0, 0, 0, 0);
    this.buildCalendarDays();
  }

  buildCalendarDays(): void {
    this.calendarDays = [];
    const todayStr = new Date().toDateString();

    for (let i = 0; i < 7; i++) {
      const d = new Date(this.currentWeekStart);
      d.setDate(d.getDate() + i);

      const dayInterviews = this.filteredInterviews.filter(item => {
        const itemDate = new Date(item.scheduledStartTime);
        return itemDate.toDateString() === d.toDateString();
      });

      this.calendarDays.push({
        date: d,
        dayName: d.toLocaleDateString('en-US', { weekday: 'short' }),
        dayNumber: d.getDate(),
        isToday: d.toDateString() === todayStr,
        interviews: dayInterviews
      });
    }
  }

  changeWeek(offset: number): void {
    this.currentWeekStart.setDate(this.currentWeekStart.getDate() + (offset * 7));
    this.buildCalendarDays();
  }

  setRoundFilter(round: string): void {
    this.selectedRoundFilter = round;
    this.applyFilters();
  }

  applyFilters(): void {
    this.filteredInterviews = this.interviews.filter(item => {
      const matchesRound = this.selectedRoundFilter === 'ALL' || item.round === this.selectedRoundFilter;
      const matchesStatus = this.selectedStatusFilter === 'ALL' || item.status === this.selectedStatusFilter;
      return matchesRound && matchesStatus;
    });
    this.buildCalendarDays();
  }

  openScheduleModal(): void {
    const today = new Date();
    const yyyy = today.getFullYear();
    const mm = String(today.getMonth() + 1).padStart(2, '0');
    const dd = String(today.getDate()).padStart(2, '0');

    this.scheduleForm.startDate = `${yyyy}-${mm}-${dd}`;
    this.showScheduleModal = true;
  }

  submitSchedule(): void {
    if (!this.scheduleForm.jobId) {
      alert('Please provide a Job ID');
      return;
    }

    const startIso = new Date(`${this.scheduleForm.startDate}T${this.scheduleForm.startTime}:00`).toISOString();
    const endDateObj = new Date(new Date(`${this.scheduleForm.startDate}T${this.scheduleForm.startTime}:00`).getTime() + (this.scheduleForm.durationMinutes * 60000));
    const endIso = endDateObj.toISOString();

    const request: ScheduleInterviewRequest = {
      jobId: this.scheduleForm.jobId,
      applicationId: this.scheduleForm.applicationId || undefined,
      round: this.scheduleForm.round,
      roundNumber: this.scheduleForm.roundNumber,
      format: this.scheduleForm.format,
      scheduledStartTime: startIso,
      scheduledEndTime: endIso,
      timeZone: this.scheduleForm.timeZone,
      meetingLink: this.scheduleForm.meetingLink || undefined,
      location: this.scheduleForm.location || undefined,
      interviewerNames: this.scheduleForm.interviewerNames || undefined,
      interviewerRoles: this.scheduleForm.interviewerRoles || undefined,
      notes: this.scheduleForm.notes || undefined
    };

    this.interviewService.scheduleInterview(request).subscribe({
      next: () => {
        this.showScheduleModal = false;
        this.loadData();
      },
      error: (err) => alert(err.error?.detail || 'Failed to schedule interview')
    });
  }

  openRescheduleModal(interview: InterviewSummary): void {
    this.selectedInterviewForAction = interview;
    const dateObj = new Date(interview.scheduledStartTime);
    const yyyy = dateObj.getFullYear();
    const mm = String(dateObj.getMonth() + 1).padStart(2, '0');
    const dd = String(dateObj.getDate()).padStart(2, '0');

    this.rescheduleForm.newDate = `${yyyy}-${mm}-${dd}`;
    this.rescheduleForm.newTime = '14:00';
    this.showRescheduleModal = true;
  }

  submitReschedule(): void {
    if (!this.selectedInterviewForAction) return;

    const startIso = new Date(`${this.rescheduleForm.newDate}T${this.rescheduleForm.newTime}:00`).toISOString();
    const endDateObj = new Date(new Date(`${this.rescheduleForm.newDate}T${this.rescheduleForm.newTime}:00`).getTime() + (this.rescheduleForm.durationMinutes * 60000));
    const endIso = endDateObj.toISOString();

    const request: RescheduleInterviewRequest = {
      scheduledStartTime: startIso,
      scheduledEndTime: endIso,
      timeZone: this.rescheduleForm.timeZone,
      reason: this.rescheduleForm.reason
    };

    this.interviewService.rescheduleInterview(this.selectedInterviewForAction.id, request).subscribe({
      next: () => {
        this.showRescheduleModal = false;
        this.loadData();
      },
      error: (err) => alert(err.error?.detail || 'Failed to reschedule interview')
    });
  }

  openCompleteModal(interview: InterviewSummary): void {
    this.selectedInterviewForAction = interview;
    this.completeForm = {
      outcome: 'PASSED',
      feedback: '',
      notes: ''
    };
    this.showCompleteModal = true;
  }

  submitComplete(): void {
    if (!this.selectedInterviewForAction) return;

    this.interviewService.updateStatus(this.selectedInterviewForAction.id, {
      status: 'COMPLETED',
      outcome: this.completeForm.outcome,
      feedback: this.completeForm.feedback,
      notes: this.completeForm.notes
    }).subscribe({
      next: () => {
        this.showCompleteModal = false;
        this.loadData();
      },
      error: (err) => alert(err.error?.detail || 'Failed to complete interview')
    });
  }

  triggerPrep(interviewId: string): void {
    this.openPrep.emit(interviewId);
  }

  formatRoundName(round: InterviewRound): string {
    return round.replace(/_/g, ' ');
  }

  formatDate(dateStr: string): string {
    return new Date(dateStr).toLocaleString('en-US', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }
}
