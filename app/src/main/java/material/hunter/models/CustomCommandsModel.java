package material.hunter.models;

public class CustomCommandsModel {

    private String label;
    private String cmd;
    private String env;
    private String mode;
    private String receive;

    public CustomCommandsModel(String label, String cmd, String env, String mode, String receive) {
        this.label = label;
        this.cmd = cmd;
        this.env = env;
        this.mode = mode;
        this.receive = receive;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getCommand() {
        return cmd;
    }

    public void setCommand(String cmd) {
        this.cmd = cmd;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getRunOnBoot() {
        return receive;
    }

    public void setRunOnBoot(String receive) {
        this.receive = receive;
    }
}