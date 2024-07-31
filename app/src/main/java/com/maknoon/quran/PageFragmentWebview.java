package com.maknoon.quran;

import static com.maknoon.quran.MainActivity.EXTRA_page;
import static com.maknoon.quran.MainActivity.nightMode;
import static com.maknoon.quran.MainActivity.pagesFolder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;

// WebView not working with files from assetPacks (FDM):
// https://stackoverflow.com/questions/58338921/load-assets-into-webview-from-android-dynamic-feature-module

// WebView not working also from app specific storage '/storage/emulated/0/Android/data/com.maknoon.quran/files' after moving files from assetPacks to assets folder using:
// org.apache.commons.io.FileUtils.moveDirectory(source, destination);
public class PageFragmentWebview extends Fragment
{
	final static String TAG = "PageFragmentWebview";

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

	static PageFragmentWebview newInstance(int page)
	{
		final PageFragmentWebview fragmentFirst = new PageFragmentWebview();
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
		final View view = inflater.inflate(R.layout.page_svg_fragment, container, false);

		final WebView pageView = view.findViewById(R.id.page);
		//pageView.getSettings().setLoadWithOverviewMode(true);
		//pageView.getSettings().setUseWideViewPort(true);
		pageView.getSettings().setAllowFileAccess(true);
		//pageView.getSettings().setAllowFileAccessFromFileURLs(true);
		//pageView.getSettings().setAllowContentAccess(true);

		pageView.setOnTouchListener(new View.OnTouchListener()
		{
			float xDown = 0, yDown = 0;
			float xMove = 0, yMove = 0;

			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				switch (event.getAction())
				{
					case MotionEvent.ACTION_DOWN:
						//Log.d(TAG, "onTouch: MotionEvent.ACTION_DOWN");
						xMove = xDown = event.getX();
						yMove = yDown = event.getY();
						break;
					case MotionEvent.ACTION_MOVE:
						//Log.d(TAG, "onTouch: MotionEvent.ACTION_MOVE");
						xMove = event.getX();
						yMove = event.getY();
						break;
					case MotionEvent.ACTION_UP:
					{
						float distX = xMove - xDown;
						float distY = yMove - yDown;
						Log.d(TAG, "distX: " + distX + " distY: " + distY);

						if (Math.abs(distX) < 10 && Math.abs(distY) < 10)
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
				}
				return false;
			}
		});

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
		{
			final String baseUrl;
			if (pagesFolder.contains("hafs"))
				baseUrl = "file:///android_asset/hafs/";
			else if (pagesFolder.contains("warsh"))
				baseUrl = mainActivity.getExternalFilesDir(null).getAbsolutePath() + "warsh";
			else if (pagesFolder.contains("douri"))
				baseUrl = "file://" + pagesFolder + "/douri/";
			else if (pagesFolder.contains("qalon"))
				baseUrl = "file://" + pagesFolder + "/qalon/";
			else //if (pagesFolder.contains("shubah"))
				baseUrl = "file://" + pagesFolder + "/shubah/";

			final String s = "<html><style>*{ margin: 0; padding: 0; }</style><body><img src=" + (page + 1) + ".svgz" + " /></body></html>";
			pageView.loadDataWithBaseURL(baseUrl, s, "text/html", "utf-8", null);
		}
		else
		{
			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
			{
				if (pagesFolder.contains("hafs"))
					pageView.loadUrl("file:///android_asset/hafs/" + (page + 1) + ".svgz");
				else if (pagesFolder.contains("warsh"))
					//pageView.loadUrl("file://" + pagesFolder + "/warsh/" + (page + 1) + ".svgz");
					pageView.loadUrl(mainActivity.getExternalFilesDir(null) + "/warsh/" + (page + 1) + ".svgz");
					//pageView.loadDataWithBaseURL(mainActivity.getExternalFilesDir(null).getAbsolutePath(),  "warsh/" + (page + 1) + ".svgz", "image/svg+xml", "utf-8", null);
				else if (pagesFolder.contains("douri"))
					pageView.loadUrl(pagesFolder + "/douri/" + (page + 1) + ".svgz");
				else if (pagesFolder.contains("qalon"))
					pageView.loadUrl(pagesFolder + "/qalon/" + (page + 1) + ".svgz");
				else //if (pagesFolder.contains("shubah"))
					pageView.loadUrl(pagesFolder + "/shubah/" + (page + 1) + ".svgz");
			}
		}

		if (nightMode)
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

			final Paint paint = new Paint();
			paint.setColorFilter(filter);
			pageView.setLayerType(View.LAYER_TYPE_HARDWARE, paint);
			//pageView.setBackgroundColor(Color.TRANSPARENT);
		}
		else
			pageView.setBackgroundColor(Color.rgb(255, 255, 242)); // "#FFFFF2"

		return view;
	}
}