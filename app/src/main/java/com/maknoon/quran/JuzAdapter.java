package com.maknoon.quran;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;

import java.util.ArrayList;

public class JuzAdapter extends ArrayAdapter<JuzNodeInfo>
{
	private final Context context;
	private final ArrayList<JuzNodeInfo> juzArrayList;

	JuzAdapter(Context context, ArrayList<JuzNodeInfo> juz_listview)
	{
		super(context, R.layout.juz_listview, juz_listview);

		this.context = context;
		this.juzArrayList = juz_listview;
	}

	@Override
	@NonNull
	public View getView(final int position, View convertView, @NonNull ViewGroup parent)
	{
		final View rowView;

		if (convertView == null)
		{
			final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			rowView = inflater.inflate(R.layout.juz_listview, parent, false);
		} else
			rowView = convertView;

		final AppCompatTextView juzView = rowView.findViewById(R.id.juz);
		juzView.setText(String.valueOf(juzArrayList.get(position).getJuz()));

		final AppCompatTextView pageView = rowView.findViewById(R.id.page);
		pageView.setText(String.valueOf(juzArrayList.get(position).getPage()));

		return rowView;
	}
}