package com.example.adria.chatproject.Adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.adria.chatproject.Fragments.ChatFragment;
import com.example.adria.chatproject.Fragments.SettingFragment;

public class ViewPagerAdapter extends FragmentPagerAdapter {
    private Fragment[] fragments;

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
        fragments = new Fragment[] {
                new ChatFragment(), //0
                new SettingFragment() //1
        };
    }

    @Override
    public Fragment getItem(int position) {
        return fragments[position];
    }

    @Override
    public int getCount() {
        return fragments.length; //2 items
    }


}
