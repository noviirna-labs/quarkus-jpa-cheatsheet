package io.github.noviirna.resource.panacheentity;

public interface RestPanacheTestFactory<T> {


    String getPath();

    T insertEntity();

    void purgeEntity();

    void delete(T entity);

}
