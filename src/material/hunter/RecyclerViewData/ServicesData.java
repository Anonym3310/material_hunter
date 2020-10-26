package material.hunter.RecyclerViewData;

import android.content.Context;
import android.widget.Switch;

import androidx.lifecycle.MutableLiveData;

import material.hunter.AsyncTask.ServicesAsyncTask;
import material.hunter.SQL.ServicesSQL;
import material.hunter.models.ServicesModel;
import material.hunter.utils.NhPaths;

import java.util.ArrayList;
import java.util.List;

public class ServicesData {
    public static boolean isDataInitiated = false;
    private static ServicesData instance;
    public List<ServicesModel> servicesModelListFull;
    private ArrayList<ServicesModel> servicesModelArrayList = new ArrayList<>();
    private MutableLiveData<List<ServicesModel>> data = new MutableLiveData<>();
    private List<ServicesModel> copyOfServicesModelListFull = new ArrayList<>();

    public synchronized static ServicesData getInstance() {
        if (instance == null) {
            instance = new ServicesData();
        }
        return instance;
    }

    public MutableLiveData<List<ServicesModel>> getKaliServicesModels(Context context) {
        if (!isDataInitiated) {
            data.setValue(ServicesSQL.getInstance(context).bindData(servicesModelArrayList));
            servicesModelListFull = new ArrayList<>(data.getValue());
            isDataInitiated = true;
        }
        return data;
    }

    public MutableLiveData<List<ServicesModel>> getKaliServicesModels() {
        return data;
    }

    public void refreshData() {
        ServicesAsyncTask servicesAsyncTask = new ServicesAsyncTask(ServicesAsyncTask.GETITEMSTATUS);
        servicesAsyncTask.setListener(new ServicesAsyncTask.KaliServicesAsyncTaskListener() {
            @Override
            public void onAsyncTaskPrepare() {

            }

            @Override
            public void onAsyncTaskFinished(List<ServicesModel> servicesModelList) {
                getKaliServicesModels().getValue().clear();
                getKaliServicesModels().getValue().addAll(servicesModelList);
                getKaliServicesModels().postValue(getKaliServicesModels().getValue());
            }
        });
        servicesAsyncTask.execute(getInitCopyOfKaliServicesModelListFull());
    }

    public void startServiceforItem(int position, Switch mSwitch, Context context) {
        ServicesAsyncTask servicesAsyncTask = new ServicesAsyncTask(ServicesAsyncTask.START_SERVICE_FOR_ITEM, position);
        servicesAsyncTask.setListener(new ServicesAsyncTask.KaliServicesAsyncTaskListener() {
            @Override
            public void onAsyncTaskPrepare() {
                mSwitch.setEnabled(false);
            }

            @Override
            public void onAsyncTaskFinished(List<ServicesModel> servicesModelList) {
                mSwitch.setEnabled(true);
                mSwitch.setChecked(servicesModelList.get(position).getStatus().startsWith("[+]"));
                getKaliServicesModels().getValue().clear();
                getKaliServicesModels().getValue().addAll(servicesModelList);
                getKaliServicesModels().postValue(getKaliServicesModels().getValue());
                if (!mSwitch.isChecked())
                    NhPaths.showMessage(context, "Failed starting " + getKaliServicesModels().getValue().get(position).getServiceName() + " service");
            }
        });
        servicesAsyncTask.execute(getInitCopyOfKaliServicesModelListFull());
    }

