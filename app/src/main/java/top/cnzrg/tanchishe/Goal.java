package top.cnzrg.tanchishe;

public class Goal {
    private String name = "目标";
    private Integer size = 1;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "Goal{" +
                "name='" + name + '\'' +
                ", size=" + size +
                '}';
    }
}
