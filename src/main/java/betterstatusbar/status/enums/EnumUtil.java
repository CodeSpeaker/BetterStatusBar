package betterstatusbar.status.enums;

import java.util.BitSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class EnumUtil {

    public static <E extends Enum<E>> BitSet getBitSet(Class<E> clazz, List<String> values) {
        BitSet bitSet = new BitSet();
        for (String value : values) {
            E element = Enum.valueOf(clazz, value);
            bitSet.set(element.ordinal());
        }
        return bitSet;
    }

    public static <E extends Enum<E>> Set<String> fromBitSet(Class<E> clazz, BitSet bitSet) {
        return bitSet.stream().mapToObj(i -> clazz.getEnumConstants()[i].name()).collect(Collectors.toSet());
    }
}
