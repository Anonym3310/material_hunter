package material.hunter.AsyncTask;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import material.hunter.ChrootManagerFragment;
import material.hunter.CustomCommandsFragment;
import material.hunter.SQL.CustomCommandsSQL;
import material.hunter.models.CustomCommandsModel;
import material.hunter.service.NotificationChannelService;
import material.hunter.utils.PathsUtil;
import material.hunter.utils.ShellExecuter;

public class CustomCommandsAsyncTask extends AsyncTask<List<CustomCommandsModel>, Void, List<CustomCommandsModel>> {
  public static final int RUNCMD = 0;
  public static final int EDITDATA = 1;
  public static final int ADDDATA = 2;
  public static final int DELETEDATA = 3;
  public static final int MOVEDATA = 4;
  public static final int BACKUPDATA = 5;
  public static final int RESTOREDATA = 6;
  public static final int RESETDATA = 7;
  public static final int ANDROID_CMD_SUCCESS = 100;
  public static final int ANDROID_CMD_FAIL = 101;
  public static final int CHROOT_CMD_SUCCESS = 102;
  public static final int CHROOT_CMD_FAIL = 103;
  private CustomCommandsAsyncTaskListener listener;
  private int actionCode;
  private int position;
  private int originalPositionIndex;
  private int targetPositionIndex;
  private WeakReference<Context> context = null;
  private ArrayList<Integer> selectedPositionsIndex;
  private ArrayList<Integer> selectedTargetIds;
  private ArrayList<String> dataArrayList;
  private CustomCommandsSQL customCommandsSQL;
  private int returnValue = 0;

  public CustomCommandsAsyncTask(int actionCode) {
    this.actionCode = actionCode;
  }

  public CustomCommandsAsyncTask(int actionCode, int position) {
    this.actionCode = actionCode;
    this.position = position;
  }

  public CustomCommandsAsyncTask(int actionCode, int position, Context context) {
    this.actionCode = actionCode;
    this.position = position;
    this.context = new WeakReference<>(context);
  }

  public CustomCommandsAsyncTask(
      int actionCode,
      int position,
      ArrayList<String> dataArrayList,
      CustomCommandsSQL customCommandsSQL) {
    this.actionCode = actionCode;
    this.position = position;
    this.dataArrayList = dataArrayList;
    this.customCommandsSQL = customCommandsSQL;
  }

  public CustomCommandsAsyncTask(
      int actionCode,
      ArrayList<Integer> selectedPositionsIndex,
      ArrayList<Integer> selectedTargetIds,
      CustomCommandsSQL customCommandsSQL) {
    this.actionCode = actionCode;
    this.selectedPositionsIndex = selectedPositionsIndex;
    this.selectedTargetIds = selectedTargetIds;
    this.customCommandsSQL = customCommandsSQL;
  }

  public CustomCommandsAsyncTask(
      int actionCode,
      int originalPositionIndex,
      int targetPositionIndex,
      CustomCommandsSQL customCommandsSQL) {
    this.actionCode = actionCode;
    this.originalPositionIndex = originalPositionIndex;
    this.targetPositionIndex = targetPositionIndex;
    this.customCommandsSQL = customCommandsSQL;
  }

  public CustomCommandsAsyncTask(int actionCode, CustomCommandsSQL customCommandsSQL) {
    this.actionCode = actionCode;
    this.customCommandsSQL = customCommandsSQL;
  }

  @Override
  protected void onPreExecute() {
    super.onPreExecute();
    ChrootManagerFragment.isAsyncTaskRunning = true;
    if (listener != null) {
      listener.onAsyncTaskPrepare();
    }
  }

