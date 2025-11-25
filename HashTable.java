public class HashTable<T> implements HashTableInterface<T>{

    private int TABLE_SIZE = 5003;
    private int size = 0;
    private double loadFactor;
    private long collisionCount = 0;
    private boolean usePAF = true;


    private HashEntry<T>[] table;
    private boolean useDoubleHashing = false;

    private boolean isPrime(int n) {
        if (n <= 1) return false;
        if (n <= 3) return true;
        if (n % 2 == 0 || n % 3 == 0) return false;
        for (int i = 5; i * i <= n; i += 6) {
            if (n % i == 0 || n % (i + 2) == 0) return false;
        }
        return true;
    }

    private int nextPrime(int n) {
        while (!isPrime(n)) n++;
        return n;
    }

    @SuppressWarnings("unchecked")
    public HashTable(double loadFactor,int initialCapacity) {
        this.loadFactor = loadFactor;
        this.TABLE_SIZE = nextPrime(initialCapacity);
        table = (HashEntry<T>[]) new HashEntry<?>[TABLE_SIZE];
    }
    // Eski constructor'lar - default değerlerle
    @SuppressWarnings("unchecked")
    public HashTable() {
        this(0.5, 5003);  // default: 0.5 load factor, 5003 size
    }

    public HashTable(int size) {
        this(0.5, size);  // default: 0.5 load factor
    }

    private void checkLoadFactor() {
        if ((double) size / TABLE_SIZE >= loadFactor) {
            resize();
        }
    }



    @SuppressWarnings("unchecked")
    public void resize() {
        int oldSize = TABLE_SIZE;
        TABLE_SIZE = nextPrime(TABLE_SIZE * 2);

        HashEntry<T>[] oldTable = table;
        table = new HashEntry[TABLE_SIZE];
        size = 0;

        for (int i = 0; i < oldSize; i++) {
            if (oldTable[i] != null) {
                put(oldTable[i].getKey(), oldTable[i].getValue());
            }
        }
    }

    // SSF
    public int hashFunctionSSF(String key) {
        //key = key.toLowerCase();
        int sum = 0;
        for (int i = 0; i < key.length(); i++) {
            sum += key.charAt(i);
        }
        return Math.abs(sum % TABLE_SIZE);
    }

    // PAF
    public int hashFunctionPAF(String key) {
        key = key.toLowerCase();
        long hash = 0;
        int z = 33;

        for (int i = 0; i < key.length(); i++) {
            hash = (hash * z + key.charAt(i)); // -96 YOK artık
            hash %= TABLE_SIZE;
        }

        return (int) (hash & 0x7FFFFFFF);  // HER ZAMAN POZİTİF
    }


    // LP
    private int findIndexLP(String key, int hash) {
        int index = hash;
        while (true) {
            if (table[index] == null || table[index].getKey().equals(key))
                return index;

            collisionCount++;

            index = (index + 1) % TABLE_SIZE;
        }
    }

    // DH
    private int secondHash(String key) {
        int q = TABLE_SIZE - 2;
        int k = hashFunctionSSF(key);
        int step = q - (k % q);
        if (step == 0) step = 1;
        return step;
    }

    private int findIndexDH(String key, int hash) {
        int step = secondHash(key);
        int index = hash;
        while (true) {
            if (table[index] == null || table[index].getKey().equals(key))
                return index;

            collisionCount++;

            index = (index + step) % TABLE_SIZE;
        }
    }

    // PUT
    public void put(String key, T value) {
        checkLoadFactor();

        int hash = usePAF ? hashFunctionPAF(key) : hashFunctionSSF(key);

        int index = useDoubleHashing ? findIndexDH(key, hash) : findIndexLP(key, hash);

        if (table[index] == null) {
            table[index] = new HashEntry<>(key, value);
            size++;
        } else {
            table[index].setValue(value);
        }
    }

    // GET
    public T get(String key) {
        int hash = usePAF ? hashFunctionPAF(key) : hashFunctionSSF(key);

        int index = hash;

        if (useDoubleHashing) {
            int step = secondHash(key);
            while (table[index] != null) {
                if (table[index].getKey().equals(key))
                    return table[index].getValue();
                index = (index + step) % TABLE_SIZE;
            }
        } else {
            while (table[index] != null) {
                if (table[index].getKey().equals(key))
                    return table[index].getValue();
                index = (index + 1) % TABLE_SIZE;
            }
        }

        return null;
    }

    public String[] getAllKeys() {
        String[] keys = new String[size];
        int idx = 0;

        for (int i = 0; i < TABLE_SIZE; i++) {
            if (table[i] != null) {
                keys[idx++] = table[i].getKey();
            }
        }
        return keys;
    }

    public void setUsePAF(boolean flag){this.usePAF = flag;}

    public void setDoubleHashing(boolean flag) {
        this.useDoubleHashing = flag;
    }

    public long getCollisionCount() {
        return collisionCount;
    }
    public void resetCollisionCount() {
        collisionCount = 0;
    }

    public boolean isEmpty(){
        return size==0;
    }
    public int size(){
        return size;
    }


}