package com.nasa.nacontacts.domain.exceptions;

import java.time.LocalDateTime;
import java.util.List;

public record RestErrorResponseWithFieldErrors(
        int status, List<FieldError> fieldErrors, LocalDateTime timestamp
) {}
