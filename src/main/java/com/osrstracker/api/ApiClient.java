package com.osrstracker.api;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import javax.inject.Inject;

@Slf4j
public class ApiClient
{
	static final String API_ENDPOINT = "https://xanlaynufesmyhryeewx.supabase.co/functions/v1/ingest-snapshot";

	@Inject
	private OkHttpClient okHttpClient;

	@Inject
	private Gson gson;

	public void sendSnapshot(SkillSnapshot snapshot, Callback callback)
	{
		String json = gson.toJson(snapshot);
		RequestBody body = RequestBody.create(MediaType.get("application/json"), json);
		Request request = new Request.Builder()
			.url(API_ENDPOINT)
			.post(body)
			.build();
		okHttpClient.newCall(request).enqueue(callback);
	}
}