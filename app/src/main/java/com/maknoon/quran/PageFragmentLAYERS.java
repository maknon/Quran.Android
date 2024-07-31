package com.maknoon.quran;

import static com.maknoon.quran.MainActivity.EXTRA_page;
import static com.maknoon.quran.MainActivity.nightMode;
import static com.maknoon.quran.MainActivity.pagesFolder;

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
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import coil.Coil;
import coil.ImageLoader;
import coil.decode.SvgDecoder;
import coil.request.ImageRequest;

public class PageFragmentLAYERS extends Fragment
{
	private int page;

	private MainActivity mainActivity;
	int layers = 0;

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

	static PageFragmentLAYERS newInstance(int page)
	{
		final PageFragmentLAYERS fragmentFirst = new PageFragmentLAYERS();
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
		/*
		fl.setOnClickListener(
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
		*/

		float[] invertMX = {
				-1f, 0f, 0f, 0f, 255f,
				0f, -1f, 0f, 0f, 255f,
				0f, 0f, -1f, 0f, 255f,
				0f, 0f, 0f, 1f, 0f
		};
		final ColorMatrix colorMatrix = new ColorMatrix(invertMX);
		final ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);

		final FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.gravity = Gravity.CENTER;

		if (pagesFolder.equals("hafs"))
		{
			final AppCompatImageView frameView = new AppCompatImageView(mainActivity);
			final ImageLoader imageLoader = Coil.imageLoader(mainActivity);
			imageLoader.enqueue(new ImageRequest.Builder(mainActivity)
					.data(Uri.parse("file:///android_asset/hafsLAYERS/" + (page + 1) + "-f.svg"))
					.decoderFactory(new SvgDecoder.Factory())
					.target(frameView)
					.build());
			fl.addView(frameView, layoutParams); layers++;

			if (nightMode)
				frameView.setColorFilter(filter);

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

					final AppCompatImageView ayaView = new AppCompatImageView(mainActivity);
					ayaView.setTag(sura + ":" + aya);
					final ImageLoader imageLoader1 = Coil.imageLoader(mainActivity);
					imageLoader1.enqueue(new ImageRequest.Builder(mainActivity)
							.data(Uri.parse("file:///android_asset/hafsLAYERS/" + (page + 1) + "-" + sura + "-" + aya + ".svg"))
							.decoderFactory(new SvgDecoder.Factory())
							.target(ayaView)
							.build());
					fl.addView(ayaView, layoutParams); layers++;
					//ayaView.setColorFilter(Color.argb(255, 255, 0, 0)); // White Tint
					if (nightMode)
						ayaView.setColorFilter(filter);

					mCursor.moveToNext();
				}
			}
			mCursor.close();

			// frameView.getHeight() is not correct, it is not the page height
			final DisplayMetrics displayMetrics = new DisplayMetrics();
			mainActivity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
			final float padding =  5 * displayMetrics.density; // padding in px page_fragment_next.xml
			final float displayHeight = displayMetrics.heightPixels - (2 * padding);
			final float displayWidth = displayMetrics.widthPixels - (2 * padding); // frameView.getWidth() is not working since it should be displayed. we are estimating it based on display

			final Vector<List<Rect>> locations = new Vector<>();
			for(String lc: location)
			{
				if(!lc.isEmpty())
				{
					final StringTokenizer tokens = new StringTokenizer(lc, "-");
					final String[] dims = tokens.nextToken().split(",");
					final float w = Float.parseFloat(dims[0]);
					final float h = Float.parseFloat(dims[1]);

					final int count = tokens.countTokens();

					final float pageHeight = displayWidth * h / w;
					final float pageWidth = displayWidth;

					final List<Rect> regions = new ArrayList<>(count);

					for (int y = 0; y < count; y++)
					{
						final StringTokenizer dimensions = new StringTokenizer(tokens.nextToken(), ",");
						if (dimensions.hasMoreTokens())
						{
							final int xx = Math.round((Integer.parseInt(dimensions.nextToken()) / w * pageWidth));
							final int yy = Math.round((Integer.parseInt(dimensions.nextToken()) / h * pageHeight));
							final int width = Math.round(Integer.parseInt(dimensions.nextToken()) / w * pageWidth);
							final int height = Math.round(Integer.parseInt(dimensions.nextToken()) / h * pageHeight);
							regions.add(new Rect(xx, yy, xx + width, yy + height));
						}
					}

					locations.add(regions);
				}
			}

			frameView.setOnTouchListener(new View.OnTouchListener()
			{
				@Override
				public boolean onTouch(View v, MotionEvent event)
				{
					switch (event.getAction())
					{
						case MotionEvent.ACTION_DOWN:
							return true; // a must or ACTION_UP will not triggered. https://stackoverflow.com/questions/15799839/motionevent-action-up-not-called

						case MotionEvent.ACTION_UP:
						{
							final int x = (int) event.getX();
							final int y = (int) event.getY();
							for (int e = 0; e < locations.size(); e++)
							{
								boolean found = false;
								final List<Rect> regions = locations.get(e);
								for (int i = 0; i < regions.size(); i++)
								{
									if (found)
									{
										final Paint paint = new Paint();
										paint.setColor(Color.parseColor("#30CD5C5C"));
										final Bitmap bg = Bitmap.createBitmap(frameView.getWidth(), frameView.getHeight(), Bitmap.Config.ARGB_8888);
										final Canvas canvas = new Canvas(bg);
										canvas.drawRect(regions.get(i), paint);
										final AppCompatImageView highlightCanvas = new AppCompatImageView(mainActivity);
										highlightCanvas.setImageBitmap(bg);
										fl.addView(highlightCanvas);
									}
									else
									{
										if (regions.get(i).contains(x, y))
										{
											final View ayaView = fl.findViewWithTag(surah.get(e) + ":" + ayah.get(e));
											if (ayaView != null)
											{
												final int childCount = fl.getChildCount();
												for (int o = 0; o < childCount; o++)
												{
													final View vi = fl.getChildAt(o);
													if (vi instanceof AppCompatImageView)
														((AppCompatImageView) vi).clearColorFilter();
												}

												if(childCount > layers)
													fl.removeViews(layers, childCount - layers);

												((AppCompatImageView) ayaView).setColorFilter(new LightingColorFilter(0xffffff, 0x880000));

												found = true;
												i = -1;
											}
										}
									}
								}

								if (found)
									return true;
							}
							break;
						}
					}
					return false;
				}
			});
		}
		else
		{
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

			if (nightMode)
			{
				//pageView.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
				//pageView.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
				pageView.setColorFilter(filter);
			}
		}

		return view;
	}
}