import java.util.*;

public final class SymbolTable {

    private final Map<String, Integer> offsetMap = new HashMap<>();

    private int address = 0;

    public int lookupOrInsert(String symbol) {
        if (!offsetMap.containsKey(symbol)) {
            offsetMap.put(symbol, address);
            address++;
        }
        return offsetMap.get(symbol);
    }

    public int lookup(String symbol) {
        if (!offsetMap.containsKey(symbol)) {
            throw new IllegalArgumentException("Could not find address for symbol: " + symbol);
        }
        return offsetMap.get(symbol);
    }

}
