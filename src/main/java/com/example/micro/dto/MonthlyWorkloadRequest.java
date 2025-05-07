package com.example.micro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor; /**
 * Request object for fetching trainer workload by month
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyWorkloadRequest {
    @NotBlank(message = "Trainer username is required")
    private String username;

    @NotNull(message = "Year is required")
    private Integer year;

    @NotNull(message = "Month is required")
    private Integer month;
}
