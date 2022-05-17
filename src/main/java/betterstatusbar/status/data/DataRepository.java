package betterstatusbar.status.data;

import betterstatusbar.status.enums.*;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class DataRepository {

    private LocalDate baseDate;
    private int count;
    private final List<Integer> data0 = new ArrayList<>();
    private final List<String> suits = new ArrayList<>();
    private final List<String> avoids = new ArrayList<>();
    private final List<String> terms = new ArrayList<>();
    private final Lock initLock = new ReentrantLock();
    private boolean isReady = false;

    private final Cache<LocalDate, CalendarData> cache = CacheBuilder.newBuilder().maximumSize(100).build();

    public DataRepository() {

    }

    public DataRepository(String path) {
        try {
            URI uri = DataRepository.class.getClassLoader().getResource(path).toURI();
            try (FileSystem ignored = FileSystems.newFileSystem(uri, new HashMap<>())) {
                init(Path.of(uri));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public DataRepository(Path path) {
        init(path);
    }

    public void init(Path path) {
        if (!initLock.tryLock() || isReady) {
            return;
        }

        try {
            ByteBuffer compressedDst = ByteBuffer.allocate(500000);
            ByteBuffer dst = ByteBuffer.allocate(3000000);
            try (FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.READ)) {
                fileChannel.read(compressedDst);
            }

            compressedDst.flip();
            Inflater inflater = new Inflater();
            inflater.setInput(compressedDst);
            inflater.finished();
            inflater.inflate(dst);

            dst.flip();
            count = dst.getInt();
            byte[] dateBytes = new byte[8];
            dst.get(dateBytes);
            baseDate = LocalDate.parse(new String(dateBytes, StandardCharsets.UTF_8), DateTimeFormatter.ofPattern("yyyyMMdd"));
            for (int i = 0; i < count; i++) {
                data0.add(dst.getInt());
            }
            for (int i = 0; i < count * 3 && dst.hasRemaining(); i++) {
                dst.mark();
                while (true) {
                    if (dst.get() == '\n') {
                        break;
                    }
                }
                int end = dst.position();
                int start = dst.reset().position();
                byte[] bytes = new byte[end - start];
                dst.get(bytes);
                switch (i / count) {
                    case 0:
                        suits.add(new String(bytes, StandardCharsets.UTF_8).replace("\n", ""));
                        break;
                    case 1:
                        avoids.add(new String(bytes, StandardCharsets.UTF_8).replace("\n", ""));
                        break;
                    case 2:
                        terms.add(new String(bytes, StandardCharsets.UTF_8).replace("\n", ""));
                        break;
                    default:
                }
            }
            isReady = true;
        } catch (DataFormatException | IOException e) {
            throw new RuntimeException(e);
        } finally {
            initLock.unlock();
        }
    }

    public void printEnumSuggest(List<CalendarData> data) {
        Map<String, Integer> suitsAndAvoids = new HashMap<>();
        Map<String, Integer> termsAndValues = new HashMap<>();

        for (CalendarData calendarData : data) {

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

        System.out.println("Events:" + suitsAndAvoids.keySet().stream().sorted(Comparator.comparing(suitsAndAvoids::get)).collect(Collectors.toList()));
        System.out.println("Terms:" + termsAndValues.keySet().stream().sorted(Comparator.comparing(termsAndValues::get)).collect(Collectors.toList()));
    }

    public Path createDataFile(List<CalendarData> data) throws IOException {
        File file = new File("data0-" + System.currentTimeMillis());
        ByteBuffer src = ByteBuffer.allocate(5000000);
        StandardOpenOption standardOpenOption = file.exists() ? StandardOpenOption.TRUNCATE_EXISTING : StandardOpenOption.CREATE_NEW;

        try (FileChannel channel = FileChannel.open(file.toPath(), standardOpenOption, StandardOpenOption.WRITE)) {
            src.putInt(data.size());
            CalendarData base = data.get(0);
            src.put(String.format("%s%02d%02d", base.getYear(), Integer.valueOf(base.getMonth()), Integer.valueOf(base.getDay())).getBytes(StandardCharsets.UTF_8));
            for (CalendarData calendarData : data) {
                // 6 + 6 + 6 + 5 + 5 + 1 + 2
                int gzYear = CelestialStemAndTerrestrialBranch.valueOf(calendarData.gzYear).ordinal() << 25;
                int gzMonth = CelestialStemAndTerrestrialBranch.valueOf(calendarData.gzMonth).ordinal() << 19;
                int gzDate = CelestialStemAndTerrestrialBranch.valueOf(calendarData.gzDate).ordinal() << 13;
                int lMonth = LunarMonths.valueOf(calendarData.lMonth).ordinal() << 8;
                int lDate = LunarDates.valueOf(calendarData.lDate).ordinal() << 3;
                int isBigMonth = Integer.parseInt(StringUtils.isNotBlank(calendarData.isBigMonth) ? calendarData.isBigMonth : "0") << 2;
                int status = Integer.parseInt(StringUtils.isNotBlank(calendarData.status) ? calendarData.status : "0");

                int sum = gzYear + gzMonth + gzDate + lMonth + lDate + isBigMonth + status;
                src.putInt(sum);
            }

            for (CalendarData calendarData : data) {
                String suitString = EnumUtil.getBase64Strings(Events.class, List.of(calendarData.suit.split("[.·]"))) + "\n";
                byte[] bytes = suitString.getBytes(StandardCharsets.UTF_8);
                src.put(bytes);
            }

            for (CalendarData calendarData : data) {
                String avoidString = EnumUtil.getBase64Strings(Events.class, List.of(calendarData.avoid.split("[.·]"))) + "\n";
                byte[] bytes = avoidString.getBytes(StandardCharsets.UTF_8);
                src.put(bytes);
            }

            for (CalendarData calendarData : data) {
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
                termString = termString.replaceFirst(",", "") + "\n";
                byte[] bytes = termString.getBytes(StandardCharsets.UTF_8);
                src.put(bytes);
            }

            src.flip();
            Deflater deflater = new Deflater(9);
            deflater.setInput(src);
            deflater.finish();
            ByteBuffer dst = ByteBuffer.allocate(src.limit());
            deflater.deflate(dst);
            dst.flip();
            channel.write(dst, 0);
        }
        return file.toPath();
    }

    public CalendarData getNewData(LocalDate localDate) {
        int offset = (int) baseDate.until(localDate, ChronoUnit.DAYS);
        if (offset < 0 || offset > count) {
            return CalendarData.DEFAULT;
        }
        Integer base = data0.get(offset);
        String suit = suits.get(offset);
        String avoid = avoids.get(offset);
        String term = terms.get(offset);

        CelestialStemAndTerrestrialBranch gzYear = CelestialStemAndTerrestrialBranch.values()[base >> 25];
        CelestialStemAndTerrestrialBranch gzMonth = CelestialStemAndTerrestrialBranch.values()[base >> 19 & 0b111111];
        CelestialStemAndTerrestrialBranch gzDate = CelestialStemAndTerrestrialBranch.values()[base >> 13 & 0b111111];
        LunarMonths lMonth = LunarMonths.values()[base >> 8 & 0b11111];
        LunarDates lDate = LunarDates.values()[base >> 3 & 0b11111];
        int isBigMonth = base >> 2 & 0b1;
        int status = base & 0b11;

        CalendarData calendarData = new CalendarData();
        calendarData.animal = gzYear.getTerrestrialBranch().getAnimal();
        calendarData.avoid = EnumUtil.fromBase64Strings(Events.class, avoid).toString().replaceAll("[\\[\\]]", "");
        calendarData.cnDay = null;
        calendarData.day = String.valueOf(localDate.getDayOfMonth());
        calendarData.desc = null;
        calendarData.gzDate = gzDate.name();
        calendarData.gzMonth = gzMonth.name();
        calendarData.gzYear = gzYear.name();
        calendarData.isBigMonth = String.valueOf(isBigMonth);
        calendarData.lDate = lDate.name();
        calendarData.lMonth = lMonth.name();
        calendarData.lunarDate = null;
        calendarData.lunarMonth = null;
        calendarData.lunarYear = null;
        calendarData.month = String.valueOf(localDate.getMonth().getValue());
        calendarData.oDate = null;
        calendarData.status = String.valueOf(status);
        calendarData.suit = EnumUtil.fromBase64Strings(Events.class, suit).toString().replaceAll("[\\[\\]]", "");
        calendarData.term = Arrays.stream(term.split(",")).filter(StringUtils::isNotBlank).map(str -> Integer.valueOf(str, 16)).map(i -> Terms.values()[i]).collect(Collectors.toList()).toString().replaceAll("[\\[\\]]", "");
        calendarData.type = null;
        calendarData.value = null;
        calendarData.year = String.valueOf(localDate.getYear());
        return calendarData;
    }

    public CalendarData getData(LocalDate localDate) {
        try {
            return cache.get(localDate, () -> getNewData(localDate));
        } catch (ExecutionException e) {
            return CalendarData.DEFAULT;
        }
    }

}