    public void stopServiceforItem(int position, Switch mSwitch, Context context) {
        ServicesAsyncTask servicesAsyncTask = new ServicesAsyncTask(ServicesAsyncTask.STOP_SERVICE_FOR_ITEM, position);
        servicesAsyncTask.setListener(new ServicesAsyncTask.KaliServicesAsyncTaskListener() {
            @Override
            public void onAsyncTaskPrepare() {
                mSwitch.setEnabled(false);
            }

            @Override
            public void onAsyncTaskFinished(List<ServicesModel> servicesModelList) {
                mSwitch.setEnabled(true);
                mSwitch.setChecked(servicesModelList.get(position).getStatus().startsWith("[+]"));
                getKaliServicesModels().getValue().clear();
                getKaliServicesModels().getValue().addAll(servicesModelList);
                getKaliServicesModels().postValue(getKaliServicesModels().getValue());
                if (mSwitch.isChecked())
                    NhPaths.showMessage(context, "Failed stopping " + getKaliServicesModels().getValue().get(position).getServiceName() + " service");
            }
        });
        servicesAsyncTask.execute(getInitCopyOfKaliServicesModelListFull());
    }

    public void editData(int position, ArrayList<String> dataArrayList, ServicesSQL servicesSQL) {
        ServicesAsyncTask servicesAsyncTask = new ServicesAsyncTask(ServicesAsyncTask.EDITDATA, position, dataArrayList, servicesSQL);
        servicesAsyncTask.setListener(new ServicesAsyncTask.KaliServicesAsyncTaskListener() {
            @Override
            public void onAsyncTaskPrepare() {

            }

            @Override
            public void onAsyncTaskFinished(List<ServicesModel> servicesModelList) {
                updateKaliServicesModelListFull(servicesModelList);
                getKaliServicesModels().getValue().clear();
                getKaliServicesModels().getValue().addAll(servicesModelList);
                getKaliServicesModels().postValue(getKaliServicesModels().getValue());
            }
        });
        servicesAsyncTask.execute(getInitCopyOfKaliServicesModelListFull());
    }

    public void addData(int position, ArrayList<String> dataArrayList, ServicesSQL servicesSQL) {
        ServicesAsyncTask servicesAsyncTask = new ServicesAsyncTask(ServicesAsyncTask.ADDDATA, position, dataArrayList, servicesSQL);
        servicesAsyncTask.setListener(new ServicesAsyncTask.KaliServicesAsyncTaskListener() {
            @Override
            public void onAsyncTaskPrepare() {

            }

            @Override
            public void onAsyncTaskFinished(List<ServicesModel> servicesModelList) {
                updateKaliServicesModelListFull(servicesModelList);
                getKaliServicesModels().getValue().clear();
                getKaliServicesModels().getValue().addAll(servicesModelList);
                getKaliServicesModels().postValue(getKaliServicesModels().getValue());
            }
        });
        servicesAsyncTask.execute(getInitCopyOfKaliServicesModelListFull());
    }

    public void deleteData(ArrayList<Integer> selectedPositionsIndex, ArrayList<Integer> selectedTargetIds, ServicesSQL servicesSQL) {
        ServicesAsyncTask servicesAsyncTask = new ServicesAsyncTask(ServicesAsyncTask.DELETEDATA, selectedPositionsIndex, selectedTargetIds, servicesSQL);
        servicesAsyncTask.setListener(new ServicesAsyncTask.KaliServicesAsyncTaskListener() {
            @Override
            public void onAsyncTaskPrepare() {

            }

            @Override
            public void onAsyncTaskFinished(List<ServicesModel> servicesModelList) {
                updateKaliServicesModelListFull(servicesModelList);
                getKaliServicesModels().getValue().clear();
                getKaliServicesModels().getValue().addAll(servicesModelList);
                getKaliServicesModels().postValue(getKaliServicesModels().getValue());
            }
        });
        servicesAsyncTask.execute(getInitCopyOfKaliServicesModelListFull());
    }

    public void moveData(int originalPositionIndex, int targetPositionIndex, ServicesSQL servicesSQL) {
        ServicesAsyncTask servicesAsyncTask = new ServicesAsyncTask(ServicesAsyncTask.MOVEDATA, originalPositionIndex, targetPositionIndex, servicesSQL);
        servicesAsyncTask.setListener(new ServicesAsyncTask.KaliServicesAsyncTaskListener() {
            @Override
            public void onAsyncTaskPrepare() {

            }

            @Override
            public void onAsyncTaskFinished(List<ServicesModel> servicesModelList) {
                updateKaliServicesModelListFull(servicesModelList);
                getKaliServicesModels().getValue().clear();
                getKaliServicesModels().getValue().addAll(servicesModelList);
                getKaliServicesModels().postValue(getKaliServicesModels().getValue());
            }
        });
        servicesAsyncTask.execute(getInitCopyOfKaliServicesModelListFull());
    }

