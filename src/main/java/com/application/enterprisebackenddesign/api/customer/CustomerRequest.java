package com.application.enterprisebackenddesign.api.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CustomerRequest (
   @NotNull @NotBlank String name,
   @NotNull @NotBlank String lastName,
   @NotNull @NotBlank @Email String email
){}