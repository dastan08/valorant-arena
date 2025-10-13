import { Component, OnInit } from '@angular/core';
import { AdminService } from '../../../core/services/admin.service';

@Component({
  selector: 'app-report-management',
  template: `
    <div class="min-h-screen bg-gray-900 text-white">
      <!-- Header -->
      <div class="bg-gray-800 border-b border-gray-700 px-8 py-4">
        <div class="flex items-center justify-between max-w-7xl mx-auto">
          <div>
            <h1 class="text-2xl font-bold text-white">Report Management</h1>
            <p class="text-sm text-gray-400">Review and resolve user reports</p>
          </div>
          <button routerLink="/admin" class="text-gray-300 hover:text-white p-2 rounded-lg hover:bg-gray-700 transition-colors" title="Back to Dashboard">
            <i class="fa-solid fa-arrow-left text-lg" aria-hidden="true"></i>
          </button>
        </div>
      </div>
      
      <!-- Content -->
      <div class="p-6 max-w-7xl mx-auto">
        <div class="bg-gray-800 rounded-xl border border-gray-700 p-8 text-center">
          <i class="fa-solid fa-flag text-6xl text-gray-600 mb-4" aria-hidden="true"></i>
          <p class="text-gray-400 text-lg">Report management functionality will be implemented here.</p>
        </div>
      </div>
    </div>
  `
})
export class ReportManagementComponent implements OnInit {
  constructor(private adminService: AdminService) {}
  
  ngOnInit(): void {}
}
