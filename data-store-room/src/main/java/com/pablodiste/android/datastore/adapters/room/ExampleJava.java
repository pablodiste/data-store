package com.pablodiste.android.datastore.adapters.room;

import java.util.List;

import kotlinx.coroutines.flow.Flow;

/*
public class ExampleJava extends ExampleAbstractList<Entity> {

}
*/

interface ExampleJavaInterface<T> {
    Flow<T> foo();
}

class ExampleJavaAbstract<T> implements ExampleJavaInterface<List<T>> {
    public Flow<List<T>> foo() {
        return null;
    }
}

class ExampleJava2 extends ExampleJavaAbstract<Entity> {

}