  @Override
  protected List<CustomCommandsModel> doInBackground(
      List<CustomCommandsModel>... copyOfcustomCommandsModelList) {
    List<CustomCommandsModel> customCommandsModelList;
    switch (actionCode) {
      case RUNCMD:
        customCommandsModelList = copyOfcustomCommandsModelList[0];
        if (customCommandsModelList != null) {
          if (customCommandsModelList.get(position).getExecutionMode().equals("interactive")) {
            String command = customCommandsModelList.get(position).getCommand();
            try {
              Intent intent = new Intent();
              intent.setClassName("com.termux", "com.termux.app.RunCommandService");
              intent.setAction("com.termux.RUN_COMMAND");
              intent.putExtra(
                  "com.termux.RUN_COMMAND_PATH", "/data/data/com.termux/files/usr/bin/su");
              intent.putExtra(
                  "com.termux.RUN_COMMAND_ARGUMENTS",
                  new String[] {
                    "-c",
                    (customCommandsModelList.get(position).getRuntimeEnv().equals("android")
                        ? command
                        : PathsUtil.APP_SCRIPTS_PATH
                            + "/bootroot_exec \""
                            + command.replace("\"", "\\\"")
                            + "\"")
                  });
              intent.putExtra("com.termux.RUN_COMMAND_WORKDIR", "/data/data/com.termux/files/home");
              intent.putExtra("com.termux.RUN_COMMAND_BACKGROUND", false);
              intent.putExtra("com.termux.RUN_COMMAND_SESSION_ACTION", "0");
              context.get().startService(intent);
            } catch (RuntimeException e) {
              PathsUtil.showSnack(
                  CustomCommandsFragment.customCommandsView, "Termux uid isn't exists.", false);
            }
          } else {
            Intent intent =
                new Intent(context.get(), NotificationChannelService.class)
                    .setAction(NotificationChannelService.CUSTOMCOMMAND_START);
            intent
                .putExtra("ENV", customCommandsModelList.get(position).getRuntimeEnv())
                .putExtra("CMD", customCommandsModelList.get(position).getCommand());
            context.get().startService(intent);
            if (customCommandsModelList.get(position).getRuntimeEnv().equals("android")) {
              returnValue =
                  new ShellExecuter()
                      .RunAsRootReturnValue(customCommandsModelList.get(position).getCommand());
              if (returnValue == 0) {
                returnValue = ANDROID_CMD_SUCCESS;
              } else {
                returnValue = ANDROID_CMD_FAIL;
              }
            } else {
              intent
                  .putExtra("ENV", "chroot")
                  .putExtra("cmd", customCommandsModelList.get(position).getCommand());
              context.get().startService(intent);
              returnValue =
                  new ShellExecuter()
                      .RunAsChrootReturnValue(customCommandsModelList.get(position).getCommand());
              if (returnValue == 0) {
                returnValue = CHROOT_CMD_SUCCESS;
              } else {
                returnValue = CHROOT_CMD_FAIL;
              }
            }
          }
        }
        break;
      case EDITDATA:
        customCommandsModelList = copyOfcustomCommandsModelList[0];
        if (customCommandsModelList != null) {
          customCommandsModelList.get(position).setCommandLabel(dataArrayList.get(0));
          customCommandsModelList.get(position).setCommand(dataArrayList.get(1));
          customCommandsModelList.get(position).setRuntimeEnv(dataArrayList.get(2));
          customCommandsModelList.get(position).setExecutionMode(dataArrayList.get(3));
          customCommandsModelList.get(position).setRunOnBoot(dataArrayList.get(4));
          updateRunOnBootScripts(customCommandsModelList);
          customCommandsSQL.editData(position, dataArrayList);
        }
        break;
      case ADDDATA:
        customCommandsModelList = copyOfcustomCommandsModelList[0];
        if (customCommandsModelList != null) {
          customCommandsModelList.add(
              position - 1,
              new CustomCommandsModel(
                  dataArrayList.get(0),
                  dataArrayList.get(1),
                  dataArrayList.get(2),
                  dataArrayList.get(3),
                  dataArrayList.get(4)));
          if (dataArrayList.get(4).equals("1")) {
            updateRunOnBootScripts(customCommandsModelList);
          }
          customCommandsSQL.addData(position, dataArrayList);
        }
        break;
      case DELETEDATA:
        customCommandsModelList = copyOfcustomCommandsModelList[0];
        if (customCommandsModelList != null) {
          Collections.sort(selectedPositionsIndex, Collections.reverseOrder());
          for (Integer selectedPosition : selectedPositionsIndex) {
            int i = selectedPosition;
            customCommandsModelList.remove(i);
          }
          customCommandsSQL.deleteData(selectedTargetIds);
        }
        break;
      case MOVEDATA:
        customCommandsModelList = copyOfcustomCommandsModelList[0];
        if (customCommandsModelList != null) {
          CustomCommandsModel tempCustomCommandsModel =
              new CustomCommandsModel(
                  customCommandsModelList.get(originalPositionIndex).getCommandLabel(),
                  customCommandsModelList.get(originalPositionIndex).getCommand(),
                  customCommandsModelList.get(originalPositionIndex).getRuntimeEnv(),
                  customCommandsModelList.get(originalPositionIndex).getExecutionMode(),
                  customCommandsModelList.get(originalPositionIndex).getRunOnBoot());
          customCommandsModelList.remove(originalPositionIndex);
          if (originalPositionIndex < targetPositionIndex) {
            targetPositionIndex = targetPositionIndex - 1;
          }
          customCommandsModelList.add(targetPositionIndex, tempCustomCommandsModel);
          customCommandsSQL.moveData(originalPositionIndex, targetPositionIndex);
        }

        break;
      case BACKUPDATA:
        break;
      case RESTOREDATA:
        customCommandsModelList = copyOfcustomCommandsModelList[0];
        if (customCommandsModelList != null) {
          customCommandsModelList.clear();
          customCommandsModelList =
              customCommandsSQL.bindData((ArrayList<CustomCommandsModel>) customCommandsModelList);
        }
        break;
      case RESETDATA:
        break;
    }
    return copyOfcustomCommandsModelList[0];
  }

