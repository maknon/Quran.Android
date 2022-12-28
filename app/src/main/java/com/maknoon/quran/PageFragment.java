package com.maknoon.quran;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.maknoon.quran.MainActivity.EXTRA_page;
import static com.maknoon.quran.MainActivity.nightMode;
import static com.maknoon.quran.MainActivity.pagesFolder;

public class PageFragment extends Fragment
{
	private int page;

	private MainActivity mainActivity;
	private final Handler hideHandler = new Handler();
	private final Runnable hideRunnable = new Runnable()
	{
		@Override
		public void run()
		{
			final ActionBar ab = mainActivity.getSupportActionBar();
			if (ab != null)
			{
				if (ab.isShowing())
					ab.hide();
			}
		}
	};

	@Override
	public void onAttach(@NonNull Context context)
	{
		super.onAttach(context);

		if (context instanceof MainActivity)
			mainActivity = (MainActivity) context;
	}

	static PageFragment newInstance(int page)
	{
		final PageFragment fragmentFirst = new PageFragment();
		final Bundle args = new Bundle();
		args.putInt(EXTRA_page, page);
		fragmentFirst.setArguments(args);
		return fragmentFirst;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		page = getArguments().getInt(EXTRA_page, 0);
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		final View view = inflater.inflate(R.layout.page_fragment, container, false);
		final AppCompatImageView pageView = view.findViewById(R.id.page);

		pageView.setOnClickListener(
				new View.OnClickListener()
				{
					public void onClick(View v)
					{
						final ActionBar ab = mainActivity.getSupportActionBar();
						if (ab != null)
						{
							if (ab.isShowing())
								ab.hide();
							else
							{
								hideHandler.removeCallbacks(hideRunnable);
								hideHandler.postDelayed(hideRunnable, 4000); // in ms
								ab.show();
							}
						}
					}
				}
		);

		/*
		cannot have svg files for the pages since it has very lengthy paths resulting in:
		java.lang.IllegalArgumentException: R is not a valid verb. Failure occurred at position 2 of path: STRING_TOO_LARGE
		No solution for it even using optimizers e.g. svgo. Android recommend 200dp x 200dp for performance. those files will hit the performance badly
		switch (page)
		{
			case 0:
				pageView.setImageDrawable(AppCompatResources.getDrawable(mainContext, R.drawable.ic_600_1));
				break;
			case 1:
				pageView.setImageDrawable(AppCompatResources.getDrawable(mainContext, R.drawable.ic_600_2));
				break;
			default:
				pageView.setImageDrawable(AppCompatResources.getDrawable(mainContext, R.drawable.ic_600_3));
				break;
		}
		*/

		try
		{
			final InputStream in;

			if (pagesFolder.contains("hafs"))
			{
				final AssetManager am = getResources().getAssets();
				in = am.open("hafs/" + (page + 1) + ".png");
			}
			else if (pagesFolder.contains("warsh"))
				in = new FileInputStream((pagesFolder + "/warsh/"+ (page + 1) + ".png"));
			else if (pagesFolder.contains("douri"))
				in = new FileInputStream((pagesFolder + "/douri/"+ (page + 1) + ".png"));
			else if (pagesFolder.contains("qalon"))
				in = new FileInputStream((pagesFolder + "/qalon/"+ (page + 1) + ".png"));
			else //if (pagesFolder.contains("shubah"))
				in = new FileInputStream((pagesFolder + "/shubah/"+ (page + 1) + ".png"));

			final Drawable pg = Drawable.createFromStream(in, null);
			pageView.setImageDrawable(pg);

			if(nightMode)
			{
				view.setBackgroundColor(Color.BLACK);
				//pg.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
				//pg.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);

				float[] invertMX = {
						-1f, 0f, 0f, 0f, 255f,
						0f, -1f, 0f, 0f, 255f,
						0f, 0f, -1f, 0f, 255f,
						0f, 0f, 0f, 1f, 0f
				};

				final ColorMatrix colorMatrix = new ColorMatrix(invertMX);
				final ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);
				pageView.setColorFilter(filter);
			}
			else
				pageView.setBackgroundColor(Color.rgb(255, 255, 242)); // "#FFFFF2"

			/*
			if(page == 2)
			{
				final RectF rectBox= new RectF(500, 500, 500, 500);
				Paint paint= new Paint();
				paint.setStyle(Paint.Style.FILL);
				paint.setColor(Color.RED);
				Canvas canvas;
				canvas.drawRoundRect(rectBox, 0, 0, paint);
				canvas.drawBitmap(pg, 0, 0, paint);
				paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));


				Drawable clone = pg.getConstantState().newDrawable();

				DrawableCompat.setTint(clone, getResources().getColor(android.R.color.holo_red_dark));
				//pg.setColorFilter(ContextCompat.getColor(mainContext, R.color.purple_500), PorterDuff.Mode.SRC_IN);

				// Not working
				//pg.setColorFilter(new PorterDuffColorFilter(Color.argb(150, 120, 255, 255), PorterDuff.Mode.MULTIPLY));

				//clone.setBounds(0, 0, pageView.getWidth(), pageView.getHeight());
				clone.setBounds(500, 500, 500, 500);
				pageView.getOverlay().add(clone);
			}
			*/

			in.close();
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}

		return view;
	}
}