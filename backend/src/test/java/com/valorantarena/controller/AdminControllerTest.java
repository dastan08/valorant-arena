package com.valorantarena.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.valorantarena.service.AdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminService adminService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getDashboardStats_ShouldReturnStats() throws Exception {
        // Given
        Map<String, Object> mockStats = new HashMap<>();
        mockStats.put("totalUsers", 100L);
        mockStats.put("totalTeams", 20L);
        mockStats.put("activeScrims", 5L);
        
        when(adminService.getDashboardStats()).thenReturn(mockStats);

        // When & Then
        mockMvc.perform(get("/api/admin/dashboard/stats")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalUsers").value(100))
                .andExpect(jsonPath("$.data.totalTeams").value(20))
                .andExpect(jsonPath("$.data.activeScrims").value(5));
    }

    @Test
    void getDashboardStats_WithoutAdminRole_ShouldReturnForbidden() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/admin/dashboard/stats")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getDashboardStats_WithUserRole_ShouldReturnForbidden() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/admin/dashboard/stats")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
