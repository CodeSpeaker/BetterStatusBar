import betterstatusbar.status.data.BaiduCalendarData;
import betterstatusbar.status.enums.*;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.intellij.util.io.HttpRequests;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.Deflater;

public class DataPrepareUtil {

    private static final String CALENDAR_URL_FORMAT = "https://sp1.baidu.com/8aQDcjqpAAV3otqbppnN2DJv/api.php?tn=wisetpl&resource_id=39043&query=%d年%d月";
    private static final JsonMapper JSON_MAPPER = new JsonMapper();

    static {
        JSON_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static void updateEnum() throws IOException {
        Map<String, Integer> suitsAndAvoids = new HashMap<>();
        Map<String, Integer> termsAndValues = new HashMap<>();

        LocalDate localDate = LocalDate.of(1900, 2, 1);
        while (true) {
            String url = String.format(CALENDAR_URL_FORMAT, localDate.getYear(), localDate.getMonth().getValue());
            String response = HttpRequests.request(url).readString();
            int start = response.indexOf("{");
            int end = response.lastIndexOf("}");
            String json = response.substring(start, end + 1);
            BaiduCalendarData tempData = JSON_MAPPER.readValue(json, BaiduCalendarData.class);
            if (tempData.data.size() == 0) {
                break;
            }
            for (BaiduCalendarData data : tempData.data) {
                for (BaiduCalendarData calendarData : data.almanac) {

                    for (String suit : List.of(calendarData.suit.split("[.·]"))) {
                        Integer count = suitsAndAvoids.computeIfAbsent(suit, s -> 0);
                        suitsAndAvoids.put(suit, count - 1);
                    }
                    for (String avoid : calendarData.avoid.split("[.·]")) {
                        Integer count = suitsAndAvoids.computeIfAbsent(avoid, s -> 0);
                        suitsAndAvoids.put(avoid, count - 1);
                    }

                    if (calendarData.term != null) {
                        for (String term : List.of(calendarData.term.split(" "))) {
                            Integer count = termsAndValues.computeIfAbsent(term, s -> 0);
                            termsAndValues.put(term, count - 1);
                        }
                    }
                    if (calendarData.value != null) {
                        for (String value : List.of(calendarData.value.split(" "))) {
                            Integer count = termsAndValues.computeIfAbsent(value, s -> 0);
                            termsAndValues.put(value, count - 1);
                        }
                    }
                }
            }
            localDate = localDate.plus(3, ChronoUnit.MONTHS);
        }

        System.out.println(suitsAndAvoids.keySet().stream().sorted(Comparator.comparing(suitsAndAvoids::get)).collect(Collectors.toList()));
        System.out.println(termsAndValues.keySet().stream().sorted(Comparator.comparing(termsAndValues::get)).collect(Collectors.toList()));

        System.out.println(localDate);
    }

    public static void updateData0() throws IOException {
        Path file = Path.of("data0");
        ByteBuffer src = ByteBuffer.allocate(0);

        try (FileChannel channel = FileChannel.open(file, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
            JSON_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            LocalDate localDate = LocalDate.of(1900, 2, 1);
            while (true) {
                String url = String.format(CALENDAR_URL_FORMAT, localDate.getYear(), localDate.getMonth().getValue());
                String response = HttpRequests.request(url).readString();
                int start = response.indexOf("{");
                int end = response.lastIndexOf("}");
                String json = response.substring(start, end + 1);
                BaiduCalendarData tempData = JSON_MAPPER.readValue(json, BaiduCalendarData.class);
                if (tempData.data.size() == 0) {
                    break;
                }
                for (BaiduCalendarData data : tempData.data) {
                    for (BaiduCalendarData calendarData : data.almanac) {
                        String suitString = EnumUtil.getBase64Strings(Events.class, List.of(calendarData.suit.split("[.·]")));
                        String avoidString = EnumUtil.getBase64Strings(Events.class, List.of(calendarData.avoid.split("[.·]")));

                        String termString = "";
                        if (StringUtils.isNotBlank(calendarData.term) || StringUtils.isNotBlank(calendarData.value)) {
                            String terms;
                            if (StringUtils.isNotBlank(calendarData.term) && StringUtils.isNotBlank(calendarData.value)) {
                                terms = calendarData.term.concat(" ").concat(calendarData.value);
                            } else if (StringUtils.isNotBlank(calendarData.term)) {
                                terms = calendarData.term;
                            } else {
                                terms = calendarData.value;
                            }
                            for (String term : new HashSet<>(List.of(terms.split(" ")))) {
                                int ordinal = Terms.valueOf(term).ordinal();
                                termString = termString.concat("," + Integer.toHexString(ordinal));
                            }
                        }
                        termString = termString.replaceFirst(",", "");

                        String formatted = String.format("%s %s %s%n", suitString, avoidString, termString);
                        System.out.print(formatted);
                        byte[] bytes = formatted.getBytes(StandardCharsets.UTF_8);
                        src = ByteBuffer.allocate(src.limit() + bytes.length).put(src).put(bytes).flip();
                    }
                }
                localDate = localDate.plus(3, ChronoUnit.MONTHS);
            }

            System.out.println(localDate);
            Deflater deflater = new Deflater(9);
            deflater.setInput(src);
            deflater.finish();
            ByteBuffer dst = ByteBuffer.allocate(src.limit());
            deflater.deflate(dst);
            dst.flip();
            channel.write(dst, 0);
        }
    }

    public static void updateData1() throws IOException {
        File file = new File("data1");
        ByteBuffer buffer = ByteBuffer.allocate(151 * 366 * 4);
        IntBuffer src = buffer.asIntBuffer();

        StandardOpenOption standardOpenOption = file.exists() ? StandardOpenOption.TRUNCATE_EXISTING : StandardOpenOption.CREATE_NEW;
        try (FileChannel channel = FileChannel.open(file.toPath(), standardOpenOption, StandardOpenOption.WRITE)) {
            JSON_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            LocalDate baseDate = LocalDate.of(1900, 1, 1);
            LocalDate localDate = LocalDate.of(1900, 2, 1);
            int i = 0;
            while (true) {
                String url = String.format(CALENDAR_URL_FORMAT, localDate.getYear(), localDate.getMonth().getValue());
                String response = HttpRequests.request(url).readString();
                int start = response.indexOf("{");
                int end = response.lastIndexOf("}");
                String json = response.substring(start, end + 1);
                BaiduCalendarData tempData = JSON_MAPPER.readValue(json, BaiduCalendarData.class);
                if (tempData.data.size() == 0) {
                    break;
                }
                for (BaiduCalendarData data : tempData.data) {
                    for (BaiduCalendarData calendarData : data.almanac) {
                        int gzYear = CelestialStemAndTerrestrialBranch.valueOf(calendarData.gzYear).ordinal() << 25;
                        int gzMonth = CelestialStemAndTerrestrialBranch.valueOf(calendarData.gzMonth).ordinal() << 19;
                        int gzDate = CelestialStemAndTerrestrialBranch.valueOf(calendarData.gzDate).ordinal() << 13;
                        int lMonth = LunarMonths.valueOf(calendarData.lMonth).ordinal() << 8;
                        int lDate = LunarDates.valueOf(calendarData.lDate).ordinal() << 3;
                        int isBigMonth = Integer.parseInt(StringUtils.isNotBlank(calendarData.isBigMonth) ? calendarData.isBigMonth : "0") << 2;
                        int status = Integer.parseInt(StringUtils.isNotBlank(calendarData.status) ? calendarData.status : "0");

                        int sum = gzYear + gzMonth + gzDate + lMonth + lDate + isBigMonth + status;
                        // 6 + 6 + 6 + 5 + 5 + 1 + 2
                        System.out.println((sum >> 25) + "\t" + (sum >> 19 & 0b111111) + "\t" + (sum >> 13 & 0b111111) + "\t" + (sum >> 8 & 0b11111) + "\t" + (sum >> 3 & 0b11111) + "\t" + (sum >> 2 & 0b1) + "\t" + (sum & 0b11));
                        src.put(sum);
                    }
                }
                localDate = localDate.plus(3, ChronoUnit.MONTHS);
            }

            System.out.println(localDate);
            src.flip();
            buffer.limit(src.limit() * 4);
            Deflater deflater = new Deflater(9);
            deflater.setInput(buffer);
            deflater.finish();
            ByteBuffer dst = ByteBuffer.allocate(1);

            int position = 0;
            while (true) {
                int deflate = deflater.deflate(dst);
                if (deflate <= 0) {
                    break;
                }
                dst.flip();
                channel.write(dst, position);
                position += deflate;
                dst.clear();
            }
        }
    }
}
