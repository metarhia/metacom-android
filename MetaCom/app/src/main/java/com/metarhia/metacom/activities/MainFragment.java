package com.metarhia.metacom.activities;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.metarhia.metacom.R;
import com.metarhia.metacom.activities.chat.ChatLoginFragment;
import com.metarhia.metacom.activities.files.FilesFragment;
import com.metarhia.metacom.models.UserConnectionsManager;
import com.metarhia.metacom.utils.PermissionUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


/**
 * A simple {@link Fragment} subclass.
 *
 * @author MariaKokshaikina
 */
public class MainFragment extends Fragment {

    public final static String MAIN_FRAGMENT_TAG = "MainFragmentTag";
    private static final String KEY_CONNECTION_ID = "keyConnectionId";
    private static final String KEY_HOST_NAME = "keyHostName";

    @BindView(R.id.toolbar_title)
    TextView mToolbarTitle;
    @BindView(R.id.toolbar_back)
    ImageView mToolbarBack;
    @BindView(R.id.tab_layout)
    TabLayout mTabLayout;
    @BindView(R.id.view_pager)
    ViewPager mViewPager;
    private Unbinder mUnbinder;

    private ArrayList<Fragment> mFragmentArrayList;
    private ArrayList<String> mFragmentTitles;
    private int mConnectionID;

    public static MainFragment newInstance(int connectionID, String hostName) {
        Bundle args = new Bundle();
        args.putInt(KEY_CONNECTION_ID, connectionID);
        args.putString(KEY_HOST_NAME, hostName);
        MainFragment fragment = new MainFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        if (getArguments() != null) {
            mConnectionID = getArguments().getInt(KEY_CONNECTION_ID);
            setPages();
            mToolbarTitle.setText(getArguments().getString(KEY_HOST_NAME));
        }

        if (PermissionUtils.checkVersion()) {
            if (!PermissionUtils.checkIfAlreadyHavePermission(getContext())) {
                showRequestDialog();
            }
        }

        return view;
    }

    private void showRequestDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.permissions)
                .setMessage(R.string.permissions_info)
                .setCancelable(false)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        PermissionUtils.requestForSpecificPermission(getActivity());
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @OnClick(R.id.toolbar_back)
    public void onBackClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.exit)
                .setMessage(R.string.confirm_exit)
                .setCancelable(false)
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        leaveServer();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void leaveServer() {
        UserConnectionsManager.get().removeConnection(UserConnectionsManager.get().getConnection(mConnectionID));
        getActivity().finish();
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
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }
}
