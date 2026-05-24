package io.github.noviirna.dto;

import io.github.noviirna.model.Student;

public record ProfileDto(Long id, String academicLevel, Student student, Long studentId) {
}