  @Override
  protected void onPostExecute(List<CustomCommandsModel> customCommandsModelList) {
    super.onPostExecute(customCommandsModelList);
    if (listener != null) {
      listener.onAsyncTaskFinished(customCommandsModelList);
    }
    ChrootManagerFragment.isAsyncTaskRunning = false;
    if (returnValue != 0) {
      Intent intent =
          new Intent(context.get(), NotificationChannelService.class)
              .setAction(NotificationChannelService.CUSTOMCOMMAND_FINISH);
      intent
          .putExtra("RETURNCODE", returnValue)
          .putExtra("CMD", customCommandsModelList.get(position).getCommand());
      context.get().startService(intent);
    }
  }

  public void setListener(CustomCommandsAsyncTaskListener listener) {
    this.listener = listener;
  }

  private void updateRunOnBootScripts(List<CustomCommandsModel> customCommandsModelList) {
    StringBuilder tmpStringBuilder = new StringBuilder();
    for (int i = 0; i < customCommandsModelList.size(); i++) {
      if (customCommandsModelList.get(i).getRunOnBoot().equals("1")) {
        tmpStringBuilder
            .append(customCommandsModelList.get(i).getRuntimeEnv())
            .append(" ")
            .append(customCommandsModelList.get(i).getCommand())
            .append("\n");
      }
    }
    new ShellExecuter()
        .RunAsRootOutput(
            "cat << 'EOF' > "
                + PathsUtil.APP_SCRIPTS_PATH
                + "/runonboot_services"
                + "\n"
                + tmpStringBuilder.toString()
                + "\nEOF");
  }

  public interface CustomCommandsAsyncTaskListener {
    void onAsyncTaskPrepare();

    void onAsyncTaskFinished(List<CustomCommandsModel> customCommandsModelList);
  }
}