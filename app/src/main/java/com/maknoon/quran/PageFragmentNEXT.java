package com.maknoon.quran;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;

import static com.maknoon.quran.MainActivity.EXTRA_page;
import static com.maknoon.quran.MainActivity.nightMode;
import static com.maknoon.quran.MainActivity.pagesFolder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import coil.Coil;
import coil.ImageLoader;
import coil.decode.SvgDecoder;
import coil.request.ImageRequest;
import coil.size.Precision;
import coil.size.Scale;
import coil.size.Size;

public class PageFragmentNEXT extends Fragment
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

	static PageFragmentNEXT newInstance(int page)
	{
		final PageFragmentNEXT fragmentFirst = new PageFragmentNEXT();
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

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		final View view = inflater.inflate(R.layout.page_fragment_next, container, false);
		view.setBackgroundColor(nightMode ? Color.BLACK : Color.rgb(255, 255, 242)); // "#FFFFF2"

		final FrameLayout fl = view.findViewById(R.id.frameLayout);

		float[] invertMX = {
				-1f, 0f, 0f, 0f, 255f,
				0f, -1f, 0f, 0f, 255f,
				0f, 0f, -1f, 0f, 255f,
				0f, 0f, 0f, 1f, 0f
		};
		final ColorMatrix colorMatrix = new ColorMatrix(invertMX);
		final ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);

		final DisplayMetrics displayMetrics = new DisplayMetrics();
		mainActivity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		final float padding = 5 * displayMetrics.density; // padding in px page_fragment_next.xml
		final float displayWidth = displayMetrics.widthPixels - (2 * padding); // pageView.getWidth() is not working since it should be displayed. we are estimating it based on display

		final FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.gravity = Gravity.CENTER;
		layoutParams.setMargins(0,0,0,0);

		final File svgFile;
		if (pagesFolder.contains("warsh"))
			svgFile = new File(pagesFolder + "/warsh/" + (page + 1) + ".svgz");
		else if (pagesFolder.contains("douri"))
			svgFile = new File(pagesFolder + "/douri/" + (page + 1) + ".svgz");
		else if (pagesFolder.contains("qalon"))
			svgFile = new File(pagesFolder + "/qalon/" + (page + 1) + ".svgz");
		else //if (pagesFolder.contains("shubah"))
			svgFile = new File(pagesFolder + "/shubah/" + (page + 1) + ".svgz");

		final AppCompatImageView pageView = new AppCompatImageView(mainActivity);
		final ImageLoader imageLoader = Coil.imageLoader(mainActivity);
		imageLoader.enqueue(new ImageRequest.Builder(mainActivity)
				.data(pagesFolder.contains("hafs") ? Uri.parse("file:///android_asset/hafs/" + (page + 1) + ".svgz") : svgFile)
				.decoderFactory(new SvgDecoder.Factory())
				.target(pageView)
				.build());
		fl.addView(pageView, layoutParams);

		if (nightMode)
			pageView.setColorFilter(filter);

		if (pagesFolder.equals("hafs"))
		{
			final Vector<String> location = new Vector<>();
			final Vector<Integer> ayah = new Vector<>();
			final Vector<Integer> surah = new Vector<>();

			final Cursor mCursor = MainActivity.db.rawQuery("SELECT * FROM Quran WHERE Page = " + (page + 1), null);
			if (mCursor.moveToFirst())
			{
				for (int i = 0; i < mCursor.getCount(); i++)
				{
					final int aya = mCursor.getInt(mCursor.getColumnIndexOrThrow("Aya"));
					final int sura = mCursor.getInt(mCursor.getColumnIndexOrThrow("Sura"));
					final String lc = mCursor.getString(mCursor.getColumnIndexOrThrow("Location"));

					location.add(lc);
					surah.add(sura);
					ayah.add(aya);

					mCursor.moveToNext();
				}
			}
			mCursor.close();

			final Vector<List<RectF>> locations = new Vector<>();
			for (String lc : location)
			{
				if (!lc.isEmpty())
				{
					final StringTokenizer tokens = new StringTokenizer(lc, "-");
					final String[] dims = tokens.nextToken().split(",");
					final float w = Float.parseFloat(dims[0]);
					final float h = Float.parseFloat(dims[1]);

					final int count = tokens.countTokens();

					final float pageWidth = displayWidth;
					final float pageHeight = pageWidth * h / w;

					final List<RectF> regions = new ArrayList<>(count);

					for (int y = 0; y < count; y++)
					{
						final StringTokenizer dimensions = new StringTokenizer(tokens.nextToken(), ",");
						if (dimensions.hasMoreTokens())
						{
							final float xx = Float.parseFloat(dimensions.nextToken()) / w * pageWidth;
							final float yy = Float.parseFloat(dimensions.nextToken()) / h * pageHeight;
							final float width = Float.parseFloat(dimensions.nextToken()) / w * pageWidth;
							final float height = Float.parseFloat(dimensions.nextToken()) / h * pageHeight;
							regions.add(new RectF(xx, yy, xx + width, yy + height));
						}
					}

					locations.add(regions);
				}
			}

			pageView.setOnTouchListener(new View.OnTouchListener()
			{
				long startTime, endTime;
				boolean allowActionBar;

				@Override
				public boolean onTouch(View v, MotionEvent event)
				{
					switch (event.getAction())
					{
						case MotionEvent.ACTION_DOWN:
							startTime = endTime = event.getEventTime();
							allowActionBar = true;
							return true; // a must or ACTION_UP will not triggered. https://stackoverflow.com/questions/15799839/motionevent-action-up-not-called

						case MotionEvent.ACTION_UP:
						{
							if(allowActionBar)
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
							startTime = endTime; // reset
							allowActionBar = false;
							return true;
						}

						case MotionEvent.ACTION_MOVE:
						{
							endTime = event.getEventTime();
							if ((endTime - startTime) > 800) // 800ms
							{
								final float x = event.getX();
								final float y = event.getY();
								for (int e = 0; e < locations.size(); e++)
								{
									boolean found = false;
									final List<RectF> regions = locations.get(e);
									final Path area = new Path();
									for (int i = 0; i < regions.size(); i++)
									{
										if (found)
										{
											final Path region = new Path();
											region.addRect(regions.get(i), Path.Direction.CW);
											area.op(region, Path.Op.UNION);
										}
										else
										{
											if (regions.get(i).contains(x, y))
											{
												final int childCount = fl.getChildCount();
												if (childCount > 1)
													fl.removeViews(1, childCount - 1);

												found = true;
												i = -1;
											}
										}
									}

									startTime = endTime;
									allowActionBar = false;

									if (found)
									{
										final Paint paint = new Paint();
										paint.setColor(Color.parseColor("#30CD5C5C"));
										final Bitmap bg = Bitmap.createBitmap(pageView.getWidth(), pageView.getHeight(), Bitmap.Config.ARGB_8888);
										final Canvas canvas = new Canvas(bg);
										canvas.drawPath(area, paint);
										final AppCompatImageView highlightCanvas = new AppCompatImageView(mainActivity);
										highlightCanvas.setImageBitmap(bg);
										fl.addView(highlightCanvas, layoutParams);
										return true;
									}
								}
							}
						}
					}
					return false;
				}
			});
		}

		return view;
	}
}