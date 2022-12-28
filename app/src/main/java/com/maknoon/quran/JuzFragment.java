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

import static com.maknoon.quran.QuranDb.juz_page;

public class JuzFragment extends Fragment
{
	private MainActivity mainActivity;

	@Override
	public void onAttach(@NonNull Context context)
	{
		super.onAttach(context);

		if (context instanceof MainActivity)
			mainActivity = (MainActivity) context;
	}

	static JuzFragment newInstance()
	{
		return new JuzFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		final View view = inflater.inflate(R.layout.juz_fragment, container, false);
		final ListView juzList = view.findViewById(R.id.juz);

		juzList.setScrollingCacheEnabled(false);

		final JuzNodeInfo[] values = new JuzNodeInfo[30];
		for(int i=0; i<30; i++)
			values[i] = new JuzNodeInfo(i+1, juz_page[i]);

		final ArrayList<JuzNodeInfo> list = new ArrayList<>(Arrays.asList(values));
		juzList.setAdapter(new JuzAdapter(mainActivity, list));

		juzList.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, final View view, int position, long id)
			{
				mainActivity.page = juz_page[position] - 1;
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