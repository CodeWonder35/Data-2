public class HashTable<T> {

    private int TABLE_SIZE = 5003;
    private int size = 0;
    private final double loadFactor = 0.5;

    private HashEntry<T>[] table;
    private boolean useDoubleHashing = false;

    @SuppressWarnings("unchecked")
    public HashTable() {
        table = new HashEntry[TABLE_SIZE];
    }

    private void checkLoadFactor() {
        if ((double) size / TABLE_SIZE >= loadFactor) {
            rehash();
        }
    }

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
    private void rehash() {
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
        key = key.toLowerCase();
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
            index = (index + step) % TABLE_SIZE;
        }
    }

    // PUT
    public void put(String key, T value) {
        checkLoadFactor();

        int hash = hashFunctionPAF(key);

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
        int hash = hashFunctionPAF(key);
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

    public void setDoubleHashing(boolean flag) {
        this.useDoubleHashing = flag;
    }
}
