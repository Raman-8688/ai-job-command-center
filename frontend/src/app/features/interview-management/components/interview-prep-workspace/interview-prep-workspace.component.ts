import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { InterviewService } from '../../services/interview.service';
import { InterviewPrepBundle, InterviewPreparation } from '../../models/interview.model';

export interface CompetencyMetric {
  category: string;
  name: string;
  colorClass: string;
  total: number;
  reviewed: number;
  percentage: number;
}

@Component({
  selector: 'app-interview-prep-workspace',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './interview-prep-workspace.component.html',
  styleUrls: ['./interview-prep-workspace.component.css']
})
export class InterviewPrepWorkspaceComponent implements OnInit, OnChanges {

  @Input() interviewId: string | null = null;
  @Output() backToCockpit = new EventEmitter<void>();

  bundle: InterviewPrepBundle | null = null;
  loading: boolean = false;
  selectedCategory: string = 'ALL';
  filteredQuestions: InterviewPreparation[] = [];

  // Expanded questions map for accordion
  expandedQuestions: Record<string, boolean> = {};

  // Dynamic competency metrics grouped by preparation categories
  competencies: CompetencyMetric[] = [];

  private readonly defaultCompetencies: { category: string; name: string; colorClass: string }[] = [
    { category: 'SYSTEM_DESIGN', name: 'System Design & Scalability', colorClass: 'fill-blue' },
    { category: 'TECH', name: 'Technical & Architecture', colorClass: 'fill-purple' },
    { category: 'BEHAVIORAL', name: 'STAR Behavioral Delivery', colorClass: 'fill-green' },
    { category: 'LEADERSHIP', name: 'Leadership & Collaboration', colorClass: 'fill-amber' }
  ];

  // Custom question form modal
  showCustomModal: boolean = false;
  customForm = {
    topicCategory: 'TECH',
    question: '',
    talkingPoints: '',
    suggestedAnswerStar: ''
  };

  categories: string[] = ['ALL', 'TECH', 'SYSTEM_DESIGN', 'BEHAVIORAL', 'LEADERSHIP'];

  constructor(private interviewService: InterviewService) {}

  ngOnInit(): void {
    this.computeCompetencies();
    if (this.interviewId) {
      this.loadPrepBundle();
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['interviewId'] && this.interviewId) {
      this.loadPrepBundle();
    }
  }

  loadPrepBundle(): void {
    if (!this.interviewId) return;
    this.loading = true;

    this.interviewService.getPrepBundle(this.interviewId).subscribe({
      next: (data) => {
        this.bundle = data;
        this.filterQuestions();
        this.computeCompetencies();
        this.loading = false;
        // Expand first question by default
        if (data.questions.length > 0) {
          this.expandedQuestions[data.questions[0].id] = true;
        }
      },
      error: (err) => {
        console.warn('Prep bundle not yet generated, triggering generation:', err);
        this.generateAiPrep();
      }
    });
  }

  generateAiPrep(): void {
    if (!this.interviewId) return;
    this.loading = true;

    this.interviewService.generateAiPrep(this.interviewId).subscribe({
      next: (data) => {
        this.bundle = data;
        this.filterQuestions();
        this.computeCompetencies();
        this.loading = false;
        if (data.questions.length > 0) {
          this.expandedQuestions[data.questions[0].id] = true;
        }
      },
      error: (err) => {
        console.error('Error generating AI prep:', err);
        this.loading = false;
      }
    });
  }

  setCategory(cat: string): void {
    this.selectedCategory = cat;
    this.filterQuestions();
  }

  filterQuestions(): void {
    if (!this.bundle) {
      this.filteredQuestions = [];
      return;
    }
    if (this.selectedCategory === 'ALL') {
      this.filteredQuestions = [...this.bundle.questions];
    } else {
      this.filteredQuestions = this.bundle.questions.filter(
        q => q.topicCategory.toUpperCase() === this.selectedCategory.toUpperCase()
      );
    }
  }

  toggleExpand(id: string): void {
    this.expandedQuestions[id] = !this.expandedQuestions[id];
  }

