import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CompanyIntelService } from '../../services/company-intel.service';
import { CompanyDossier } from '../../models/company-intel.model';

@Component({
  selector: 'app-company-dossier',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './company-dossier.component.html',
  styleUrls: ['./company-dossier.component.css']
})
export class CompanyDossierComponent implements OnInit, OnChanges {

  @Input() initialJobId?: string;

  targetJobId: string = '';
  dossier: CompanyDossier | null = null;
  loading: boolean = false;
  generating: boolean = false;
  errorMessage: string = '';
  successMessage: string = '';
  notFound: boolean = false;

  constructor(private intelService: CompanyIntelService) {}

  ngOnInit(): void {
    if (this.initialJobId) {
      this.targetJobId = this.initialJobId;
      this.fetchDossier();
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['initialJobId'] && this.initialJobId) {
      this.targetJobId = this.initialJobId;
      this.fetchDossier();
    }
  }

  fetchDossier(): void {
    if (!this.targetJobId.trim()) return;

    this.loading = true;
    this.errorMessage = '';
    this.notFound = false;

    this.intelService.getCompanyDossier(this.targetJobId.trim()).subscribe({
      next: (d) => {
        this.dossier = d;
        this.loading = false;
        this.notFound = false;
      },
      error: (err) => {
        this.loading = false;
        if (err.status === 404) {
          this.notFound = true;
          this.dossier = null;
        } else {
          this.errorMessage = 'Failed to load company dossier: ' + (err.error?.detail || 'Unexpected error');
        }
      }
    });
  }

  generateDossier(): void {
    if (!this.targetJobId.trim() || this.generating) return;

    this.generating = true;
    this.errorMessage = '';

    this.intelService.generateCompanyDossier(this.targetJobId.trim(), {}).subscribe({
      next: (d) => {
        this.dossier = d;
        this.generating = false;
        this.notFound = false;
        this.showToast('Grounded company technical dossier generated successfully!');
      },
      error: (err) => {
        this.generating = false;
        this.errorMessage = 'Failed to generate technical dossier: ' + (err.error?.detail || 'Grounding error');
        console.error(err);
      }
    });
  }

  formatDate(dateStr?: string): string {
    if (!dateStr) return 'N/A';
    return new Date(dateStr).toLocaleString(undefined, {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  formatParagraphs(text?: string): string[] {
    if (!text) return [];
    return text.split('\n').filter(p => p.trim().length > 0);
  }

  showToast(msg: string): void {
    this.successMessage = msg;
    setTimeout(() => {
      if (this.successMessage === msg) {
        this.successMessage = '';
      }
    }, 4000);
  }
}
