package top.cnzrg.tanchishe.music.lrc;

public class LrcRow implements Comparable<LrcRow> {
    private String timeStr;
    private long time;
    private long totalTime;
    private String content;

    public LrcRow() {

    }

    public LrcRow(long time, String content) {
        setTime(time);
        setContent(content);
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setTimeStr(String timeStr) {
        this.timeStr = timeStr;
    }

    public void setTotalTime(long totalTime) {
        this.totalTime = totalTime;
    }

    public String getTimeStr() {
        return timeStr;
    }

    public long getTime() {
        return time;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public String getContent() {
        return content;
    }

    @Override
    public int compareTo(LrcRow o) {
        if (o == null) {
            return -1;
        }
        return (int) (time - o.getTime());
    }

    @Override
    public String toString() {
        return "LrcRow{" +
                "timeStr='" + timeStr + '\'' +
                ", time=" + time +
                ", totalTime=" + totalTime +
                ", content='" + content + '\'' +
                '}';
    }
}
