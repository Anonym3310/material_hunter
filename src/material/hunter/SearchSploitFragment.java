package material.hunter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Objects;

import material.hunter.utils.NhPaths;
import material.hunter.utils.ShellExecuter;

public class SearchSploitFragment extends Fragment {
    private static final String ARG_SECTION_NUMBER = "section_number";
    private Boolean withFilters = true;
    private String sel_type;
    private String sel_platform;
    private String sel_search = "";
    private TextView numex;
    private AlertDialog adi;
    private Boolean isLoaded = false;
    private ListView searchSploitListView;
    private List<SearchSploit> full_exploitList;
    private SearchSploitSQL database;
    private Context context;
    private Activity activity;

    public static SearchSploitFragment newInstance(int sectionNumber) {
        SearchSploitFragment fragment = new SearchSploitFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    private static void hideSoftKeyboard(final View caller) {
        caller.postDelayed(() -> {
            InputMethodManager imm = (InputMethodManager) caller.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(caller.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }, 100);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
        activity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.searchsploit, container, false);
        setHasOptionsMenu(true);
        database = new SearchSploitSQL(context);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(getString(R.string.searchsploit_title));
        builder.setMessage(getString(R.string.searchsploit_loading));
        adi = builder.create();
        adi.setCancelable(false);
        adi.show();
        // Search Bar
        numex = rootView.findViewById(R.id.numex);
        // Load/reload database button
        final Button searchSearchSploit = rootView.findViewById(R.id.serchsploit_loadDB);
        searchSearchSploit.setVisibility(View.GONE);
        searchSearchSploit.setOnClickListener(v -> {
            final ProgressDialog pd = new ProgressDialog(activity);
            pd.setTitle(getString(R.string.searchsploit_feeding));
            pd.setMessage(getString(R.string.searchsploit_wait));
            pd.setCancelable(false);
            pd.show();
            new Thread(() -> {
                final Boolean isFeeded = database.doDbFeed();
                searchSearchSploit.post(() -> {
                    if (isFeeded) {
                        try {
                            // Search List
                            String sd = NhPaths.SD_PATH;
                            String data = NhPaths.APP_PATH;
                            String DATABASE_NAME = "SearchSploit";
                            String currentDBPath = "../databases/" + DATABASE_NAME;
                            String backupDBPath = "/nh_files/" + DATABASE_NAME; // From SD directory.

                            File backupDB = new File(data, currentDBPath);
                            File currentDB = new File(sd, backupDBPath);

                            FileChannel src = new FileInputStream(currentDB).getChannel();
                            FileChannel dst = new FileOutputStream(backupDB).getChannel();
                            dst.transferFrom(src, 0, src.size());

                            src.close();
                            dst.close();
                            main(rootView);
                            pd.dismiss();
                        } catch (Exception e) {
                            NhPaths.showSnack(getView(), getString(R.string.searchsploit_feed_failed), 2);
                            pd.dismiss();
                        }
                    } else {
                        NhPaths.showSnack(getView(), getString(R.string.searchsploit_missing_csv), 2);
                    }
                });
            }).start();
        });
        new android.os.Handler().postDelayed(() -> main(rootView), 250);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.searchsploit, menu);
        final MenuItem searchItem = menu.findItem(R.id.searchsploit_search);
        final androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) searchItem.getActionView();
        searchView.setOnSearchClickListener(view -> menu.findItem(R.id.rawSearch_ON).setVisible(false));
        searchView.setOnCloseListener(() -> {
            menu.findItem(R.id.rawSearch_ON).setVisible(true);
            return false;
        });
        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.length() > 1) {
                    sel_search = query;
                } else {
                    sel_search = "";
                }
                loadExploits();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if (query.length() == 0) {
                    sel_search = "";
                    loadExploits();
                }
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == R.id.rawSearch_ON) {
            if (getView() == null) return true;
            if (!withFilters) {
                getView().findViewById(R.id.search_filters).setVisibility(View.VISIBLE);
                withFilters = true;
                item.setTitle(getString(R.string.searchsploit_enable_raw));
            } else {
                    /*AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setTitle("Warning!");
                    builder.setMessage("The exploit db is pretty big (+30K exploits), activating raw search will make the search slow.\nIs useful to do global searches when you don't find a exploit.")
                            .setNegativeButton(getString(R.string.cancel), (dialog, id) -> dialog.dismiss())
                            .setPositiveButton("Enable", (dialog, id) -> {*/
                getView().findViewById(R.id.search_filters).setVisibility(View.GONE);
                item.setTitle(getString(R.string.searchsploit_disable_raw));
                withFilters = false;
                /*});
                    AlertDialog ad = builder.create();
                    ad.setCancelable(false);
                    ad.show();*/
            }
            loadExploits();
            hideSoftKeyboard(getView());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void main(final View rootView) {
        searchSploitListView = rootView.findViewById(R.id.searchResultsList);
        long exploitCount = database.getCount();
        Button searchSearchSploit = rootView.findViewById(R.id.serchsploit_loadDB);
        if (exploitCount == 0) {
            searchSearchSploit.setVisibility(View.VISIBLE);
            adi.dismiss();
            hideSoftKeyboard(Objects.requireNonNull(getView()));
            return;
        } else {
            searchSearchSploit.setVisibility(View.GONE);
        }

        final List<String> platformList = database.getPlatforms();
        Spinner platformSpin = rootView.findViewById(R.id.exdb_platform_spinner);
        ArrayAdapter<String> adp12 = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, platformList);
        adp12.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        platformSpin.setAdapter(adp12);
        platformSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                sel_platform = platformList.get(position);
                loadExploits();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        final List<String> typeList = database.getTypes();
        Spinner typeSpin = rootView.findViewById(R.id.exdb_type_spinner);
        ArrayAdapter<String> adp13 = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, typeList);
        adp13.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpin.setAdapter(adp13);
        typeSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                sel_type = typeList.get(position);
                loadExploits();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
        loadExploits();
    }

    @SuppressLint("SetTextI18n")
    private void loadExploits() {
        if ((sel_platform != null) && (sel_type != null)) {
            List<SearchSploit> exploitList;
            if (withFilters) {
                exploitList = database.getAllExploitsFiltered(sel_search, sel_type, sel_platform);
            } else {
                if (sel_search.equals("")) {
                    exploitList = full_exploitList;
                } else {
                    exploitList = database.getAllExploitsRaw(sel_search);
                }
            }
            if (exploitList == null) {
                new android.os.Handler().postDelayed(
                        this::loadExploits, 1500);
                return;
            }
            numex.setText(exploitList.size() + " " + getString(R.string.searchsploit_results));
            ExploitLoader exploitAdapter = new ExploitLoader(context, exploitList);
            searchSploitListView.setAdapter(exploitAdapter);
            if (!isLoaded) {
                new Thread(() -> full_exploitList = database.getAllExploitsRaw("")).start();
                adi.dismiss();
                isLoaded = true;
                hideSoftKeyboard(Objects.requireNonNull(getView()));
            }
        }
    }
}

