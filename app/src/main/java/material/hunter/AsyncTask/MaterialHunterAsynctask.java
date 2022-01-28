package material.hunter.AsyncTask;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import material.hunter.SQL.MaterialHunterSQL;
import material.hunter.models.MaterialHunterModel;
import material.hunter.utils.ShellExecuter;

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
    private MaterialHunterSQL materialhunterSQL;
    private List<MaterialHunterModel> materialhunterModelList;

    public MaterialHunterAsynctask(int actionCode) {
        this.actionCode = actionCode;
    }

    public MaterialHunterAsynctask(int actionCode, int position) {
        this.actionCode = actionCode;
        this.position = position;
    }

    public MaterialHunterAsynctask(int actionCode, int position, ArrayList<String> dataArrayList, MaterialHunterSQL materialhunterSQL) {
        this.actionCode = actionCode;
        this.position = position;
        this.dataArrayList = dataArrayList;
        this.materialhunterSQL = materialhunterSQL;
    }

    public MaterialHunterAsynctask(int actionCode, ArrayList<Integer> selectedPositionsIndex, ArrayList<Integer> selectedTargetIds, MaterialHunterSQL materialhunterSQL) {
        this.actionCode = actionCode;
        this.selectedPositionsIndex = selectedPositionsIndex;
        this.selectedTargetIds = selectedTargetIds;
        this.materialhunterSQL = materialhunterSQL;
    }

    public MaterialHunterAsynctask(int actionCode, int originalPositionIndex, int targetPositionIndex, MaterialHunterSQL materialhunterSQL) {
        this.actionCode = actionCode;
        this.originalPositionIndex = originalPositionIndex;
        this.targetPositionIndex = targetPositionIndex;
        this.materialhunterSQL = materialhunterSQL;
    }

    public MaterialHunterAsynctask(int actionCode, MaterialHunterSQL materialhunterSQL) {
        this.actionCode = actionCode;
        this.materialhunterSQL = materialhunterSQL;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (listener != null) {
            listener.onAsyncTaskPrepare();
        }
    }

    @Override
    protected List<MaterialHunterModel> doInBackground(List<MaterialHunterModel>... copyOfmaterialhunterModelList) {
        switch (actionCode) {
            case GETITEMRESULTS:
                materialhunterModelList = copyOfmaterialhunterModelList[0];
                if (materialhunterModelList != null) {
                    for (int i = 0; i < materialhunterModelList.size(); i++) {
                        materialhunterModelList.get(i).setResult(materialhunterModelList.get(i).getRunOnCreate().equals("1") ? new ShellExecuter().RunAsRootOutput(materialhunterModelList.get(i).getCommand()).split("\\n") : "Please click RUN button manually.".split("\\n"));
                    }
                }
                break;
            case RUNCMDFORITEM:
                materialhunterModelList = copyOfmaterialhunterModelList[0];
                if (materialhunterModelList != null) {
                    materialhunterModelList.get(position).setResult(new ShellExecuter().RunAsRootOutput(materialhunterModelList.get(position).getCommand()).split("\\n"));
                }
                break;
            case EDITDATA:
                materialhunterModelList = copyOfmaterialhunterModelList[0];
                if (materialhunterModelList != null) {
                    materialhunterModelList.get(position).setTitle(dataArrayList.get(0));
                    materialhunterModelList.get(position).setCommand(dataArrayList.get(1));
                    materialhunterModelList.get(position).setDelimiter(dataArrayList.get(2));
                    materialhunterModelList.get(position).setRunOnCreate(dataArrayList.get(3));
                    if (dataArrayList.get(3).equals("1")) {
                        materialhunterModelList.get(position).setResult(new ShellExecuter().RunAsRootOutput(dataArrayList.get(1)).split(dataArrayList.get(2)));
                    }
                    materialhunterSQL.editData(position, dataArrayList);
                }
                break;
            case ADDDATA:
                materialhunterModelList = copyOfmaterialhunterModelList[0];
                if (materialhunterModelList != null) {

                    materialhunterModelList.add(position - 1, new MaterialHunterModel(
                            dataArrayList.get(0),
                            dataArrayList.get(1),
                            dataArrayList.get(2),
                            dataArrayList.get(3),
                            "".split(dataArrayList.get(2))));
                    if (dataArrayList.get(3).equals("1")) {
                        materialhunterModelList.get(position - 1).setResult(new ShellExecuter().RunAsRootOutput(dataArrayList.get(1)).split(dataArrayList.get(2)));
                    }
                    materialhunterSQL.addData(position, dataArrayList);
                }
                break;
            case DELETEDATA:
                materialhunterModelList = copyOfmaterialhunterModelList[0];
                if (materialhunterModelList != null) {
                    Collections.sort(selectedPositionsIndex, Collections.reverseOrder());
                    for (Integer selectedPosition : selectedPositionsIndex) {
                        int i = selectedPosition;
                        materialhunterModelList.remove(i);
                    }
                    materialhunterSQL.deleteData(selectedTargetIds);
                }
                break;
            case MOVEDATA:
                materialhunterModelList = copyOfmaterialhunterModelList[0];
                if (materialhunterModelList != null) {
                    MaterialHunterModel tempMaterialHunterModel = new MaterialHunterModel(
                            materialhunterModelList.get(originalPositionIndex).getTitle(),
                            materialhunterModelList.get(originalPositionIndex).getCommand(),
                            materialhunterModelList.get(originalPositionIndex).getDelimiter(),
                            materialhunterModelList.get(originalPositionIndex).getRunOnCreate(),
                            materialhunterModelList.get(originalPositionIndex).getResult()
                    );
                    materialhunterModelList.remove(originalPositionIndex);
                    if (originalPositionIndex < targetPositionIndex) {
                        targetPositionIndex = targetPositionIndex - 1;
                    }
                    materialhunterModelList.add(targetPositionIndex, tempMaterialHunterModel);
                    materialhunterSQL.moveData(originalPositionIndex, targetPositionIndex);
                }
                break;
            case BACKUPDATA:
                break;
            case RESTOREDATA:
                materialhunterModelList = copyOfmaterialhunterModelList[0];
                if (materialhunterModelList != null) {
                    materialhunterModelList.clear();
                    materialhunterModelList = materialhunterSQL.bindData((ArrayList<MaterialHunterModel>) materialhunterModelList);
                }
                break;
            case RESETDATA:
                break;
        }
        return copyOfmaterialhunterModelList[0];
    }

    @Override
    protected void onPostExecute(List<MaterialHunterModel> materialhunterModelList) {
        super.onPostExecute(materialhunterModelList);
        if (listener != null) {
            listener.onAsyncTaskFinished(materialhunterModelList);
        }
    }

    public void setListener(MaterialHunterAsynctaskListener listener) {
        this.listener = listener;
    }

    public interface MaterialHunterAsynctaskListener {
        void onAsyncTaskPrepare();

        void onAsyncTaskFinished(List<MaterialHunterModel> materialhunterModelList);
    }
}
