import java.util.*;

public final class SymbolTable {

    private final Map<String, Integer> offsetMap = new HashMap<>();

    private int address = 0;

    public int insert(String symbol) {
        if (!offsetMap.containsKey(symbol)) {
            offsetMap.put(symbol, address++);
        }
        return address - 1;
    }

    public int lookupOrInsert(String symbol) {
        if (!offsetMap.containsKey(symbol)) {
            offsetMap.put(symbol, address++);
        }
        for (Map.Entry<String, Integer> entry : offsetMap.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
        System.out.println("---");
        return offsetMap.get(symbol);
    }

    public int lookup(String symbol) {
//        for (Map.Entry<String, Integer> entry : offsetMap.entrySet()) {
//            System.out.println(entry.getKey() + ": " + entry.getValue());
//        }
        if (!offsetMap.containsKey(symbol)) {
            throw new IllegalArgumentException("Could not find address for symbol: " + symbol);
        }
        return offsetMap.get(symbol);
    }

}
