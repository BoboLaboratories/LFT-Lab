import java.util.*;

public final class SymbolTable {

    private final Map<String, Integer> offsetMap = new HashMap<>();

    public void insert(String symbol, int address) {
        if (!offsetMap.containsValue(address)) {
            offsetMap.put(symbol, address);
        } else
            throw new IllegalArgumentException("Reference to a memory location already occupied by another variable");
    }

    public int lookupAddress(String symbol) {
        return offsetMap.getOrDefault(symbol, -1);
    }

}
