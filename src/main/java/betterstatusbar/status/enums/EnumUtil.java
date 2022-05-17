package betterstatusbar.status.enums;

import java.util.*;
import java.util.stream.Collectors;

public class EnumUtil {

    private static final String SEPARATOR = ",";

    public static <E extends Enum<E>> BitSet getBitSet(Class<E> clazz, List<String> values) {
        BitSet bitSet = new BitSet();
        for (String value : values) {
            E element = Enum.valueOf(clazz, value);
            bitSet.set(element.ordinal());
        }
        return bitSet;
    }

    public static <E extends Enum<E>> String getBase64Strings(Class<E> clazz, List<String> values) {
        BitSet bitSet = getBitSet(clazz, values);

        StringBuilder result = new StringBuilder();
        for (long l : bitSet.toLongArray()) {
            byte[] bytes = long2Bytes(l);
            result.append(Base64.getEncoder().encodeToString(bytes)).append(",");
        }

        return result.deleteCharAt(result.length() - 1).toString();
    }

    public static <E extends Enum<E>> Set<E> fromBitSet(BitSet bitSet, Class<E> clazz) {
        return bitSet.stream().mapToObj(i -> clazz.getEnumConstants()[i]).collect(Collectors.toSet());
    }

    public static <E extends Enum<E>> Set<E> fromBase64Strings(String base64Strings, Class<E> clazz) {
        String[] strings = base64Strings.split(SEPARATOR);
        BitSet bitSet = BitSet.valueOf(Arrays.stream(strings).mapToLong(str -> bytes2Long(Base64.getDecoder().decode(str))).toArray());
        return fromBitSet(bitSet, clazz);
    }

    public static <E extends Enum<E>> Set<String> fromBitSet(Class<E> clazz, BitSet bitSet) {
        return bitSet.stream().mapToObj(i -> clazz.getEnumConstants()[i].name()).collect(Collectors.toSet());
    }

    public static <E extends Enum<E>> Set<String> fromBase64Strings(Class<E> clazz, String base64Strings) {
        String[] strings = base64Strings.split(SEPARATOR);
        BitSet bitSet = BitSet.valueOf(Arrays.stream(strings).mapToLong(str -> bytes2Long(Base64.getDecoder().decode(str))).toArray());
        return fromBitSet(clazz, bitSet);
    }

    private static byte[] long2Bytes(long l) {
        byte[] result = new byte[0];
        boolean isZero = true;
        for (int i = 0; i < 8; i++) {
            byte b = (byte) (l >>> ((7 - i) * 8) & 0xFF);
            if (isZero) {
                if (b == 0) {
                    continue;
                }
                isZero = false;
                result = new byte[8 - i];
            }
            result[i - 8 + result.length] = b;
        }
        return result;
    }

    private static long bytes2Long(byte[] bytes) {
        int length = bytes.length;
        return ((long) (((length - 8 >= 0) ? bytes[length - 8] : 0) & 0x0FF) << 56)
                | ((long) (((length - 7 >= 0) ? bytes[length - 7] : 0) & 0x0FF) << 48)
                | ((long) (((length - 6 >= 0) ? bytes[length - 6] : 0) & 0x0FF) << 40)
                | ((long) (((length - 5 >= 0) ? bytes[length - 5] : 0) & 0x0FF) << 32)
                | ((long) (((length - 4 >= 0) ? bytes[length - 4] : 0) & 0x0FF) << 24)
                | ((long) (((length - 3 >= 0) ? bytes[length - 3] : 0) & 0x0FF) << 16)
                | ((long) (((length - 2 >= 0) ? bytes[length - 2] : 0) & 0x0FF) << 8)
                | ((long) (((length - 1 >= 0) ? bytes[length - 1] : 0) & 0x0FF));
    }
}
