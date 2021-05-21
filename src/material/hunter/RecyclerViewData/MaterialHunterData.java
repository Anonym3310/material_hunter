package material.hunter.RecyclerViewData;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import material.hunter.AsyncTask.MaterialHunterAsynctask;
import material.hunter.SQL.MaterialHunterSQL;
import material.hunter.models.MaterialHunterModel;

public class MaterialHunterData {
    public static boolean isDataInitiated = false;
    private static MaterialHunterData instance;
    private final ArrayList<MaterialHunterModel> materialhunterModelArrayList = new ArrayList<>();
    private final MutableLiveData<List<MaterialHunterModel>> data = new MutableLiveData<>();
    private final List<MaterialHunterModel> copyOfMaterialHunterModelListFull = new ArrayList<>();
    public List<MaterialHunterModel> materialhunterModelListFull;

    public synchronized static MaterialHunterData getInstance() {
        if (instance == null) {
            instance = new MaterialHunterData();
        }
        return instance;
    }

    public MutableLiveData<List<MaterialHunterModel>> getMaterialHunterModels(Context context) {
        if (!isDataInitiated) {
            data.setValue(MaterialHunterSQL.getInstance(context).bindData(materialhunterModelArrayList));
            materialhunterModelListFull = new ArrayList<>(data.getValue());
            isDataInitiated = true;
        }
        return data;
    }

    public MutableLiveData<List<MaterialHunterModel>> getMaterialHunterModels() {
        return data;
    }

    public void refreshData() {
        MaterialHunterAsynctask materialhunterAsynctask = new MaterialHunterAsynctask(MaterialHunterAsynctask.GETITEMRESULTS);
        materialhunterAsynctask.setListener(new MaterialHunterAsynctask.MaterialHunterAsynctaskListener() {
            @Override
            public void onAsyncTaskPrepare() {

            }

            @Override
            public void onAsyncTaskFinished(List<MaterialHunterModel> materialhunterModelList) {
                getMaterialHunterModels().getValue().clear();
                getMaterialHunterModels().getValue().addAll(materialhunterModelList);
                getMaterialHunterModels().postValue(getMaterialHunterModels().getValue());
            }
        });
        materialhunterAsynctask.execute(getInitCopyOfMaterialHunterModelListFull());
    }

    public void runCommandforItem(int position) {
        MaterialHunterAsynctask materialhunterAsynctask = new MaterialHunterAsynctask(MaterialHunterAsynctask.RUNCMDFORITEM, position);
        materialhunterAsynctask.setListener(new MaterialHunterAsynctask.MaterialHunterAsynctaskListener() {
            @Override
            public void onAsyncTaskPrepare() {

            }

            @Override
            public void onAsyncTaskFinished(List<MaterialHunterModel> materialhunterModelList) {
                getMaterialHunterModels().getValue().clear();
                getMaterialHunterModels().getValue().addAll(materialhunterModelList);
                getMaterialHunterModels().postValue(getMaterialHunterModels().getValue());
            }
        });
        materialhunterAsynctask.execute(getInitCopyOfMaterialHunterModelListFull());
    }

    public void editData(int position, ArrayList<String> dataArrayList, MaterialHunterSQL materialhunterSQL) {
        MaterialHunterAsynctask materialhunterAsynctask = new MaterialHunterAsynctask(MaterialHunterAsynctask.EDITDATA, position, dataArrayList, materialhunterSQL);
        materialhunterAsynctask.setListener(new MaterialHunterAsynctask.MaterialHunterAsynctaskListener() {
            @Override
            public void onAsyncTaskPrepare() {

            }

            @Override
            public void onAsyncTaskFinished(List<MaterialHunterModel> materialhunterModelList) {
                updateMaterialHunterModelListFull(materialhunterModelList);
                getMaterialHunterModels().getValue().clear();
                getMaterialHunterModels().getValue().addAll(materialhunterModelList);
                getMaterialHunterModels().postValue(getMaterialHunterModels().getValue());
            }
        });
        materialhunterAsynctask.execute(getInitCopyOfMaterialHunterModelListFull());
    }

    public void addData(int position, ArrayList<String> dataArrayList, MaterialHunterSQL materialhunterSQL) {
        MaterialHunterAsynctask materialhunterAsynctask = new MaterialHunterAsynctask(MaterialHunterAsynctask.ADDDATA, position, dataArrayList, materialhunterSQL);
        materialhunterAsynctask.setListener(new MaterialHunterAsynctask.MaterialHunterAsynctaskListener() {
            @Override
            public void onAsyncTaskPrepare() {

            }

            @Override
            public void onAsyncTaskFinished(List<MaterialHunterModel> materialhunterModelList) {
                updateMaterialHunterModelListFull(materialhunterModelList);
                getMaterialHunterModels().getValue().clear();
                getMaterialHunterModels().getValue().addAll(materialhunterModelList);
                getMaterialHunterModels().postValue(getMaterialHunterModels().getValue());
            }
        });
        materialhunterAsynctask.execute(getInitCopyOfMaterialHunterModelListFull());
    }

