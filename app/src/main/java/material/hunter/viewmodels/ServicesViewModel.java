package material.hunter.viewmodels;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import material.hunter.RecyclerViewData.ServicesData;
import material.hunter.models.ServicesModel;

/*
    ViewModel class for services model, this is to be observed the List of ServicesModel class.
    This class should be initiated every time the ServicesFragment is created.
    After the ServicesData singleton is created, it will live until the app dies.
 */
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
