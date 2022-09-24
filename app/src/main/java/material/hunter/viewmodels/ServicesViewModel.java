package material.hunter.viewmodels;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import material.hunter.RecyclerViewData.ServicesData;
import material.hunter.models.ServicesModel;

import java.util.List;

public class ServicesViewModel extends ViewModel {

    private MutableLiveData<List<ServicesModel>> mutableLiveDataServicesModelList;

    public void init(Context context) {
        if (mutableLiveDataServicesModelList != null) {
            return;
        }
        ServicesData servicesData = ServicesData.getInstance();
        if (ServicesData.isDataInitiated) {
            mutableLiveDataServicesModelList = servicesData.getServicesModels();
        } else {
            mutableLiveDataServicesModelList = servicesData.getServicesModels(context);
        }
    }

    public LiveData<List<ServicesModel>> getLiveDataServicesModelList() {
        return mutableLiveDataServicesModelList;
    }
}