package com.maknoon.quran;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

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

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		final View view = inflater.inflate(R.layout.page_fragment_next, container, false);
		final FrameLayout fl = view.findViewById(R.id.frameLayout);

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

		final InputStream in;

		try
		{
			if(pagesFolder.equals("hafs"))
			{
				final AssetManager am = getResources().getAssets();
				in = am.open("hafs/" + (page + 1) + "-f.png");

				final Drawable pg = Drawable.createFromStream(in, null);
				final AppCompatImageView frameView = new AppCompatImageView(mainActivity);
				frameView.setImageDrawable(pg);
				fl.addView(frameView);

				final DBHelper mDbHelper = new DBHelper(mainActivity);
				final SQLiteDatabase db = mDbHelper.getReadableDatabase();
				final Cursor mCursor = db.rawQuery("SELECT * FROM Quran WHERE Page = " + (page + 1), null);
				if (mCursor.moveToFirst())
				{
					for (int i = 0; i < mCursor.getCount(); i++)
					{
						final int aya = mCursor.getInt(mCursor.getColumnIndexOrThrow("Aya"));
						final int sura = mCursor.getInt(mCursor.getColumnIndexOrThrow("Sura"));

						try (InputStream in1 = am.open("hafs/" + (page + 1) + "-" + sura + "-" + aya + ".png"))
						{
							final Drawable pg1 = Drawable.createFromStream(in1, null);
							final AppCompatImageView ayaView = new AppCompatImageView(mainActivity);
							ayaView.setDrawingCacheEnabled(true);
							ayaView.setOnTouchListener(new View.OnTouchListener()
							{
								@Override
								public boolean onTouch(View v, MotionEvent event)
								{
									Log.e("SALAM", "touch " + v);

									final Bitmap bmp = Bitmap.createBitmap(v.getDrawingCache());

									int x1 = (int) event.getX() - 30;
									int x2 = (int) event.getX() + 30;
									int y1 = (int) event.getY() - 30;
									int y2 = (int) event.getY() + 30;

									if (x1 < 0) x1 = 0;
									if (y1 < 0) y1 = 0;
									if (x2 >= bmp.getWidth()) x2 = bmp.getWidth() - 1;
									if (y2 >= bmp.getHeight()) y2 = bmp.getHeight() - 1;

									for (int x = x1; x < x2; x++)
									{
										for (int y = y1; y < y2; y++)
										{
											final int color = bmp.getPixel(x, y);
											if (color != Color.TRANSPARENT)
											{
												/*
												float[] invertMX = {
														-1f, 0f, 0f, 0f, 255f,
														0f, -1f, 0f, 0f, 255f,
														0f, 0f, -1f, 0f, 255f,
														0f, 0f, 0f, 1f, 0f
												};

												final ColorMatrix colorMatrix = new ColorMatrix(invertMX);
												final ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);
												((AppCompatImageView) v).setColorFilter(filter);

												((AppCompatImageView) v).setColorFilter(new PorterDuffColorFilter(Color.BLUE, PorterDuff.Mode.SRC_IN));
												((AppCompatImageView) v).setColorFilter(Color.BLUE, PorterDuff.Mode.SRC_IN);
												*/

												final int childCount = fl.getChildCount();
												for (int o = 0; o < childCount; o++)
												{
													final View vi = fl.getChildAt(o);
													if (vi instanceof AppCompatImageView)
														((AppCompatImageView) vi).clearColorFilter();
												}

												((AppCompatImageView) v).setColorFilter(new LightingColorFilter(0xffffff, 0x880000));
												return true;
											}
										}
									}
									return false;

									/*
									final Bitmap bmp = Bitmap.createBitmap(v.getDrawingCache());
									final int pointerCount = event.getPointerCount();
									for (int i = 0; i < pointerCount; i++)
									{
										if (event.getPressure(i) > 0)
										{
											Log.e("getPressure ", "" + event.getPressure(i));
											Log.e("getSize", + event.getX() + "\t   " + event.getSize());

											int color = bmp.getPixel((int) event.getX(i), (int) event.getY(i));
											if (color != Color.TRANSPARENT)
											{
												//click portion without transparent color

												final int childCount = fl.getChildCount();
												for (int o = 0; o < childCount; o++)
												{
													final View vi = fl.getChildAt(o);
													if(vi instanceof AppCompatImageView)
														((AppCompatImageView) vi).clearColorFilter();
												}

												((AppCompatImageView) v).setColorFilter(new LightingColorFilter(0xffffff, 0x880000));
												return true;
											}
										}
									}
									return false;
									*/
								}
							});
							ayaView.setImageDrawable(pg1);
							fl.addView(ayaView);
						} catch (IOException e)
						{
							e.printStackTrace();
						}

						mCursor.moveToNext();
					}
				}
				mCursor.close();
				db.close();
				mDbHelper.close();
			}
			else
			{
				in = new FileInputStream((pagesFolder + "/warsh/"+ (page + 1) + ".png"));
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		if(nightMode)
		{
			final int childCount = fl.getChildCount();
			for (int o = 0; o < childCount; o++)
			{
				final View vi = fl.getChildAt(o);
				if (vi instanceof AppCompatImageView)
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
					((AppCompatImageView) vi).setColorFilter(filter);
				}
			}
		}
		else
			fl.setBackgroundColor(Color.rgb(255, 255, 242)); // "#FFFFF2"

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

		return view;
	}
}