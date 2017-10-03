package com.metarhia.metacom.activities;


import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.metarhia.metacom.R;
import com.metarhia.metacom.activities.chat.ChatLoginFragment;
import com.metarhia.metacom.activities.files.FilesFragment;
import com.metarhia.metacom.interfaces.BackPressedHandler;
import com.metarhia.metacom.models.UserConnectionsManager;
import com.metarhia.metacom.utils.KeyboardUtils;
import com.metarhia.metacom.utils.PermissionUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


/**
 * @author MariaKokshaikina
 */
public class MainFragment extends Fragment implements BackPressedHandler {

    public final static String MAIN_FRAGMENT_TAG = "MainFragmentTag";
    private static final String KEY_CONNECTION_ID = "keyConnectionId";
    private static final String KEY_HOST_NAME = "keyHostName";
    private static final String KEY_PORT = "keyPort";
    private static final String KEY_EXIT_DIALOG = "keyExitDialog";
    private static final String KEY_PERMISSIONS_DIALOG = "keyPermissionsDialog";

    @BindView(R.id.toolbar_title)
    TextView mToolbarTitle;
    @BindView(R.id.tab_layout)
    TabLayout mTabLayout;
    @BindView(R.id.view_pager)
    ViewPager mViewPager;
    private Unbinder mUnbinder;

    private ArrayList<Fragment> mFragmentArrayList;
    private ArrayList<String> mFragmentTitles;
    private int mConnectionID;
    private boolean mExitDialog;
    private boolean mPermissionsDialog;
    private boolean isUIVisible = true;

    public static MainFragment newInstance(int connectionID, String hostName, int port) {
        Bundle args = new Bundle();
        args.putInt(KEY_CONNECTION_ID, connectionID);
        args.putString(KEY_HOST_NAME, hostName);
        args.putInt(KEY_PORT, port);

        MainFragment fragment = new MainFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        if (getArguments() != null) {
            mConnectionID = getArguments().getInt(KEY_CONNECTION_ID);

            String host = getArguments().getString(KEY_HOST_NAME);
            String port = String.valueOf(getArguments().getInt(KEY_PORT));

            String toolbarTitle = String.format(getString(R.string.title_pattern), host, port);
            setPages();
            mToolbarTitle.setText(toolbarTitle);
        }

        if (savedInstanceState == null) {
            if (PermissionUtils.checkVersion() &&
                    !PermissionUtils.checkIfAlreadyHavePermission(getContext())) {
                showRequestDialog();
            }
        } else {
            if (savedInstanceState.getBoolean(KEY_EXIT_DIALOG)) {
                handleBackPress();
            }
            if (savedInstanceState.getBoolean(KEY_PERMISSIONS_DIALOG)) {
                showRequestDialog();
            }
        }

        return view;
    }

    private void showRequestDialog() {
        mPermissionsDialog = true;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.permissions)
                .setMessage(R.string.permissions_info)
                .setCancelable(false)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        PermissionUtils.requestForStoragePermission(MainFragment.this);
                        mPermissionsDialog = false;
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PermissionUtils.REQUEST_CODE: {
                if (!(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    showForbidDialog();
                }
            }
        }
    }

    private void showForbidDialog() {
        if (isUIVisible) {
            Toast.makeText(getContext(), getString(R.string.permissions_are_not_granted),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.toolbar_back)
    public void onBackClick() {
        handleBackPress();
    }

    public void leaveServer() {
        UserConnectionsManager.get().removeConnection(UserConnectionsManager.get().getConnection
                (mConnectionID));
    }

    private void setPages() {
        mFragmentArrayList = new ArrayList<>();
        mFragmentArrayList.add(FilesFragment.newInstance(mConnectionID));
        mFragmentArrayList.add(ChatLoginFragment.newInstance(mConnectionID));

        PagerAdapter pagerAdapter = new FragmentPagerAdapter(getFragmentManager()) {

            @Override
            public int getCount() {
                return mFragmentArrayList.size();
            }

            @Override
            public Fragment getItem(int position) {
                return mFragmentArrayList.get(position);
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return mFragmentTitles.get(position);
            }
        };

        mViewPager.setAdapter(pagerAdapter);

        mFragmentTitles = new ArrayList<>();
        mFragmentTitles.add(getResources().getString(R.string.files));
        mFragmentTitles.add(getResources().getString(R.string.chat));

        mTabLayout.setupWithViewPager(mViewPager);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                KeyboardUtils.hideKeyboard(getActivity());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void handleBackPress() {
        mExitDialog = true;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style
                .AlertDialogStyle);
        builder.setTitle(R.string.leave_server)
                .setMessage(R.string.leave_server_desc)
                .setCancelable(false)
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mExitDialog = false;
                                dialog.cancel();
                            }
                        })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        leaveServer();
                        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences
                                (getActivity().getApplicationContext());
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putBoolean(getString(R.string.shared_preferences_is_authorized), false);
                        editor.apply();
                        getActivity().finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
        alert.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color
                .black14)));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_EXIT_DIALOG, mExitDialog);
        outState.putBoolean(KEY_PERMISSIONS_DIALOG, mPermissionsDialog);
    }

    @Override
    public void onPause() {
        super.onPause();
        isUIVisible = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        isUIVisible = true;
    }
}
