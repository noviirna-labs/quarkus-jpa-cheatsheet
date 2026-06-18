package io.github.noviirna.resource.factory;

public interface RestPanacheTestFactory<T> {


    String getPath();

    T insertEntity();

    void purgeEntity();




}
