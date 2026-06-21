package io.github.noviirna.dto;

import io.github.noviirna.entity.Profile;
import io.github.noviirna.entity.Student;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;


public record ProfileStudentDto(
        @NotNull
        @Valid
        Profile profile,
        @NotNull
        @Valid
        Student student
) implements Serializable {
}