    public void deleteData(ArrayList<Integer> selectedPositionsIndex, ArrayList<Integer> selectedTargetIds, MaterialHunterSQL materialhunterSQL) {
        MaterialHunterAsynctask materialhunterAsynctask = new MaterialHunterAsynctask(MaterialHunterAsynctask.DELETEDATA, selectedPositionsIndex, selectedTargetIds, materialhunterSQL);
        materialhunterAsynctask.setListener(new MaterialHunterAsynctask.MaterialHunterAsynctaskListener() {
            @Override
            public void onAsyncTaskPrepare() {

            }

            @Override
            public void onAsyncTaskFinished(List<MaterialHunterModel> materialhunterModelList) {
                updateMaterialHunterModelListFull(materialhunterModelList);
                getMaterialHunterModels().getValue().clear();
                getMaterialHunterModels().getValue().addAll(materialhunterModelList);
                getMaterialHunterModels().postValue(getMaterialHunterModels().getValue());
            }
        });
        materialhunterAsynctask.execute(getInitCopyOfMaterialHunterModelListFull());
    }

    public void moveData(int originalPositionIndex, int targetPositionIndex, MaterialHunterSQL materialhunterSQL) {
        MaterialHunterAsynctask materialhunterAsynctask = new MaterialHunterAsynctask(MaterialHunterAsynctask.MOVEDATA, originalPositionIndex, targetPositionIndex, materialhunterSQL);
        materialhunterAsynctask.setListener(new MaterialHunterAsynctask.MaterialHunterAsynctaskListener() {
            @Override
            public void onAsyncTaskPrepare() {

            }

            @Override
            public void onAsyncTaskFinished(List<MaterialHunterModel> materialhunterModelList) {
                updateMaterialHunterModelListFull(materialhunterModelList);
                getMaterialHunterModels().getValue().clear();
                getMaterialHunterModels().getValue().addAll(materialhunterModelList);
                getMaterialHunterModels().postValue(getMaterialHunterModels().getValue());
            }
        });
        materialhunterAsynctask.execute(getInitCopyOfMaterialHunterModelListFull());
    }

    public String backupData(MaterialHunterSQL materialhunterSQL, String storedDBpath) {
        return materialhunterSQL.backupData(storedDBpath);
    }

    public String restoreData(MaterialHunterSQL materialhunterSQL, String storedDBpath) {
        String returnedResult = materialhunterSQL.restoreData(storedDBpath);
        if (returnedResult == null) {
            MaterialHunterAsynctask materialhunterAsynctask = new MaterialHunterAsynctask(MaterialHunterAsynctask.RESTOREDATA, materialhunterSQL);
            materialhunterAsynctask.setListener(new MaterialHunterAsynctask.MaterialHunterAsynctaskListener() {
                @Override
                public void onAsyncTaskPrepare() {

                }

                @Override
                public void onAsyncTaskFinished(List<MaterialHunterModel> materialhunterModelList) {
                    updateMaterialHunterModelListFull(materialhunterModelList);
                    getMaterialHunterModels().getValue().clear();
                    getMaterialHunterModels().getValue().addAll(materialhunterModelList);
                    getMaterialHunterModels().postValue(getMaterialHunterModels().getValue());
                    refreshData();
                }
            });
            materialhunterAsynctask.execute(getInitCopyOfMaterialHunterModelListFull());
            return null;
        } else {
            return returnedResult;
        }
    }

    public void resetData(MaterialHunterSQL materialhunterSQL) {
        materialhunterSQL.resetData();
        MaterialHunterAsynctask materialhunterAsynctask = new MaterialHunterAsynctask(MaterialHunterAsynctask.RESTOREDATA, materialhunterSQL);
        materialhunterAsynctask.setListener(new MaterialHunterAsynctask.MaterialHunterAsynctaskListener() {
            @Override
            public void onAsyncTaskPrepare() {

            }

            @Override
            public void onAsyncTaskFinished(List<MaterialHunterModel> materialhunterModelList) {
                updateMaterialHunterModelListFull(materialhunterModelList);
                getMaterialHunterModels().getValue().clear();
                getMaterialHunterModels().getValue().addAll(materialhunterModelList);
                getMaterialHunterModels().postValue(getMaterialHunterModels().getValue());
                refreshData();
            }
        });
        materialhunterAsynctask.execute(getInitCopyOfMaterialHunterModelListFull());
    }

    public void updateMaterialHunterModelListFull(List<MaterialHunterModel> copyOfMaterialHunterModelList) {
        materialhunterModelListFull.clear();
        materialhunterModelListFull.addAll(copyOfMaterialHunterModelList);
    }

    private List<MaterialHunterModel> getInitCopyOfMaterialHunterModelListFull() {
        copyOfMaterialHunterModelListFull.clear();
        copyOfMaterialHunterModelListFull.addAll(materialhunterModelListFull);
        return copyOfMaterialHunterModelListFull;
    }
}
