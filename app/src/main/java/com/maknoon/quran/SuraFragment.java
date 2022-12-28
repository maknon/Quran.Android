package com.maknoon.quran;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Arrays;

import static com.maknoon.quran.QuranDb.sura_ar;
import static com.maknoon.quran.QuranDb.sura_page;

public class SuraFragment extends Fragment
{
	private MainActivity mainActivity;

	@Override
	public void onAttach(@NonNull Context context)
	{
		super.onAttach(context);

		if (context instanceof MainActivity)
			mainActivity = (MainActivity) context;
	}

	static SuraFragment newInstance()
	{
		return new SuraFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		final View view = inflater.inflate(R.layout.sura_fragment, container, false);
		final ListView suraList = view.findViewById(R.id.sura);
		suraList.setScrollingCacheEnabled(false);

		final SuraNodeInfo[] values = new SuraNodeInfo[sura_ar.length];
		for(int i=0; i<sura_ar.length; i++)
			values[i] = new SuraNodeInfo(sura_ar[i], sura_page[i]);

		final ArrayList<SuraNodeInfo> list = new ArrayList<>(Arrays.asList(values));
		suraList.setAdapter(new SuraAdapter(mainActivity, list));
		suraList.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, final View view, int position, long id)
			{
				mainActivity.page = sura_page[position] - 1;
				mainActivity.mViewPager.setCurrentItem(mainActivity.page, false);

				final Fragment prev = mainActivity.getSupportFragmentManager().findFragmentByTag("goTo");
				if (prev != null)
				{
					final DialogFragment df = (DialogFragment) prev;
					df.dismiss();
				}
			}
		});

		return view;
	}
}