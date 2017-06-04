package com.metarhia.metacom.activities;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.metarhia.metacom.R;
import com.metarhia.metacom.activities.chat.ChatLoginFragment;
import com.metarhia.metacom.activities.files.FilesFragment;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {

    public final static String MAIN_FRAGMENT_TAG = "MainFragmentTag";
    private static final String KEY_CONNECTION_ID = "keyConnectionId";
    private Unbinder mUnbinder;

    @BindView(R.id.toolbar_title)
    TextView mToolbarTitle;

    @BindView(R.id.toolbar_back)
    ImageView mToolbarBack;

    @BindView(R.id.tab_layout)
    TabLayout mTabLayout;

    @BindView(R.id.view_pager)
    ViewPager mViewPager;

    private ArrayList<Fragment> mFragmentArrayList;
    private ArrayList<String> mFragmentTitles;

    public static MainFragment newInstance(int connectionID) {
        Bundle args = new Bundle();
        args.putInt(KEY_CONNECTION_ID, connectionID);
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

        mToolbarTitle.setText(getString(R.string.hostname));

        if (getArguments() != null) {
            int connectionID = getArguments().getInt(KEY_CONNECTION_ID);
            setPages(connectionID);
        }

        return view;
    }

    @OnClick(R.id.toolbar_back)
    public void onBackClick() {
        getActivity().finish();
    }

    private void setPages(int connectionID) {
        mFragmentArrayList = new ArrayList<>();
        mFragmentArrayList.add(FilesFragment.newInstance(connectionID));
        mFragmentArrayList.add(ChatLoginFragment.newInstance(connectionID));

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
