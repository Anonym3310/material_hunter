package material.hunter.AsyncTask;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import material.hunter.ChrootManagerFragment;
import material.hunter.SQL.ServicesSQL;
import material.hunter.models.ServicesModel;
import material.hunter.utils.NhPaths;
import material.hunter.utils.ShellExecuter;

public class ServicesAsyncTask extends AsyncTask<List<ServicesModel>, Void, List<ServicesModel>> {
    public static final int GETITEMSTATUS = 0;
    public static final int START_SERVICE_FOR_ITEM = 1;
    public static final int STOP_SERVICE_FOR_ITEM = 2;
    public static final int EDITDATA = 3;
    public static final int ADDDATA = 4;
    public static final int DELETEDATA = 5;
    public static final int MOVEDATA = 6;
    public static final int BACKUPDATA = 7;
    public static final int RESTOREDATA = 8;
    public static final int RESETDATA = 9;
    public static final int UPDATE_RUNONCHROOTSTART_SCRIPTS = 10;
    private ServicesAsyncTaskListener listener;
    private int actionCode;
    private int position;
    private int originalPositionIndex;
    private int targetPositionIndex;
    private ArrayList<Integer> selectedPositionsIndex;
    private ArrayList<Integer> selectedTargetIds;
    private ArrayList<String> dataArrayList;
    private ServicesSQL servicesSQL;

    public ServicesAsyncTask(int actionCode) {
        this.actionCode = actionCode;
    }

    public ServicesAsyncTask(int actionCode, int position) {
        this.actionCode = actionCode;
        this.position = position;
    }

    public ServicesAsyncTask(int actionCode, int position, ArrayList<String> dataArrayList, ServicesSQL servicesSQL) {
        this.actionCode = actionCode;
        this.position = position;
        this.dataArrayList = dataArrayList;
        this.servicesSQL = servicesSQL;
    }

    public ServicesAsyncTask(int actionCode, ArrayList<Integer> selectedPositionsIndex, ArrayList<Integer> selectedTargetIds, ServicesSQL servicesSQL) {
        this.actionCode = actionCode;
        this.selectedPositionsIndex = selectedPositionsIndex;
        this.selectedTargetIds = selectedTargetIds;
        this.servicesSQL = servicesSQL;
    }

    public ServicesAsyncTask(int actionCode, int originalPositionIndex, int targetPositionIndex, ServicesSQL servicesSQL) {
        this.actionCode = actionCode;
        this.originalPositionIndex = originalPositionIndex;
        this.targetPositionIndex = targetPositionIndex;
        this.servicesSQL = servicesSQL;
    }