    public String backupData(ServicesSQL servicesSQL, String storedDBpath) {
        return servicesSQL.backupData(storedDBpath);
    }

    public String restoreData(ServicesSQL servicesSQL, String storedDBpath) {
        String returnedResult = servicesSQL.restoreData(storedDBpath);
        if (returnedResult == null) {
            ServicesAsyncTask servicesAsyncTask = new ServicesAsyncTask(ServicesAsyncTask.RESTOREDATA, servicesSQL);
            servicesAsyncTask.setListener(new ServicesAsyncTask.KaliServicesAsyncTaskListener() {
                @Override
                public void onAsyncTaskPrepare() {

                }

                @Override
                public void onAsyncTaskFinished(List<ServicesModel> servicesModelList) {
                    updateKaliServicesModelListFull(servicesModelList);
                    getKaliServicesModels().getValue().clear();
                    getKaliServicesModels().getValue().addAll(servicesModelList);
                    getKaliServicesModels().postValue(getKaliServicesModels().getValue());
                    refreshData();
                }
            });
            servicesAsyncTask.execute(getInitCopyOfKaliServicesModelListFull());
            return null;
        } else {
            return returnedResult;
        }
    }

    public void resetData(ServicesSQL servicesSQL) {
        servicesSQL.resetData();
        ServicesAsyncTask servicesAsyncTask = new ServicesAsyncTask(ServicesAsyncTask.RESTOREDATA, servicesSQL);
        servicesAsyncTask.setListener(new ServicesAsyncTask.KaliServicesAsyncTaskListener() {
            @Override
            public void onAsyncTaskPrepare() {

            }

            @Override
            public void onAsyncTaskFinished(List<ServicesModel> servicesModelList) {
                updateKaliServicesModelListFull(servicesModelList);
                getKaliServicesModels().getValue().clear();
                getKaliServicesModels().getValue().addAll(servicesModelList);
                getKaliServicesModels().postValue(getKaliServicesModels().getValue());
                refreshData();
            }
        });
        servicesAsyncTask.execute(getInitCopyOfKaliServicesModelListFull());
    }

    public void updateRunOnChrootStartServices(int position, ArrayList<String> dataArrayList, ServicesSQL servicesSQL) {
        ServicesAsyncTask servicesAsyncTask = new ServicesAsyncTask(ServicesAsyncTask.UPDATE_RUNONCHROOTSTART_SCRIPTS, position, dataArrayList, servicesSQL);
        servicesAsyncTask.setListener(new ServicesAsyncTask.KaliServicesAsyncTaskListener() {
            @Override
            public void onAsyncTaskPrepare() {

            }

            @Override
            public void onAsyncTaskFinished(List<ServicesModel> servicesModelList) {
                updateKaliServicesModelListFull(servicesModelList);
                getKaliServicesModels().getValue().clear();
                getKaliServicesModels().getValue().addAll(servicesModelList);
                getKaliServicesModels().postValue(getKaliServicesModels().getValue());
            }
        });
        servicesAsyncTask.execute(getInitCopyOfKaliServicesModelListFull());
    }

    public void updateKaliServicesModelListFull(List<ServicesModel> copyOfServicesModelList) {
        servicesModelListFull.clear();
        servicesModelListFull.addAll(copyOfServicesModelList);
    }

    private List<ServicesModel> getInitCopyOfKaliServicesModelListFull() {
        copyOfServicesModelListFull.clear();
        copyOfServicesModelListFull.addAll(servicesModelListFull);
        return copyOfServicesModelListFull;
    }

}
