package material.hunter.RecyclerViewData;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;

import material.hunter.AsyncTask.MaterialHunterAsynctask;
import material.hunter.SQL.MaterialHunterSQL;
import material.hunter.models.MaterialHunterModel;

import java.util.ArrayList;
import java.util.List;

public class MaterialHunterData {
    public static boolean isDataInitiated = false;
    private static MaterialHunterData instance;
    public List<MaterialHunterModel> nethunterModelListFull;
    private ArrayList<MaterialHunterModel> nethunterModelArrayList = new ArrayList<>();
    private MutableLiveData<List<MaterialHunterModel>> data = new MutableLiveData<>();
    private List<MaterialHunterModel> copyOfMaterialHunterModelListFull = new ArrayList<>();

    public synchronized static MaterialHunterData getInstance() {
        if (instance == null) {
            instance = new MaterialHunterData();
        }
        return instance;
    }

    public MutableLiveData<List<MaterialHunterModel>> getMaterialHunterModels(Context context) {
        if (!isDataInitiated) {
            data.setValue(MaterialHunterSQL.getInstance(context).bindData(nethunterModelArrayList));
            nethunterModelListFull = new ArrayList<>(data.getValue());
            isDataInitiated = true;
        }
        return data;
    }

    public MutableLiveData<List<MaterialHunterModel>> getMaterialHunterModels() {
        return data;
    }

    public void refreshData() {
        MaterialHunterAsynctask nethunterAsynctask = new MaterialHunterAsynctask(MaterialHunterAsynctask.GETITEMRESULTS);
        nethunterAsynctask.setListener(new MaterialHunterAsynctask.MaterialHunterAsynctaskListener() {
            @Override
            public void onAsyncTaskPrepare() {

            }

            @Override
            public void onAsyncTaskFinished(List<MaterialHunterModel> nethunterModelList) {
                getMaterialHunterModels().getValue().clear();
                getMaterialHunterModels().getValue().addAll(nethunterModelList);
                getMaterialHunterModels().postValue(getMaterialHunterModels().getValue());
            }
        });
        nethunterAsynctask.execute(getInitCopyOfMaterialHunterModelListFull());
    }

    public void runCommandforItem(int position) {
        MaterialHunterAsynctask nethunterAsynctask = new MaterialHunterAsynctask(MaterialHunterAsynctask.RUNCMDFORITEM, position);
        nethunterAsynctask.setListener(new MaterialHunterAsynctask.MaterialHunterAsynctaskListener() {
            @Override
            public void onAsyncTaskPrepare() {

            }

            @Override
            public void onAsyncTaskFinished(List<MaterialHunterModel> nethunterModelList) {
                getMaterialHunterModels().getValue().clear();
                getMaterialHunterModels().getValue().addAll(nethunterModelList);
                getMaterialHunterModels().postValue(getMaterialHunterModels().getValue());
            }
        });
        nethunterAsynctask.execute(getInitCopyOfMaterialHunterModelListFull());
    }

    public void editData(int position, ArrayList<String> dataArrayList, MaterialHunterSQL nethunterSQL) {
        MaterialHunterAsynctask nethunterAsynctask = new MaterialHunterAsynctask(MaterialHunterAsynctask.EDITDATA, position, dataArrayList, nethunterSQL);
        nethunterAsynctask.setListener(new MaterialHunterAsynctask.MaterialHunterAsynctaskListener() {
            @Override
            public void onAsyncTaskPrepare() {

            }

            @Override
            public void onAsyncTaskFinished(List<MaterialHunterModel> nethunterModelList) {
                updateMaterialHunterModelListFull(nethunterModelList);
                getMaterialHunterModels().getValue().clear();
                getMaterialHunterModels().getValue().addAll(nethunterModelList);
                getMaterialHunterModels().postValue(getMaterialHunterModels().getValue());
            }
        });
        nethunterAsynctask.execute(getInitCopyOfMaterialHunterModelListFull());
    }

    public void addData(int position, ArrayList<String> dataArrayList, MaterialHunterSQL nethunterSQL) {
        MaterialHunterAsynctask nethunterAsynctask = new MaterialHunterAsynctask(MaterialHunterAsynctask.ADDDATA, position, dataArrayList, nethunterSQL);
        nethunterAsynctask.setListener(new MaterialHunterAsynctask.MaterialHunterAsynctaskListener() {
            @Override
            public void onAsyncTaskPrepare() {

            }

            @Override
            public void onAsyncTaskFinished(List<MaterialHunterModel> nethunterModelList) {
                updateMaterialHunterModelListFull(nethunterModelList);
                getMaterialHunterModels().getValue().clear();
                getMaterialHunterModels().getValue().addAll(nethunterModelList);
                getMaterialHunterModels().postValue(getMaterialHunterModels().getValue());
            }
        });
        nethunterAsynctask.execute(getInitCopyOfMaterialHunterModelListFull());
    }

