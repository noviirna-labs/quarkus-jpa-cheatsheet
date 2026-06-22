package io.github.noviirna.entity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.io.Serial;
import java.time.LocalTime;


@Entity
@Table(name = "courses")
public class Course extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 5716107637584368332L;

    @NotBlank
    @Size(max = 30, min = 3)
    @Schema(examples = "math")
    public String subject;

    @NotNull
    @JsonSerialize(using = LocalTimeSerializer.class)
    @JsonDeserialize(using = LocalTimeDeserializer.class)
    @Schema(examples = "23:59:59")
    public LocalTime schedule;
}
