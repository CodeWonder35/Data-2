public interface HashTableInterface<T> {

    void put(String key, T value);
    T get(String key);
    void resize();
    boolean isEmpty();
    int size();


}
