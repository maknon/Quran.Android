package com.maknoon.quran;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;

public class PageNumberFragment extends Fragment
{
	private MainActivity mainActivity;
	TextInputEditText pageText;

	@Override
	public void onAttach(@NonNull Context context)
	{
		super.onAttach(context);

		if (context instanceof MainActivity)
			mainActivity = (MainActivity) context;
	}

	static PageNumberFragment newInstance()
	{
		return new PageNumberFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		final View view = inflater.inflate(R.layout.pagenumber_fragment, container, false);

		pageText = view.findViewById(R.id.page);
		pageText.setFilters(new InputFilter[]{new InputFilterMinMax(1, 604)});
		pageText.setOnEditorActionListener(new TextView.OnEditorActionListener()
		{
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
			{
				boolean handled = false;
				if (actionId == EditorInfo.IME_ACTION_DONE)
				{
					handled = true;
					final Editable ed = pageText.getText();
					if (ed != null && !ed.toString().isEmpty())
					{
						mainActivity.page = Integer.parseInt(ed.toString()) - 1;
						mainActivity.mViewPager.setCurrentItem(mainActivity.page, false);

						final Fragment prev = mainActivity.getSupportFragmentManager().findFragmentByTag("goTo");
						if (prev != null)
						{
							final DialogFragment df = (DialogFragment) prev;
							df.dismiss();
						}
					}
				}
				return handled;
			}
		});

		return view;
	}
}