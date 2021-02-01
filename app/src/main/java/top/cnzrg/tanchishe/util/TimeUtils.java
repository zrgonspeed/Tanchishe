package top.cnzrg.tanchishe.util;

public class TimeUtils {

    /**
     * 毫秒转成 mm:ss
     * 四舍五入
     *
     * @param time
     * @return
     */
    public static String timeToString(long time) {
        float a = time * 1f / 1000f;

        int round = Math.round(a);

        // 计算秒数
        int ss = round % 60;
        // 计算分钟数
        int mm = round / 60;

        String m1 = "";
        if (mm < 10) {
            m1 = "0" + mm;
        } else {
            m1 = mm + "";
        }

        String s1 = "";
        if (ss < 10) {
            s1 = "0" + ss;
        } else {
            s1 = ss + "";
        }

        String str = m1 + ":" + s1;

        return str;
    }
}
