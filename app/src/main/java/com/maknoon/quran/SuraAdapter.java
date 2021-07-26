package com.maknoon.quran;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;

import java.util.ArrayList;

public class SuraAdapter extends ArrayAdapter<SuraNodeInfo>
{
	private final Context context;
	private final ArrayList<SuraNodeInfo> suraArrayList;

	SuraAdapter(Context context, ArrayList<SuraNodeInfo> sura_listview)
	{
		super(context, R.layout.sura_listview, sura_listview);

		this.context = context;
		this.suraArrayList = sura_listview;
	}

	@Override
	@NonNull
	public View getView(final int position, View convertView, @NonNull ViewGroup parent)
	{
		final View rowView;

		if (convertView == null)
		{
			final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			rowView = inflater.inflate(R.layout.sura_listview, parent, false);
		} else
			rowView = convertView;

		final AppCompatTextView suraView = rowView.findViewById(R.id.sura);
		suraView.setText(suraArrayList.get(position).getSura());

		final AppCompatTextView pageView = rowView.findViewById(R.id.page);
		pageView.setText(String.valueOf(suraArrayList.get(position).getPage()));

		return rowView;
	}
}