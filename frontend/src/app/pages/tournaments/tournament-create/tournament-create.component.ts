import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { TournamentService } from '../../../core/services/tournament.service';
import { TournamentFormat } from '../../../core/models/tournament.model';

@Component({
  selector: 'app-tournament-create',
  templateUrl: './tournament-create.component.html',
  styleUrls: ['./tournament-create.component.css']
})
export class TournamentCreateComponent implements OnInit {
  currentStep = 1;
  totalSteps = 4;
  tournamentForm: FormGroup;
  submitting = false;
  savingDraft = false;
  bannerFile: File | null = null;
  bannerPreview: string | null = null;

  tournamentFormats = [
    { value: TournamentFormat.SINGLE_ELIMINATION, label: 'Single Elimination' },
    { value: TournamentFormat.DOUBLE_ELIMINATION, label: 'Double Elimination' },
    { value: TournamentFormat.ROUND_ROBIN, label: 'Round Robin' },
    { value: TournamentFormat.SWISS, label: 'Swiss System' }
  ];

  gameModes = [
    { value: 'SOLO', label: 'Solo (1v1)' },
    { value: 'DUO', label: 'Duo (2v2)' },
    { value: 'TEAM_5V5', label: 'Team (5v5)' }
  ];

  regions = [
    { value: 'NA', label: 'North America' },
    { value: 'EU', label: 'Europe' },
    { value: 'ASIA', label: 'Asia' },
    { value: 'OCE', label: 'Oceania' },
    { value: 'SA', label: 'South America' }
  ];

  availableMaps = [
    'Bind', 'Haven', 'Split', 'Ascent', 'Icebox', 'Breeze', 'Fracture', 'Pearl', 'Lotus', 'Sunset'
  ];

  matchFormats = [
    { value: 'BO1', label: 'Best of 1' },
    { value: 'BO3', label: 'Best of 3' },
    { value: 'BO5', label: 'Best of 5' }
  ];

  constructor(
    private fb: FormBuilder,
    private tournamentService: TournamentService,
    private router: Router,
    private http: HttpClient
  ) {
    this.tournamentForm = this.createForm();
  }

  ngOnInit(): void {}

  createForm(): FormGroup {
    return this.fb.group({
      // Step 1: Basic Info
      name: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
      description: ['', [Validators.required, Validators.minLength(10)]],
      bannerUrl: [''],

      // Step 2: Configuration
      gameMode: ['TEAM_5V5', Validators.required],
      format: [TournamentFormat.SINGLE_ELIMINATION, Validators.required],
      maxParticipants: [16, [Validators.required, Validators.min(4), Validators.max(128)]],
      minParticipants: [4, [Validators.required, Validators.min(2)]],
      teamSize: [5, [Validators.required, Validators.min(1)]],
      region: ['NA', Validators.required],
      minRank: [''],
      maxRank: [''],

      // Step 3: Schedule
      registrationStart: ['', Validators.required],
      registrationEnd: ['', Validators.required],
      checkInStart: ['', Validators.required],
      checkInEnd: ['', Validators.required],
      tournamentStart: ['', Validators.required],

      // Step 4: Rules & Prizes
      rules: ['', [Validators.required, Validators.minLength(20)]],
      prizePool: [0, [Validators.min(0)]],
      entryFee: [0, [Validators.min(0)]],
      matchFormat: ['BO3', Validators.required],
      mapPool: [[]],
      allowSubstitutes: [false],
      maxSubstitutes: [0, [Validators.min(0), Validators.max(5)]],
      prizeDistribution: [{}]
    });
  }

  nextStep(): void {
    if (this.isStepValid(this.currentStep)) {
      if (this.currentStep < this.totalSteps) {
        this.currentStep++;
      }
    }
  }

  previousStep(): void {
    if (this.currentStep > 1) {
      this.currentStep--;
    }
  }

  goToStep(step: number): void {
    if (step <= this.currentStep || this.isStepValid(step - 1)) {
      this.currentStep = step;
    }
  }

  isStepValid(step: number): boolean {
    const stepFields = this.getStepFields(step);
    return stepFields.every(field => {
      const control = this.tournamentForm.get(field);
      return control && control.valid;
    });
  }

