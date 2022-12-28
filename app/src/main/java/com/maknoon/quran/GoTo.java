package com.maknoon.quran;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.List;

public class GoTo extends DialogFragment
{
	private MainActivity mainActivity;

	@Override
	public void onAttach(@NonNull Context context)
	{
		super.onAttach(context);

		if (context instanceof MainActivity)
			mainActivity = (MainActivity) context;
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		//super.onCreateView(inflater, container, savedInstanceState);
		final Dialog d = getDialog();
		if(d != null)
		{
			d.requestWindowFeature(Window.FEATURE_NO_TITLE);
			d.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		}

		return inflater.inflate(R.layout.go_to, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
	{
		final TabLayout tabLayout = view.findViewById(R.id.tabs);
		final ViewPager2 viewPager = view.findViewById(R.id.viewpager);

		final PagerAdapter pageAdapter = new PagerAdapter(getChildFragmentManager(), getLifecycle());
		viewPager.setAdapter(pageAdapter);
		viewPager.setOffscreenPageLimit(pageAdapter.getItemCount());

		tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
		{
			@Override
			public void onTabSelected(TabLayout.Tab tab)
			{
				final int index = tab.getPosition();
				if (index == 2)
				{
					//final PageNumberFragment pf = (PageNumberFragment) getChildFragmentManager().getFragments().get(2);
					final List<Fragment> f = getChildFragmentManager().getFragments();
					for(Fragment fr : f)
					{
						if(fr instanceof PageNumberFragment)
						{
							final PageNumberFragment pf = (PageNumberFragment) fr;
							pf.pageText.requestFocus();

							final InputMethodManager imm = (InputMethodManager) mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
							imm.showSoftInput(pf.pageText, InputMethodManager.SHOW_IMPLICIT);
							break;
						}
					}
				}
			}

			@Override
			public void onTabUnselected(TabLayout.Tab tab)
			{
			}

			@Override
			public void onTabReselected(TabLayout.Tab tab)
			{
			}
		});

		new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy()
		{
			@Override
			public void onConfigureTab(@NonNull TabLayout.Tab tab, int position)
			{
				if (position == 0)
					tab.setText(R.string.sura);
				else
				if (position == 1)
					tab.setText(R.string.juz);
				else
				if (position == 2)
					tab.setText(R.string.page);
			}
		}).attach();
	}

	@Override
	public void onStart()
	{
		super.onStart();

		final Dialog d = getDialog();
		if(d != null)
		{
			final Window window = d.getWindow();
			if (window != null)
			{
				//getDialog().getWindow().getDecorView().findViewById(android.R.id.content).invalidate();
				// Be sure to set Background. If it is not set, the window property setting is invalid
				//window.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.text_color)));
				if (getActivity() != null)
				{
					final WindowManager windowManager = getActivity().getWindowManager();
					if (windowManager != null)
					{
						final WindowManager.LayoutParams params = window.getAttributes();
						params.gravity = Gravity.CENTER;
						// Use ViewGroup.LayoutParams so that the Dialog width fills the entire screen
						params.width = WindowManager.LayoutParams.MATCH_PARENT;
						params.height = WindowManager.LayoutParams.MATCH_PARENT;
						window.setAttributes(params);
					}
				}
			}
		}
	}

	static class PagerAdapter extends FragmentStateAdapter
	{
		PagerAdapter(FragmentManager fm, @NonNull Lifecycle lc)
		{
			super(fm, lc);
		}

		@NonNull
		@Override
		public Fragment createFragment(int position)
		{
			switch (position)
			{
				case 0:
					return SuraFragment.newInstance();
				case 1:
					return JuzFragment.newInstance();
				default:
					return PageNumberFragment.newInstance();
			}
		}

		@Override
		public int getItemCount()
		{
			return 3;
		}
	}
}
