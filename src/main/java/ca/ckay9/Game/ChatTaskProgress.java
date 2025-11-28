package ca.ckay9.Game;

public class ChatTaskProgress {
    private VillagerTask task;
    private String answer;

    public ChatTaskProgress(VillagerTask task, String answer) {
        this.task = task;
        this.answer = answer;
    }

    public String getAnswer() {
        return this.answer;
    }

    public void setAnswer(String newAnswer) {
        this.answer = newAnswer;
    }

    public VillagerTask getTask() {
        return this.task;
    }

    public void setTask(VillagerTask task) {
        this.task = task;
    }
}