  getStepFields(step: number): string[] {
    switch (step) {
      case 1:
        return ['name', 'description'];
      case 2:
        return ['gameMode', 'format', 'maxParticipants', 'minParticipants', 'teamSize', 'region'];
      case 3:
        return ['registrationStart', 'registrationEnd', 'checkInStart', 'checkInEnd', 'tournamentStart'];
      case 4:
        return ['rules', 'matchFormat'];
      default:
        return [];
    }
  }

  toggleMap(map: string): void {
    const mapPool = this.tournamentForm.get('mapPool')?.value || [];
    const index = mapPool.indexOf(map);
    
    if (index > -1) {
      mapPool.splice(index, 1);
    } else {
      mapPool.push(map);
    }
    
    this.tournamentForm.patchValue({ mapPool });
  }

  isMapSelected(map: string): boolean {
    const mapPool = this.tournamentForm.get('mapPool')?.value || [];
    return mapPool.includes(map);
  }

  saveDraft(): void {
    this.savingDraft = true;
    const formData = this.prepareFormData();
    
    this.tournamentService.createTournament(formData).subscribe({
      next: (response) => {
        if (response.success) {
          this.router.navigate(['/tournaments/organizer']);
        }
        this.savingDraft = false;
      },
      error: () => {
        this.savingDraft = false;
      }
    });
  }

  submit(): void {
    if (this.tournamentForm.valid) {
      this.submitting = true;
      
      // Upload banner first if exists
      if (this.bannerFile) {
        this.uploadBanner().then(bannerUrl => {
          this.tournamentForm.patchValue({ bannerUrl });
          this.createTournament();
        }).catch(() => {
          alert('Failed to upload banner');
          this.submitting = false;
        });
      } else {
        this.createTournament();
      }
    }
  }
  
  private uploadBanner(): Promise<string> {
    return new Promise((resolve, reject) => {
      const formData = new FormData();
      formData.append('file', this.bannerFile!);
      
      this.http.post<any>('http://localhost:8080/api/upload/tournament-banner', formData)
        .subscribe({
          next: (response) => {
            if (response.success) {
              resolve(response.data);
            } else {
              reject();
            }
          },
          error: () => reject()
        });
    });
  }
  
  private createTournament(): void {
    const formData = this.prepareFormData();
    
    this.tournamentService.createTournament(formData).subscribe({
      next: (response) => {
        if (response.success) {
          this.router.navigate(['/tournaments', response.data.id]);
        }
        this.submitting = false;
      },
      error: () => {
        this.submitting = false;
      }
    });
  }

  prepareFormData(): any {
    const formValue = this.tournamentForm.value;
    
    // Convert date strings to ISO format
    return {
      ...formValue,
      registrationStart: new Date(formValue.registrationStart).toISOString(),
      registrationEnd: new Date(formValue.registrationEnd).toISOString(),
      checkInStart: new Date(formValue.checkInStart).toISOString(),
      checkInEnd: new Date(formValue.checkInEnd).toISOString(),
      tournamentStart: new Date(formValue.tournamentStart).toISOString()
    };
  }

  cancel(): void {
    if (confirm('Are you sure you want to cancel? All progress will be lost.')) {
      this.router.navigate(['/tournaments/organizer']);
    }
  }

  getStepTitle(step: number): string {
    switch (step) {
      case 1: return 'Basic Information';
      case 2: return 'Configuration';
      case 3: return 'Schedule';
      case 4: return 'Rules & Prizes';
      default: return '';
    }
  }

  get progressPercentage(): number {
    return (this.currentStep / this.totalSteps) * 100;
  }

  onBannerSelect(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      const file = input.files[0];
      
      // Validate file type
      if (!file.type.startsWith('image/')) {
        alert('Please select an image file');
        return;
      }
      
      // Validate file size (max 5MB)
      if (file.size > 5 * 1024 * 1024) {
        alert('File size must be less than 5MB');
        return;
      }
      
      this.bannerFile = file;
      
      // Create preview
      const reader = new FileReader();
      reader.onload = (e) => {
        this.bannerPreview = e.target?.result as string;
      };
      reader.readAsDataURL(file);
    }
  }

  removeBanner(): void {
    this.bannerFile = null;
    this.bannerPreview = null;
    this.tournamentForm.patchValue({ bannerUrl: '' });
  }
}
