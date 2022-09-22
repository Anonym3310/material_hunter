package material.hunter.utils;

import melville37.contract.JSON;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MHRepo {

    private static JSONObject mRepo = null;

    public static JSONObject getRepo() {
        return mRepo;
    }

    public static boolean setRepo(JSONObject repo) {
        if (isValid(repo)) {
            mRepo = repo;
            return true;
        } else {
            return false;
        }
    }

    public static boolean isValid(JSONObject repo) {
        ArrayList<String> keys = JSON.getKeys(repo);
        for (int i = 0; i < keys.size(); i++) {
            try {
                String obj = repo.getString(keys.get(i));
                JSONObject chroot = new JSONObject(obj);
                if (chroot.has("name") && (chroot.has("url") || chroot.has("file")) && chroot.has("author"))
                    continue;
                else
                    return false;
            } catch (JSONException e) {
                return false;
            }
        }
        return true;
    }

    public static ArrayList<String> getMainKeys() {
        ArrayList<String> mainKeys = new ArrayList<String>();
        try {
            for (String key : JSON.getKeys(mRepo)) {
                String jsonData = mRepo.getString(key);
                JSONObject jsonObj = new JSONObject(jsonData);
                String name = jsonObj.getString("name");
                mainKeys.add(name);
            }
        } catch (JSONException e) {
        }
        return mainKeys;
    }

    public static String getKeyData(String key) throws JSONException {
        return mRepo.getString(key);
    }
}