package io.github.noviirna.dto;

import io.github.noviirna.entity.Profile;
import io.github.noviirna.entity.Student;

import java.io.Serializable;

public record ProfileStudentDto(
        Profile profile,
        Student student
) implements Serializable {
}
