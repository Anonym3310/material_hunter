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

    public MutableLiveData<List<ServicesModel>> getServicesModels(Context context) {
        if (!isDataInitiated) {
            data.setValue(ServicesSQL.getInstance(context).bindData(servicesModelArrayList));
            servicesModelListFull = new ArrayList<>(data.getValue());
            isDataInitiated = true;
        }
        return data;
    }

    public MutableLiveData<List<ServicesModel>> getServicesModels() {
        return data;
    }

    public void refreshData() {
        ServicesAsyncTask servicesAsyncTask = new ServicesAsyncTask(ServicesAsyncTask.GETITEMSTATUS);
        servicesAsyncTask.setListener(new ServicesAsyncTask.ServicesAsyncTaskListener() {
            @Override
            public void onAsyncTaskPrepare() {

            }

            @Override
            public void onAsyncTaskFinished(List<ServicesModel> servicesModelList) {
                getServicesModels().getValue().clear();
                getServicesModels().getValue().addAll(servicesModelList);
                getServicesModels().postValue(getServicesModels().getValue());
            }
        });
        servicesAsyncTask.execute(getInitCopyOfServicesModelListFull());
    }

    public void startServiceforItem(int position, Switch mSwitch, Context context) {
        ServicesAsyncTask servicesAsyncTask = new ServicesAsyncTask(ServicesAsyncTask.START_SERVICE_FOR_ITEM, position);
        servicesAsyncTask.setListener(new ServicesAsyncTask.ServicesAsyncTaskListener() {
            @Override
            public void onAsyncTaskPrepare() {
                mSwitch.setEnabled(false);
            }

            @Override
            public void onAsyncTaskFinished(List<ServicesModel> servicesModelList) {
                mSwitch.setEnabled(true);
                mSwitch.setChecked(servicesModelList.get(position).getStatus().startsWith("[+]"));
                getServicesModels().getValue().clear();
                getServicesModels().getValue().addAll(servicesModelList);
                getServicesModels().postValue(getServicesModels().getValue());
                if (!mSwitch.isChecked())
                    NhPaths.showMessage(context, "Failed starting " + getServicesModels().getValue().get(position).getServiceName() + " service");
            }
        });
        servicesAsyncTask.execute(getInitCopyOfServicesModelListFull());
    }

    public void stopServiceforItem(int position, Switch mSwitch, Context context) {
        ServicesAsyncTask servicesAsyncTask = new ServicesAsyncTask(ServicesAsyncTask.STOP_SERVICE_FOR_ITEM, position);
        servicesAsyncTask.setListener(new ServicesAsyncTask.ServicesAsyncTaskListener() {
            @Override
            public void onAsyncTaskPrepare() {
                mSwitch.setEnabled(false);
            }

            @Override
            public void onAsyncTaskFinished(List<ServicesModel> servicesModelList) {
                mSwitch.setEnabled(true);
                mSwitch.setChecked(servicesModelList.get(position).getStatus().startsWith("[+]"));
                getServicesModels().getValue().clear();
                getServicesModels().getValue().addAll(servicesModelList);
                getServicesModels().postValue(getServicesModels().getValue());
                if (mSwitch.isChecked())
                    NhPaths.showMessage(context, "Failed stopping " + getServicesModels().getValue().get(position).getServiceName() + " service");
            }
        });
        servicesAsyncTask.execute(getInitCopyOfServicesModelListFull());
    }

    public void editData(int position, ArrayList<String> dataArrayList, ServicesSQL servicesSQL) {
        ServicesAsyncTask servicesAsyncTask = new ServicesAsyncTask(ServicesAsyncTask.EDITDATA, position, dataArrayList, servicesSQL);
        servicesAsyncTask.setListener(new ServicesAsyncTask.ServicesAsyncTaskListener() {
            @Override
            public void onAsyncTaskPrepare() {

            }

            @Override
            public void onAsyncTaskFinished(List<ServicesModel> servicesModelList) {
                updateServicesModelListFull(servicesModelList);
                getServicesModels().getValue().clear();
                getServicesModels().getValue().addAll(servicesModelList);
                getServicesModels().postValue(getServicesModels().getValue());
            }
        });
        servicesAsyncTask.execute(getInitCopyOfServicesModelListFull());
    }

    public void addData(int position, ArrayList<String> dataArrayList, ServicesSQL servicesSQL) {
        ServicesAsyncTask servicesAsyncTask = new ServicesAsyncTask(ServicesAsyncTask.ADDDATA, position, dataArrayList, servicesSQL);
        servicesAsyncTask.setListener(new ServicesAsyncTask.ServicesAsyncTaskListener() {
            @Override
            public void onAsyncTaskPrepare() {

            }

            @Override
            public void onAsyncTaskFinished(List<ServicesModel> servicesModelList) {
                updateServicesModelListFull(servicesModelList);
                getServicesModels().getValue().clear();
                getServicesModels().getValue().addAll(servicesModelList);
                getServicesModels().postValue(getServicesModels().getValue());
            }
        });
        servicesAsyncTask.execute(getInitCopyOfServicesModelListFull());
    }

    public void deleteData(ArrayList<Integer> selectedPositionsIndex, ArrayList<Integer> selectedTargetIds, ServicesSQL servicesSQL) {
        ServicesAsyncTask servicesAsyncTask = new ServicesAsyncTask(ServicesAsyncTask.DELETEDATA, selectedPositionsIndex, selectedTargetIds, servicesSQL);
        servicesAsyncTask.setListener(new ServicesAsyncTask.ServicesAsyncTaskListener() {
            @Override
            public void onAsyncTaskPrepare() {

            }

            @Override
            public void onAsyncTaskFinished(List<ServicesModel> servicesModelList) {
                updateServicesModelListFull(servicesModelList);
                getServicesModels().getValue().clear();
                getServicesModels().getValue().addAll(servicesModelList);
                getServicesModels().postValue(getServicesModels().getValue());
            }
        });
        servicesAsyncTask.execute(getInitCopyOfServicesModelListFull());
    }

    public void moveData(int originalPositionIndex, int targetPositionIndex, ServicesSQL servicesSQL) {
        ServicesAsyncTask servicesAsyncTask = new ServicesAsyncTask(ServicesAsyncTask.MOVEDATA, originalPositionIndex, targetPositionIndex, servicesSQL);
        servicesAsyncTask.setListener(new ServicesAsyncTask.ServicesAsyncTaskListener() {
            @Override
            public void onAsyncTaskPrepare() {

            }

            @Override
            public void onAsyncTaskFinished(List<ServicesModel> servicesModelList) {
                updateServicesModelListFull(servicesModelList);
                getServicesModels().getValue().clear();
                getServicesModels().getValue().addAll(servicesModelList);
                getServicesModels().postValue(getServicesModels().getValue());
            }
        });
        servicesAsyncTask.execute(getInitCopyOfServicesModelListFull());
    }

    public String backupData(ServicesSQL servicesSQL, String storedDBpath) {
        return servicesSQL.backupData(storedDBpath);
    }

    public String restoreData(ServicesSQL servicesSQL, String storedDBpath) {
        String returnedResult = servicesSQL.restoreData(storedDBpath);
        if (returnedResult == null) {
            ServicesAsyncTask servicesAsyncTask = new ServicesAsyncTask(ServicesAsyncTask.RESTOREDATA, servicesSQL);
            servicesAsyncTask.setListener(new ServicesAsyncTask.ServicesAsyncTaskListener() {
                @Override
                public void onAsyncTaskPrepare() {

                }

                @Override
                public void onAsyncTaskFinished(List<ServicesModel> servicesModelList) {
                    updateServicesModelListFull(servicesModelList);
                    getServicesModels().getValue().clear();
                    getServicesModels().getValue().addAll(servicesModelList);
                    getServicesModels().postValue(getServicesModels().getValue());
                    refreshData();
                }
            });
            servicesAsyncTask.execute(getInitCopyOfServicesModelListFull());
            return null;
        } else {
            return returnedResult;
        }
    }

    public void resetData(ServicesSQL servicesSQL) {
        servicesSQL.resetData();
        ServicesAsyncTask servicesAsyncTask = new ServicesAsyncTask(ServicesAsyncTask.RESTOREDATA, servicesSQL);
        servicesAsyncTask.setListener(new ServicesAsyncTask.ServicesAsyncTaskListener() {
            @Override
            public void onAsyncTaskPrepare() {

            }

            @Override
            public void onAsyncTaskFinished(List<ServicesModel> servicesModelList) {
                updateServicesModelListFull(servicesModelList);
                getServicesModels().getValue().clear();
                getServicesModels().getValue().addAll(servicesModelList);
                getServicesModels().postValue(getServicesModels().getValue());
                refreshData();
            }
        });
        servicesAsyncTask.execute(getInitCopyOfServicesModelListFull());
    }

    public void updateRunOnChrootStartServices(int position, ArrayList<String> dataArrayList, ServicesSQL servicesSQL) {
        ServicesAsyncTask servicesAsyncTask = new ServicesAsyncTask(ServicesAsyncTask.UPDATE_RUNONCHROOTSTART_SCRIPTS, position, dataArrayList, servicesSQL);
        servicesAsyncTask.setListener(new ServicesAsyncTask.ServicesAsyncTaskListener() {
            @Override
            public void onAsyncTaskPrepare() {

            }

            @Override
            public void onAsyncTaskFinished(List<ServicesModel> servicesModelList) {
                updateServicesModelListFull(servicesModelList);
                getServicesModels().getValue().clear();
                getServicesModels().getValue().addAll(servicesModelList);
                getServicesModels().postValue(getServicesModels().getValue());
            }
        });
        servicesAsyncTask.execute(getInitCopyOfServicesModelListFull());
    }

    public void updateServicesModelListFull(List<ServicesModel> copyOfServicesModelList) {
        servicesModelListFull.clear();
        servicesModelListFull.addAll(copyOfServicesModelList);
    }

    private List<ServicesModel> getInitCopyOfServicesModelListFull() {
        copyOfServicesModelListFull.clear();
        copyOfServicesModelListFull.addAll(servicesModelListFull);
        return copyOfServicesModelListFull;
    }

}
