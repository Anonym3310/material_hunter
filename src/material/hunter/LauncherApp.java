/**
 * Created by AnglerVonMur on 26.07.15.
 */
package material.hunter;

public class LauncherApp {

    final static String TABLE = "LAUNCHERS";
    final static String ID = "ID";
    final static String BTN_LABEL = "BTN_LABEL";
    final static String CMD = "COMMAND";
    final static String[] COLUMNS = {ID, BTN_LABEL, CMD};
    private long id;
    private String btn_label;
    private String command;

    LauncherApp() {
    }

    public LauncherApp(long id, String btn_name, String command) {
        this.id = id;
        this.btn_label = btn_name;
        this.command = command;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    String getBtn_label() {
        return btn_label;
    }

    void setBtn_label(String btn_label) {
        this.btn_label = btn_label;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}