package top.cnzrg.tanchishe.music.lrc;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.DateUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LrcUtils {
    private static Context context;

    private static final Pattern PATTERN_LINE = Pattern.compile("((\\[\\d\\d:\\d\\d\\.\\d{2,3}\\])+)(.+)");
    private static final Pattern PATTERN_TIME = Pattern.compile("\\[(\\d\\d):(\\d\\d)\\.(\\d{2,3})\\]");

    public static void setContext(Context context) {
        LrcUtils.context = context;
    }

    /**
     * 从歌词文件中加载全部歌词字符串
     *
     * @param lrcFileName
     * @return
     */
    public static String getLrcStrFromFile(String lrcFileName) {
        String lrcText = null;
        try {
            InputStream is = context.getAssets().open(lrcFileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            lrcText = new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lrcText;
    }

    /**
     * 从文本解析歌词
     */
    public static List<LrcRow> parseLrc(String lrcText) {
        if (TextUtils.isEmpty(lrcText)) {
            return null;
        }

        if (lrcText.startsWith("\uFEFF")) {
            lrcText = lrcText.replace("\uFEFF", "");
        }

        List<LrcRow> entryList = new ArrayList<>();
        String[] array = lrcText.split("\\n");
        for (String line : array) {
            List<LrcRow> list = parseLine(line);
            if (list != null && !list.isEmpty()) {
                entryList.addAll(list);
            }
        }

        Collections.sort(entryList);

        // 每一行的时间 = 下一行 - 当前行
        for (int i = 0; i < entryList.size(); i++) {
            LrcRow lrcRow = entryList.get(i);

            if (i + 1 >= entryList.size()) {
                // 最后一句歌词的时间
                lrcRow.setTotalTime(10000);
            } else {
                LrcRow nextLrcRow = entryList.get(i + 1);
                lrcRow.setTotalTime(nextLrcRow.getTime() - lrcRow.getTime());
            }
        }

        return entryList;
    }

    /**
     * 解析一行歌词
     */
    private static List<LrcRow> parseLine(String line) {
        if (TextUtils.isEmpty(line)) {
            return null;
        }

        line = line.trim();
        // [00:17.65]让我掉下眼泪的
        Matcher lineMatcher = PATTERN_LINE.matcher(line);
        if (!lineMatcher.matches()) {
            return null;
        }

        String times = lineMatcher.group(1);
        String text = lineMatcher.group(3);
        List<LrcRow> entryList = new ArrayList<>();

        // [00:17.65]
        Matcher timeMatcher = PATTERN_TIME.matcher(times);
        while (timeMatcher.find()) {
            long min = Long.parseLong(timeMatcher.group(1));
            long sec = Long.parseLong(timeMatcher.group(2));
            String milString = timeMatcher.group(3);
            long mil = Long.parseLong(milString);
            // 如果毫秒是两位数，需要乘以10
            if (milString.length() == 2) {
                mil = mil * 10;
            }
            long time = min * DateUtils.MINUTE_IN_MILLIS + sec * DateUtils.SECOND_IN_MILLIS + mil;

            entryList.add(new LrcRow(time, text));
        }
        return entryList;
    }
}
