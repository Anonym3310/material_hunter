package melville37.contract;

import java.io.IOException;

import java.net.MalformedURLException;

import java.util.ArrayList;
import java.util.Iterator;

import melville37.contract.Web;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSON {

    public static JSONObject getFromWeb(String link) throws JSONException, MalformedURLException, IOException {
        JSONObject result = null;
        String content = Web.getContent(link);
        if (content != null) {
            result = new JSONObject(content);       
        }
        return result;
    }

    public static ArrayList<String> getKeys(JSONObject json) {
        ArrayList<String> result = new ArrayList<String>();
        Iterator<String> keys = json.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            result.add(key);
        }
        return result;
    }
}