  saveQuestionNotes(q: InterviewPreparation): void {
    if (!this.interviewId) return;

    this.interviewService.updatePrepNotes(this.interviewId, q.id, {
      userAnswerNotes: q.userAnswerNotes,
      isReviewed: q.isReviewed
    }).subscribe({
      next: () => {
        // Recalculate bundle readiness if needed
        if (this.bundle) {
          const reviewed = this.bundle.questions.filter(item => item.isReviewed).length;
          this.bundle.readinessScore = Math.min(100, Math.round(65 + ((reviewed / this.bundle.questions.length) * 35)));
        }
        this.computeCompetencies();
      },
      error: (err) => {
        console.error('Error saving notes:', err);
        this.computeCompetencies();
      }
    });
  }

  toggleReviewed(q: InterviewPreparation): void {
    q.isReviewed = !q.isReviewed;
    this.computeCompetencies();
    this.saveQuestionNotes(q);
  }

  openCustomModal(): void {
    this.customForm = {
      topicCategory: 'TECH',
      question: '',
      talkingPoints: '',
      suggestedAnswerStar: ''
    };
    this.showCustomModal = true;
  }

  submitCustomQuestion(): void {
    if (!this.interviewId || !this.customForm.question) return;

    this.interviewService.addCustomPrepQuestion(this.interviewId, {
      topicCategory: this.customForm.topicCategory,
      question: this.customForm.question,
      talkingPoints: this.customForm.talkingPoints,
      suggestedAnswerStar: this.customForm.suggestedAnswerStar
    }).subscribe({
      next: (created) => {
        this.showCustomModal = false;
        if (this.bundle) {
          this.bundle.questions.push(created);
          this.filterQuestions();
          this.computeCompetencies();
        }
      },
      error: (err) => alert(err.error?.detail || 'Failed to add custom question')
    });
  }

  computeCompetencies(): void {
    if (!this.bundle || !this.bundle.questions || this.bundle.questions.length === 0) {
      this.competencies = this.defaultCompetencies.map(def => ({
        category: def.category,
        name: def.name,
        colorClass: def.colorClass,
        total: 0,
        reviewed: 0,
        percentage: 0
      }));
      return;
    }

    const allQuestions = this.bundle.questions;
    const computed: CompetencyMetric[] = [];

    for (const def of this.defaultCompetencies) {
      const catQuestions = allQuestions.filter(
        q => q.topicCategory && q.topicCategory.toUpperCase() === def.category
      );
      const total = catQuestions.length;
      const reviewed = catQuestions.filter(q => q.isReviewed).length;
      const percentage = total > 0 ? Math.round((reviewed / total) * 100) : 0;

      computed.push({
        category: def.category,
        name: def.name,
        colorClass: def.colorClass,
        total,
        reviewed,
        percentage
      });
    }

    // Support any custom category present in questions
    const standardKeys = new Set(this.defaultCompetencies.map(d => d.category));
    const extraCategories = Array.from(
      new Set(
        allQuestions
          .map(q => q.topicCategory ? q.topicCategory.toUpperCase() : 'OTHER')
          .filter(cat => !standardKeys.has(cat))
      )
    );

    const fallbackColors = ['fill-blue', 'fill-purple', 'fill-green', 'fill-amber'];
    extraCategories.forEach((cat, index) => {
      const catQuestions = allQuestions.filter(
        q => (q.topicCategory ? q.topicCategory.toUpperCase() : 'OTHER') === cat
      );
      const total = catQuestions.length;
      const reviewed = catQuestions.filter(q => q.isReviewed).length;
      const percentage = total > 0 ? Math.round((reviewed / total) * 100) : 0;
      const label = cat.replace(/_/g, ' ');

      computed.push({
        category: cat,
        name: label.charAt(0).toUpperCase() + label.slice(1).toLowerCase(),
        colorClass: fallbackColors[index % fallbackColors.length],
        total,
        reviewed,
        percentage
      });
    });

    this.competencies = computed;
  }

  getReviewedCount(): number {
    if (!this.bundle) return 0;
    return this.bundle.questions.filter(q => q.isReviewed).length;
  }
}
