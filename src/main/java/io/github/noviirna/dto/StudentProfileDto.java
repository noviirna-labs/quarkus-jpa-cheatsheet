package io.github.noviirna.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import io.github.noviirna.entity.Student;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.io.Serializable;

public record StudentProfileDto(
        @Schema(examples = "junior")
        String academicLevel,
        @JsonUnwrapped Student student) implements Serializable {
}
