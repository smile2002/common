package up.csd.async;

/**
 * Created by Smile on 2018/5/22.
 */
public enum TaskStatus {
    INIT("一开始啥都没干"),
    HALF_DONE("干了一半，等回调呢"),
    FAILED("失败了"),
    DONE("老子全干完啦");

    private String desc;

    TaskStatus(String desc) {this.desc = desc;}

    @Override
    public String toString() {
        return "TaskStatus[" + desc + "]";
    }
}