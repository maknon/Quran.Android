package com.maknoon.quran;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.play.core.assetpacks.AssetPackLocation;
import com.google.android.play.core.assetpacks.AssetPackManager;
import com.google.android.play.core.assetpacks.AssetPackManagerFactory;
import com.google.android.play.core.assetpacks.AssetPackState;
import com.google.android.play.core.assetpacks.AssetPackStateUpdateListener;
import com.google.android.play.core.assetpacks.AssetPackStates;
import com.google.android.play.core.assetpacks.model.AssetPackStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity
{
	final static String TAG = "MainActivity";

	SharedPreferences mPrefs;

	int page = 0;
	static String pagesFolder = "hafs";
	static boolean nightMode = false;

	PagerAdapter pagerAdapter;
	ViewPager2 mViewPager;

	static String assetPackName;

	static final String EXTRA_page = "com.maknoon.quran.page";
	static final String EXTRA_nightMode = "com.maknoon.quran.nightMode";
	static final String EXTRA_pagesFolder = "com.maknoon.quran.pagesFolder";

	AssetPackManager assetPackManager;

	MenuItem hafsMenuItem, warshMenuItem, douriMenuItem, qalonMenuItem, shubahMenuItem;

	// This flag should be set to true to enable VectorDrawable support for API < 21
	static
	{
		AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
	}

	DBHelper mDbHelper;
	static SQLiteDatabase db;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		mPrefs = getSharedPreferences("setting", Context.MODE_PRIVATE);

		if (savedInstanceState != null) // when recreate() this activity if language is change. savedInstanceState will be saved so we need to force the direction
		{
			page = savedInstanceState.getInt(EXTRA_page);
			nightMode = savedInstanceState.getBoolean(EXTRA_nightMode);
		}
		else
			page = mPrefs.getInt(EXTRA_page, 0);

		pagesFolder = mPrefs.getString(EXTRA_pagesFolder, "hafs");

		if (pagesFolder.contains("hafs"))
			assetPackName = null;
		else if (pagesFolder.contains("warsh"))
			assetPackName = "asset_pack_warsh";
		else if (pagesFolder.contains("douri"))
			assetPackName = "asset_pack_douri";
		else if (pagesFolder.contains("qalon"))
			assetPackName = "asset_pack_qalon";
		else if (pagesFolder.contains("shubah"))
			assetPackName = "asset_pack_shubah";

		final ActionBar ab = getSupportActionBar();
		if (ab != null)
		{
			ab.setTitle(null);
			ab.hide();
			ab.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#59000000")));
			ab.setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#73000000")));
		}

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		pagerAdapter = new PagerAdapter(getSupportFragmentManager(), getLifecycle());
		mViewPager = findViewById(R.id.viewpager);
		mViewPager.setAdapter(pagerAdapter);
		mViewPager.setCurrentItem(page, false);
		mViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback()
		{
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
			{
				super.onPageScrolled(position, positionOffset, positionOffsetPixels);
			}

			@Override
			public void onPageSelected(int position)
			{
				super.onPageSelected(position);

				page = position;

				final SharedPreferences.Editor mEditor = mPrefs.edit();
				mEditor.putInt(EXTRA_page, page).apply();
			}

			@Override
			public void onPageScrollStateChanged(int state)
			{
				super.onPageScrollStateChanged(state);
			}
		});

		mDbHelper = new DBHelper(this);
		db = mDbHelper.getReadableDatabase();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		final int id = item.getItemId();

		if (id == android.R.id.home)
		{
			final ActionBar ab = getSupportActionBar();
			if (ab != null)
				ab.hide();
			return true;
		}

		if (id == R.id.list)
		{
			final DialogFragment goTo = new GoTo();
			goTo.show(getSupportFragmentManager(), "goTo");
			return true;
		}

		if (id == R.id.menu_hafs)
		{
			pagesFolder = "hafs";

			final SharedPreferences.Editor mEditor = mPrefs.edit();
			mEditor.putString(EXTRA_pagesFolder, pagesFolder).apply();

			refresh();

			return true;
		}

		if (id == R.id.menu_warsh)
		{
			menuAction("asset_pack_warsh");
			return true;
		}

		if (id == R.id.menu_douri)
		{
			menuAction("asset_pack_douri");
			return true;
		}

		if (id == R.id.menu_qalon)
		{
			menuAction("asset_pack_qalon");
			return true;
		}

		if (id == R.id.menu_shubah)
		{
			menuAction("asset_pack_shubah");
			return true;
		}

		if (id == R.id.darkmode)
		{
			if (nightMode)
			{
				nightMode = false;
				item.setIcon(R.drawable.baseline_dark_mode_24);
			}
			else
			{
				nightMode = true;
				item.setIcon(R.drawable.baseline_light_mode_24);
			}
			refresh();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	void refresh()
	{
		mViewPager.setAdapter(null);
		mViewPager.setAdapter(pagerAdapter);
		mViewPager.setCurrentItem(page, false);

		updateMenu();
	}

	void updateMenu()
	{
		if(assetPackManager == null)
			assetPackManager = AssetPackManagerFactory.getInstance(this.getApplicationContext());

		AssetPackLocation assetPackPath = assetPackManager.getPackLocation("asset_pack_warsh");
		if (assetPackPath != null) // qiraat is downloaded
			warshMenuItem.setTitle(R.string.warsh);
		else
			warshMenuItem.setTitle(R.string.menu_warsh);

		assetPackPath = assetPackManager.getPackLocation("asset_pack_douri");
		if (assetPackPath != null) // qiraat is downloaded
			douriMenuItem.setTitle(R.string.douri);
		else
			douriMenuItem.setTitle(R.string.menu_douri);

		assetPackPath = assetPackManager.getPackLocation("asset_pack_qalon");
		if (assetPackPath != null) // qiraat is downloaded
			qalonMenuItem.setTitle(R.string.qalon);
		else
			qalonMenuItem.setTitle(R.string.menu_qalon);

		assetPackPath = assetPackManager.getPackLocation("asset_pack_shubah");
		if (assetPackPath != null) // qiraat is downloaded
			shubahMenuItem.setTitle(R.string.shubah);
		else
			shubahMenuItem.setTitle(R.string.menu_shubah);
	}

	void menuAction(final String assetName)
	{
		assetPackName = assetName;

		if(assetPackManager == null)
			assetPackManager = AssetPackManagerFactory.getInstance(this.getApplicationContext());

		final AssetPackLocation assetPackPath = assetPackManager.getPackLocation(assetPackName);
		if (assetPackPath != null) // qiraat is downloaded
		{
			pagesFolder = assetPackPath.assetsPath();

			final SharedPreferences.Editor mEditor = mPrefs.edit();
			mEditor.putString(EXTRA_pagesFolder, pagesFolder).apply();

			refresh();
		}
		else
		{
			assetPackManager.clearListeners();
			assetPackManager.registerListener(new AssetPackStateUpdateListener()
			{
				@Override
				public void onStateUpdate(@NonNull AssetPackState state)
				{
					switch (state.status())
					{
						case AssetPackStatus.PENDING:
							Log.i(TAG, "AssetPackState Pending");
							break;

						case AssetPackStatus.DOWNLOADING:
							long downloaded = state.bytesDownloaded();
							long totalSize = state.totalBytesToDownload();
							double percent = 100.0 * downloaded / totalSize;
							Log.i(TAG, "AssetPackState Percent Done=" + String.format("%.2f", percent));
							showToast(getString(R.string.download_qiraat_percentage, String.valueOf((int) percent)));
							break;

						case AssetPackStatus.TRANSFERRING:
							// 100% downloaded and assets are being transferred.
							// Notify user to wait until transfer is complete.
							Log.i(TAG, "AssetPackState TRANSFERRING transfer progress percentage " + state.transferProgressPercentage());
							break;

						case AssetPackStatus.COMPLETED:
							// Asset pack is ready to use. Start the use
							Log.i(TAG, "AssetPackState COMPLETED");
							showToast(getString(R.string.download_qiraat_completed));
							menuSetVisible(true);

							final MaterialAlertDialogBuilder ad = new MaterialAlertDialogBuilder(MainActivity.this);
							ad.setMessage(R.string.download_qiraat_finished)
									.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
									{
										public void onClick(DialogInterface dialog, int id)
										{
											if (assetPackManager == null)
												assetPackManager = AssetPackManagerFactory.getInstance(MainActivity.this);

											final AssetPackLocation assetPackPath = assetPackManager.getPackLocation(assetPackName);
											if (assetPackPath != null)
											{
												pagesFolder = assetPackPath.assetsPath();
												final SharedPreferences.Editor mEditor = mPrefs.edit();
												mEditor.putString(EXTRA_pagesFolder, pagesFolder).apply();
												refresh();
												dialog.dismiss();
											}
										}
									})
									.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
									{
										public void onClick(DialogInterface dialog, int id)
										{
											updateMenu();
											dialog.dismiss();
										}
									});

							final AlertDialog d = ad.create();
							if (!MainActivity.this.isFinishing()) // to avoid crashes if user close the app
								d.show();

							break;

						case AssetPackStatus.FAILED:
							// Request failed. Notify user.
							Log.e(TAG, state.errorCode() + "");
							showToast(getString(R.string.download_qiraat_error, state.errorCode()));
							menuSetVisible(true);
							break;

						case AssetPackStatus.CANCELED:
							// Request canceled. Notify user.
							showToast(getString(R.string.download_qiraat_cancelled));
							menuSetVisible(true);
							break;

						case AssetPackStatus.WAITING_FOR_WIFI:
							/*
							if (!waitForWifiConfirmationShown)
							{
								assetPackManager.showCellularDataConfirmation(MainActivity.this)
										.addOnSuccessListener(new OnSuccessListener()
										{
											@Override
											public void onSuccess(Integer resultCode)
											{
												if (resultCode == RESULT_OK)
												{
													Log.d(TAG, "Confirmation dialog has been accepted.");
												}
												else if (resultCode == RESULT_CANCELED)
												{
													Log.d(TAG, "Confirmation dialog has been denied by the user.");
												}
											}
										});
								waitForWifiConfirmationShown = true;
							}
							*/
							break;

						case AssetPackStatus.NOT_INSTALLED:
							// Asset pack is not downloaded yet.
							Log.e(TAG, "AssetPackStatus NOT_INSTALLED");
							break;
						case AssetPackStatus.UNKNOWN:
							showToast(getString(R.string.download_qiraat_error));
							menuSetVisible(true);
							break;
					}
				}
			});

			assetPackManager.getPackStates(Collections.singletonList(assetPackName))
					.addOnCompleteListener(new OnCompleteListener<AssetPackStates>()
					{
						@Override
						public void onComplete(@NonNull Task<AssetPackStates> task)
						{
							AssetPackStates assetPackStates;
							try
							{
								assetPackStates = task.getResult();
								final AssetPackState assetPackState = assetPackStates.packStates().get(assetPackName);

								if (assetPackState.status() == AssetPackStatus.COMPLETED)
								{
									Toast.makeText(MainActivity.this, "Already Downloaded ! reset", Toast.LENGTH_SHORT).show();
									menuSetVisible(true);
									updateMenu();
								}
								else
								{
									final MaterialAlertDialogBuilder ad = new MaterialAlertDialogBuilder(MainActivity.this);
									ad.setMessage(getString(R.string.download_qiraat, (int) (assetPackState.totalBytesToDownload() / (1024f * 1024f))))
											.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
											{
												public void onClick(DialogInterface dialog, int id)
												{
													final List<String> ls = new ArrayList<>();
													ls.add(assetPackName);
													assetPackManager.fetch(ls);
													dialog.dismiss();
													menuSetVisible(false);
												}
											})
											.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
											{
												public void onClick(DialogInterface dialog, int id)
												{
													dialog.dismiss();
												}
											});

									final AlertDialog d = ad.create();
									d.show();
								}
							} catch (Exception e)
							{
								Log.e(TAG, e.getMessage());
								menuSetVisible(true);
							}
						}
					});
		}
	}

	Toast toast;
	void showToast(String text)
	{
		if(toast != null)
			toast.cancel();

		toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
		toast.show();
	}

	@Override
	public boolean onCreateOptionsMenu(@NonNull Menu menu)
	{
		getMenuInflater().inflate(R.menu.main, menu);
		final MenuItem darkmode = menu.findItem(R.id.darkmode);
		hafsMenuItem = menu.findItem(R.id.menu_hafs);
		warshMenuItem = menu.findItem(R.id.menu_warsh);
		douriMenuItem = menu.findItem(R.id.menu_douri);
		qalonMenuItem = menu.findItem(R.id.menu_qalon);
		shubahMenuItem = menu.findItem(R.id.menu_shubah);

		updateMenu();

		if(nightMode)
			darkmode.setIcon(R.drawable.baseline_light_mode_24);
		else
			darkmode.setIcon(R.drawable.baseline_dark_mode_24);

		return super.onCreateOptionsMenu(menu);
	}

	void menuSetVisible(boolean visible)
	{
		hafsMenuItem.setVisible(visible);
		warshMenuItem.setVisible(visible);
		douriMenuItem.setVisible(visible);
		qalonMenuItem.setVisible(visible);
		shubahMenuItem.setVisible(visible);
	}

	static class PagerAdapter extends FragmentStateAdapter
	{
		PagerAdapter(FragmentManager fm, @NonNull Lifecycle lc)
		{
			super(fm, lc);
		}

		@NonNull
		@Override
		public Fragment createFragment(int position)
		{
			return PageFragmentNEXT.newInstance(position);
		}

		@Override
		public int getItemCount()
		{
			return 604;
		}
	}

	@Override
	protected void onDestroy()
	{
		if(db != null)
			db.close();
		if(mDbHelper != null)
			mDbHelper.close();

		super.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(@NonNull Bundle savedInstanceState)
	{
		savedInstanceState.putInt(EXTRA_page, page);
		savedInstanceState.putBoolean(EXTRA_nightMode, nightMode);

		super.onSaveInstanceState(savedInstanceState);
	}
}