import java.util.*;

public class Main {
    static String concert(List<Instrument> l) {
        List<String> parts = new ArrayList<>();
        for (Instrument i : l) parts.add(i.son());
        return String.join(" | ", parts);
    }
}
