package org.monsieura.reinstaller;

import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.res.DrawableRes;

import hugo.weaving.DebugLog;
import timber.log.Timber;

@EActivity(R.layout.activity_my)
@OptionsMenu(R.menu.my)
public class MyActivity extends ListActivity {


    private CountDownTimer timer;

    @DrawableRes(R.drawable.ic_launcher)
    Drawable defaultIcon;

    @DrawableRes(android.R.drawable.presence_invisible)
    Drawable notInstalledIcon;

    @DrawableRes(android.R.drawable.presence_online)
    Drawable installedIcon;

    @DebugLog
    private void _refreshAll() {
        ((BaseAdapter) getListAdapter()).notifyDataSetChanged();
        getListView().invalidate();
    }

    @Override @DebugLog
    protected void onResume() {
        super.onResume();
        _refreshAll();
        timer = new CountDownTimer(15 * 60 * 1000, 1000) {
            @Override
            public void onTick(long l) {
                Timber.d("tick");
                _refreshAll();
            }

            @Override
            public void onFinish() {

            }
        };
        timer.start();
    }

    @Override @DebugLog
    protected void onPause() {
        super.onPause();
        timer.cancel();
    }

    @AfterViews
    @DebugLog
    public void init() {
        //log init
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }


        setListAdapter(new BaseAdapter() {




            @Override
            public int getCount() {
                return Config.applications.length;
            }

            @Override
            public String getItem(int i) {
                return Config.applications[i];
            }

            @Override
            public long getItemId(int i) {
                return Config.applications[i].hashCode();
            }

            @Override
            public View getView(int position, View convertview, ViewGroup parent) {
                View row = null;

                //test de la vue recyclée
                if (convertview == null) { //nous ne sommes pas sur le bon type de vu
                    Timber.d("inflating");
                    LayoutInflater inflater = getLayoutInflater();
                    row = inflater.inflate(R.layout.list_row, parent, false);
                } else {
                    row = convertview;
                }

                ImageView pb = (ImageView) row.findViewById(R.id.status);
                ImageView ic = (ImageView) row.findViewById(R.id.icon);
                TextView subTextView = (TextView) row.findViewById(R.id.subtextView);
                TextView titleTextView = (TextView) row.findViewById(R.id.titleTextView);

                String pack = getItem(position);

                if(isAppInstalled(pack)){
                    ic.setImageDrawable(getAppIcon(pack));
                    titleTextView.setText(getAppLabel(pack));
                }else{
                    ic.setImageDrawable(defaultIcon);
                    titleTextView.setText(pack);
                }
                subTextView.setText(isAppInstalled(pack) ? "installed" : "not installed");
                pb.setImageDrawable(isAppInstalled(pack) ? installedIcon : notInstalledIcon);

                return row;
            }
        });
    }

    @ItemClick(android.R.id.list)
    @DebugLog
    public void myListItemClicked(int itemPosition) {
        Intent i = getMarketIntent(itemPosition);
        startActivity(i);
    }

    @DebugLog
    private Intent getMarketIntent(int position) {
        String appPackageName = (String) getListAdapter().getItem(position);
        Intent i = null;
        try {
            i = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName));
        } catch (android.content.ActivityNotFoundException anfe) {
            i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName));
        }
        return i;
    }


    @Override
    @DebugLog
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            _startInstallAtItem(requestCode + 1);

    }

    @OptionsItem(R.id.action_install_all)
    @DebugLog
    public void batchInstall() {
        Timber.d("Batching installation");
        _startInstallAtItem(0);
    }

    @DebugLog
    private void _startInstallAtItem(int itemNumber) {
        int i = itemNumber;
        //test si item installé
        if(i < getListAdapter().getCount()) {
            while (isAppInstalled((String) getListAdapter().getItem(i))) {
                if (i >= getListAdapter().getCount() - 1) {
                    Toast.makeText(getApplicationContext(), "Toutes les installations sont lancées", Toast.LENGTH_LONG).show();
                } else {
                    i++;
                }
            }
            Intent intent = getMarketIntent(i);
            startActivityForResult(intent, i);
        }
    }

    @DebugLog
    private boolean isAppInstalled(String uri) {
        PackageManager pm = getPackageManager();
        boolean app_installed = false;
        try {
            PackageInfo info = pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }


    @DebugLog
    private Drawable getAppIcon(String uri) {
        PackageManager pm = getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            if(info != null){
                return info.applicationInfo.loadIcon(getPackageManager());
            }
        } catch (PackageManager.NameNotFoundException e) {
           return null;
        }
        return null;
    }

    @DebugLog
    private CharSequence getAppLabel(String uri) {
        PackageManager pm = getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            if(info != null){
                return info.applicationInfo.loadLabel(pm);
            }
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
        return null;
    }

    /*

     */
}
