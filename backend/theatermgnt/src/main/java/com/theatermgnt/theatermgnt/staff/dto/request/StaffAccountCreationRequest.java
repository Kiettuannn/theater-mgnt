package com.theatermgnt.theatermgnt.staff.dto.request;

import jakarta.validation.constraints.*;

import com.theatermgnt.theatermgnt.account.dto.request.BaseAccountCreationRequest;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StaffAccountCreationRequest extends BaseAccountCreationRequest {
    @Size(max = 100, message = "JOB_TITLE_TOO_LONG")
    String jobTitle;

    @NotBlank(message = "CINEMA_ID_REQUIRED")
    String cinemaId;
}
