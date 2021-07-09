package com.maknoon.quran;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;

import java.util.Locale;

public class ContextWrapper extends android.content.ContextWrapper
{
	public ContextWrapper(final Context base)
	{
		super(base);
	}

	public static ContextWrapper wrap(Context context, final Locale newLocale)
	{
		final Resources res = context.getResources();
		final Configuration configuration = res.getConfiguration();
		final Configuration configuration2 = new Configuration(configuration);

		if (Build.VERSION.SDK_INT >= 24)
		{
			final LocaleList localeList = new LocaleList(newLocale);
			LocaleList.setDefault(localeList);
			configuration2.setLocales(localeList);
			//configuration2.setLayoutDirection(newLocale);
			//configuration2.setLocale(newLocale); //it works as well
		}
		else
		{
			Locale.setDefault(newLocale);
			configuration2.locale = newLocale;
		}

		return new ContextWrapper(context.createConfigurationContext(configuration2));

		/*
		if (Build.VERSION.SDK_INT >= 17)
			return new ContextWrapper(context.createConfigurationContext(configuration2));
		else
		{
			context.getResources().updateConfiguration(configuration2, res.getDisplayMetrics());
			return new ContextWrapper(context);
		}
		*/
	}
}