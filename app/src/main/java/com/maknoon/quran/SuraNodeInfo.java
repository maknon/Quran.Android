package com.maknoon.quran;

public class SuraNodeInfo
{
	public final String sura;
	public final int page;

	SuraNodeInfo(String sura, int page)
	{
		this.sura = sura;
		this.page = page;
	}

	public String getSura() {return sura;}
	public int getPage() {return page;}
}