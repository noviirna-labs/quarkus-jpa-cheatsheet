package io.github.noviirna.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.io.Serializable;

@MappedSuperclass
public abstract class BaseEntity extends PanacheEntityBase implements Serializable {

    @Schema(examples = "123")
    @Id
    @GeneratedValue
    // will be returned in response body json payload
    // and never be returned in a request body json payload
    // this field is only can be transformed from Java to JSON
    public Long id;

}
