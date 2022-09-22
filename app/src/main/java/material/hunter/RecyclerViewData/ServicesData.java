package material.hunter.RecyclerViewData;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;

import com.google.android.material.switchmaterial.SwitchMaterial;

import material.hunter.SQL.ServicesSQL;
import material.hunter.Services;
import material.hunter.models.ServicesModel;
import material.hunter.utils.PathsUtil;
import material.hunter.utils.ShellExecuter;

import melville37.MelvilleExecutor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServicesData {

    public static boolean isDataInitiated = false;
    private static ServicesData instance;
    private final ArrayList<ServicesModel> servicesModelArrayList = new ArrayList<>();
    private final MutableLiveData<List<ServicesModel>> data = new MutableLiveData<>();
    private final List<ServicesModel> copyOfServicesModelListFull = new ArrayList<>();
    public List<ServicesModel> servicesModelListFull;

    public static synchronized ServicesData getInstance() {
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
        List<ServicesModel> model = getInitCopyOfServicesModelListFull();

        new MelvilleExecutor() {

            @Override
            public void onPreExecute() {}

            @Override
            public void doInBackground() {
                if (model != null) {
                    for (int i = 0; i < model.size(); i++) {
                        model.get(i)
                                .setStatus(
                                        new ShellExecuter()
                                                                .RunAsRootReturnValue(
                                                                        PathsUtil.BUSYBOX
                                                                                + " ps | grep -v"
                                                                                + " grep | grep '"
                                                                                + model.get(i)
                                                                                        .getCommandforCheckServiceStatus()
                                                                                + "'")
                                                        == 0
                                                ? "[+] Service is running"
                                                : "[-] Service isn't running");
                    }
                }
            }

            @Override
            public void onPostExecute() {
                getServicesModels().getValue().clear();
                getServicesModels().getValue().addAll(model);
                getServicesModels().postValue(getServicesModels().getValue());
            }
        }.run();
    }

    public void startServiceforItem(int position, SwitchMaterial mSwitch, Context context) {
        List<ServicesModel> model = getInitCopyOfServicesModelListFull();

        new MelvilleExecutor() {

            @Override
            public void onPreExecute() {
                mSwitch.setEnabled(false);
            }

            @Override
            public void doInBackground() {
                if (model != null) {
                    model.get(position)
                            .setStatus(
                                    new ShellExecuter()
                                                            .RunAsChrootReturnValue(
                                                                    model.get(position)
                                                                            .getCommandforStartService())
                                                    == 0
                                            ? "[+] Service is running"
                                            : "[-] Service isn't running");
                }
            }

            @Override
            public void onPostExecute() {
                mSwitch.setEnabled(true);
                mSwitch.setChecked(model.get(position).getStatus().startsWith("[+]"));
                getServicesModels().getValue().clear();
                getServicesModels().getValue().addAll(model);
                getServicesModels().postValue(getServicesModels().getValue());
                if (!mSwitch.isChecked())
                    PathsUtil.showSnack(
                            Services._view,
                            "Failed starting "
                                    + getServicesModels().getValue().get(position).getServiceName()
                                    + " service.",
                            false);
            }
        }.run();
    }

    public void stopServiceforItem(int position, SwitchMaterial mSwitch, Context context) {
        List<ServicesModel> model = getInitCopyOfServicesModelListFull();

        new MelvilleExecutor() {

            @Override
            public void onPreExecute() {
                mSwitch.setEnabled(false);
            }

            @Override
            public void doInBackground() {
                if (model != null) {
                    model.get(position)
                            .setStatus(
                                    new ShellExecuter()
                                                            .RunAsChrootReturnValue(
                                                                    model.get(position)
                                                                            .getCommandforStopService())
                                                    == 0
                                            ? "[-] Service isn't running"
                                            : "[+] Service is running");
                }
            }

            @Override
            public void onPostExecute() {
                mSwitch.setEnabled(true);
                mSwitch.setChecked(model.get(position).getStatus().startsWith("[+]"));
                getServicesModels().getValue().clear();
                getServicesModels().getValue().addAll(model);
                getServicesModels().postValue(getServicesModels().getValue());
                if (mSwitch.isChecked())
                    PathsUtil.showSnack(
                            Services._view,
                            "Failed stopping "
                                    + getServicesModels().getValue().get(position).getServiceName()
                                    + " service.",
                            false);
            }
        }.run();
    }

    public void editData(int position, ArrayList<String> dataArrayList, ServicesSQL servicesSQL) {
        List<ServicesModel> model = getInitCopyOfServicesModelListFull();

        new MelvilleExecutor() {

            @Override
            public void onPreExecute() {}

            @Override
            public void doInBackground() {
                if (model != null) {
                    model.get(position).setServiceName(dataArrayList.get(0));
                    model.get(position).setCommandforStartService(dataArrayList.get(1));
                    model.get(position).setCommandforStopService(dataArrayList.get(2));
                    model.get(position).setCommandforCheckServiceStatus(dataArrayList.get(3));
                    model.get(position).setRunOnChrootStart(dataArrayList.get(4));
                    updateRunOnChrootStartScripts(model);
                    servicesSQL.editData(position, dataArrayList);
                }
            }

            @Override
            public void onPostExecute() {
                updateServicesModelListFull(model);
                getServicesModels().getValue().clear();
                getServicesModels().getValue().addAll(model);
                getServicesModels().postValue(getServicesModels().getValue());
            }
        }.run();
    }

    public void addData(int position, ArrayList<String> dataArrayList, ServicesSQL servicesSQL) {
        List<ServicesModel> model = getInitCopyOfServicesModelListFull();

        new MelvilleExecutor() {

            @Override
            public void onPreExecute() {}

            @Override
            public void doInBackground() {
                if (model != null) {
                    model.add(
                            position - 1,
                            new ServicesModel(
                                    dataArrayList.get(0),
                                    dataArrayList.get(1),
                                    dataArrayList.get(2),
                                    dataArrayList.get(3),
                                    dataArrayList.get(4),
                                    ""));
                    if (dataArrayList.get(4).equals("1")) {
                        updateRunOnChrootStartScripts(model);
                    }
                    servicesSQL.addData(position, dataArrayList);
                }
            }

            @Override
            public void onPostExecute() {
                updateServicesModelListFull(model);
                getServicesModels().getValue().clear();
                getServicesModels().getValue().addAll(model);
                getServicesModels().postValue(getServicesModels().getValue());
            }
        }.run();
    }

    public void deleteData(
            ArrayList<Integer> selectedPositionsIndex,
            ArrayList<Integer> selectedTargetIds,
            ServicesSQL servicesSQL) {
        List<ServicesModel> model = getInitCopyOfServicesModelListFull();

        new MelvilleExecutor() {

            @Override
            public void onPreExecute() {}

            @Override
            public void doInBackground() {
                if (model != null) {
                    Collections.sort(selectedPositionsIndex, Collections.reverseOrder());
                    for (Integer selectedPosition : selectedPositionsIndex) {
                        int i = selectedPosition;
                        model.remove(i);
                    }
                    servicesSQL.deleteData(selectedTargetIds);
                }
            }

            @Override
            public void onPostExecute() {
                updateServicesModelListFull(model);
                getServicesModels().getValue().clear();
                getServicesModels().getValue().addAll(model);
                getServicesModels().postValue(getServicesModels().getValue());
            }
        }.run();
    }

    public void moveData(
            final int originalPositionIndex, final int targetPositionIndex, ServicesSQL servicesSQL) {
        List<ServicesModel> model = getInitCopyOfServicesModelListFull();

        new MelvilleExecutor() {

            @Override
            public void onPreExecute() {}

            @Override
            public void doInBackground() {
                if (model != null) {
                    int mTargetPositionIndex = targetPositionIndex;
                    ServicesModel tempServicesModel =
                            new ServicesModel(
                                    model.get(originalPositionIndex).getServiceName(),
                                    model.get(originalPositionIndex).getCommandforStartService(),
                                    model.get(originalPositionIndex).getCommandforStopService(),
                                    model.get(originalPositionIndex)
                                            .getCommandforCheckServiceStatus(),
                                    model.get(originalPositionIndex).getRunOnChrootStart(),
                                    model.get(originalPositionIndex).getStatus());
                    model.remove(originalPositionIndex);
                    if (originalPositionIndex < targetPositionIndex) {
                        mTargetPositionIndex = targetPositionIndex - 1;
                    }
                    model.add(mTargetPositionIndex, tempServicesModel);
                    servicesSQL.moveData(originalPositionIndex, targetPositionIndex);
                }
            }

            @Override
            public void onPostExecute() {
                updateServicesModelListFull(model);
                getServicesModels().getValue().clear();
                getServicesModels().getValue().addAll(model);
                getServicesModels().postValue(getServicesModels().getValue());
            }
        }.run();
    }

    public String backupData(ServicesSQL servicesSQL, String storedDBpath) {
        return servicesSQL.backupData(storedDBpath);
    }

    public String restoreData(ServicesSQL servicesSQL, String storedDBpath) {
        String returnedResult = servicesSQL.restoreData(storedDBpath);

        if (returnedResult == null) {
            new MelvilleExecutor() {

                List<ServicesModel> model = getInitCopyOfServicesModelListFull();

                @Override
                public void onPreExecute() {}

                @Override
                public void doInBackground() {
                    if (model != null) {
                        model.clear();
                        servicesSQL.bindData((ArrayList<ServicesModel>) model);
                    }
                }

                @Override
                public void onPostExecute() {
                    updateServicesModelListFull(model);
                    getServicesModels().getValue().clear();
                    getServicesModels().getValue().addAll(model);
                    getServicesModels().postValue(getServicesModels().getValue());
                    refreshData();
                }
            }.run();
            return null;
        } else {
            return returnedResult;
        }
    }

    public void resetData(ServicesSQL servicesSQL) {
        new MelvilleExecutor() {

            List<ServicesModel> model = getInitCopyOfServicesModelListFull();

            @Override
            public void onPreExecute() {
                servicesSQL.resetData();
            }

            @Override
            public void doInBackground() {
                if (model != null) {
                    model.clear();
                    model = servicesSQL.bindData((ArrayList<ServicesModel>) model);
                }
            }

            @Override
            public void onPostExecute() {
                updateServicesModelListFull(model);
                getServicesModels().getValue().clear();
                getServicesModels().getValue().addAll(model);
                getServicesModels().postValue(getServicesModels().getValue());
                refreshData();
            }
        }.run();
    }

    public void updateRunOnChrootStartServices(
            int position, ArrayList<String> dataArrayList, ServicesSQL servicesSQL) {
        List<ServicesModel> model = getInitCopyOfServicesModelListFull();

        new MelvilleExecutor() {

            @Override
            public void onPreExecute() {}

            @Override
            public void doInBackground() {
                if (model != null) {
                    model.get(position).setServiceName(dataArrayList.get(0));
                    model.get(position).setCommandforStartService(dataArrayList.get(1));
                    model.get(position).setCommandforStopService(dataArrayList.get(2));
                    model.get(position).setCommandforCheckServiceStatus(dataArrayList.get(3));
                    model.get(position).setRunOnChrootStart(dataArrayList.get(4));
                    servicesSQL.editData(position, dataArrayList);
                    updateRunOnChrootStartScripts(model);
                }
            }

            @Override
            public void onPostExecute() {
                updateServicesModelListFull(model);
                getServicesModels().getValue().clear();
                getServicesModels().getValue().addAll(model);
                getServicesModels().postValue(getServicesModels().getValue());
            }
        }.run();
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

    private void updateRunOnChrootStartScripts(List<ServicesModel> servicesModelList) {
        StringBuilder tmpStringBuilder = new StringBuilder();
        for (int i = 0; i < servicesModelList.size(); i++) {
            if (servicesModelList.get(i).getRunOnChrootStart().equals("1")) {
                tmpStringBuilder
                        .append(servicesModelList.get(i).getCommandforStartService())
                        .append("\n");
            }
        }
        new ShellExecuter()
                .RunAsRootOutput(
                        "cat << 'EOF' > "
                                + PathsUtil.APP_SCRIPTS_PATH
                                + "/services"
                                + "\n"
                                + tmpStringBuilder.toString()
                                + "\nEOF");
    }
}