    public ServicesAsyncTask(int actionCode, ServicesSQL servicesSQL) {
        this.actionCode = actionCode;
        this.servicesSQL = servicesSQL;
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
    protected List<ServicesModel> doInBackground(List<ServicesModel>... copyOfServicesModelList) {
        List<ServicesModel> servicesModelList;
        switch (actionCode) {
            case GETITEMSTATUS:
                servicesModelList = copyOfServicesModelList[0];
                if (servicesModelList != null) {
                    for (int i = 0; i < servicesModelList.size(); i++) {
                        servicesModelList.get(i).setStatus(new ShellExecuter().RunAsRootReturnValue(NhPaths.BUSYBOX + " ps | grep -v grep | grep '" + servicesModelList.get(i).getCommandforCheckServiceStatus() + "'") == 0 ? "[+] Service is running" : "[-] Service is NOT running");
                    }
                }
                break;
            case START_SERVICE_FOR_ITEM:
                servicesModelList = copyOfServicesModelList[0];
                if (servicesModelList != null) {
                    servicesModelList.get(position).setStatus(new ShellExecuter().RunAsChrootReturnValue(servicesModelList.get(position).getCommandforStartService()) == 0 ? "[+] Service is running" : "[-] Service is NOT running");
                }
                break;
            case STOP_SERVICE_FOR_ITEM:
                servicesModelList = copyOfServicesModelList[0];
                if (servicesModelList != null) {
                    servicesModelList.get(position).setStatus(new ShellExecuter().RunAsChrootReturnValue(servicesModelList.get(position).getCommandforStopService()) == 0 ? "[-] Service is NOT running" : "[+] Service is running");
                }
                break;
            case EDITDATA:
                servicesModelList = copyOfServicesModelList[0];
                if (servicesModelList != null) {
                    servicesModelList.get(position).setServiceName(dataArrayList.get(0));
                    servicesModelList.get(position).setCommandforStartService(dataArrayList.get(1));
                    servicesModelList.get(position).setCommandforStopService(dataArrayList.get(2));
                    servicesModelList.get(position).setCommandforCheckServiceStatus(dataArrayList.get(3));
                    servicesModelList.get(position).setRunOnChrootStart(dataArrayList.get(4));
                    updateRunOnChrootStartScripts(servicesModelList);
                    servicesSQL.editData(position, dataArrayList);
                }
                break;
            case ADDDATA:
                servicesModelList = copyOfServicesModelList[0];
                if (servicesModelList != null) {

                    servicesModelList.add(position - 1, new ServicesModel(
                            dataArrayList.get(0),
                            dataArrayList.get(1),
                            dataArrayList.get(2),
                            dataArrayList.get(3),
                            dataArrayList.get(4),
                            ""));
                    if (dataArrayList.get(4).equals("1")) {
                        updateRunOnChrootStartScripts(servicesModelList);
                    }
                    servicesSQL.addData(position, dataArrayList);
                }
                break;
            case DELETEDATA:
                servicesModelList = copyOfServicesModelList[0];
                if (servicesModelList != null) {
                    Collections.sort(selectedPositionsIndex, Collections.reverseOrder());
                    for (Integer selectedPosition : selectedPositionsIndex) {
                        int i = selectedPosition;
                        servicesModelList.remove(i);
                    }
                    servicesSQL.deleteData(selectedTargetIds);
                }
                break;
            case MOVEDATA:
                servicesModelList = copyOfServicesModelList[0];
                if (servicesModelList != null) {
                    ServicesModel tempServicesModel = new ServicesModel(
                            servicesModelList.get(originalPositionIndex).getServiceName(),
                            servicesModelList.get(originalPositionIndex).getCommandforStartService(),
                            servicesModelList.get(originalPositionIndex).getCommandforStopService(),
                            servicesModelList.get(originalPositionIndex).getCommandforCheckServiceStatus(),
                            servicesModelList.get(originalPositionIndex).getRunOnChrootStart(),
                            servicesModelList.get(originalPositionIndex).getStatus()
                    );
                    servicesModelList.remove(originalPositionIndex);
                    if (originalPositionIndex < targetPositionIndex) {
                        targetPositionIndex = targetPositionIndex - 1;
                    }
                    servicesModelList.add(targetPositionIndex, tempServicesModel);
                    servicesSQL.moveData(originalPositionIndex, targetPositionIndex);
                }
                break;
            case BACKUPDATA:
                break;
            case RESTOREDATA:
                servicesModelList = copyOfServicesModelList[0];
                if (servicesModelList != null) {
                    servicesModelList.clear();
                    servicesModelList = servicesSQL.bindData((ArrayList<ServicesModel>) servicesModelList);
                }
                break;
            case RESETDATA:
                break;
            case UPDATE_RUNONCHROOTSTART_SCRIPTS:
                servicesModelList = copyOfServicesModelList[0];
                if (servicesModelList != null) {
                    servicesModelList.get(position).setServiceName(dataArrayList.get(0));
                    servicesModelList.get(position).setCommandforStartService(dataArrayList.get(1));
                    servicesModelList.get(position).setCommandforStopService(dataArrayList.get(2));
                    servicesModelList.get(position).setCommandforCheckServiceStatus(dataArrayList.get(3));
                    servicesModelList.get(position).setRunOnChrootStart(dataArrayList.get(4));
                    servicesSQL.editData(position, dataArrayList);
                    updateRunOnChrootStartScripts(servicesModelList);
                }
                break;
        }
        return copyOfServicesModelList[0];
    }

    @Override
    protected void onPostExecute(List<ServicesModel> servicesModelList) {
        super.onPostExecute(servicesModelList);
        if (listener != null) {
            listener.onAsyncTaskFinished(servicesModelList);
        }
        ChrootManagerFragment.isAsyncTaskRunning = false;
    }

    public void setListener(ServicesAsyncTaskListener listener) {
        this.listener = listener;
    }

    private void updateRunOnChrootStartScripts(List<ServicesModel> servicesModelList) {
        StringBuilder tmpStringBuilder = new StringBuilder();
        for (int i = 0; i < servicesModelList.size(); i++) {
            if (servicesModelList.get(i).getRunOnChrootStart().equals("1")) {
                tmpStringBuilder.append(servicesModelList.get(i).getCommandforStartService()).append("\n");
            }
        }
        new ShellExecuter().RunAsRootOutput("cat << 'EOF' > " + NhPaths.APP_SCRIPTS_PATH + "/services" + "\n" + tmpStringBuilder.toString() + "\nEOF");
    }

    public interface ServicesAsyncTaskListener {
        void onAsyncTaskPrepare();

        void onAsyncTaskFinished(List<ServicesModel> servicesModelList);
    }
}