class ExploitLoader extends BaseAdapter {

    private final List<SearchSploit> _exploitList;
    private final Context _mContext;

    ExploitLoader(Context context, List<SearchSploit> exploitList) {

        _mContext = context;
        _exploitList = exploitList;

    }

    public int getCount() {
        return _exploitList.size();
    }

    //FILE TO HID
    // FIXME check bootkali fixme strings
    private void start(String file) {
        String[] command = new String[1];
        command[0] = "/data/data/material.hunter/files/scripts/bootkali file2hid-file " + file;
        ShellExecuter exe = new ShellExecuter();
        exe.RunAsRoot(command);
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolderItem vH;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) _mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.searchsploit_item, parent, false);
            vH = new ViewHolderItem();
            vH.description = convertView.findViewById(R.id.description);
            vH.type = convertView.findViewById(R.id.type);
            vH.platform = convertView.findViewById(R.id.platform);
            vH.author = convertView.findViewById(R.id.author);
            vH.date = convertView.findViewById(R.id.exploit_date);
            vH.viewSource = convertView.findViewById(R.id.viewSource);
            vH.openWeb = convertView.findViewById(R.id.openWeb);
            //vH.sendHid = convertView.findViewById(R.id.searchsploit_sendhid_button);
            convertView.setTag(vH);
        } else {
            vH = (ViewHolderItem) convertView.getTag();
        }
        final SearchSploit exploitItem = getItem(position);
        final String _file = exploitItem.getFile();
        final long _id = exploitItem.getId();
        String _desc = exploitItem.getDescription();
        String _date = exploitItem.getDate();
        String _author = exploitItem.getAuthor();
        String _type = exploitItem.getType();
        String _platform = exploitItem.getPlatform();

        vH.viewSource.setOnClickListener(null);
        vH.openWeb.setOnClickListener(null);
        vH.description.setText(_desc);
        vH.type.setText(_type);
        vH.platform.setText(_platform);
        vH.author.setText(_author);
        vH.date.setText(_date);
        vH.viewSource.setOnClickListener(v -> {
            Intent i = new Intent(_mContext, EditSourceActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra("path", "/data/local/nhsystem/kalifs/usr/share/exploitdb/" + _file);
            _mContext.startActivity(i);
        });
        /*vH.sendHid.setOnClickListener(v -> {
            start("usr/share/exploitdb/" + _file);
            //_mContext.startActivity(i);
        });*/
        vH.openWeb.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            String url = "https://www.exploit-db.com/exploits/" + _id + "/";
            i.setData(Uri.parse(url));
            _mContext.startActivity(i);
        });
        return convertView;
    }

    public SearchSploit getItem(int position) {
        return _exploitList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    static class ViewHolderItem {
        TextView type;
        TextView platform;
        TextView author;
        TextView date;
        TextView description;
        Button viewSource;
        Button openWeb;
        //Button sendHid;
    }
}
