package com.maknoon.quran;

public class JuzNodeInfo
{
	public final int juz;
	public final int page;

	JuzNodeInfo(int juz, int page)
	{
		this.juz = juz;
		this.page = page;
	}

	public int getJuz() {return juz;}
	public int getPage() {return page;}
}