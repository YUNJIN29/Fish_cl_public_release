package com.fishmart.task;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TaskCompletionRequest { @NotNull private Boolean completed; }