    public void deleteData(ArrayList<Integer> selectedPositionsIndex, ArrayList<Integer> selectedTargetIds, MaterialHunterSQL nethunterSQL) {
        MaterialHunterAsynctask nethunterAsynctask = new MaterialHunterAsynctask(MaterialHunterAsynctask.DELETEDATA, selectedPositionsIndex, selectedTargetIds, nethunterSQL);
        nethunterAsynctask.setListener(new MaterialHunterAsynctask.MaterialHunterAsynctaskListener() {
            @Override
            public void onAsyncTaskPrepare() {

            }

            @Override
            public void onAsyncTaskFinished(List<MaterialHunterModel> nethunterModelList) {
                updateMaterialHunterModelListFull(nethunterModelList);
                getMaterialHunterModels().getValue().clear();
                getMaterialHunterModels().getValue().addAll(nethunterModelList);
                getMaterialHunterModels().postValue(getMaterialHunterModels().getValue());
            }
        });
        nethunterAsynctask.execute(getInitCopyOfMaterialHunterModelListFull());
    }

    public void moveData(int originalPositionIndex, int targetPositionIndex, MaterialHunterSQL nethunterSQL) {
        MaterialHunterAsynctask nethunterAsynctask = new MaterialHunterAsynctask(MaterialHunterAsynctask.MOVEDATA, originalPositionIndex, targetPositionIndex, nethunterSQL);
        nethunterAsynctask.setListener(new MaterialHunterAsynctask.MaterialHunterAsynctaskListener() {
            @Override
            public void onAsyncTaskPrepare() {

            }

            @Override
            public void onAsyncTaskFinished(List<MaterialHunterModel> nethunterModelList) {
                updateMaterialHunterModelListFull(nethunterModelList);
                getMaterialHunterModels().getValue().clear();
                getMaterialHunterModels().getValue().addAll(nethunterModelList);
                getMaterialHunterModels().postValue(getMaterialHunterModels().getValue());
            }
        });
        nethunterAsynctask.execute(getInitCopyOfMaterialHunterModelListFull());
    }

    public String backupData(MaterialHunterSQL nethunterSQL, String storedDBpath) {
        return nethunterSQL.backupData(storedDBpath);
    }

    public String restoreData(MaterialHunterSQL nethunterSQL, String storedDBpath) {
        String returnedResult = nethunterSQL.restoreData(storedDBpath);
        if (returnedResult == null) {
            MaterialHunterAsynctask nethunterAsynctask = new MaterialHunterAsynctask(MaterialHunterAsynctask.RESTOREDATA, nethunterSQL);
            nethunterAsynctask.setListener(new MaterialHunterAsynctask.MaterialHunterAsynctaskListener() {
                @Override
                public void onAsyncTaskPrepare() {

                }

                @Override
                public void onAsyncTaskFinished(List<MaterialHunterModel> nethunterModelList) {
                    updateMaterialHunterModelListFull(nethunterModelList);
                    getMaterialHunterModels().getValue().clear();
                    getMaterialHunterModels().getValue().addAll(nethunterModelList);
                    getMaterialHunterModels().postValue(getMaterialHunterModels().getValue());
                    refreshData();
                }
            });
            nethunterAsynctask.execute(getInitCopyOfMaterialHunterModelListFull());
            return null;
        } else {
            return returnedResult;
        }
    }

    public void resetData(MaterialHunterSQL nethunterSQL) {
        nethunterSQL.resetData();
        MaterialHunterAsynctask nethunterAsynctask = new MaterialHunterAsynctask(MaterialHunterAsynctask.RESTOREDATA, nethunterSQL);
        nethunterAsynctask.setListener(new MaterialHunterAsynctask.MaterialHunterAsynctaskListener() {
            @Override
            public void onAsyncTaskPrepare() {

            }

            @Override
            public void onAsyncTaskFinished(List<MaterialHunterModel> nethunterModelList) {
                updateMaterialHunterModelListFull(nethunterModelList);
                getMaterialHunterModels().getValue().clear();
                getMaterialHunterModels().getValue().addAll(nethunterModelList);
                getMaterialHunterModels().postValue(getMaterialHunterModels().getValue());
                refreshData();
            }
        });
        nethunterAsynctask.execute(getInitCopyOfMaterialHunterModelListFull());
    }

    public void updateMaterialHunterModelListFull(List<MaterialHunterModel> copyOfMaterialHunterModelList) {
        nethunterModelListFull.clear();
        nethunterModelListFull.addAll(copyOfMaterialHunterModelList);
    }

    private List<MaterialHunterModel> getInitCopyOfMaterialHunterModelListFull() {
        copyOfMaterialHunterModelListFull.clear();
        copyOfMaterialHunterModelListFull.addAll(nethunterModelListFull);
        return copyOfMaterialHunterModelListFull;
    }

}
