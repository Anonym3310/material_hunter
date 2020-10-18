package material.hunter.AsyncTask;

import android.os.AsyncTask;

import material.hunter.SQL.MaterialHunterSQL;
import material.hunter.models.MaterialHunterModel;
import material.hunter.utils.ShellExecuter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MaterialHunterAsynctask extends AsyncTask<List<MaterialHunterModel>, Void, List<MaterialHunterModel>> {

    public static final int GETITEMRESULTS = 0;
    public static final int RUNCMDFORITEM = 1;
    public static final int EDITDATA = 2;
    public static final int ADDDATA = 3;
    public static final int DELETEDATA = 4;
    public static final int MOVEDATA = 5;
    public static final int BACKUPDATA = 6;
    public static final int RESTOREDATA = 7;
    public static final int RESETDATA = 8;
    private MaterialHunterAsynctaskListener listener;
    private int actionCode;
    private int position;
    private int originalPositionIndex;
    private int targetPositionIndex;
    private ArrayList<Integer> selectedPositionsIndex;
    private ArrayList<Integer> selectedTargetIds;
    private ArrayList<String> dataArrayList;
    private MaterialHunterSQL nethunterSQL;
    private List<MaterialHunterModel> nethunterModelList;

    public MaterialHunterAsynctask(int actionCode) {
        this.actionCode = actionCode;
    }

    public MaterialHunterAsynctask(int actionCode, int position) {
        this.actionCode = actionCode;
        this.position = position;
    }

    public MaterialHunterAsynctask(int actionCode, int position, ArrayList<String> dataArrayList, MaterialHunterSQL nethunterSQL) {
        this.actionCode = actionCode;
        this.position = position;
        this.dataArrayList = dataArrayList;
        this.nethunterSQL = nethunterSQL;
    }

    public MaterialHunterAsynctask(int actionCode, ArrayList<Integer> selectedPositionsIndex, ArrayList<Integer> selectedTargetIds, MaterialHunterSQL nethunterSQL) {
        this.actionCode = actionCode;
        this.selectedPositionsIndex = selectedPositionsIndex;
        this.selectedTargetIds = selectedTargetIds;
        this.nethunterSQL = nethunterSQL;
    }

    public MaterialHunterAsynctask(int actionCode, int originalPositionIndex, int targetPositionIndex, MaterialHunterSQL nethunterSQL) {
        this.actionCode = actionCode;
        this.originalPositionIndex = originalPositionIndex;
        this.targetPositionIndex = targetPositionIndex;
        this.nethunterSQL = nethunterSQL;
    }

    public MaterialHunterAsynctask(int actionCode, MaterialHunterSQL nethunterSQL) {
        this.actionCode = actionCode;
        this.nethunterSQL = nethunterSQL;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (listener != null) {
            listener.onAsyncTaskPrepare();
        }
    }

    @Override
    protected List<MaterialHunterModel> doInBackground(List<MaterialHunterModel>... copyOfnethunterModelList) {
        switch (actionCode) {
            case GETITEMRESULTS:
                nethunterModelList = copyOfnethunterModelList[0];
                if (nethunterModelList != null) {
                    for (int i = 0; i < nethunterModelList.size(); i++) {
                        nethunterModelList.get(i).setResult(nethunterModelList.get(i).getRunOnCreate().equals("1") ? new ShellExecuter().RunAsRootOutput(nethunterModelList.get(i).getCommand()).split("\\n") : "Please click RUN button manually.".split("\\n"));
                    }
                }
                break;
            case RUNCMDFORITEM:
                nethunterModelList = copyOfnethunterModelList[0];
                if (nethunterModelList != null) {
                    nethunterModelList.get(position).setResult(new ShellExecuter().RunAsRootOutput(nethunterModelList.get(position).getCommand()).split("\\n"));
                }
                break;
            case EDITDATA:
                nethunterModelList = copyOfnethunterModelList[0];
                if (nethunterModelList != null) {
                    nethunterModelList.get(position).setTitle(dataArrayList.get(0));
                    nethunterModelList.get(position).setCommand(dataArrayList.get(1));
                    nethunterModelList.get(position).setDelimiter(dataArrayList.get(2));
                    nethunterModelList.get(position).setRunOnCreate(dataArrayList.get(3));
                    if (dataArrayList.get(3).equals("1")) {
                        nethunterModelList.get(position).setResult(new ShellExecuter().RunAsRootOutput(dataArrayList.get(1)).split(dataArrayList.get(2)));
                    }
                    nethunterSQL.editData(position, dataArrayList);
                }
                break;
            case ADDDATA:
                nethunterModelList = copyOfnethunterModelList[0];
                if (nethunterModelList != null) {

                    nethunterModelList.add(position - 1, new MaterialHunterModel(
                            dataArrayList.get(0),
                            dataArrayList.get(1),
                            dataArrayList.get(2),
                            dataArrayList.get(3),
                            "".split(dataArrayList.get(2))));
                    if (dataArrayList.get(3).equals("1")) {
                        nethunterModelList.get(position - 1).setResult(new ShellExecuter().RunAsRootOutput(dataArrayList.get(1)).split(dataArrayList.get(2)));
                    }
                    nethunterSQL.addData(position, dataArrayList);
                }
                break;
            case DELETEDATA:
                nethunterModelList = copyOfnethunterModelList[0];
                if (nethunterModelList != null) {
                    Collections.sort(selectedPositionsIndex, Collections.reverseOrder());
                    for (Integer selectedPosition : selectedPositionsIndex) {
                        int i = selectedPosition;
                        nethunterModelList.remove(i);
                    }
                    nethunterSQL.deleteData(selectedTargetIds);
                }
                break;
            case MOVEDATA:
                nethunterModelList = copyOfnethunterModelList[0];
                if (nethunterModelList != null) {
                    MaterialHunterModel tempMaterialHunterModel = new MaterialHunterModel(
                            nethunterModelList.get(originalPositionIndex).getTitle(),
                            nethunterModelList.get(originalPositionIndex).getCommand(),
                            nethunterModelList.get(originalPositionIndex).getDelimiter(),
                            nethunterModelList.get(originalPositionIndex).getRunOnCreate(),
                            nethunterModelList.get(originalPositionIndex).getResult()
                    );
                    nethunterModelList.remove(originalPositionIndex);
                    if (originalPositionIndex < targetPositionIndex) {
                        targetPositionIndex = targetPositionIndex - 1;
                    }
                    nethunterModelList.add(targetPositionIndex, tempMaterialHunterModel);
                    nethunterSQL.moveData(originalPositionIndex, targetPositionIndex);
                }
                break;
            case BACKUPDATA:
                break;
            case RESTOREDATA:
                nethunterModelList = copyOfnethunterModelList[0];
                if (nethunterModelList != null) {
                    nethunterModelList.clear();
                    nethunterModelList = nethunterSQL.bindData((ArrayList<MaterialHunterModel>) nethunterModelList);
                }
                break;
            case RESETDATA:
                break;
        }
        return copyOfnethunterModelList[0];
    }

    @Override
    protected void onPostExecute(List<MaterialHunterModel> nethunterModelList) {
        super.onPostExecute(nethunterModelList);
        if (listener != null) {
            listener.onAsyncTaskFinished(nethunterModelList);
        }
    }

    public void setListener(MaterialHunterAsynctaskListener listener) {
        this.listener = listener;
    }

    public interface MaterialHunterAsynctaskListener {
        void onAsyncTaskPrepare();

        void onAsyncTaskFinished(List<MaterialHunterModel> nethunterModelList);
    }
}
