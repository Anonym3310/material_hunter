package material.hunter.viewmodels;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import material.hunter.RecyclerViewData.MaterialHunterData;
import material.hunter.models.MaterialHunterModel;

import java.util.List;

/*
    ViewModel class for materialhunter model, this is to be observed the List of MaterialHunterModel class.
    This class should be initiated every time the MaterialHunterFragment is created.
    After the MaterialHunterData singleton is created, it will live until the app dies.
 */
public class MaterialHunterViewModel extends ViewModel {
    private MutableLiveData<List<MaterialHunterModel>> mutableLiveDataMaterialHunterModelList;

    public void init(Context context) {
        if (mutableLiveDataMaterialHunterModelList != null) {
            return;
        }
        MaterialHunterData materialhunterData = MaterialHunterData.getInstance();
        if (MaterialHunterData.isDataInitiated) {
            mutableLiveDataMaterialHunterModelList = materialhunterData.getMaterialHunterModels();
        } else {
            mutableLiveDataMaterialHunterModelList = materialhunterData.getMaterialHunterModels(context);
        }
    }

    public LiveData<List<MaterialHunterModel>> getLiveDataMaterialHunterModelList() {
        return mutableLiveDataMaterialHunterModelList;
    }
}
