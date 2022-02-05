package material.hunter.models;

/*
   MaterialHunter Model class, each model object represent the data of each recyclerview item.
*/
public class MaterialHunterModel {
  private String title;
  private String command;
  private String delimiter;
  private String runOnCreate;
  private String[] result;

  public MaterialHunterModel(
      String title, String command, String delimiter, String runOnCreate, String[] result) {
    this.title = title;
    this.command = command;
    this.delimiter = delimiter;
    this.runOnCreate = runOnCreate;
    this.result = result;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getCommand() {
    return command;
  }

  public void setCommand(String command) {
    this.command = command;
  }

  public String getDelimiter() {
    return delimiter;
  }

  public void setDelimiter(String delimiter) {
    this.delimiter = delimiter;
  }

  public String getRunOnCreate() {
    return runOnCreate;
  }

  public void setRunOnCreate(String runOnCreate) {
    this.runOnCreate = runOnCreate;
  }

  public String[] getResult() {
    return result;
  }

  public void setResult(String[] result) {
    this.result = result;
  }
}
