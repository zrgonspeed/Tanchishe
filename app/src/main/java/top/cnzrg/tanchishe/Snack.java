package top.cnzrg.tanchishe;

public class Snack {
    private String name = "蛇";
    private Integer len = 1;

    public Snack() {
    }

    public Snack(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getLen() {
        return len;
    }

    public void setLen(Integer len) {
        this.len = len;
    }

    @Override
    public String toString() {
        return "Snack{" +
                "name='" + name + '\'' +
                ", len=" + len +
                '}';
    }
}
